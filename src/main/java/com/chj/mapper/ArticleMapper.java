package com.chj.mapper;

import com.chj.pojo.Article;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleMapper {
    @Insert("insert into article(title, content, cover_img, category_id, create_user, create_time, update_time,state) " +
            "values(#{title},#{content},#{coverImg},#{categoryId},#{createUser},#{createTime},#{updateTime},#{state})")
    void add(Article article);

    List<Article> list(Integer userId, String categoryId, String state);

    @Update("update article set title=#{title},content=#{content},cover_img=#{coverImg},state=#{state},category_id=#{categoryId},update_time=#{updateTime} where id=#{id}")
    void update(Article article);

@Select("select * from article where id=#{id}")
    Article findById(Integer id);

@Delete("delete from article where id=#{id}")
void deleteById(Integer id);
    @Select("select count(*) from article where create_user = #{id}")
    int getTotal(Long id);
    @Select("select * from article where create_user = #{id} and content like concat('%', #{data}, '%')")
    List<Article> listArticle(String data, Long id);
}
