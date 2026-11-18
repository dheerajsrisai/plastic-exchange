package com.plasticexchange.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
import java.nio.file.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${app.upload-dir}") private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try { Files.createDirectories(Paths.get(uploadDir)); } catch (Exception ignored) { }
        String loc = Paths.get(uploadDir).toAbsolutePath().toUri().toString();
        if (!loc.endsWith("/")) loc += "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations(loc);
    }
}
