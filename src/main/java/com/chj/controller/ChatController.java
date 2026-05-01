package com.chj.controller;

import com.chj.pojo.Result;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    record ChatInput(String userInput) {}



    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestBody ChatInput chatInput) {
        Map<String,Integer> map = ThreadLocalUtil.get();
        String userId = String.valueOf(map.get("id"));
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
    @DeleteMapping()
    public Result deleteMemory(){
        Map<String,Integer> map = ThreadLocalUtil.get();
        String userId=String.valueOf(map.get("id"));
        chatMemory.clear(userId);
        log.info("删除用户{}的聊天记录", userId);
        return Result.success("记忆清楚成功");
    }
}
