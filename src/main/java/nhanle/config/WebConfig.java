package nhanle.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${app.upload-dir:./uploads}")
  private String uploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    Path p = Paths.get(uploadDir).toAbsolutePath().normalize();
    String location = p.toUri().toString();      // file:///D:/picture_web_test/
    if (!location.endsWith("/")) location += "/";

    registry.addResourceHandler("/images/**")
            .addResourceLocations(location);
  }
}
