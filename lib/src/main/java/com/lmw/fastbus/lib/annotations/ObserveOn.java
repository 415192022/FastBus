package com.lmw.fastbus.lib.annotations;



import com.lmw.fastbus.lib.entity.ThreadMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ObserveOn {
    ThreadMode value();
}
