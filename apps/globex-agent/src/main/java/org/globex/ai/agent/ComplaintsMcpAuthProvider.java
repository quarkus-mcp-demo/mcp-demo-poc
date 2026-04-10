package org.globex.ai.agent;

import io.quarkiverse.langchain4j.mcp.auth.McpClientAuthProvider;
import io.quarkiverse.langchain4j.mcp.runtime.McpClientName;
import io.quarkus.oidc.client.OidcClient;
import io.quarkus.oidc.client.OidcClients;
import io.quarkus.oidc.client.Tokens;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
@McpClientName("complaints")
public class ComplaintsMcpAuthProvider implements McpClientAuthProvider {

    private static final Logger LOG = Logger.getLogger(ComplaintsMcpAuthProvider.class);

    @Inject
    OidcClients oidcClients;

    @Override
    public String getAuthorization(Input input) {
        try {
            OidcClient oidcClient = oidcClients.getClient("complaints");
            Tokens tokens = oidcClient.getTokens().await().indefinitely();
            LOG.debug("Retrieved Bearer token for complaints MCP server");
            return "Bearer " + tokens.getAccessToken();
        } catch (Exception e) {
            LOG.error("Failed to get OIDC token for complaints MCP server", e);
            return null;
        }
    }
}
