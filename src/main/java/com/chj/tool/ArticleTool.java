package com.chj.tool;

import com.chj.pojo.Article;
import com.chj.service.ArticleService;
import com.chj.service.CategoryService;
import com.chj.utils.ThreadLocalUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ArticleTool {
    @Resource
    private ArticleService articleService;
    @Tool(description = "获取用户笔记（文章）数量")
    public int getArticleCnt(){
        log.info("调用查询笔记数量tool");
        return articleService.getTotal();
    }
    @Tool(description = "返回笔记内容中存在{data}的笔记列表")
    public List<Article> listArticle(String data){
        log.info("调用查询笔记列表tool");
        return articleService.listArticle(data);
    }
    @Tool(description = "根据笔记{id}返回笔记的详情")
    public Article getArticle(Integer id){
        log.info("调用根据id{}查看笔记详情tool",id);
        return articleService.findById( id);
    }
    @Tool(description = "根据笔记{id}删除笔记")
    public void deleteArticle(Integer id){
        log.info("调用根据id{}删除笔记tool",id);
        articleService.deleteById(id);
    }
    @Tool(description = "添加笔记")
    public void addArticle(Article article){
        log.info("调用添加笔记tool");
        articleService.add(article);
    }

}
