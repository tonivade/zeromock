/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.monad.IOOf.toIO;
import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.core.Matcher1;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.typeclasses.Instances;

public final class HttpIOService implements HttpRouteBuilderK<IO<?>, HttpIOService> {

  private final HttpServiceK<IO<?>> serviceK;

  public HttpIOService(String name) {
    this(new HttpServiceK<>(name, Instances.monad()));
  }

  private HttpIOService(HttpServiceK<IO<?>> serviceK) {
    this.serviceK = requireNonNull(serviceK);
  }

  public String name() {
    return serviceK.name();
  }

  public HttpIOService mount(String path, HttpIOService other) {
    return new HttpIOService(serviceK.mount(path, other.serviceK));
  }

  public HttpIOService exec(IORequestHandler handler) {
    return new HttpIOService(serviceK.exec(handler));
  }

  public ThenStepK<IO<?>, HttpIOService> preFilter(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(serviceK.monad(), handler -> addPreFilter(matcher, handler::apply));
  }

  public HttpIOService preFilter(PreFilter filter) {
    return preFilter(filter.andThen(IO::pure)::apply);
  }

  public HttpIOService preFilter(IOPreFilter filter) {
    return new HttpIOService(serviceK.preFilter(filter));
  }

  public HttpIOService postFilter(PostFilter filter) {
    return postFilter(filter.andThen(IO::pure)::apply);
  }

  public HttpIOService postFilter(IOPostFilter filter) {
    return new HttpIOService(serviceK.postFilter(filter));
  }

  @Override
  public ThenStepK<IO<?>, HttpIOService> when(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(serviceK.monad(), handler -> addMapping(matcher, handler::apply));
  }

  public IO<Option<HttpResponse>> execute(HttpRequest request) {
    return serviceK.execute(request).fix(toIO());
  }

  public HttpIOService combine(HttpIOService other) {
    return new HttpIOService(serviceK.combine(other.serviceK));
  }

  public HttpServiceK<IO<?>> build() {
    return serviceK;
  }

  public HttpIOService addMapping(Matcher1<HttpRequest> matcher, IORequestHandler handler) {
    return new HttpIOService(serviceK.addMapping(matcher, handler));
  }

  private HttpIOService addPreFilter(Matcher1<HttpRequest> matcher, IORequestHandler handler) {
    return preFilter(filter(serviceK.monad(), matcher, handler)::apply);
  }

  @Override
  public String toString() {
    return "HttpIOService(" + serviceK.name() + ")";
  }
}
