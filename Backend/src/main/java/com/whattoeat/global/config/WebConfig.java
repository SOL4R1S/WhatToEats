package com.whattoeat.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.path:uploads}")
    private String uploadPath;

    @Value("${app.upload.url-prefix}")
    private String uploadUrlPrefix;


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String prefix = uploadUrlPrefix;

        if (prefix.startsWith("http://") || prefix.startsWith("https://")) {
            int pathStart = prefix.indexOf('/', prefix.indexOf("://") + 3);
            prefix = pathStart == -1 ? "/" : prefix.substring(pathStart);
        }
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }


        registry.addResourceHandler(prefix + "**").addResourceLocations("file:" + uploadPath + "/");
    }
}
