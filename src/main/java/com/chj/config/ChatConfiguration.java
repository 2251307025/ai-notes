package com.chj.config;


import com.chj.tool.ArticleTool;
import com.chj.tool.CategoryTool;
import com.chj.tool.TtlToolCallbackWrapper;
import com.chj.tool.UserTool;
import com.chj.utils.PromptUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;

import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Configuration
@Slf4j
public class ChatConfiguration {
//    @Bean
//    public ChatClient deepSeekChatClient(DeepSeekChatModel deepSeekChatModel, ChatMemory chatMemory) {
//        return ChatClient.builder(deepSeekChatModel)
//                .build();//   .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
//    }

    //    @Bean
//    @Primary
//    public ChatMemoryRepository chatMemoryRepository(){
//        return new InMemoryChatMemoryRepository();
//    }

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



    @Bean
    public ChatClient deepSeekChatClient(DeepSeekChatModel deepSeekChatModel) {
        return ChatClient.builder(deepSeekChatModel)
                .defaultSystem(PromptUtil.systemPrompt)
                .defaultOptions(DeepSeekChatOptions.builder().build())
                .build();
    }
}


