/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.PreFilter.filter;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.Function2;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.Promise;
import com.github.tonivade.purefun.instances.FutureInstances;
import com.github.tonivade.purefun.type.Option;

public final class AsyncHttpService {

  private final HttpServiceK<Future.µ> serviceK;

  public AsyncHttpService(String name) {
    this(new HttpServiceK<>(name, FutureInstances.monad()));
  }

  private AsyncHttpService(HttpServiceK<Future.µ> serviceK) {
    this.serviceK = requireNonNull(serviceK);
  }

  public String name() {
    return serviceK.name();
  }

  public HttpServiceK<Future.µ> build() {
    return serviceK;
  }

  public AsyncHttpService mount(String path, AsyncHttpService other) {
    return new AsyncHttpService(serviceK.mount(path, other.serviceK));
  }

  public AsyncHttpService exec(AsyncRequestHandler handler) {
    return new AsyncHttpService(serviceK.exec(handler));
  }

  public AsyncHttpService preFilter(Matcher1<HttpRequest> matcher, RequestHandler handler) {
    return preFilter(filter(matcher, handler));
  }

  public AsyncHttpService preFilter(PreFilter filter) {
    return new AsyncHttpService(serviceK.preFilter(filter));
  }

  public AsyncHttpService postFilter(PostFilter filter) {
    return new AsyncHttpService(serviceK.postFilter(filter));
  }

  public AsyncHttpService add(Matcher1<HttpRequest> matcher, AsyncRequestHandler handler) {
    return new AsyncHttpService(serviceK.add(matcher, handler));
  }

  public MappingBuilder<AsyncHttpService> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::add).when(matcher);
  }

  public Option<Promise<HttpResponse>> execute(HttpRequest request) {
    return serviceK.execute(request).map(Future::narrowK).map(Future::toPromise);
  }

  public AsyncHttpService combine(AsyncHttpService other) {
    return new AsyncHttpService(this.serviceK.combine(other.serviceK));
  }

  @Override
  public String toString() {
    return "AsyncHttpService(" + serviceK.name() + ")";
  }

  public static final class MappingBuilder<T> {
    private final Function2<Matcher1<HttpRequest>, AsyncRequestHandler, T> finisher;
    private Matcher1<HttpRequest> matcher;

    public MappingBuilder(Function2<Matcher1<HttpRequest>, AsyncRequestHandler, T> finisher) {
      this.finisher = requireNonNull(finisher);
    }

    public MappingBuilder<T> when(Matcher1<HttpRequest> matcher) {
      this.matcher = matcher;
      return this;
    }

    public T then(AsyncRequestHandler handler) {
      return finisher.apply(matcher, handler);
    }
  }
}
