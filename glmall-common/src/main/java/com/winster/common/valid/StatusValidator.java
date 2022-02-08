package com.winster.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * StatusValid校验器
 */
public class StatusValidator implements ConstraintValidator<StatusValid, Integer> {
    private Set<Integer> set = new HashSet<>();

    @Override
    public void initialize(StatusValid constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        int[] status = constraintAnnotation.status();
        for (int s : status) {
            set.add(s);
        }
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        // value就是请求传过来的值，也就是我们需要校验是否合法的目标值
        return set.contains(value);
    }
}
