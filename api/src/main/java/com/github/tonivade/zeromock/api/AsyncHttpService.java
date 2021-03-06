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
import com.github.tonivade.purefun.typeclasses.Instance;

public final class AsyncHttpService implements HttpRouteBuilderK<Future_, AsyncHttpService> {

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

  public ThenStepK<Future_, AsyncHttpService> preFilter(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(Instance.monad(Future_.class), handler -> addPreFilter(matcher, handler::apply));
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

  @Override
  public ThenStepK<Future_, AsyncHttpService> when(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(Instance.monad(Future_.class), handler -> addMapping(matcher, handler::apply));
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
}
