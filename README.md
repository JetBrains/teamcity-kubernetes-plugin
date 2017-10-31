# TeamCity Kubernetes Support Plugin
[![official JetBrains project](http://jb.gg/badges/official.svg)](https://plugins.jetbrains.com/plugin/9818-kubernetes-cloud-support)
https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityPluginsByJetBrains_TeamCityKubernetesPlugin_Build)/statusIcon.svg)](https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityPluginsByJetBrains_TeamCityKubernetesPlugin_Build&guest=1)

Run TeamCity cloud agents in a Kubernetes cluster.

## Compatibility

The plugin is compatible with TeamCity 2017.1.x and later.

## Installation

You can [download the plugin](https://teamcity.jetbrains.com/guestAuth/app/rest/builds/buildType:TeamCityPluginsByJetBrains_TeamCityKubernetesPlugin_Build,tags:release/artifacts/content/teamcity-kubernetes-plugin.zip) and install it as an [additional TeamCity plugin](https://confluence.jetbrains.com/display/TCDL/Installing+Additional+Plugins).

## Configuration

Configure Kubernetes [Cloud Profile](https://confluence.jetbrains.com/display/TCD10/Agent+Cloud+Profile#AgentCloudProfile-ConfiguringCloudProfile) for your project in the Server Administration UI.

The plugin supports Kubernetes cluster images to start new pods with a TeamCity build agent running in one of the containers. The plugin supports the [official TeamCity Build Agent Docker image](https://hub.docker.com/r/jetbrains/teamcity-agent) out of the box. You can use your own image as well.

## License

Apache 2.0

## Feedback

Please feel free to post feedback in the repository [issues](https://github.com/JetBrains/teamcity-kubernetes-plugin/issues).
