/* (C)2022 */
package io.github.kayr.ezyquery;

/**
 * This class may be necessary anymore. Explore ways of removing it. The generate classes should
 * make use of the mapper
 *
 * @param <T>
 */
public interface EzyQueryWithResult<T> extends EzyQuery {
  default Class<T> resultClass() {
    return null;
  }
}
