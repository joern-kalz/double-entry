package com.github.joern.kalz.doubleentry.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    private static final String[] UI_LOCALS = new String[] {"en-US", "de-DE"};

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/ui/" + UI_LOCALS[0] + "/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        for (var local : UI_LOCALS) {
            addAngularResolver(registry, local);
        }
    }

    private void addAngularResolver(ResourceHandlerRegistry registry, String path) {
        registry.addResourceHandler("/ui/" + path + "/**")
                .addResourceLocations("classpath:/static/ui/" + path + "/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        return requestedResource.exists() && requestedResource.isReadable() ? requestedResource
                                : new ClassPathResource("/static/ui/" + path + "/index.html");
                    }
                });
    }
}
