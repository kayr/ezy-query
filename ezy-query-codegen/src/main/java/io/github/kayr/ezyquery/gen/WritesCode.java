package io.github.kayr.ezyquery.gen;

import java.nio.file.Path;
import java.nio.file.Paths;

public interface WritesCode {

  default Path writeTo(String path, String... paths) {
    return writeTo(Paths.get(path, paths));
  }

  Path writeTo(Path path);
}
