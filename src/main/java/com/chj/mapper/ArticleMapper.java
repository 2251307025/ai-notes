package com.chj.mapper;

import com.chj.pojo.Article;
import com.chj.pojo.CategoryStats;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleMapper {
    @Insert("insert into article(title, content, cover_img, category_id, create_user, create_time, update_time,state) " +
            "values(#{title},#{content},#{coverImg},#{categoryId},#{createUser},#{createTime},#{updateTime},#{state})")
    void add(Article article);

    List<Article> list(Integer userId, String categoryId, String state);

    List<Article> listByIdCursor(@Param("lastId") Integer lastId,
                                 @Param("limit") Integer limit,
                                 @Param("userId") Integer userId,
                                 @Param("categoryId") String categoryId,
                                 @Param("state") String state);

    @Update("update article set title=#{title},content=#{content},cover_img=#{coverImg},state=#{state},category_id=#{categoryId},update_time=#{updateTime} where id=#{id}")
    void update(Article article);

@Select("select * from article where id=#{id}")
    Article findById(Integer id);

@Delete("delete from article where id=#{id}")
void deleteById(Integer id);
    @Select("select count(*) from article where create_user = #{id}")
    int getTotal(Long id);

    @Select("SELECT c.id AS category_id, c.category_name, COUNT(a.id) AS article_count " +
            "FROM category c LEFT JOIN article a ON c.id = a.category_id " +
            "WHERE c.create_user = #{userId} GROUP BY c.id, c.category_name ORDER BY c.id")
    List<CategoryStats> getArticleStats(Integer userId);
    @Select("select * from article where create_user = #{id} and content like concat('%', #{data}, '%')")
    List<Article> listArticle(String data, Long id);
}
