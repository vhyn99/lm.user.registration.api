package com.legalmatch.api;

import javax.servlet.Filter;
import com.legalmatch.api.filter.GraphqlFilter;
import com.legalmatch.api.filter.LoadActionFilter;
import com.legalmatch.api.filter.PlaygroundFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;

@Configuration
@EnableCaching
@EnableJpaRepositories
public class AppConfig {

    @Value("${lm.api.playground.enabled:false}") private boolean isPlaygroundEnabled;
    @Value("${lm.api.key:key01}") private String apiKey;
    @Value("${lm.load.action.enabled:false}") private boolean isLoadActionEnabled;

    @Bean
    public Filter openFilter() {
        return new OpenEntityManagerInViewFilter();
    }

    /**
     * Registers "/graphql" url pattern to FilterRegistrationBean.
     *
     */
    @Bean
    public FilterRegistrationBean<GraphqlFilter> loggingFilter() {
        FilterRegistrationBean<GraphqlFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new GraphqlFilter(apiKey));
        registrationBean.addUrlPatterns("/graphql");
        registrationBean.setOrder(2);

        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<PlaygroundFilter> playgroundFilter() {
        FilterRegistrationBean<PlaygroundFilter> filterBean = new FilterRegistrationBean<>();
        filterBean.setFilter(new PlaygroundFilter(isPlaygroundEnabled));
        filterBean.addUrlPatterns("/graphql-playground.html");
        filterBean.setOrder(1);
        return filterBean;
    }

    @Bean
    public FilterRegistrationBean<LoadActionFilter> loadActionFilter() {
        FilterRegistrationBean<LoadActionFilter> filterBean = new FilterRegistrationBean<>();
        filterBean.setFilter(new LoadActionFilter(isLoadActionEnabled));
        filterBean.addUrlPatterns(
                "/load",
                "/load/*"
        );
        return filterBean;
    }
}


