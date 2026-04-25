package com.chj.config;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.MysqlChatMemoryRepositoryDialect;
import org.springframework.ai.chat.memory.repository.jdbc.PostgresChatMemoryRepositoryDialect;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ChatConfiguration {
    @Bean
    public ChatClient deepSeekChatClient(DeepSeekChatModel deepSeekChatModel, ChatMemory chatMemory) {
        return ChatClient.builder(deepSeekChatModel)
                .build();//   .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
    }

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


//@Bean
//public ChatClient deepSeekChatClient(DeepSeekChatModel deepSeekChatModel) {
//    return ChatClient.builder(deepSeekChatModel)
//            .defaultOptions(OllamaChatOptions.builder().disableThinking().build())
//            .build();
//}
}

