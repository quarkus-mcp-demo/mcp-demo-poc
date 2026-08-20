I need to set up a Quarkus application with SPIFFE-based OIDC authentication to Keycloak, plus OAuth 2.0 Token Exchange to call a backend service.

## Part 1: SPIFFE Client Authentication for App A (webapp)

Set up SPIFFE as the client authentication method for Keycloak OIDC:

- Add `quarkus-oidc` with `application-type=web-app`
- Create a `SpiffeClientAssertionTypeRequestFilter` implementing `io.quarkus.oidc.common.OidcRequestFilter` that intercepts OIDC token requests and replaces the default `client-assertion-type` (`urn:ietf:params:oauth:client-assertion-type:jwt-bearer`) with the SPIFFE one (`urn:ietf:params:oauth:client-assertion-type:jwt-spiffe`)
- Configure OIDC credentials to use a JWT SVID file:
  ```
  quarkus.oidc.credentials.jwt.token-path=/svids/jwt.token
  quarkus.oidc.credentials.jwt.source=bearer
  quarkus.oidc.credentials.jwt.issuer=<spire-oidc-discovery-provider-url>
  quarkus.oidc.credentials.jwt.subject=spiffe://<trust-domain>/ns/<namespace>/sa/<service-account>
  ```

Helm chart for App A needs:
- An init container + sidecar running `spiffe-helper` (upstream: `ghcr.io/spiffe/spiffe-helper`, OpenShift: `registry.redhat.io/zero-trust-workload-identity-manager/spiffe-helper-rhel9`)
- A ConfigMap with `helper.conf` that configures the SPIRE agent socket path, cert output dir, and JWT SVID audience (set to the Keycloak issuer URL)
- Volumes: `spiffe-workload-api` (CSI driver `csi.spiffe.io`), `svids` (emptyDir shared between spiffe-helper and app), `spiffe-helper-config` (ConfigMap)
- The webapp container mounts `/svids` to read the JWT SVID
- Env vars to override OIDC config: `QUARKUS_OIDC_AUTH_SERVER_URL`, `QUARKUS_OIDC_CLIENT_ID`, `QUARKUS_OIDC_CREDENTIALS_JWT_TOKEN_PATH=/svids/jwt.token`, `QUARKUS_OIDC_AUTHENTICATION_FORCE_REDIRECT_HTTPS_SCHEME=true`

Keycloak client setup for the webapp:
- Client authentication: ON
- Standard flow: enabled
- Client authenticator: Signed JWT
- Valid redirect URIs: the app's route URL

## Part 2: Token Exchange to Backend Service (App B)

- Add `quarkus-oidc-client` and `quarkus-rest-client-jackson` to App A
- Configure a named OIDC client (`exchange`) with `grant.type=exchange` and `scopes=<api-scope-name>`, using the same SPIFFE credentials as the main OIDC config
- Create a REST client interface for App B with `@HeaderParam("Authorization")`
- Create a `/token-exchange` page that exchanges the user's access token, calls App B, and displays original token, exchanged token, and API response

## Part 3: App B (backend API)

- Separate Quarkus project with `quarkus-oidc` (`application-type=service`) and `quarkus-rest-jackson`
- Validates bearer tokens with `quarkus.oidc.token.audience` matching its client ID
- Exposes `/api/userinfo` returning the authenticated user's JWT claims as JSON
- No SPIFFE needed — bearer-only validation
- Simpler Helm chart: single container, no SPIFFE sidecar, just OIDC env vars
- Deploy in the same namespace as App A

## Keycloak Setup (v26)

- Enable `token-exchange` feature on the server
- Create webapp client (Signed JWT auth, Standard flow, Standard Token Exchange enabled)
- Create API client (client auth ON, no flows — bearer-only)
- Create a client scope with an Audience mapper pointing to the API client
- Add scope as optional to the webapp client

## Container Images

- Base image: `registry.access.redhat.com/ubi9/openjdk-21-runtime:latest`
- Multi-arch build: `podman build --platform linux/amd64,linux/arm64 --manifest <image> .`
- Push with: `podman manifest push <image>`

## Customization

Adjust these values for your project:
- Trust domain and SPIRE OIDC discovery provider URL
- Keycloak URL and realm name
- Client IDs for webapp and API
- Kubernetes namespace
- Container image registry/repository
- Ingress/route hostnames

## Reference

- https://quarkus.io/blog/secure-mcp-oidc-client/
