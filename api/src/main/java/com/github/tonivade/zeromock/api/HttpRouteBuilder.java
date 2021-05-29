package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Matcher1;

public interface HttpRouteBuilder<T extends HttpRouteBuilder<T>> {

  ThenStep<T> when(Matcher1<HttpRequest> matcher);

  default ThenStep<T> get(String path) {
    return when(Matchers.get(path));
  }

  default ThenStep<T> post(String path) {
    return when(Matchers.post(path));
  }

  default ThenStep<T> put(String path) {
    return when(Matchers.put(path));
  }

  default ThenStep<T> delete(String path) {
    return when(Matchers.delete(path));
  }

  default ThenStep<T> patch(String path) {
    return when(Matchers.patch(path));
  }

  default ThenStep<T> head(String path) {
    return when(Matchers.head(path));
  }

  default ThenStep<T> options(String path) {
    return when(Matchers.options(path));
  }
  
  @FunctionalInterface
  interface ThenStep<T> {
    
    T then(RequestHandler handler);
    
    default T ok(String body) {
      return then(Handlers.ok(body));
    }
    
    default T created(String body) {
      return then(Handlers.created(body));
    }
    
    default T error(String body) {
      return then(Handlers.error(body));
    }
    
    default T noContent() {
      return then(Handlers.noContent());
    }
    
    default T notFound() {
      return then(Handlers.notFound());
    }
    
    default T forbidden() {
      return then(Handlers.forbidden());
    }
    
    default T badRequest() {
      return then(Handlers.badRequest());
    }
    
    default T unauthorized() {
      return then(Handlers.unauthorized());
    }
    
    default T unavailable() {
      return then(Handlers.unavailable());
    }
    
    default T error() {
      return then(Handlers.error());
    }
  }
}
