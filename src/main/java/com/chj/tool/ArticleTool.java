package com.chj.tool;

import com.chj.pojo.Article;
import com.chj.pojo.CategoryStats;
import com.chj.service.ArticleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class ArticleTool {
    @Resource
    private ArticleService articleService;

    @Tool(description = "获取用户笔记（文章）数量")
    public int getArticleCnt() {
        log.info("调用查询笔记数量tool");
        return articleService.getTotal();
    }

    @Tool(description = "返回笔记内容中存在{data}的笔记列表" +
            ",data是一个集合，请你根据用户的输入，使用一些相似的词进行搜索" +
            ",这个方法返回的实体类中有分类的id，但是不要直接返回id给用户，" +
            "请你查询分类列表，把id替换成对应的名称")
    public List<Article> listArticle(List<String> data) {
        log.info("调用查询笔记列表tool");
        List<Article> result = articleService.listArticle(data);
        return result;
    }

    @Tool(description = "根据笔记{id}返回笔记的详情")
    public Article getArticle(Integer id) {
        log.info("调用根据id{}查看笔记详情tool", id);
        return articleService.findById(id);
    }

    @Tool(description = "根据笔记{id}删除笔记")
    public void deleteArticle(Integer id) {
        log.info("调用根据id{}删除笔记tool", id);
        articleService.deleteById(id);
    }

    @Tool(description = "添加笔记")
    public void addArticle(@ToolParam(description = "笔记标题") String title,
                           @ToolParam(description = "笔记内容") String content,
                           @ToolParam(description = "分类ID,用户无法提供分类id，请你查询分类列表后，让用户选择分类") Integer categoryId,
                           @ToolParam(description = "状态：已发布 或 草稿，默认为草稿") String state) {
        log.info("调用添加笔记tool title={}", title);
        Article article = new Article();
        article.setTitle(title);
        article.setContent(content);
        article.setCategoryId(categoryId);
        article.setState(state != null ? state : "草稿");
        articleService.add(article);
    }

    @Tool(description = "更新笔记")
    public void updateArticle(@ToolParam(description = "笔记ID") Integer id,
                              @ToolParam(description = "笔记标题") String title,
                              @ToolParam(description = "笔记内容") String content,
                              @ToolParam(description = "分类ID,用户无法提供分类id，请你查询分类列表后，让用户选择分类") Integer categoryId,
                              @ToolParam(description = "状态：已发布 或 草稿，默认为草稿") String state) {
        log.info("调用更新笔记tool id={}", id);
        Article article = new Article();
        article.setId(id);
        article.setTitle(title);
        article.setContent(content);
        article.setCategoryId(categoryId);
        article.setState(state != null ? state : "草稿");
        article.setUpdateTime(LocalDateTime.now());
        articleService.update(article);
    }

    @Tool(description = "获取各分类下的笔记数量统计")
    public List<CategoryStats> getArticleStats() {
        log.info("调用获取笔记统计tool");
        return articleService.getArticleStats();
    }
}
