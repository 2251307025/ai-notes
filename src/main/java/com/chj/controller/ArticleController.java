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
@Component
@RestController
@Slf4j
@RequestMapping("/article")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    @PostMapping
    public Result add(@RequestBody @Validated Article article) {
        log.info("请求路径: /article, 添加文章, article={}", article);
        articleService.add(article);
        return Result.success();
    }
    //游标分页/无限滚动 || 分页查询
    @GetMapping("/public")
    public Result<PageBean<Article>> listPublic(@RequestParam(required = false) Integer pageNum,
                                                @RequestParam(defaultValue = "5") Integer pageSize,
                                                @RequestParam(required = false) Integer lastId,
                                                @RequestParam(required = false) Integer categoryId,
                                                @RequestParam(required = false) String data){
        log.info("请求路径: /article/public, 查询公共笔记列表");
        if (lastId!=null){
            return Result.success(articleService.listByCursor(lastId, pageSize, categoryId,"已发布",null));
        }
        return Result.success(articleService.list(pageSize,pageNum,categoryId,"已发布",data,null));
    }
    @GetMapping
    public Result<PageBean<Article>> list(@RequestParam(required = false) Integer pageNum,
                                          @RequestParam(defaultValue = "5") Integer pageSize,
                                          @RequestParam(required = false) Integer lastId,
                                          @RequestParam(required = false) Integer categoryId,
                                          @RequestParam(required = false) String state,
                                          @RequestParam(required = false) String data){
        log.info("请求路径: /article, 游标分页查询, lastId={}", lastId);
        if (lastId!=null){
            PageBean<Article> pb = articleService.listByCursorByPrivate(lastId, pageSize, categoryId, state);
            return Result.success(pb);
        }
        return Result.success(articleService.listByPrivate(pageSize,pageNum,categoryId,state,data));
    }
    @PutMapping
    public Result update(@RequestBody @Validated Article article){
        log.info("请求路径: /article, 更新文章, article={}", article);
        articleService.update(article);
        return Result.success();
    }
    @GetMapping("/detail")
    public Result<Article> detail(Integer id){
        log.info("请求路径: /article/detail, id={}", id);
        Article article=articleService.findById(id);
        return Result.success(article);
    }
    @DeleteMapping
    public Result delete(Integer id){
        log.info("请求路径: /article, id={}", id);
        articleService.deleteById(id);
        return Result.success();
    }
}