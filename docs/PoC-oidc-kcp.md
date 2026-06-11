# Auth PoC: TeamCity `oidc` strategy → Keycloak → KCP workspace

Goal: prove the **built-in** `oidc` auth strategy authenticates a TeamCity Kubernetes
cloud profile to a **KCP workspace**, before writing any VM code. No fork build required —
the `oidc` ("Open ID") strategy already ships in the installed plugin.

## How the strategy behaves (read from source)

`OIDCAuthStrategy.kt` + `RefreshableStrategy.kt`:

- Inputs: `client_id`, `client_secret` (secure), `issuer_url`, `refresh_token` (secure).
- On use: resolves `<issuer>/.well-known/openid-configuration` (tries an Azure-style
  `v2.0/...` first, then falls back to the plain path — the fallback is what Keycloak
  uses), reads `token_endpoint`, then does a **`grant_type=refresh_token`** POST with the
  client creds as HTTP Basic, and uses the returned **`id_token`** as the cluster bearer
  token. Caches by `expires_in`, refreshes on expiry.

Implications:
- The refresh response must contain `id_token` → the original token must have been issued
  with scope `openid`.
- For a long-lived CI credential, mint an **offline** refresh token (`scope=openid
  offline_access`) so it survives SSO session idle/max. (See token-freshness gotchas in
  the federated-JWT notes.)

## Keycloak setup

1. Confidential client, e.g. `teamcity-kcp`: client authentication ON (note the secret),
   standard flow enabled.
2. Ensure the client/realm issues an `id_token` whose `iss` and `aud` match what KCP is
   configured to trust (KCP `--oidc-issuer-url` / `--oidc-client-id`).
3. Mint the initial offline refresh token once (auth-code or password grant), e.g.:

   ```bash
   curl -s -X POST "https://<kc>/realms/<realm>/protocol/openid-connect/token" \
     -d grant_type=password -d client_id=teamcity-kcp \
     -d client_secret=<secret> -d username=<svc-user> -d password=<pw> \
     -d 'scope=openid offline_access' | jq -r .refresh_token
   ```

   Capture `refresh_token` — that is the value for the strategy's "Refresh token" field.

## TeamCity cloud profile

Project Settings → Cloud Profiles → add **Kubernetes**:

- **Kubernetes API server URL** = the KCP **workspace** URL, e.g.
  `https://<kcp-host>/clusters/root:smog:bcn`.
- **Authentication strategy** = **Open ID**.
- Fill `client_id`, `client_secret`, `issuer_url` (`https://<kc>/realms/<realm>`),
  `refresh_token`.
- Provide the KCP CA cert (prefer over disabling TLS verify).
- Namespace = the workspace namespace where `SmogVM` claims live (if namespaced).

## Verifying auth WITHOUT pods (the KCP twist)

KCP serves no pods, so the stock "test connection" (which lists pods) will fail even when
auth is fine. Isolate auth from the plugin's pod assumption:

1. Get an id_token the same way the strategy does, then hit the workspace directly:

   ```bash
   ID_TOKEN=$(curl -s -X POST "https://<kc>/realms/<realm>/protocol/openid-connect/token" \
     -u teamcity-kcp:<secret> \
     -d grant_type=refresh_token -d refresh_token="<refresh_token>" | jq -r .id_token)

   curl -sk -H "Authorization: Bearer $ID_TOKEN" \
     "https://<kcp-host>/clusters/root:smog:bcn/api/v1/namespaces" | jq '.items[].metadata.name'
   # or list your CRs:
   curl -sk -H "Authorization: Bearer $ID_TOKEN" \
     "https://<kcp-host>/clusters/root:smog:bcn/apis/<group>/<version>/xsmogvms"
   ```

2. 200 + a JSON list → Keycloak→KCP trust + RBAC are good; auth is proven.
   401/403 → fix issuer/aud trust or RBAC binding for the token's user/groups.

## Outcome

If step 2 returns data, the `oidc` strategy will authenticate the cloud profile. The
remaining work is the VM lifecycle (see `DESIGN-xsmogvm-kcp.md`) — including relaxing the
connection test so it does not assume pods.
