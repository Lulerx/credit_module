package com.itle.credit.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * create by Luler on 2022/12/26 16:19
 *
 * @description  Mybatis分页插件拦截器配置
 */
@SpringBootConfiguration
@MapperScan("com.itle.credit.mapper")
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());//创建乐观锁拦截器 OptimisticLockerInnerInterceptor
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.ORACLE)); //插件分页拦截器
        return interceptor;
    }

}
