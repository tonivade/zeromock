/*
 * Copyright (c) 2018-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.type.IdOf.toId;
import static com.github.tonivade.zeromock.api.PreFilter.filter;
import static java.util.Objects.requireNonNull;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.instances.IdInstances;
import com.github.tonivade.purefun.type.Id_;
import com.github.tonivade.purefun.type.Option;

public final class HttpService {

  private final HttpServiceK<Id_> serviceK;

  public HttpService(String name) {
    this(new HttpServiceK<>(name, IdInstances.monad()));
  }

  private HttpService(HttpServiceK<Id_> serviceK) {
    this.serviceK = requireNonNull(serviceK);
  }

  public String name() {
    return serviceK.name();
  }

  public HttpServiceK<Id_> build() {
    return serviceK;
  }

  public HttpService mount(String path, HttpService other) {
    return new HttpService(serviceK.mount(path, other.serviceK));
  }

  public HttpService exec(RequestHandler handler) {
    return new HttpService(serviceK.exec(handler.liftId()::apply));
  }

  public ThenStep<HttpService> preFilter(Matcher1<HttpRequest> matcher) {
    return handler -> addPreFilter(matcher, handler);
  }

  public HttpService preFilter(PreFilter filter) {
    return new HttpService(serviceK.preFilter(filter.liftId()::apply));
  }

  public HttpService postFilter(PostFilter filter) {
    return new HttpService(serviceK.postFilter(filter.liftId()::apply));
  }

  public ThenStep<HttpService> when(Matcher1<HttpRequest> matcher) {
    return handler -> addMapping(matcher, handler);
  }

  public HttpService.ThenStep<HttpService> get(String path) {
    return when(Matchers.get(path));
  }

  public HttpService.ThenStep<HttpService> post(String path) {
    return when(Matchers.post(path));
  }

  public HttpService.ThenStep<HttpService> put(String path) {
    return when(Matchers.put(path));
  }

  public HttpService.ThenStep<HttpService> delete(String path) {
    return when(Matchers.delete(path));
  }

  public HttpService.ThenStep<HttpService> patch(String path) {
    return when(Matchers.patch(path));
  }

  public HttpService.ThenStep<HttpService> head(String path) {
    return when(Matchers.head(path));
  }

  public HttpService.ThenStep<HttpService> options(String path) {
    return when(Matchers.options(path));
  }

  public Option<HttpResponse> execute(HttpRequest request) {
    return serviceK.execute(request).fix(toId()).get();
  }

  public HttpService combine(HttpService other) {
    return new HttpService(serviceK.combine(other.serviceK));
  }

  protected HttpService addMapping(Matcher1<HttpRequest> matcher, RequestHandler handler) {
    return new HttpService(serviceK.addMapping(matcher, handler.liftId()::apply));
  }

  protected HttpService addPreFilter(Matcher1<HttpRequest> matcher, RequestHandler handler) {
    return preFilter(filter(matcher, handler));
  }

  @Override
  public String toString() {
    return "HttpService(" + serviceK.name() + ")";
  }
  
  @FunctionalInterface
  public interface ThenStep<T> {
    
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
