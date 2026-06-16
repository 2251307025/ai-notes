package com.chj.service.impl;

import com.chj.anno.AutoFill;
import com.chj.mapper.CategoryMapper;
import com.chj.pojo.Category;
import com.chj.service.CategoryService;
import com.chj.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl implements CategoryService {
@Autowired
    private CategoryMapper categoryMapper;
    @Override
    @AutoFill(AutoFill.OperationType.INSERT)
    public void add(Category category) {
        categoryMapper.add(category);
    }

    @Override
    public void add(String categoryName, String categoryAlias) {
        Category c=new Category();
        c.setCategoryName(categoryName);
        c.setCategoryAlias(categoryAlias);
        add(c);
    }

    @Override
    public List<Category> list() {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        return categoryMapper.list(userId);

    }

    @Override
    public Category findById(Integer id) {
        return categoryMapper.findById(id);

    }

    @Override
    @AutoFill(AutoFill.OperationType.UPDATE)
    public void update(Category category) {
        categoryMapper.update(category);
    }

    @Override
    public void delete(Integer id) {
        categoryMapper.delete(id);
    }
}
