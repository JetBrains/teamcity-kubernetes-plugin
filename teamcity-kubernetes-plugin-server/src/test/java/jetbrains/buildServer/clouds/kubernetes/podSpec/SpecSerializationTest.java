package jetbrains.buildServer.clouds.kubernetes.podSpec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.utils.Serialization;
import jetbrains.buildServer.BaseTestCase;
import org.testng.annotations.Test;

public class SpecSerializationTest extends BaseTestCase {

  @Test
  public void testSerializationWithAdditionalProperties() throws Exception {
    final ObjectMapper mapper = Serialization.yamlMapper();

    final String deploymentSpec = "apiVersion: apps/v1\n" +
                                  "kind: Deployment\n" +
                                  "metadata:\n" +
                                  "  annotations:\n" +
                                  "    deployment.kubernetes.io/revision: \"1\"\n" +
                                  "  labels:\n" +
                                  "    app: teamcity-agents\n" +
                                  "    release: teamcity-agents-stable\n" +
                                  "    site: small\n" +
                                  "  name: simpledep\n" +
                                  "  namespace: default\n" +
                                  "spec:\n" +
                                  "  progressDeadlineSeconds: 600\n" +
                                  "  replicas: 0\n" +
                                  "  revisionHistoryLimit: 10\n" +
                                  "  selector:\n" +
                                  "    matchLabels:\n" +
                                  "      app: teamcity-agents\n" +
                                  "      release: teamcity-agents-stable\n" +
                                  "      site: small\n" +
                                  "  strategy:\n" +
                                  "    rollingUpdate:\n" +
                                  "      maxSurge: 25%\n" +
                                  "      maxUnavailable: 25%\n" +
                                  "    type: RollingUpdate\n" +
                                  "  template:\n" +
                                  "    metadata:\n" +
                                  "      labels:\n" +
                                  "        app: teamcity-agents\n" +
                                  "        release: teamcity-agents-stable\n" +
                                  "        site: small\n" +
                                  "    spec:\n" +
                                  "      containers:\n" +
                                  "        - image: jetbrains/teamcity-agent:latest\n" +
                                  "          imagePullPolicy: Always\n" +
                                  "          name: teamcity-agent\n" +
                                  "          resources: {}\n" +
                                  "          terminationMessagePath: /dev/termination-log\n" +
                                  "          terminationMessagePolicy: File\n" +
                                  "      dnsPolicy: ClusterFirst\n" +
                                  "      restartPolicy: Always\n" +
                                  "      schedulerName: default-scheduler\n" +
                                  "      securityContext: {}\n" +
                                  "      terminationGracePeriodSeconds: 30\n" +
                                  "      topologySpreadConstraints:\n" +
                                  "        - labelSelector:\n" +
                                  "            matchLabels:\n" +
                                  "              app: teamcity-agents\n" +
                                  "              release: teamcity-agents-stable\n" +
                                  "              site: small\n" +
                                  "          matchLabelKeys:\n" +
                                  "            - pod-template-hash\n" +
                                  "          maxSkew: 1\n" +
                                  "          minDomains: 3\n" +
                                  "          nodeAffinityPolicy: Honor\n" +
                                  "          nodeTaintsPolicy: Honor\n" +
                                  "          topologyKey: topology.kubernetes.io/zone\n" +
                                  "          whenUnsatisfiable: DoNotSchedule";


    Deployment deployment = mapper.readValue(deploymentSpec, Deployment.class);

    mapper.writeValueAsString(deployment);
  }
}
