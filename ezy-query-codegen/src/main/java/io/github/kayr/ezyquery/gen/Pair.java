package io.github.kayr.ezyquery.gen;

@lombok.AllArgsConstructor(staticName = "of")
@lombok.Getter
public class Pair<K, V> {
  private final K one;
  private final V two;
}
