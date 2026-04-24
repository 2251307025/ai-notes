package com.chj.controller;

import com.chj.pojo.Article;
import com.chj.pojo.Result;
import com.chj.service.ArticleService;
import com.chj.utils.AliOssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {
    @Autowired
    AliOssUtil oss;
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) throws Exception {
        String url = oss.upload(file);
        return Result.success(url);
    }
}
