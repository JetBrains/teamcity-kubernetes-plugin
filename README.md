# TeamCity Kubernetes Support Plugin
[![official JetBrains project](http://jb.gg/badges/official.svg)](https://plugins.jetbrains.com/plugin/9818-kubernetes-cloud-support)
[![plugin status](https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_TeamCityKubernetesPlugin_Build)/statusIcon.svg)](https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_TeamCityKubernetesPlugin_Build&guest=1)

Run [TeamCity cloud agents](https://confluence.jetbrains.com/display/TCD10/TeamCity+Integration+with+Cloud+Solutions) in a [Kubernetes](https://kubernetes.io/) cluster. 

Support [Helm](https://docs.helm.sh/) build steps.

## Compatibility

The plugin is compatible with TeamCity 2017.1.x and later.

## Installation

You can [download the plugin](https://teamcity.jetbrains.com/guestAuth/app/rest/builds/buildType:TeamCityPluginsByJetBrains_TeamCityKubernetesPlugin_Build,tags:release/artifacts/content/teamcity-kubernetes-plugin.zip) and install it as an [additional TeamCity plugin](https://confluence.jetbrains.com/display/TCDL/Installing+Additional+Plugins).

## Configuration

### Cloud Agents

Configure Kubernetes [Cloud Profile](https://confluence.jetbrains.com/display/TCD10/Agent+Cloud+Profile#AgentCloudProfile-ConfiguringCloudProfile) for your project in the Server Administration UI.

The plugin supports Kubernetes cluster images to start new pods with a TeamCity build agent running in one of the containers. The plugin supports the [official TeamCity Build Agent Docker image](https://hub.docker.com/r/jetbrains/teamcity-agent) out of the box. You can use your own image as well.

### Helm Steps

Add **Helm** build step to build configuration, choose one of supported commands: [install](https://docs.helm.sh/helm/#helm-install), [upgrade](https://docs.helm.sh/helm/#helm-upgrade), [rollback](https://docs.helm.sh/helm/#helm-rollback), [test](https://docs.helm.sh/helm/#helm-test), [delete](https://docs.helm.sh/helm/#helm-delete). 

Build agent to be compatible with Helm runner should provide **Helm_Path** configuration parameter which should point to the location of Helm executable. 
Plugin searches Helm in default location **/usr/local/bin/helm** on Linux machines.

## License

Apache 2.0

## Feedback

Please feel free to post feedback in the repository [issues](https://github.com/JetBrains/teamcity-kubernetes-plugin/issues).
