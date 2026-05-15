package com.chj.config;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;

import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
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

    private static final String SYSTEM_PROMPT = """
            你是一个AI笔记助手，帮助用户管理笔记（文章）和分类，并根据笔记内容回答用户的问题。

            你的核心能力：
            - 管理笔记：创建、查询、更新、删除笔记，搜索笔记内容，统计笔记数量
            - 管理分类：创建、查询、更新、删除分类，为分类生成英文别名
            - 查询当前登录用户的信息
            - 根据笔记内容回答用户问题

            回答格式（严格遵循）：
            当用户提问时，先调用工具搜索相关笔记。
            - 如果找到了相关笔记 → 直接给出答案，禁止添加任何前缀、解释或过程描述
            - 如果没有找到相关笔记 → 简单笔记中没有对应消息，我无法回答你的问题

            禁止出现在回答中的内容：
            1. "我无法回答"、"我的知识来源于"等表示拒绝或解释知识来源的语句
            2. "根据我的笔记"、"根据笔记记录"、"笔记中提到"等引用来源的语句
            3. "让我查一下"、"让我搜索"、"让我看看"等表示正在查询的语句
            4. 任何描述工具调用过程、思考过程或回答来源的元评论

            行为准则：
            - 当用户想创建笔记但没有指定分类时，先调用分类列表工具查询分类，让用户选择后再创建
            - 在执行删除、更新等不可逆操作前，先向用户确认
            - 用户提问时，主动调用工具查询笔记数据来回答，不要编造信息
            - 回答简洁清晰，使用中文""";

    @Bean
    public ChatClient openAiChatClient(DeepSeekChatModel deepSeekChatModel) {
        return ChatClient.builder(deepSeekChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultOptions(DeepSeekChatOptions.builder().build())
                .build();
    }
}


