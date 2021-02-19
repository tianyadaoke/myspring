package com.spring;

import com.luban.service.UserService;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LubanApplicationContext {
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public LubanApplicationContext(Class configClass) {
        List<Class> classList = scan(configClass);
        for (Class clazz : classList) {
            if (clazz.isAnnotationPresent(Component.class)) {
                BeanDefinition beanDefinition = new BeanDefinition();
                beanDefinition.setBeanClass(clazz);

                Component component = (Component) clazz.getAnnotation(Component.class);
                String beanName = component.value();
                //System.out.println(beanName);
                //todo 假设都是单例

                if (clazz.isAnnotationPresent(Scope.class)) {
                    Scope scope = (Scope) clazz.getAnnotation(Scope.class);
                    beanDefinition.setScope(scope.value());
                } else {
                    beanDefinition.setScope("singleton");
                }
                if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                    BeanPostProcessor bpp = null;
                    try {
                        bpp = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                        beanPostProcessorList.add(bpp);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }

                }
                beanDefinitionMap.put(beanName, beanDefinition);
            }
        }
        // 生成单例bean---》单例池
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")) {
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        // 实例化
        Class beanClass = beanDefinition.getBeanClass();
        try {
            Object bean = beanClass.getDeclaredConstructor().newInstance();
            //填充属性
            Field[] fields = beanClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    UserService userService = (UserService) getBean(field.getName());
                    field.setAccessible(true);
                    field.set(bean, userService);
                }
            }
            // aware
            if (bean instanceof BeanNameAware) {
                ((BeanNameAware) bean).setBeanName(beanName);
            }
            // 。。。程序员定义的逻辑
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                bean=beanPostProcessor.postProcessBeforeInitialization(bean,beanName);
            }
            //初始化
            if (bean instanceof InitializingBean) {
                ((InitializingBean) bean).afterPropertiesSet();
            }
            //。。。程序员定义的逻辑 aop
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                bean=beanPostProcessor.postProcessAfterInitialization(bean,beanName);
            }

            return bean;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition.getScope().equals("prototype")) {
            // 生成原型
            return createBean(beanName, beanDefinition);
        } else {
            // 单例
            Object bean = singletonObjects.get(beanName);
            if (bean == null) {
                Object o = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, o);
                return o;
            }
            return bean;
        }
    }

    private List<Class> scan(Class configClass) {
        List<Class> classes = new ArrayList<>();
        // 扫描类
        /* 判断是否存在,这里简略
       if(configClass.isAnnotationPresent(ComponentScan.class)) {

       }
       */
        ComponentScan componentScan = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
        String scanPath = componentScan.value();
        // System.out.println(scanPath);
        scanPath = scanPath.replace(".", "/");
        //System.out.println(scanPath);
        // 如何扫描类
        ClassLoader classLoader = LubanApplicationContext.class.getClassLoader();
        URL resource = classLoader.getResource(scanPath);
        File file = new File(resource.getFile());
        // 指定目录下的文件列表
        File[] files = file.listFiles();
        for (File f : files) {
            // System.out.println(f);
            String absolutePath = f.getAbsolutePath();
            absolutePath = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"));
            // System.out.println(absolutePath);
            absolutePath = absolutePath.replace("\\", ".");
            //  System.out.println(absolutePath);
            try {
                Class<?> clazz = classLoader.loadClass(absolutePath);
                classes.add(clazz);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        }
        return classes;
    }


}
