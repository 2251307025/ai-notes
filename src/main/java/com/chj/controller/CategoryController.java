package com.chj.controller;

import com.chj.pojo.Category;
import com.chj.pojo.Result;
import com.chj.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @PostMapping
    public Result add(@RequestBody @Validated(Category.Add.class) Category category){
        log.info("请求路径: /category, 添加分类, category={}", category);
        categoryService.add(category);
        return Result.success();
    }
    @GetMapping
    public Result<List<Category>> list(){
        log.info("请求路径: /category");
        List<Category> list =categoryService.list();
        return Result.success(list);
    }
    @GetMapping("/detail")
    public Result<Category> detail(Integer id){
        log.info("请求路径: /category/detail, id={}", id);
        Category ca= categoryService.findById(id);
        return Result.success(ca);
    }
    @PutMapping
    public Result update(@RequestBody @Validated(Category.Update.class) Category category){
        log.info("请求路径: /category, 更新分类, category={}", category);
        categoryService.update(category);
        return Result.success();
    }
    @DeleteMapping
    public Result delete(Integer id){
        log.info("请求路径: /category, id={}", id);
        categoryService.delete(id);
        return Result.success();
    }
}
