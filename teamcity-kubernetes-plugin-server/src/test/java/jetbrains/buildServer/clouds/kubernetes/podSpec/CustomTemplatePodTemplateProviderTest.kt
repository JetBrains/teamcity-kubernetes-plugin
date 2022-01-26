/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

package jetbrains.buildServer.clouds.kubernetes.podSpec

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import io.fabric8.kubernetes.api.model.PersistentVolume
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimSpec
import io.fabric8.kubernetes.api.model.PodTemplateSpec
import io.fabric8.kubernetes.client.utils.Serialization
import jetbrains.buildServer.BaseTestCase
import jetbrains.buildServer.clouds.CloudInstanceUserData
import jetbrains.buildServer.serverSide.ServerSettings
import jetbrains.buildServer.serverSide.impl.ServerSettingsImpl
import jetbrains.buildServer.util.TestFor
import org.assertj.core.api.BDDAssertions.then
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.ByteArrayInputStream

@Test
class CustomTemplatePodTemplateProviderTest : BaseTestCase() {
    
    private lateinit var myServerSettings : ServerSettings
    
    @BeforeMethod
    override fun setUp() {
        super.setUp()
        myServerSettings = object : ServerSettingsImpl() {
            override fun getServerUUID(): String? {
                return "SERVER-UUID"
            }
        }

    }

    @TestFor(issues = ["TW-63014"])
    fun no_labels(){
        val noLabelsYaml = "apiVersion: v1\n" +
                "kind: Pod\n" +
                "metadata:\n" +
                "  name: teamcity-agent\n" +
                "spec:\n" +
                "  containers:\n" +
                "  - name: teamcity-agent\n" +
                "    image: jetbrains/teamcity-agent"
        val provider = CustomTemplatePodTemplateProvider(myServerSettings) // { "${it.id}-123" }
        val userData = CloudInstanceUserData("", "", "http://127.0.0.1:9999", null, "kube-321",
                "Test Profile", emptyMap())
        val kubeTemplSpec = provider.getPodTemplateInternal(userData, "kube-img", "namespacccess", "instance-name", noLabelsYaml)
        then(kubeTemplSpec.metadata.labels).containsOnlyKeys("teamcity-cloud-profile", "teamcity-cloud-image", "teamcity-agent", "teamcity-server-uuid")
    }

    fun pod_with_pvc(){
        var str="apiVersion: v1\n" +
                "kind: Pod\n" +
                "metadata:\n" +
                "  labels:\n" +
                "    app: teamcity-agent\n" +
                "    agent-id: %instance.id%\n" +
                "spec:\n" +
                "  serviceAccount: deployer\n" +
                "  volumes:\n" +
                "    - name: agent-data\n" +
                "      persistentVolumeClaim:\n" +
                "        claimName: %instance.id%\n" +
                "  initContainers:\n" +
                "    - name: init-agent\n" +
                "      imagePullPolicy: IfNotPresent\n" +
                "      image: \"repo.labs.intellij.net/kubernetes-agents/devops-linux:latest\"\n" +
                "      command:\n" +
                "        - /bin/bash\n" +
                "        - -c\n" +
                "        - 'cd /usr/bin && curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.15.0/bin/linux/amd64/kubectl && chmod +x ./kubectl && cp -r /opt/buildagent/* /opt/buildagent-copy/'\n" +
                "      volumeMounts:\n" +
                "        - mountPath: /opt/buildagent-copy/\n" +
                "          name: agent-data\n" +
                "  containers:\n" +
                "    - name: teamcity-agent\n" +
                "      image: \"repo.labs.intellij.net/kubernetes-agents/devops-linux:latest\"\n" +
                "      lifecycle:\n" +
                "        preStop:\n" +
                "          exec:\n" +
                "            command:\n" +
                "              - /bin/sh\n" +
                "              - -c\n" +
                "              - \"rm -rf /opt/buildagent/*\"\n" +
                "      volumeMounts:\n" +
                "        - mountPath: /opt/buildagent/\n" +
                "          name: agent-data\n" +
                "      env:\n" +
                "        - name: DOCKER_WRAPPER_KUBE\n" +
                "          value: \"true\"\n" +
                "        - name: RUN_AS_BUILDAGENT\n" +
                "          value: \"true\"\n" +
                "        - name: VOLUME_PATH\n" +
                "          value: \"/data/agents/%instance.id%/\"\n" +
                "        - name: TEAMCITY_KUBERNETES_INSTANCE_NAME\n" +
                "          value: \"%instance.id%\"\n" +
                "---\n" +
                "apiVersion: v1\n" +
                "kind: PersistentVolumeClaim\n" +
                "metadata:\n" +
                "  labels:\n" +
                "    app: teamcity-agent\n" +
                "    agent-id: %instance.id%\n" +
                "  name: %instance.id%\n" +
                "spec:\n" +
                "  storageClassName: local-pv\n" +
                "  accessModes:\n" +
                "    - ReadWriteOnce\n" +
                "  volumeMode: Filesystem\n" +
                "  resources:\n" +
                "    requests:\n" +
                "      storage: 91Gi"

        str = str.replace("%instance.id%", "myInstanceId")
        val factory = YAMLFactory()
        val mapper = ObjectMapper()
        val parser = factory.createParser(str)
        val docs = mapper.readValues<ObjectNode>(parser, ObjectNode::class.java).readAll()
        docs.forEach {
            if (it["kind"]?.textValue() == "Pod") {
                val podTemplateSpec = Serialization.unmarshal(
                        ByteArrayInputStream(it.toString().toByteArray()),
                        PodTemplateSpec::class.java
                )
                println()
            } else if (it["kind"]?.textValue() == "PersistentVolumeClaim") {
                val podTemplateSpec = Serialization.unmarshal(
                        ByteArrayInputStream(it.toString().toByteArray()),
                        PersistentVolumeClaim::class.java
                )
                println()
            }

        }

//        val podTemplateSpec = Serialization.unmarshal(ByteArrayInputStream(str.toByteArray()),PodTemplateSpec::class.java)
//        val pvcSpec = Serialization.unmarshal(ByteArrayInputStream(str.toByteArray()),PersistentVolumeClaimSpec::class.java)

    }



}