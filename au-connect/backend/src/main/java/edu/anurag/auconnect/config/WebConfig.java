package edu.anurag.auconnect.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/** Serves uploaded photos from /uploads/**. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final Path uploadRoot;

    public WebConfig(@Value("${app.upload-dir}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadRoot.toUri().toString();
        if (!location.endsWith("/")) location += "/"; // required, and absent if the folder doesn't exist yet
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCachePeriod(3600);
    }
}
