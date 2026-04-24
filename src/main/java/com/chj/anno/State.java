package com.chj.anno;

import com.chj.validation.StateValidation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented//元注解
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)

@Constraint(validatedBy = StateValidation.class)
public @interface State {
    String message() default "{state参数的值只能是已发布或者草稿}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
