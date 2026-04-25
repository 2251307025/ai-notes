package com.chj.service;

import com.chj.pojo.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {
    void add(Category category);
    void add(String categoryName,String categoryAlias);

    List<Category> list();

    Category findById(Integer id);

    void update(Category category);

    void delete(Integer id);


}
