/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.PreFilter.filter;
import static java.util.Objects.requireNonNull;
import com.github.tonivade.purefun.core.Matcher1;
import com.github.tonivade.purefun.type.Id;
import com.github.tonivade.purefun.type.IdOf;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.typeclasses.Instances;

public final class HttpService implements HttpRouteBuilder<HttpService> {

  private final HttpServiceK<Id<?>> serviceK;

  public HttpService(String name) {
    this(new HttpServiceK<>(name, Instances.<Id<?>>monad()));
  }

  private HttpService(HttpServiceK<Id<?>> serviceK) {
    this.serviceK = requireNonNull(serviceK);
  }

  public String name() {
    return serviceK.name();
  }

  public HttpServiceK<Id<?>> build() {
    return serviceK;
  }

  public HttpService mount(String path, HttpService other) {
    return new HttpService(serviceK.mount(path, other.serviceK));
  }

  public HttpService exec(RequestHandler handler) {
    return new HttpService(serviceK.exec(handler.lift(Instances.<Id<?>>monad())));
  }

  public ThenStep<HttpService> preFilter(Matcher1<HttpRequest> matcher) {
    return handler -> addPreFilter(matcher, handler);
  }

  public HttpService preFilter(PreFilter filter) {
    return new HttpService(serviceK.preFilter(filter.lift(Instances.<Id<?>>monad())));
  }

  public HttpService postFilter(PostFilter filter) {
    return new HttpService(serviceK.postFilter(filter.lift(Instances.<Id<?>>monad())));
  }

  @Override
  public ThenStep<HttpService> when(Matcher1<HttpRequest> matcher) {
    return handler -> addMapping(matcher, handler);
  }

  public Option<HttpResponse> execute(HttpRequest request) {
    return serviceK.execute(request).fix(IdOf::toId).value();
  }

  public HttpService combine(HttpService other) {
    return new HttpService(serviceK.combine(other.serviceK));
  }

  private HttpService addMapping(Matcher1<HttpRequest> matcher, RequestHandler handler) {
    return new HttpService(serviceK.addMapping(matcher, handler.lift(Instances.<Id<?>>monad())));
  }

  private HttpService addPreFilter(Matcher1<HttpRequest> matcher, RequestHandler handler) {
    return preFilter(filter(matcher, handler));
  }

  @Override
  public String toString() {
    return "HttpService(" + serviceK.name() + ")";
  }
}
