package com.github.joern.kalz.doubleentry.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                    .antMatchers("/sign-up").permitAll()
                    .anyRequest().authenticated()
                .and().csrf()
                    .ignoringAntMatchers("/sign-up")
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());

    }

}
