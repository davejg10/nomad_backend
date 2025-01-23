//package com.nomad.backend.config;
//
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.ApplicationContext;
//import org.springframework.stereotype.Component;
//
//import java.util.Arrays;
//
//@Component
//public class BeanListener implements CommandLineRunner {
//
//    private final ApplicationContext applicationContext;
//
//    public BeanListener(ApplicationContext applicationContext) {
//        this.applicationContext = applicationContext;
//    }
//
//    @Override
//    public void run(String... args) {
//        String[] beanNames = applicationContext.getBeanDefinitionNames();
//        Arrays.sort(beanNames); // Sort alphabetically for better readability
//        for (String beanName : beanNames) {
//            System.out.println(beanName);
//        }
//    }
//}