package com.chj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStats {
    private Integer categoryId;
    private String categoryName;
    private Integer articleCount;
}
