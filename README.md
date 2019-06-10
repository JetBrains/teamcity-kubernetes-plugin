# TeamCity Kubernetes Support Plugin
[![official JetBrains project](http://jb.gg/badges/official.svg)](https://plugins.jetbrains.com/plugin/9818-kubernetes-cloud-support)
[![plugin status](https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_TeamCityKubernetesPlugin_Build)/statusIcon.svg)](https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_TeamCityKubernetesPlugin_Build20181x&guest=1)

Run [TeamCity cloud agents](https://confluence.jetbrains.com/display/TCD10/TeamCity+Integration+with+Cloud+Solutions) in a [Kubernetes](https://kubernetes.io/) cluster. 

Support [Helm](https://docs.helm.sh/) build steps.

## Compatibility

The plugin is compatible with TeamCity 2017.1.x and later.

## Installation

You can [download the plugin](https://teamcity.jetbrains.com/guestAuth/app/rest/builds/buildType:TeamCityPluginsByJetBrains_TeamCityKubernetesPlugin_Build20172x,tags:release/artifacts/content/teamcity-kubernetes-plugin.zip) and install it as an [additional TeamCity plugin](https://confluence.jetbrains.com/display/TCDL/Installing+Additional+Plugins).

## Configuration

### Cloud agents

Configure Kubernetes [Cloud Profile](https://confluence.jetbrains.com/display/TCD10/Agent+Cloud+Profile#AgentCloudProfile-ConfiguringCloudProfile) for your project in the Server Administration UI.

The plugin supports Kubernetes cluster images to start new pods with a TeamCity build agent running in one of the containers. The plugin supports the [official TeamCity Build Agent Docker image](https://hub.docker.com/r/jetbrains/teamcity-agent) out of the box. You can use your own image as well.

### Helm steps

Add **Helm** build step to build configuration, choose one of supported commands: [install](https://docs.helm.sh/helm/#helm-install), [upgrade](https://docs.helm.sh/helm/#helm-upgrade), [rollback](https://docs.helm.sh/helm/#helm-rollback), [test](https://docs.helm.sh/helm/#helm-test), [delete](https://docs.helm.sh/helm/#helm-delete). 

Or use Kotlin DSL

```kotlin
object Helm_Deployment : BuildType({
    uuid = "866dd903-6f55-4a54-a621-065b380dd7fc"
    extId = "Helm_Deployment"
    name = "Deployment"

    steps {
        helmInstall {
            chart = "stable/teamcity-server"
            param("teamcity.helm.command", "helm-install")
        }
    }
})
```

Build agent to be compatible with Helm runner should provide **Helm_Path** configuration parameter which should point to the location of Helm executable. 
Plugin searches Helm in default location **/usr/local/bin/helm** on Linux machines.

## License

Apache 2.0

## Feedback

Please feel free to post feedback in the repository [issues](https://youtrack.jetbrains.com/issues/TW).

## Contributing guidelines

Follow general instructions to [build TeamCity plugins using Maven](https://confluence.jetbrains.com/display/TCD10/Developing+Plugins+Using+Maven).
Plugin uses [TeamCity SDK Maven plugin](https://github.com/JetBrains/teamcity-sdk-maven-plugin)

``` bash
# build and package plugin
mvn clean package

# deploy packed plugin to local Teamcity installation and start server and build agent
mvn tc-sdk:start

# stop locally running server and build agent
mvn tc-sdk:stop
```
