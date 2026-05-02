package com.chj.service.impl;

import com.chj.mapper.ArticleMapper;
import com.chj.pojo.Article;
import com.chj.pojo.CategoryStats;
import com.chj.pojo.PageBean;
import com.chj.service.ArticleService;
import com.chj.utils.AliOssUtil;
import com.chj.utils.ThreadLocalUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    ArticleMapper articleMapper;
    @Override
    public void add(Article article) {
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        Map<String,Object> map = ThreadLocalUtil.get();
        article.setCreateUser((Integer) map.get("id"));
        articleMapper.add(article);
    }

    @Override
    public PageBean<Article> list(Integer pageNum, Integer pageSize, Integer categoryId, String state) {
        PageBean<Article> pb=new PageBean<>();
        PageHelper.startPage(pageNum,pageSize);
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        List<Article>  as=articleMapper.list(userId,categoryId,state);
        Page<Article> p= (Page<Article>) as;
        pb.setTotal(p.getTotal());
        pb.setItems(p.getResult());
        return pb;
    }

    @Override
    public PageBean<Article> listByCursor(Integer lastId, Integer pageSize, Integer categoryId, String state) {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        List<Article> articles = articleMapper.listByIdCursor(lastId, pageSize + 1, userId, categoryId, state);
        boolean hasMore = articles.size() > pageSize;
        List<Article> items = hasMore ? articles.subList(0, pageSize) : articles;
        PageBean<Article> pb = new PageBean<>();
        pb.setItems(items);
        pb.setHasMore(hasMore);
        return pb;
    }

    @Override
    public void update(Article article) {
        article.setUpdateTime(LocalDateTime.now());
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId= (Integer) map.get("id");
        article.setCreateUser(userId);
        articleMapper.update(article);
    }

    @Override
    public Article findById(Integer id) {
        return articleMapper.findById(id);
    }

    @Override
    public void deleteById(Integer id) {
        articleMapper.deleteById(id);
    }

    @Override
    public int getTotal() {
        Map<String,Object> map = ThreadLocalUtil.get();
        return articleMapper.getTotal((Integer) map.get("id"));
    }

    @Override
    public List<CategoryStats> getArticleStats() {
        Map<String,Object> map = ThreadLocalUtil.get();
        return articleMapper.getArticleStats((Integer)map.get("id"));
    }

    @Override
    public List<Article> listArticleByCategoryId(Integer categoryId) {
        Map<String,Object> map = ThreadLocalUtil.get();
        return articleMapper.listArticleByCategoryId(categoryId,(Integer)map.get("id"));
    }

    @Override
    public List<Article> listArticle(List<String> data) {
        Map<String,Object> map=ThreadLocalUtil.get();
        Set<Article> set=new HashSet<>();
        Integer userId= (Integer) map.get("id");
        for (String item : data) {
            List<Article> articles = articleMapper.listArticle(item, userId);
            set.addAll(articles);
        }
        return new ArrayList<>(set);
    }
}
