/*
 * Copyright (c) 2018-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.concurrent.FutureOf.toFuture;
import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static java.util.Objects.requireNonNull;

import java.util.concurrent.Executor;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.Future_;
import com.github.tonivade.purefun.concurrent.Promise;
import com.github.tonivade.purefun.instances.FutureInstances;
import com.github.tonivade.purefun.type.Option;

public final class AsyncHttpService {

  private final HttpServiceK<Future_> serviceK;

  public AsyncHttpService(String name) {
    this(name, Future.DEFAULT_EXECUTOR);
  }

  public AsyncHttpService(String name, Executor executor) {
    this(new HttpServiceK<>(name, FutureInstances.monad(executor)));
  }

  private AsyncHttpService(HttpServiceK<Future_> serviceK) {
    this.serviceK = requireNonNull(serviceK);
  }

  public String name() {
    return serviceK.name();
  }

  public HttpServiceK<Future_> build() {
    return serviceK;
  }

  public AsyncHttpService mount(String path, AsyncHttpService other) {
    return new AsyncHttpService(serviceK.mount(path, other.serviceK));
  }

  public AsyncHttpService exec(AsyncRequestHandler handler) {
    return new AsyncHttpService(serviceK.exec(handler));
  }

  public ThenStep<AsyncHttpService> preFilter(Matcher1<HttpRequest> matcher) {
    return handler -> addPreFilter(matcher, handler);
  }

  public AsyncHttpService preFilter(PreFilter filter) {
    return preFilter(filter.andThen(Future::success)::apply);
  }

  public AsyncHttpService preFilter(AsyncPreFilter filter) {
    return new AsyncHttpService(serviceK.preFilter(filter));
  }

  public AsyncHttpService postFilter(PostFilter filter) {
    return postFilter(filter.andThen(Future::success)::apply);
  }

  public AsyncHttpService postFilter(AsyncPostFilter filter) {
    return new AsyncHttpService(serviceK.postFilter(filter));
  }

  public ThenStep<AsyncHttpService> when(Matcher1<HttpRequest> matcher) {
    return handler -> addMapping(matcher, handler);
  }

  public AsyncHttpService.ThenStep<AsyncHttpService> get(String path) {
    return when(Matchers.get(path));
  }

  public AsyncHttpService.ThenStep<AsyncHttpService> post(String path) {
    return when(Matchers.post(path));
  }

  public AsyncHttpService.ThenStep<AsyncHttpService> put(String path) {
    return when(Matchers.put(path));
  }

  public AsyncHttpService.ThenStep<AsyncHttpService> delete(String path) {
    return when(Matchers.delete(path));
  }

  public AsyncHttpService.ThenStep<AsyncHttpService> patch(String path) {
    return when(Matchers.patch(path));
  }

  public AsyncHttpService.ThenStep<AsyncHttpService> head(String path) {
    return when(Matchers.head(path));
  }

  public AsyncHttpService.ThenStep<AsyncHttpService> options(String path) {
    return when(Matchers.options(path));
  }

  public Promise<Option<HttpResponse>> execute(HttpRequest request) {
    return serviceK.execute(request).fix(toFuture()).toPromise();
  }

  public AsyncHttpService combine(AsyncHttpService other) {
    return new AsyncHttpService(serviceK.combine(other.serviceK));
  }

  protected AsyncHttpService addMapping(Matcher1<HttpRequest> matcher, AsyncRequestHandler handler) {
    return new AsyncHttpService(serviceK.addMapping(matcher, handler));
  }

  protected AsyncHttpService addPreFilter(Matcher1<HttpRequest> matcher, AsyncRequestHandler handler) {
    return preFilter(filter(FutureInstances.monad(), matcher, handler)::apply);
  }

  @Override
  public String toString() {
    return "AsyncHttpService(" + serviceK.name() + ")";
  }
  
  @FunctionalInterface
  public interface ThenStep<T> {

    T then(AsyncRequestHandler handler);

    default T ok(String body) {
      return then(Handlers.ok(body).async());
    }
    
    default T created(String body) {
      return then(Handlers.created(body).async());
    }
    
    default T error(String body) {
      return then(Handlers.error(body).async());
    }
    
    default T noContent() {
      return then(Handlers.noContent().async());
    }
    
    default T notFound() {
      return then(Handlers.notFound().async());
    }
    
    default T forbidden() {
      return then(Handlers.forbidden().async());
    }
    
    default T badRequest() {
      return then(Handlers.badRequest().async());
    }
    
    default T unauthorized() {
      return then(Handlers.unauthorized().async());
    }
    
    default T unavailable() {
      return then(Handlers.unavailable().async());
    }
    
    default T error() {
      return then(Handlers.error().async());
    }
  }
}
