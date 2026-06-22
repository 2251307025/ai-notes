package com.chj.tool;

import com.chj.pojo.Article;
import com.chj.pojo.CategoryStats;
import com.chj.pojo.PageBean;
import com.chj.service.ArticleService;
import com.chj.vo.ArticleVO;
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
            ",data是一个集合，请你根据用户的输入（用户可能会拼写错误，请你校验用户的拼写），使用一些相似的词进行搜索" +
            "你需要拆解用户输入获取data集合")
    public List<ArticleVO> listArticle(List<String> data) {
        log.info("调用查询笔记列表tool");
        log.info("data={}", data);
        List<ArticleVO> result = articleService.listArticle(data);
        log.info("result={}", result);
        return result;
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
    @Tool(description = "根据分类ID获取笔记列表")
    public List<ArticleVO> listArticleByCategoryId(@ToolParam(description = "分类ID") Integer categoryId) {
        log.info("调用根据分类ID获取笔记列表tool categoryId={}", categoryId);
        return articleService.listArticleByCategoryId(categoryId);
    }
}
