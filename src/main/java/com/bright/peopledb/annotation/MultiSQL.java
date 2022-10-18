package com.bright.peopledb.annotation;

/*
 * @Project Name: PeopleDB
 * @Author: Okechukwu Bright Onwumere
 * @Created: 24/08/2022
 */

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MultiSQL {
    SQL[] value();
}
