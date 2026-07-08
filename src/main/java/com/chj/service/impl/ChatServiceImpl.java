package com.chj.service.impl;

import com.chj.pojo.ChatInput;
import com.chj.pojo.Result;
import com.chj.service.ChatService;
import com.chj.tool.ArticleTool;
import com.chj.tool.CategoryTool;
import com.chj.tool.TtlToolCallbackWrapper;
import com.chj.tool.UserTool;
import com.chj.utils.MinioUtil;
import com.chj.utils.PromptUtil;
import com.chj.utils.ThreadLocalUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.image.*;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    @Resource
    private ChatMemory chatMemory;
    @Resource
    private MinioUtil minioUtil;
    @Resource
    private ImageModel imageModel;
    @Resource
    private ArticleTool articleTool;
    @Resource
    private CategoryTool categoryTool;
    @Resource
    private UserTool userTool;

    /**
     * 所有可用模型的 ChatClient 注册表（key = modelId）。
     */
    @Resource
    private Map<String, ChatClient> siliconFlowChatClients;

    /**
     * 默认模型 ID。
     */
    @Value("${siliconflow.default-model}")
    private String defaultModel;

    @Override
    public String generateImage(String prompt) {
        if (prompt == null || prompt.isEmpty()) {
            throw new RuntimeException("图片生成提示词不能为空");
        }
        prompt = PromptUtil.imagePrompt + prompt;
        try {
            ImageOptions options = ImageOptionsBuilder.builder()
                    .height(512)
                    .width(512)
                    .build();
            ImagePrompt imagePrompt = new ImagePrompt(prompt, options);
            ImageResponse response = imageModel.call(imagePrompt);
            String tempUrl = response.getResult().getOutput().getUrl();
            InputStream inputStream = new URL(tempUrl).openStream();
            String fileName = "ai-generated-" + UUID.randomUUID() + ".png";
            String url = minioUtil.upload(inputStream, fileName);
            log.info("图片访问url：{}", url);
            return url;
        } catch (Exception e) {
            log.error("图片生成异常: {}", e.getMessage(), e);
            throw new RuntimeException("图片生成异常: " + e.getMessage(), e);
        }
    }

    @Override
    public Flux<ServerSentEvent<String>> chat(ChatInput chatInput) {
        Map<String, Integer> map = ThreadLocalUtil.get();
        String userId = String.valueOf(map.get("id"));
        String userInput = chatInput.getUserInput();
        if (userId == null || userInput == null || userInput.isEmpty()) {
            return Flux.just(ServerSentEvent.<String>builder()
                    .data("{\"error\":\"用户输入信息不能为空\"}")
                    .event("error")
                    .build());
        }

        // 根据 modelId 选择对应 ChatClient，为空时使用默认模型
        String modelId = chatInput.getModelId();
        if (modelId == null || modelId.isEmpty()) {
            modelId = defaultModel;
        }
        ChatClient client = siliconFlowChatClients.get(modelId);
        if (client == null) {
            log.warn("用户 {} 请求了未注册的模型 {}，回退到默认模型 {}", userId, modelId, defaultModel);
            modelId = defaultModel;
            client = siliconFlowChatClients.get(modelId);
            if (client == null) {
                return Flux.just(ServerSentEvent.<String>builder()
                        .data("{\"error\":\"没有可用的AI模型\"}")
                        .event("error")
                        .build());
            }
        }

        log.info("用户 {} 使用模型 {} 进行对话", userId, modelId);

        // 捕获最终使用的模型ID，供lambda使用（lambda要求变量为final或事实上final）
        final String resolvedModelId = modelId;

        ToolCallback[] toolCallbacks = Arrays.stream(
                        ToolCallbacks.from(articleTool, categoryTool, userTool))
                .map(TtlToolCallbackWrapper::new)
                .toArray(ToolCallback[]::new);

        return client.prompt()
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
                    log.error("聊天流异常: userId={}, modelId={}, error={}", userId, resolvedModelId, e.getMessage(), e);
                    return Flux.just(ServerSentEvent.<String>builder()
                            .data("{\"error\":\"" +
                                    (StringUtils.hasText(e.getMessage()) ? e.getMessage() : "内部错误，请稍后重试") +
                                    "\"}")
                            .event("error")
                            .build());
                });
    }

    @Override
    public Result deleteMemory() {
        Map<String, Integer> map = ThreadLocalUtil.get();
        String userId = String.valueOf(map.get("id"));
        chatMemory.clear(userId);
        log.info("删除用户{}的聊天记录", userId);
        return Result.success("记忆清除成功");
    }
}
