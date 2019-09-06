/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.clouds.kubernetes.auth

import com.amazonaws.DefaultRequest
import com.amazonaws.auth.*
import com.amazonaws.auth.presign.PresignerFacade
import com.amazonaws.auth.presign.PresignerParams
import com.amazonaws.http.HttpMethodName
import com.amazonaws.internal.auth.DefaultSignerProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest
import com.intellij.openapi.util.Pair
import jetbrains.buildServer.clouds.kubernetes.KubeCloudException
import jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants
import jetbrains.buildServer.clouds.kubernetes.connector.KubeApiConnection
import jetbrains.buildServer.util.TimeService
import java.net.URI
import java.net.URISyntaxException
import java.util.*

class EKSAuthStrategy(myTimeService: TimeService) : RefreshableStrategy<EKSData>(myTimeService) {
    var credentialsProvider: AWSCredentialsProvider? = null

    override fun retrieveNewToken(dataHolder: EKSData): Pair<String, Long>? {
        val credentials = getAwsCredentialProvider(dataHolder)
        val tokenService : AWSSecurityTokenServiceClient = AWSSecurityTokenServiceClientBuilder
                .standard()
                .withRegion(Regions.EU_WEST_1)
                .withCredentials(credentials)
                .build() as AWSSecurityTokenServiceClient

        val token = generateToken(dataHolder.clusterName, Date(), tokenService, credentials, "https", "sts.amazonaws.com")
        return Pair(token, 60*12) // expire time = 12 minutes
    }

    override fun createKey(dataHolder: EKSData): Pair<String, String> {
        if (dataHolder.useInstanceProfile) {
            return Pair.create("instance-profile", dataHolder.clusterName)
        } else {
            return Pair.create(dataHolder.accessId, dataHolder.clusterName)
        }
    }

    private fun getAwsCredentialProvider(dataHolder: EKSData): AWSCredentialsProvider {
        // Only initialize the credential provider once. Static creds never change and the AssumeRole handles refreshing credentials itself
        if (credentialsProvider != null) return credentialsProvider!!

        var baseCreds = if (dataHolder.useInstanceProfile) {
            InstanceProfileCredentialsProvider.getInstance()
        } else {
            AWSStaticCredentialsProvider(BasicAWSCredentials(dataHolder.accessId, dataHolder.secretKey))
        }

        credentialsProvider = if (!dataHolder.iamRoleArn.isNullOrEmpty()) {
            val stsClient = AWSSecurityTokenServiceClientBuilder
                    .standard()
                    .withRegion(Regions.EU_WEST_1)
                    .withCredentials(baseCreds)
                    .build() as AWSSecurityTokenServiceClient
            STSAssumeRoleSessionCredentialsProvider.Builder(dataHolder.iamRoleArn, "teamcity-kubernetes-plugin-session")
                    .withStsClient(stsClient)
                    .withRoleSessionDurationSeconds(60 * 12)
                    .build() as STSAssumeRoleSessionCredentialsProvider
        } else {
            baseCreds
        }
        return credentialsProvider!!
    }

    @Throws(URISyntaxException::class)
    private fun generateToken(clusterName: String,
                              expirationDate: Date,
                              awsSecurityTokenServiceClient: AWSSecurityTokenServiceClient,
                              credentialsProvider: AWSCredentialsProvider,
                              scheme: String,
                              host: String): String {
        try {
            val callerIdentityRequestDefaultRequest = DefaultRequest<GetCallerIdentityRequest>(GetCallerIdentityRequest(), "sts")
            val uri = URI(scheme, host, null, null)
            callerIdentityRequestDefaultRequest.resourcePath = "/"
            callerIdentityRequestDefaultRequest.endpoint = uri
            callerIdentityRequestDefaultRequest.httpMethod = HttpMethodName.GET
            callerIdentityRequestDefaultRequest.addParameter("Action", "GetCallerIdentity")
            callerIdentityRequestDefaultRequest.addParameter("Version", "2011-06-15")
            callerIdentityRequestDefaultRequest.addHeader("x-k8s-aws-id", clusterName)

            val signer = SignerFactory.createSigner(SignerFactory.VERSION_FOUR_SIGNER, SignerParams("sts", "us-east-1"))
            val signerProvider = DefaultSignerProvider(awsSecurityTokenServiceClient, signer)
            val presignerParams = PresignerParams(uri,
                    credentialsProvider,
                    signerProvider,
                    SdkClock.STANDARD)

            val presignerFacade = PresignerFacade(presignerParams)
            val url = presignerFacade.presign(callerIdentityRequestDefaultRequest, expirationDate)
            val encodedUrl = Base64.getUrlEncoder().withoutPadding().encodeToString(url.toString().toByteArray())
            return "k8s-aws-v1.$encodedUrl"
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            throw e
        }

    }

    override fun createData(connection: KubeApiConnection): EKSData {
        val useInstanceProfile = (connection.getCustomParameter(KubeParametersConstants.EKS_USE_INSTANCE_PROFILE) ?: "false").toBoolean()
        var accessId: String? = null
        var secretKey: String? = null
        if (!useInstanceProfile) {
            accessId = connection.getCustomParameter(KubeParametersConstants.EKS_ACCESS_ID) ?: throw KubeCloudException("Access ID is empty for connection to " + connection.apiServerUrl)
            secretKey = connection.getCustomParameter(KubeParametersConstants.EKS_SECRET_KEY) ?: throw KubeCloudException("Secret key is empty for connection to " + connection.apiServerUrl)
        }
        val iamRoleArn: String? = connection.getCustomParameter(KubeParametersConstants.EKS_IAM_ROLE_ARN)
        val clusterName = connection.getCustomParameter(KubeParametersConstants.EKS_CLUSTER_NAME) ?: throw KubeCloudException("Cluster name is empty for connection to " + connection.apiServerUrl)

        return EKSData(useInstanceProfile, accessId, secretKey, iamRoleArn, clusterName)
    }

    override fun getId() = "eks"

    override fun getDisplayName() = "Amazon EKS (experimental)"

    override fun getDescription() = "Amazon EKS"
}

data class EKSData(val useInstanceProfile: Boolean,
                   val accessId : String?,
                   val secretKey: String?,
                   val iamRoleArn: String?,
                   val clusterName: String)