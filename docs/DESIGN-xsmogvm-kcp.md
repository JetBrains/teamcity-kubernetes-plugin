# TeamCity → KCP → XSmogVM cloud agents

Fork of `JetBrains/teamcity-kubernetes-plugin` (Apache-2.0, base commit `6868bd6`).

## Goal

Run TeamCity cloud build agents as VMs, where:

- the cloud connection targets a **KCP workspace** (not a normal kube-apiserver),
- agents are provisioned by creating an **`XSmogVM`** Crossplane resource (not a raw
  KubeVirt `VirtualMachineInstance`), which Crossplane reconciles into the actual
  KubeVirt VM + networking on a physical cluster,
- auth uses the plugin's existing **`oidc`** strategy against Keycloak (the federation
  that already lets KCP trust Keycloak id_tokens).

## Why the stock plugin can't do this

The instance lifecycle is hardcoded to **Pods**:

- `KubeApiConnector`: `Pod createPod(Pod)`, `deletePod(name)`, discovery via
  `kubernetesClient.pods().withLabels(labels).list()`.
- All deploy modes (`SimpleRunContainerProvider`, `CustomTemplatePodTemplateProvider`,
  `DeploymentBuildAgentPodTemplateProvider`) return a `Pod`.

Against **KCP this is meaningless** — KCP serves no pods/nodes. And an `XSmogVM` is a
different GVK entirely. So this is "add a custom-resource lifecycle beside the Pod
lifecycle", not "add a deploy mode".

## Design (generic custom-resource deploy mode)

1. **Dependency:** none new — use fabric8 `client.genericKubernetesResources(context)`
   with a `ResourceDefinitionContext` built from the XSmogVM GVK. Avoids a
   `kubevirt-client` dep and keeps it generic.
2. **Connector:** generalize create/delete/get/list from `Pod` to
   `GenericKubernetesResource` (new methods `createResource/deleteResource/listResources`
   keyed by GVK + label selector), or add a parallel `XSmogVmApiConnector`.
3. **New deploy mode:** `CustomResourceTemplateProvider` — takes user-supplied YAML
   (the `XSmogVM` manifest), injects TeamCity instance tags as labels + injects agent
   bootstrap config into the VM spec (see #5), and stamps the instance id.
4. **Readiness:** configurable status path — for Crossplane, poll
   `status.conditions[type=Ready].status == "True"` instead of pod phase `Running`.
5. **Agent bootstrap glue:** the VM image runs a TeamCity agent that self-registers with
   the same instance tags the plugin matches on (`KubeTeamCityLabels` /
   `KubeContainerEnvironment`: server URL, instance name, image id, auth token). For a VM
   this is delivered via the `XSmogVM` spec (cloud-init / sysprep), reusing the existing
   golden-image flow.
6. **KCP awareness:**
   - API URL is a workspace URL: `https://<kcp>/clusters/root:smog:<ws>`.
   - The connection "test"/discovery must NOT list pods. List namespaces or the XSmogVM
     CRs in the workspace instead — relax `KubeApiConnectorImpl` accordingly.

## Sequencing

1. **Auth PoC** (no fork build needed) — verify `oidc` + Keycloak authenticates to the
   KCP workspace. See `PoC-oidc-kcp.md`.
2. **Static self-registering VM** — `XSmogVM` whose image self-registers as a TC agent;
   working agents today, and it nails down the bootstrap contract for #5.
3. **Custom-resource deploy mode** — the connector generalization + provider + UI so
   TeamCity manages XSmogVM lifecycle (scale up / tear down).

## Build note

No JDK/Maven in the current dev env. Build in a JDK 17 + Maven container (or in TeamCity).
Base toolchain per upstream `pom.xml`.
