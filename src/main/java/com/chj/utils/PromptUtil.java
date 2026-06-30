package com.chj.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
@Slf4j
public class PromptUtil {
    public static String imagePrompt;
    public static String systemPrompt;
    static{
        imagePrompt=loadImagePrompt();
        systemPrompt=loadSystemPrompt();
    }
    private static String loadImagePrompt(){
        try {
            ClassPathResource resource = new ClassPathResource("prompt/image-prompt.txt");
            String systemPrompt = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            log.info("加载生成图片提示词成功：{}",systemPrompt);
            return systemPrompt;
        }catch (Exception e){
            log.error("无法加载系统提示词文件：prompt/image-prompt.txt",e);
            return "";
        }
    }
    private static String loadSystemPrompt(){
        try {
            ClassPathResource resource = new ClassPathResource("prompt/system-prompt.txt");
            String systemPrompt =StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            log.info("加载系统提示词成功：{}",systemPrompt);
            return systemPrompt;
        }catch (Exception e){
            throw new RuntimeException("无法加载系统提示词文件：prompt/system-prompt.txt",e);
        }
    }
}
