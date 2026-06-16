package com.chj.service;

import com.chj.controller.ChatController;
import com.chj.pojo.ChatInput;
import com.chj.pojo.Result;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface ChatService {
    Flux<ServerSentEvent<String>> chat(ChatInput chatInput);

    Result deleteMemory();

    String generateImage(String prompt);
}
