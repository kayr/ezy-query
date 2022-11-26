package io.github.kayr.gradle.ezyquery;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class EzyQueryFileUtils {

  private EzyQueryFileUtils() {}

  // throw checked exception as unchecked
  private static <T extends Throwable> void throwUnchecked(Throwable t) throws T {
    throw (T) t;
  }

  static void deleteFolder(final Path mainOutputDir) {
    if (Files.isDirectory(mainOutputDir)) {
      try (Stream<Path> list = Files.list(mainOutputDir)) {
        list.forEach(EzyQueryFileUtils::deleteFolder);
      } catch (IOException e) {
        throwUnchecked(e);
      }
    }
    deleteFile(mainOutputDir);
  }

  private static void deleteFile(Path f) {
    try {
      Files.deleteIfExists(f);
    } catch (IOException e) {
      throwUnchecked(e);
    }
  }

  static void deleteFolder(final File mainOutputDir) {
    try {
      deleteFolder(mainOutputDir.toPath());
    } catch (Exception x) {
      throwUnchecked(x);
    }
  }
}
