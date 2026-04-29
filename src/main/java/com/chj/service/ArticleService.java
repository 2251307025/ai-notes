package com.chj.service;

import com.chj.pojo.Article;
import com.chj.pojo.CategoryStats;
import com.chj.pojo.PageBean;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public interface ArticleService {
    void add(Article article);

    PageBean<Article> list(Integer pageNum, Integer pageSize, Integer categoryId, String state);

    PageBean<Article> listByCursor(Integer lastId, Integer pageSize, Integer categoryId, String state);

    void update(Article article);

    Article findById(Integer id);

    void deleteById(Integer id);

    int getTotal();

    List<Article> listArticle(String data);

    List<CategoryStats> getArticleStats();
}
