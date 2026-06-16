package com.chj.anno;
import java.lang.annotation.*;
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoFill {
    OperationType value();
    enum OperationType{
        INSERT,UPDATE
    }
}
