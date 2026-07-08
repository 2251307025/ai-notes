package com.chj.config;

import com.chj.utils.PromptUtil;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Slf4j
@EnableConfigurationProperties
public class ChatConfiguration {

    // ==================== Chat Memory ====================

    @Bean
    @Primary
    public ChatMemoryRepository chatMemoryRepository(JdbcTemplate jdbcTemplate) {
        return JdbcChatMemoryRepository.builder()
                .jdbcTemplate(jdbcTemplate)
                .build();
    }

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();
    }

    // ==================== SiliconFlow Properties ====================

    @Bean
    @ConfigurationProperties(prefix = "siliconflow")
    public SiliconFlowProperties siliconFlowProperties() {
        return new SiliconFlowProperties();
    }

    // ==================== Multi-Model ChatClient Registry ====================

    /**
     * 预构建所有可用模型的 ChatClient。
     * 直接使用 spring.ai.openai 自动配置好的 OpenAiChatModel（已指向 SiliconFlow），
     * 具体模型名称由 defaultOptions.model() 决定。
     * key = modelId（如 "Qwen/Qwen3-8B"），value = 对应的 ChatClient。
     */
    @Bean
    public Map<String, ChatClient> siliconFlowChatClients(
            OpenAiChatModel openAiChatModel,
            SiliconFlowProperties props) {

        Map<String, ChatClient> clients = new LinkedHashMap<>();

        for (SiliconFlowProperties.ModelItem model : props.getModels()) {
            String modelId = model.getModelId();
            try {
                ChatClient client = ChatClient.builder(openAiChatModel)
                        .defaultSystem(PromptUtil.systemPrompt)
                        .defaultOptions(OpenAiChatOptions.builder()
                                .model(modelId)
                                .build())
                        .build();
                clients.put(modelId, client);
                log.info("已注册硅基流模型：{} ({})", modelId, model.getModelName());
            } catch (Exception e) {
                log.error("注册模型 {} 失败：{}", modelId, e.getMessage(), e);
            }
        }

        if (clients.isEmpty()) {
            throw new IllegalStateException("未注册任何硅基流模型，请检查 siliconflow.models 配置");
        }

        // 校验默认模型
        if (!clients.containsKey(props.getDefaultModel())) {
            String first = clients.keySet().iterator().next();
            log.warn("默认模型 {} 未在 models 列表中，将使用第一个可用模型 {}",
                    props.getDefaultModel(), first);
        }

        return clients;
    }

    // ==================== Properties DTO ====================

    @Data
    public static class SiliconFlowProperties {
        private String apiKey;
        private String baseUrl;
        private String defaultModel;
        private List<ModelItem> models;

        @Data
        public static class ModelItem {
            private String modelId;
            private String modelName;
        }
    }
}
