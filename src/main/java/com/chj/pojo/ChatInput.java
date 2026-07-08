package com.chj.pojo;

import lombok.Data;

@Data
public class ChatInput {
    private String userInput;

    /**
     * 模型ID，如 "Qwen/Qwen3-8B"。为空时使用默认模型。
     */
    private String modelId;
}
