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
@McpClientName("complaints") 
public class ComplaintsMcpAuthProvider implements McpClientAuthProvider {


    public ComplaintsMcpAuthProvider() {
        Log.debug("Initializing ComplaintsMcpAuthProvider");
    }
    private static final Logger LOG = Logger.getLogger(ComplaintsMcpAuthProvider.class);

    
    @NamedOidcClient("complaints") OidcClient complaintsClient;
    
    TokensHelper tokens = new TokensHelper();

    @Override
    public String getAuthorization(Input input) {
        LOG.debug("ComplaintsMcpAuthProvider Getting authorization token for MCP server");
        LOG.debug("ComplaintsMcpAuthProvider Input: ======== \n" + input.toString() + "\n ========");

        String uriString = input.uri().toString();
        

        LOG.debug("Using Complaints client for URI: " + uriString);

        String accessToken = tokens.getTokens(complaintsClient).await().indefinitely().getAccessToken();

        LOG.debug("\n\n\n Obtained access token for Complaints MCP server: " + accessToken);
        return "Bearer " + accessToken;
    }

    

}



    