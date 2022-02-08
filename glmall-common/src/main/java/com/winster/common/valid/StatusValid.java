package com.winster.common.valid;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 状态校验注解：
 * @Constraint(validatedBy = { })是用来指定校验器的
 */
@Documented
@Constraint(validatedBy = {StatusValidator.class})
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface StatusValid {

    /**
     * 状态的取值范围
     * @return
     */
    int[] status() default {};

    /*jsr303规范，以下参数必须得有*/
    String message() default "{javax.validation.constraints.StatusValid.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
