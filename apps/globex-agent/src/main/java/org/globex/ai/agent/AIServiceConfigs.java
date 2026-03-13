package org.globex.ai.agent;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithParentName;

import java.util.Map;

@ConfigMapping(prefix = "aiservice")
public interface AIServiceConfigs {

    @WithParentName
    Map<String, AIServiceConfig> aiServiceConfigs();
}
