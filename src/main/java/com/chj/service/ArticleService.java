package com.chj.service;

import com.chj.pojo.Article;
import com.chj.pojo.CategoryStats;
import com.chj.pojo.PageBean;
import com.chj.vo.ArticleVO;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public interface ArticleService {
    void add(Article article);

    PageBean<Article> listByCursor(Integer lastId, Integer pageSize, Integer categoryId, String state,Integer userId);

    void update(Article article);

    Article findById(Integer id);

    void deleteById(Integer id);

    int getTotal();

    List<ArticleVO> listArticle(List<String> data);

    List<CategoryStats> getArticleStats();

    List<ArticleVO> listArticleByCategoryId(Integer categoryId);

    PageBean<Article> listByCursorByPrivate(Integer lastId, Integer pageSize, Integer categoryId, String state);

    PageBean<Article> list(Integer pageSize, Integer pageNum, Integer categoryId, String status, String data, Integer userId);

    PageBean<Article> listByPrivate(Integer pageSize, Integer pageNum, Integer categoryId, String state, String data);

    ArticleVO findByTitle(String title);
}
