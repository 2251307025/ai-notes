package com.chj.controller;

import com.chj.config.ChatConfiguration;
import com.chj.pojo.ChatInput;
import com.chj.pojo.Result;
import com.chj.service.ChatService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/chat")
@RestController
@Slf4j
public class ChatController {
    @Resource
    private ChatService chatService;
    @Resource
    private ChatConfiguration.SiliconFlowProperties siliconFlowProperties;

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestBody ChatInput chatInput) {
        return chatService.chat(chatInput);
    }

    @DeleteMapping()
    public Result deleteMemory() {
        return chatService.deleteMemory();
    }

    @PostMapping("/image")
    public Result<String> generateImage(@RequestBody ChatInput chatInput) {
        log.info("chat/image 生成图片提示词：{}", chatInput);
        return Result.success(chatService.generateImage(chatInput.getUserInput()));
    }

    /**
     * 获取可用的 AI 模型列表。
     *
     * @return 模型列表（modelId + modelName）
     */
    @GetMapping("/models")
    public Result<List<ModelInfo>> listModels() {
        List<ModelInfo> models = siliconFlowProperties.getModels().stream()
                .map(m -> new ModelInfo(m.getModelId(), m.getModelName()))
                .collect(Collectors.toList());
        return Result.success(models);
    }

    /**
     * 模型信息 DTO。
     */
    public record ModelInfo(String modelId, String modelName) {
    }
}
