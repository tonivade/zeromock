package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.Witness;

public interface HttpRouteBuilderK<F extends Witness, T extends HttpRouteBuilderK<F, T, R>, R extends RequestHandlerK<F>> {

  ThenStep<T, R> when(Matcher1<HttpRequest> matcher);

  default ThenStep<T, R> get(String path) {
    return when(Matchers.get(path));
  }

  default ThenStep<T, R> post(String path) {
    return when(Matchers.post(path));
  }

  default ThenStep<T, R> put(String path) {
    return when(Matchers.put(path));
  }

  default ThenStep<T, R> delete(String path) {
    return when(Matchers.delete(path));
  }

  default ThenStep<T, R> patch(String path) {
    return when(Matchers.patch(path));
  }

  default ThenStep<T, R> head(String path) {
    return when(Matchers.head(path));
  }

  default ThenStep<T, R> options(String path) {
    return when(Matchers.options(path));
  }
  
  @FunctionalInterface
  interface ThenStep<T, R> {

    T then(R handler);
  }
}
