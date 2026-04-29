package org.globex.ai.agent.auth;

import io.quarkiverse.langchain4j.mcp.auth.McpClientAuthProvider;
import io.quarkiverse.langchain4j.mcp.runtime.McpClientName;
import io.quarkus.logging.Log;
import io.quarkus.oidc.client.NamedOidcClient;
import io.quarkus.oidc.client.OidcClient;
import io.quarkus.oidc.client.runtime.TokensHelper;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;


@ApplicationScoped
@McpClientName("globex-store") 
public class GlobexMcpAuthProvider implements McpClientAuthProvider {


    public GlobexMcpAuthProvider() {
        Log.debug("Initializing GlobexMcpAuthProvider");
    }
    private static final Logger LOG = Logger.getLogger(GlobexMcpAuthProvider.class);

    
    @NamedOidcClient("globex-store") OidcClient globexStoreClient;
    
    TokensHelper tokens = new TokensHelper();

    @Override
    public String getAuthorization(Input input) {
        LOG.debug("GlobexMcpAuthProvider Getting authorization token for MCP server");
        LOG.debug("GlobexMcpAuthProvider Input: ======== \n" + input.toString() + "\n ========");

        String accessToken = tokens.getTokens(globexStoreClient).await().indefinitely().getAccessToken();

        LOG.debug("\n\n\n Obtained access token for GlobexMcpAuthProvider MCP server: " + accessToken);
        return "Bearer " + accessToken;
    }


}



    