# Building the plugin and integrating XSmogVM agents with TeamCity

This fork of `JetBrains/teamcity-kubernetes-plugin` adds a **custom resource deploy mode**
that lets a TeamCity Kubernetes cloud profile provision build agents by creating
**XSmogVM** Crossplane resources in a **KCP workspace** (instead of Pods in a regular
cluster). Crossplane reconciles each XSmogVM into a KubeVirt VM whose golden image runs a
TeamCity build agent that self-registers as an ephemeral cloud agent.

Companion docs:

- [DESIGN-xsmogvm-kcp.md](DESIGN-xsmogvm-kcp.md) — why the stock plugin can't do this and the design.
- [PoC-oidc-kcp.md](PoC-oidc-kcp.md) — proving Keycloak `oidc` auth against the KCP workspace before anything else.

---

## 1. Building

### Prerequisites

- Docker (no local JDK/Maven needed), or JDK 17 + Maven 3.9 locally.
- Network access to `download.jetbrains.com` and `packages.jetbrains.team` (the build
  pulls TeamCity open-API artifacts and the `5.16.0-teamcity-patched` fabric8 client).

### Containerized build (recommended)

```bash
cd teamcity-kubevirt-plugin
docker run --rm \
  -v "$PWD":/src \
  -v kubevirt-plugin-m2:/root/.m2 \
  -w /src \
  maven:3.9-eclipse-temurin-17 \
  mvn -B package -Dteamcity.version=2025.11
```

Notes:

- **`-Dteamcity.version` is required**: the pom defaults to `LOCAL-SNAPSHOT`, which only
  exists in JetBrains' internal dev environment. Use **2025.11** — the base commit tracks
  upstream master, which needs `CanStartNewInstanceResult.ReasonType` (added in 2025.11;
  2025.07 and older fail to compile). Don't use 2026.1: its published `server-api`/
  `cloud-interface` artifacts are restructured stubs that this pom layout can't consume.
- The named volume `kubevirt-plugin-m2` caches dependencies between builds.
- Add `-DskipTests` to skip the TestNG suite once you trust a change.
- Known: 4 upstream tests (`KubernetesCredentialsFactoryImplTest`,
  `AvailableKubeConnectionsControllerTest`, `EKSAuthStrategyTest`,
  `DefaultServiceAccountAuthStrategyTest`) fail outside JetBrains' environment with
  `NoClassDefFoundError: jetbrains/buildServer/license/ReleaseDateHolder` — the published
  test harness lacks internal license classes. Unrelated to this fork's changes; the
  build still packages successfully.
- The code targets Java 8 (`maven.compiler.release=8`); JDK 17 cross-compiles it fine.

### Build output

The deployable plugin package is the **server-side zip** produced by the root module:

```
target/teamcity-kubernetes-plugin.zip
```

(The exact name matches `teamcity-plugin.xml`'s plugin name; check `target/` after the build.)

### Installing into TeamCity

1. TeamCity UI → **Administration → Plugins → Upload plugin zip**, or copy the zip into
   `<TeamCity Data Directory>/plugins/`.
2. Restart the server (or use plugin reload if your version supports it).
3. The stock "Kubernetes Support" bundled plugin must be **disabled** if its version
   collides with this fork (both register cloud code `kube`). Administration → Plugins →
   disable the bundled one.

---

## 2. What the new deploy mode does

A cloud image configured with **“Use custom resource template (VM / KCP)”**:

1. Generates an instance name from the **agent name prefix** (e.g. `winvm-3`).
2. Takes your **custom resource manifest** (YAML), substitutes placeholders, stamps
   `metadata.name` with the instance name and injects the TeamCity tracking labels
   (`teamcity-agent`, `teamcity-server-uuid`, `teamcity-cloud-profile`, `teamcity-cloud-image`).
3. `POST`s it to the API server / KCP workspace using the GVK **derived from the
   manifest's `apiVersion`/`kind`** (plural derived from kind, or set explicitly).
4. Discovers/synchronizes instances every 60 s by listing that GVK with the labels above
   (`teamcity.kube.pods.monitoring.period` to tune).
5. Marks the instance **RUNNING** when **the build agent registers on the server**
   (primary signal — your golden image "connects to TeamCity and that marks it ready"),
   or when the resource reports a Crossplane-style `Ready=True` condition.
   A `Synced=False` condition is surfaced as the instance error in the cloud profile UI.
6. On scale-down/idle-timeout, TeamCity **deletes the custom resource**; Crossplane
   garbage-collects the VM. Ephemeral agents: every build (or idle period) ends with the
   VM destroyed, the next demand creates a fresh one.

### Placeholders available in the manifest

| Placeholder | Value | Must end up as (in the VM) |
|---|---|---|
| `%instance.id%` | generated resource/instance name | env `TC_K8S_INSTANCE_NAME` |
| `%agent.name%` | agent name prefix + instance id | agent `name` in `buildAgent.properties` |
| `%server.url%` | TeamCity server URL | agent `serverUrl` |
| `%server.uuid%` | TeamCity server UUID | env `TC_K8S_SERVER_UUID` |
| `%profile.id%` | cloud profile id | env `TC_K8S_CLOUD_PROFILE_ID` |
| `%image.id%` | cloud image id | env `TC_K8S_IMAGE_NAME` |

---

## 3. The agent bootstrap contract (golden image)

TeamCity matches a registering agent to a cloud instance through **environment
variables visible to the agent process**. The VM image must set, before the agent starts:

```
TC_K8S_SERVER_UUID      = %server.uuid%
TC_K8S_CLOUD_PROFILE_ID = %profile.id%
TC_K8S_IMAGE_NAME       = %image.id%
TC_K8S_INSTANCE_NAME    = %instance.id%
SERVER_URL              = %server.url%        (what the agent connects to)
```

On Windows, set them machine-wide before the agent service starts, e.g. in a
cloud-init/sysprep step:

```powershell
[Environment]::SetEnvironmentVariable('TC_K8S_SERVER_UUID','%server.uuid%','Machine')
[Environment]::SetEnvironmentVariable('TC_K8S_CLOUD_PROFILE_ID','%profile.id%','Machine')
[Environment]::SetEnvironmentVariable('TC_K8S_IMAGE_NAME','%image.id%','Machine')
[Environment]::SetEnvironmentVariable('TC_K8S_INSTANCE_NAME','%instance.id%','Machine')
[Environment]::SetEnvironmentVariable('SERVER_URL','%server.url%','Machine')
# point the preinstalled agent at the server and name it
(Get-Content C:\BuildAgent\conf\buildAgent.properties) `
  -replace '^serverUrl=.*','serverUrl=%server.url%' `
  -replace '^name=.*','name=%agent.name%' |
  Set-Content C:\BuildAgent\conf\buildAgent.properties
Restart-Service TCBuildAgent
```

Alternatively, append the values as `env.TC_K8S_*=...` lines to
`buildAgent.properties` — TeamCity treats those identically for matching.

How the manifest delivers this is up to your XSmogVM spec — typically a cloud-init
`userData`/sysprep field where you embed the placeholders.

### Ephemeral behavior

- Set the cloud profile's **terminate conditions** ("terminate instance: after first
  build", or an idle timeout) — TeamCity then deletes the XSmogVM after each build,
  giving you single-use VMs.
- Agent authorization is automatic for cloud agents once the instance is matched.

---

## 4. TeamCity configuration walkthrough

### 4.1 Keycloak / KCP auth (once)

Follow [PoC-oidc-kcp.md](PoC-oidc-kcp.md): create the confidential Keycloak client, mint
an **offline** refresh token (`scope=openid offline_access`), verify with `curl` that the
resulting `id_token` can list resources in the workspace.

### 4.2 Cloud profile

Project **Settings → Cloud Profiles → Add profile → Kubernetes Agents**:

| Field | Value |
|---|---|
| Kubernetes API server URL | the KCP **workspace** URL, e.g. `https://<kcp-host>/clusters/root:smog:bcn` |
| CA certificate | the KCP CA (avoid disabling TLS verification) |
| Namespace | leave **empty** for cluster-scoped XSmogVMs (the connection test then just verifies API reachability — this fork no longer requires a namespace); set it if your resources are namespaced claims |
| Authentication strategy | **Open ID** (`client_id`, `client_secret`, `issuer_url` = `https://<kc>/realms/<realm>`, `refresh_token`) |

Click **Test connection** — with an empty namespace it verifies reachability/auth (a 403
on listing namespaces still counts as success, since KCP RBAC may only grant access to
your CRs).

### 4.3 Cloud image

**Add image** in the profile:

| Field | Value |
|---|---|
| Agent name prefix | e.g. `winvm` (required for this mode; also used as the resource name prefix) |
| Pod specification | **Use custom resource template (VM / KCP)** |
| Custom resource manifest | your XSmogVM YAML (below) |
| Cluster-scoped resource | check for a Crossplane composite/XR (`XSmogVM`); uncheck for a namespaced claim |
| Resource plural | `xsmogvms` (optional — derived from the kind if empty) |
| Max instances / agent pool | as usual |

Example manifest:

```yaml
apiVersion: smog.example.io/v1alpha1   # your real group/version
kind: XSmogVM
metadata:
  labels:
    smog.example.io/purpose: teamcity-agent
spec:
  image: windows-server-2022-tc-agent   # golden image with the TC agent preinstalled
  cpu: 8
  memoryGi: 16
  userData: |
    # consumed by cloud-init/sysprep inside the VM
    serverUrl=%server.url%
    agentName=%agent.name%
    serverUuid=%server.uuid%
    profileId=%profile.id%
    imageId=%image.id%
    instanceId=%instance.id%
```

`metadata.name` is set by the plugin — don't hardcode it. Labels you add are preserved;
the plugin adds its tracking labels on top, and discovery re-reads the resource by those
labels, so the composition must not strip labels from the XSmogVM itself.

### 4.4 RBAC in the KCP workspace

The identity behind the OIDC token needs, on the XSmogVM GVK (and `namespaces` get/list
if you set a namespace):

```
verbs: get, list, create, delete
```

### 4.5 Verify end to end

1. Test connection on the profile → success.
2. Agents → Cloud tab → **Start** an instance of the image; an `xsmogvm` named
   `<prefix>-1` should appear in the workspace (`kubectl --server <workspace-url> get xsmogvms`).
3. The VM boots, the agent registers; the instance flips to Running on the cloud tab the
   moment the agent connects (this fork marks it ready on agent registration).
4. Run a build pinned to the image's agent pool; on completion/idle-timeout TeamCity
   deletes the resource and Crossplane tears the VM down.

---

## 5. Troubleshooting

| Symptom | Likely cause / fix |
|---|---|
| Test connection: `invalid namespace` | You set a namespace that doesn't exist in the workspace. For cluster-scoped XSmogVMs, leave it empty. |
| Test connection: 401 | OIDC trust broken — check issuer URL, client secret, refresh token freshness (use an offline token), KCP `--oidc-*` flags. |
| Resource created but instance stuck "Starting" | The VM never registered an agent. Check VM console: env vars set? `serverUrl` reachable from the VM network? Server UUID/profile id mismatched (agent registers but isn't matched — see `teamcity-clouds.log`). |
| Instance error `ReconcileError: ...` | Crossplane `Synced=False` — the composition failed; inspect the XSmogVM with `kubectl describe`. |
| `405` / `404` creating the resource | Wrong plural (set **Resource plural** explicitly) or the API isn't bound into the workspace. |
| Agent registers but a new instance starts for each build forever | Profile terminate conditions — that's the ephemeral design; tune idle timeout if you want reuse. |
| Plugin doesn't load | Conflict with the bundled Kubernetes plugin — disable it. |

Server logs: `teamcity-clouds.log` carries everything this plugin does (creation,
discovery, agent-registered transitions, deletions).

---

## 6. Code map of the fork's changes

| Area | Files |
|---|---|
| Generic CR connector ops | `connector/KubeApiConnector(.Impl)`, `connector/CustomResourceContext` |
| Deploy mode | `podSpec/CustomResourceTemplateProvider` (registered in `BuildAgentPodTemplateProvidersImpl`) |
| Instance lifecycle | `KubeCloudCustomResourceInstance`, CR branches in `KubeCloudClient` (start/terminate) and `KubeCloudImageImpl.populateInstances` |
| Readiness on agent registration | `KubeBackgroundUpdaterImpl` (agentRegistered listener) |
| KCP-friendly connection test | `KubeApiConnectorImpl.testConnection` (empty namespace ⇒ reachability/auth check) |
| UI | `editProfile.jsp`, `kubeSettings.js` (manifest editor, cluster-scoped flag, plural override) |
| Image params | `KubeParametersConstants`, `KubeCloudImageData`, `KubeCloudImage(.Impl)` |
| Tests | `CustomResourceContextTest`, `CustomResourceTemplateProviderTest`, `KubeCloudCustomResourceInstanceTest` |
