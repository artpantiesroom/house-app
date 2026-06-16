package com.houseapp.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class AppStartupConfig implements ApplicationRunner {
  private final String databasePath;
  private final String uploadDir;

  public AppStartupConfig(
      @Value("${app.database.path}") String databasePath,
      @Value("${app.upload.dir}") String uploadDir
  ) {
    this.databasePath = databasePath;
    this.uploadDir = uploadDir;
  }

  @Override
  public void run(ApplicationArguments args) throws IOException {
    Path database = Path.of(databasePath).toAbsolutePath();
    Path parent = database.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    Path uploads = Path.of(uploadDir).toAbsolutePath();
    Files.createDirectories(uploads);
    Files.createDirectories(uploads.resolve("avatars"));
  }
}
