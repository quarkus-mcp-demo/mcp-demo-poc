package org.globex.ai.agent.auth;

import io.quarkiverse.langchain4j.mcp.auth.McpClientAuthProvider;
import io.quarkiverse.langchain4j.mcp.runtime.McpClientName;
import io.quarkus.oidc.client.OidcClient;
import io.quarkus.oidc.client.OidcClients;
import io.quarkus.oidc.client.Tokens;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
@McpClientName("globex-store")
public class GlobexStoreMcpAuthProvider implements McpClientAuthProvider {

    private static final Logger LOG = Logger.getLogger(GlobexStoreMcpAuthProvider.class);

    @Inject
    OidcClients oidcClients;

    @Override
    public String getAuthorization(Input input) {
        try {
            OidcClient oidcClient = oidcClients.getClient("globex-store");
            Tokens tokens = oidcClient.getTokens().await().indefinitely();
            LOG.debug("Retrieved Bearer token for globex-store MCP server");
            return "Bearer " + tokens.getAccessToken();
        } catch (Exception e) {
            LOG.error("Failed to get OIDC token for globex-store MCP server", e);
            return null;
        }
    }
}
