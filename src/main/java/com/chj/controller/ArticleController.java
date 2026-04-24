package com.chj.controller;

import com.chj.pojo.Article;
import com.chj.pojo.PageBean;
import com.chj.pojo.Result;
import com.chj.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@Component
@RestController
@Slf4j
@RequestMapping("/article")
public class ArticleController {
    @Autowired
    ArticleService articleService;

    @PostMapping
    public Result add(@RequestBody @Validated Article article) {
        log.info("添加{}",article);
        articleService.add(article);
        return Result.success();
    }
    //带有分页的查询
    @GetMapping
    public Result<PageBean<Article>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "5") Integer pageSize,
                                          @RequestParam(required = false) String categoryId,
                                          @RequestParam(required = false) String state){
        log.info("分页查询");
        PageBean<Article> pb=articleService.list(pageNum,pageSize,categoryId,state);
        return Result.success(pb);
    }
    @PutMapping
    public Result update(@RequestBody @Validated Article article){
        log.info("更新{}",article);
        articleService.update(article);
        return Result.success();
    }
    @GetMapping("/detail")
    public Result<Article> detail(Integer id){
        log.info("根据id查询文章",id);
        Article article=articleService.findById(id);
        return Result.success(article);
    }
    @DeleteMapping
    public Result delete(Integer id){
        articleService.deleteById(id);
        return Result.success();
    }
}