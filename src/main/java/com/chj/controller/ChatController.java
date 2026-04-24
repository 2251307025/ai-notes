package com.chj.controller;

import com.chj.tool.ChatTool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    private ChatTool chatTool;
    record message(String text){}

    @GetMapping(produces = "text/stream;charset=utf8")
    public Flux<String> client(String userInput, String userId){
        List<Message> messages = chatMemory.get(userId);
        log.info("历史记录{}",messages);
        log.info("用户{}询问{}",userId,userInput);
        ToolCallback[] toolCallbacks = ToolCallbacks.from(chatTool);
        return deepSeekChatClient.prompt()
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId(userId).build()).user(userInput)
                .toolCallbacks(toolCallbacks).stream().content();
    }
}
