package jetbrains.buildServer.clouds.kubernetes.auth

import com.intellij.openapi.util.Pair
import jetbrains.buildServer.clouds.CloudConstants
import jetbrains.buildServer.clouds.kubernetes.KubeCloudException
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.*
import jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategy.*
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import jetbrains.buildServer.util.TimeService
import org.apache.commons.lang.CharSet
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.auth.signer.Aws4Signer
import software.amazon.awssdk.auth.signer.params.Aws4PresignerParams
import software.amazon.awssdk.http.SdkHttpFullRequest
import software.amazon.awssdk.http.SdkHttpMethod
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest
import java.net.URI
import java.net.URISyntaxException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.*

class EKSAuthStrategy(myTimeService: TimeService, private val projectManager: ProjectManager) : RefreshableStrategy<EKSData>(myTimeService) {

    override fun retrieveNewToken(dataHolder: EKSData): Pair<String, Long>? {
        val credentials = getAwsCredentialProvider(dataHolder)
        val tokenService = StsClient.builder()
            .region(Region.EU_WEST_1)
            .credentialsProvider(credentials)
            .build()

        val token = generateToken(dataHolder.clusterName, Date(), tokenService, credentials, "https", "sts.eu-west-1.amazonaws.com")
        return Pair(token, 60 * 15) // expire time = 15 minutes
    }

    override fun createKey(dataHolder: EKSData): Pair<String, String> {
        if (dataHolder.useInstanceProfile) {
            return Pair.create("instance-profile", dataHolder.clusterName)
        } else {
            return Pair.create(dataHolder.accessId, dataHolder.clusterName)
        }
    }

    private fun getAwsCredentialProvider(dataHolder: EKSData): AwsCredentialsProvider {
        val baseCreds = if (dataHolder.useInstanceProfile) {
            InstanceProfileCredentialsProvider.create()
        } else {
            StaticCredentialsProvider.create(AwsBasicCredentials.create(dataHolder.accessId, dataHolder.secretKey))
        }

        return if (!dataHolder.iamRoleArn.isNullOrEmpty()) {
            val stsClient1 = StsClient.builder()
                .region(Region.EU_WEST_1)
                .credentialsProvider(baseCreds)
                .build()
            val assumeRoleRequest = AssumeRoleRequest.builder()
                .roleArn(dataHolder.iamRoleArn)
                .roleSessionName("teamcity-kubernetes-plugin-session")
                .durationSeconds(60 * 15)
                .build()
            StsAssumeRoleCredentialsProvider.builder()
                .stsClient(stsClient1)
                .refreshRequest(assumeRoleRequest)
                .build()
        } else {
            baseCreds
        }
    }

    @Throws(URISyntaxException::class)
    private fun generateToken(
        clusterName: String,
        expirationDate: Date,
        awsSecurityTokenServiceClient: StsClient,
        credentialsProvider: AwsCredentialsProvider,
        scheme: String,
        host: String
    ): String {
        try {
            val uri = URI(scheme, host, "/", null)
            val requestToSign = SdkHttpFullRequest
                .builder()
                .method(SdkHttpMethod.GET)
                .uri(uri)
                .appendHeader("x-k8s-aws-id", clusterName)
                .appendRawQueryParameter("Action", "GetCallerIdentity")
                .appendRawQueryParameter("Version", "2011-06-15")
                .build();

            val presignerParams = Aws4PresignerParams.builder()
                .awsCredentials(credentialsProvider.resolveCredentials())
                .signingRegion(Region.EU_WEST_1)
                .signingName("sts")
                .signingClockOverride(Clock.fixed(Instant.now(), ZoneId.of("UTC")))
                .expirationTime(expirationDate.toInstant())
                .build();

            val signedRequest = Aws4Signer.create().presign(requestToSign, presignerParams)

            val encodedUrl = Base64.getUrlEncoder().withoutPadding().encodeToString(signedRequest.uri.toString().encodeToByteArray())
            return ("k8s-aws-v1." + encodedUrl)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            throw e
        }

    }

    override fun createData(connection: KubeApiConnection): EKSData {
        val useInstanceProfile = (connection.getCustomParameter(EKS_USE_INSTANCE_PROFILE) ?: "false").toBoolean()
        var accessId: String? = null
        var secretKey: String? = null
        if (!useInstanceProfile) {
            accessId = connection.getCustomParameter(EKS_ACCESS_ID) ?: throw KubeCloudException("Access ID is empty for connection to " + connection.apiServerUrl)
            secretKey =
                connection.getCustomParameter(SECURE_PREFIX + EKS_SECRET_KEY) ?: throw KubeCloudException("Secret key is empty for connection to " + connection.apiServerUrl)
        }
        val iamRoleArn: String? = connection.getCustomParameter(EKS_IAM_ROLE_ARN)
        val clusterName = connection.getCustomParameter(EKS_CLUSTER_NAME) ?: throw KubeCloudException("Cluster name is empty for connection to " + connection.apiServerUrl)

        return EKSData(useInstanceProfile, accessId, secretKey, iamRoleArn, clusterName)
    }

    override fun getId() = "eks"

    override fun getDisplayName() = "Amazon EKS"

    override fun getDescription() = null

    override fun process(props: MutableMap<String, String>): MutableCollection<InvalidProperty> {
        val retval = arrayListOf<InvalidProperty>();
        if (props[EKS_CLUSTER_NAME].isNullOrEmpty()) {
            retval.add(InvalidProperty(EKS_CLUSTER_NAME, "Cluster name is required"))
        }

        if (props[EKS_USE_INSTANCE_PROFILE].isNullOrEmpty() || !props[EKS_USE_INSTANCE_PROFILE]!!.toBoolean()) {
            if (props[EKS_ACCESS_ID].isNullOrEmpty()) {
                retval.add(InvalidProperty(EKS_ACCESS_ID, "Access ID is required if instance profile is not used"))
            }
            if (props[SECURE_PREFIX + EKS_SECRET_KEY].isNullOrEmpty()) {
                retval.add(InvalidProperty(EKS_SECRET_KEY, "Secret Key is required if instance profile is not used"))
            }
        }
        return retval;
    }

    override fun fillAdditionalSettings(mv: MutableMap<String, Any>, projectId: String, isAvailable: Boolean) {
        mv.put(EKS_USE_INSTANCE_PROFILE_ENABLED, isEksLocalAvailable(projectId))
    }

    private fun isEksLocalAvailable(projectId: String): Boolean {
        if (TeamCityProperties.getBoolean(EKSAuthStrategy.ENABLE_LOCAL_AWS_ACCOUNT)) {
            return true
        }
        val project = projectManager.findProjectById(projectId)
        if (project == null) {
            return false
        }

        return isAuthStrategyUsed(project)
    }

    private fun isAuthStrategyUsed(project: SProject): Boolean {
        //TW-91106 The local instance profile strategy is disabled by default but enabled for whoever was already using it
        return (
                project.getOwnFeaturesOfType(CloudConstants.CLOUD_PROFILE_FEATURE_TYPE) +
                        project.getOwnFeaturesOfType(OAuthConstants.FEATURE_TYPE))
            .filter { features: SProjectFeatureDescriptor -> id == features.parameters[AUTH_STRATEGY] }
            .any { features -> features.parameters[KubeParametersConstants.EKS_USE_INSTANCE_PROFILE].toBoolean() }
    }


    companion object {
        const val ENABLE_LOCAL_AWS_ACCOUNT = "teamcity.kubernetes.localAwsAccount.enable"
    }
}

data class EKSData(
    val useInstanceProfile: Boolean,
    val accessId: String?,
    val secretKey: String?,
    val iamRoleArn: String?,
    val clusterName: String
)