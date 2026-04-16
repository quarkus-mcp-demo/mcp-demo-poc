package org.globex.ai.agent.auth;

import io.quarkiverse.langchain4j.mcp.auth.McpClientAuthProvider;
import io.quarkus.logging.Log;
import io.quarkus.oidc.client.NamedOidcClient;
import io.quarkus.oidc.client.OidcClient;
import io.quarkus.oidc.client.runtime.TokensHelper;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;


@ApplicationScoped
public class GlobexMcpAuthProvider implements McpClientAuthProvider {


    public GlobexMcpAuthProvider() {
        Log.debug("Initializing GlobexMcpAuthProvider");
    }
    private static final Logger LOG = Logger.getLogger(GlobexMcpAuthProvider.class);

    
    @NamedOidcClient("complaints") OidcClient complaintsClient;
    @NamedOidcClient("globex-store") OidcClient globexStoreClient;
    
    TokensHelper tokens = new TokensHelper();

    @Override
    public String getAuthorization(Input input) {
        LOG.debug("GlobexMcpAuthProvider Getting authorization token for MCP server");
        LOG.debug("GlobexMcpAuthProvider Input: ======== \n" + input.toString() + "\n ========");

        String uriString = input.uri().toString();
        String clientType = determineClientType(uriString);

        if (clientType == null) {
            LOG.error("No matching OIDC client found for URI: " + uriString);
            return null;
        }

        OidcClient client = switch (clientType) {
            case "globex-store" -> globexStoreClient;
            case "complaints" -> complaintsClient;
            default -> null;
        };

        LOG.debug("Using " + clientType + " client for URI: " + uriString);

        String accessToken = tokens.getTokens(client).await().indefinitely().getAccessToken();

        LOG.debug("\n\n\n Obtained access token for " + clientType + " MCP server: " + accessToken);
        return "Bearer " + accessToken;
    }

    private String determineClientType(String uriString) {
        if (uriString.contains("globex-store") || uriString.contains("8085")) {
            return "globex-store";
        } else if (uriString.contains("complaints")) {
            return "complaints";
        }
        return "unknown";
    }

}



    