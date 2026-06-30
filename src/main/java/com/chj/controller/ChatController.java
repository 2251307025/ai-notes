package com.chj.controller;

import com.chj.pojo.ChatInput;
import com.chj.pojo.Result;
import com.chj.service.ChatService;
import com.chj.tool.ArticleTool;
import com.chj.tool.CategoryTool;
import com.chj.tool.TtlToolCallbackWrapper;
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
    private ChatService chatService;
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestBody ChatInput chatInput) {
        return chatService.chat(chatInput);
    }
    @DeleteMapping()
    public Result deleteMemory(){
        return chatService.deleteMemory();
    }
    @PostMapping("/image")
    public Result<String> generateImage(@RequestBody ChatInput chatInput){
        log.info("chat/image 生成图片提示词：{}",chatInput);
        return Result.success(chatService.generateImage(chatInput.getUserInput()));
    }
}
