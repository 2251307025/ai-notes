package com.chj.controller;

import com.chj.tool.ArticleTool;
import com.chj.tool.CategoryTool;
import com.chj.tool.UserTool;
import com.chj.utils.ThreadLocalUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RequestMapping("/chat")
@RestController
@Slf4j
public class ChatController {
    @Resource
    private ChatClient deepSeekChatClient;
    @Resource
    private ChatMemory chatMemory;
    @Resource
    private ArticleTool articleTool;
    @Resource
    private CategoryTool categoryTool;
    @Resource
    private UserTool userTool;
    record ChatInput(String userInput, String userId) {}

    private static final String SYSTEM_PROMPT = """
            你是一个AI笔记助手，帮助用户管理笔记（文章）和分类，并根据笔记内容回答用户的问题。

            你的核心能力：
            - 管理笔记：创建、查询、更新、删除笔记，搜索笔记内容，统计笔记数量
            - 管理分类：创建、查询、更新、删除分类，为分类生成英文别名
            - 查询当前登录用户的信息

            行为准则：
            - 当用户想创建笔记但没有指定分类时，先调用分类列表工具查询分类，让用户选择后再创建
            - 在执行删除、更新等不可逆操作前，先向用户确认
            - 用户提问时，主动调用工具查询笔记数据来回答，不要编造信息
            - 回答简洁清晰，使用中文""";

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestBody ChatInput chatInput) {
        String userId = chatInput.userId;
        String userInput = chatInput.userInput;
        if (userId == null || userInput == null) {
            return Flux.just(ServerSentEvent.<String>builder()
                    .data("{\"error\":\"用户输入信息不能为空\"}")
                    .event("error")
                    .build());
        }
        List<Message> messages = chatMemory.get(userId);
        log.info("历史记录{}", messages);
        log.info("用户{}询问{}", userId, userInput);
        ToolCallback[] toolCallbacks = ToolCallbacks.from(articleTool, categoryTool, userTool);
        return deepSeekChatClient.prompt()
                .system(SYSTEM_PROMPT)
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId(userId).build())
                .user(userInput)
                .toolCallbacks(toolCallbacks)
                .stream()
                .content()
                .map(content -> ServerSentEvent.<String>builder()
                        .data(content)
                        .event("message")
                        .build())
                .onErrorResume(e -> {
                    log.error("聊天流异常: userId={}, error={}", userId, e.getMessage(), e);
                    return Flux.just(ServerSentEvent.<String>builder()
                            .data("{\"error\":\"" +
                                    (StringUtils.hasText(e.getMessage()) ? e.getMessage() : "内部错误，请稍后重试") +
                                    "\"}")
                            .event("error")
                            .build());
                });
    }
}
