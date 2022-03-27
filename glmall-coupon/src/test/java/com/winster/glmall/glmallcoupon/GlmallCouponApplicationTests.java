package com.winster.glmall.glmallcoupon;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

//@SpringBootTest
class GlmallCouponApplicationTests {

    @Test
    void contextLoads() {
        LocalDate now = LocalDate.now();
        LocalDate plusDays1 = now.plusDays(1);
        LocalDate plusDays2 = now.plusDays(2);
        System.out.println(now);
        System.out.println(plusDays1);
        System.out.println(plusDays2);

        System.out.println("--------------------------------------------------------");

        LocalTime min = LocalTime.MIN;
        LocalTime max = LocalTime.MAX;
        LocalTime midnight = LocalTime.MIDNIGHT;
        System.out.println(min);
        System.out.println(max);
        System.out.println(midnight);

        System.out.println("--------------------------------------------------------");

        LocalDateTime start = LocalDateTime.of(now, min);
        LocalDateTime end = LocalDateTime.of(plusDays2, max);
        System.out.println(start);
        System.out.println(end);

        System.out.println("--------------------------------------------------------");
        System.out.println(LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.MAX).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

}
