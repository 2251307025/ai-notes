package com.chj.controller;

import com.chj.pojo.Article;
import com.chj.pojo.Result;
import com.chj.service.ArticleService;
import com.chj.utils.MinioUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
public class UploadController {
    @Autowired
    MinioUtil minioUtil;
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) throws Exception {
        log.info("请求路径: /upload, fileName={}", file.getOriginalFilename());
        String url = minioUtil.upload(file);
        return Result.success(url);
    }
}
