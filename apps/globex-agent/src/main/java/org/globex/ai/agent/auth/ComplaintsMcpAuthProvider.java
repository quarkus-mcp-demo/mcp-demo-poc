package org.globex.ai.agent.auth;

import io.quarkiverse.langchain4j.mcp.auth.McpClientAuthProvider;
import io.quarkiverse.langchain4j.mcp.runtime.McpClientName;
import io.quarkus.logging.Log;
import io.quarkus.oidc.client.NamedOidcClient;
import io.quarkus.oidc.client.OidcClient;
import io.quarkus.oidc.client.OidcClients;
import io.quarkus.oidc.client.Tokens;
import io.quarkus.oidc.client.runtime.TokensHelper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;



@McpClientName("complaints")
@ApplicationScoped
public class ComplaintsMcpAuthProvider implements McpClientAuthProvider {


    public ComplaintsMcpAuthProvider() {
        Log.debug("Initializing ComplaintsMcpAuthProvider");
    }
    private static final Logger LOG = Logger.getLogger(ComplaintsMcpAuthProvider.class);

    
    @NamedOidcClient("complaints") OidcClient client;
    
    TokensHelper tokens = new TokensHelper();

    @Override
    public String getAuthorization(Input input) {
        LOG.debug("Getting authorization token for complaints MCP server");
        String accessToken = tokens.getTokens(client).await().indefinitely().getAccessToken();
        LOG.debug("\n\n\n Obtained access token for complaints MCP server: " + accessToken);
        return "Bearer " + accessToken;
    }

}



    