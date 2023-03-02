/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
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

public final class HttpService implements HttpRouteBuilder<HttpService> {

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

  @Override
  public ThenStep<HttpService> when(Matcher1<HttpRequest> matcher) {
    return handler -> addMapping(matcher, handler);
  }

  public Option<HttpResponse> execute(HttpRequest request) {
    return serviceK.execute(request).fix(toId()).get();
  }

  public HttpService combine(HttpService other) {
    return new HttpService(serviceK.combine(other.serviceK));
  }

  private HttpService addMapping(Matcher1<HttpRequest> matcher, RequestHandler handler) {
    return new HttpService(serviceK.addMapping(matcher, handler.liftId()::apply));
  }

  private HttpService addPreFilter(Matcher1<HttpRequest> matcher, RequestHandler handler) {
    return preFilter(filter(matcher, handler));
  }

  @Override
  public String toString() {
    return "HttpService(" + serviceK.name() + ")";
  }
}
