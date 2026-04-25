package com.chj.tool;

import com.chj.pojo.Category;
import com.chj.service.CategoryService;
import com.chj.utils.ThreadLocalUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
@Slf4j
public class CategoryTool {
    @Resource
    private CategoryService categoryService;
    @Tool(description = "获取笔记分类列表")
    public List<Category> listCategory(){
        log.info("调用获取笔记分类列表tool");
        return categoryService.list();
    }
    @Tool(description = "添加文章分类")
    public void addCategory(@ToolParam(description = "分类名称") String categoryName,
                            @ToolParam(description = "分类别名") String categoryAlias){
        log.info("调用添加文章分类tool");
        categoryService.add(categoryName,categoryAlias);
    }
}
