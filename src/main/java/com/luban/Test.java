package com.luban;

import com.luban.service.OrderService;
import com.spring.LubanApplicationContext;

public class Test {
    public static void main(String[] args) {
        // 启动spring
        LubanApplicationContext context = new LubanApplicationContext(AppConfig.class);
        // getBean
        /* 没毛病
        OrderService orderService = (OrderService) context.getBean("orderService");
        System.out.println(orderService);
        orderService.test();
        */
        // 测试单例 和原型都没问题
        System.out.println( context.getBean("orderService"));
        /*
        System.out.println( context.getBean("orderService"));
        System.out.println( context.getBean("orderService"));
        System.out.println( context.getBean("orderService"));
            */
    }
}
