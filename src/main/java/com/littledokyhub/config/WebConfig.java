package com.littledokyhub.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final String productImagesLocation;

    public WebConfig(@Value("${app.upload.products-dir:uploads/products}") String productImagesDirectory) {
        Path directory = Path.of(productImagesDirectory).toAbsolutePath().normalize();
        String location = directory.toUri().toString();
        this.productImagesLocation = location.endsWith("/") ? location : location + "/";
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations(productImagesLocation);
    }
}