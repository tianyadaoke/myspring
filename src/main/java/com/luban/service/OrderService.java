package com.luban.service;

import com.spring.*;

@Component("orderService")
//@Scope("prototype")
public class OrderService implements InitializingBean, BeanNameAware {
    @Autowired
    private UserService userService;
    private String beanName;

    public void test(){
        System.out.println(userService);
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("初始化");
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName=beanName;
    }
}
