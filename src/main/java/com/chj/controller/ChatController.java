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
    record ChatInput(String userInput,String userId){}

    @PostMapping(produces = "text/stream;charset=utf8")
    public Flux<String> client(@RequestBody ChatInput chatInput){
        String userId = chatInput.userId;
        String userInput=chatInput.userInput;
        if (userId==null||userInput==null){
            throw new RuntimeException("用户存在输入信息是空");
        }
        List<Message> messages = chatMemory.get(userId);
        log.info("历史记录{}",messages);
        log.info("用户{}询问{}",userId,userInput);
        ToolCallback[] toolCallbacks = ToolCallbacks.from(articleTool,categoryTool,userTool);
        return deepSeekChatClient.prompt()
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId(userId).build()).user(userInput)
                .toolCallbacks(toolCallbacks).stream().content();
    }
}
