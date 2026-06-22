package com.chj.vo;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class ArticleVO {
    private Integer id;//主键ID
    private String title;//文章标题
    private String content;//文章内容
    private String coverImg;//封面图像
    private String state;//发布状态 已发布|草稿
    private String category;//文章分类
    private String createUser;//创建人
    private LocalDateTime createTime;//创建时间
}
