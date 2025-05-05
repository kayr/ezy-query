package io.github.kayr.ezyquery.gen;

@lombok.AllArgsConstructor(staticName = "of")
@lombok.Getter
@lombok.ToString
public class Pair<K, V> {
  private final K one;
  private final V two;
}
