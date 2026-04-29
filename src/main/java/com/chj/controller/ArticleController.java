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
    //带有分页的查询（支持偏移分页和游标分页/无限滚动）
    @GetMapping
    public Result<PageBean<Article>> list(@RequestParam(required = false) Integer pageNum,
                                          @RequestParam(defaultValue = "5") Integer pageSize,
                                          @RequestParam(required = false) Integer lastId,
                                          @RequestParam(required = false) Integer categoryId,
                                          @RequestParam(required = false) String state){
        if (lastId != null) {
            log.info("游标分页查询, lastId={}", lastId);
            PageBean<Article> pb = articleService.listByCursor(lastId, pageSize, categoryId, state);
            return Result.success(pb);
        }
        log.info("偏移分页查询");
        int page = (pageNum != null) ? pageNum : 1;
        PageBean<Article> pb = articleService.list(page, pageSize, categoryId, state);
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