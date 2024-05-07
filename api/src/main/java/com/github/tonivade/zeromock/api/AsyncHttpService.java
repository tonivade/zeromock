/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static java.util.Objects.requireNonNull;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.FutureOf;
import com.github.tonivade.purefun.concurrent.Promise;
import com.github.tonivade.purefun.core.Matcher1;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.typeclasses.Instance;
import com.github.tonivade.purefun.typeclasses.Instances;
import java.util.concurrent.Executor;

public final class AsyncHttpService implements HttpRouteBuilderK<Future<?>, AsyncHttpService> {

  private final HttpServiceK<Future<?>> serviceK;

  public AsyncHttpService(String name) {
    this(name, Future.DEFAULT_EXECUTOR);
  }

  public AsyncHttpService(String name, Executor executor) {
    this(new HttpServiceK<>(name, new Instance<Future<?>>() {}.monad(executor)));
  }

  private AsyncHttpService(HttpServiceK<Future<?>> serviceK) {
    this.serviceK = requireNonNull(serviceK);
  }

  public String name() {
    return serviceK.name();
  }

  public HttpServiceK<Future<?>> build() {
    return serviceK;
  }

  public AsyncHttpService mount(String path, AsyncHttpService other) {
    return new AsyncHttpService(serviceK.mount(path, other.serviceK));
  }

  public AsyncHttpService exec(AsyncRequestHandler handler) {
    return new AsyncHttpService(serviceK.exec(handler));
  }

  public ThenStepK<Future<?>, AsyncHttpService> preFilter(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(Instances.monad(), handler -> addPreFilter(matcher, handler::apply));
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
  public ThenStepK<Future<?>, AsyncHttpService> when(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(Instances.monad(), handler -> addMapping(matcher, handler::apply));
  }

  public Promise<Option<HttpResponse>> execute(HttpRequest request) {
    return serviceK.execute(request).fix(FutureOf::<Option<HttpResponse>>toFuture).toPromise();
  }

  public AsyncHttpService combine(AsyncHttpService other) {
    return new AsyncHttpService(serviceK.combine(other.serviceK));
  }

  private AsyncHttpService addMapping(Matcher1<HttpRequest> matcher, AsyncRequestHandler handler) {
    return new AsyncHttpService(serviceK.addMapping(matcher, handler));
  }

  private AsyncHttpService addPreFilter(Matcher1<HttpRequest> matcher, AsyncRequestHandler handler) {
    return preFilter(filter(Instances.monad(), matcher, handler)::apply);
  }

  @Override
  public String toString() {
    return "AsyncHttpService(" + serviceK.name() + ")";
  }
}
