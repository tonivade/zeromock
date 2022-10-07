/*
 * Copyright (c) 2018-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.monad.IOOf.toIO;
import static com.github.tonivade.purefun.typeclasses.Instances.monad;
import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.IO_;
import com.github.tonivade.purefun.type.Option;

public final class HttpIOService implements HttpRouteBuilderK<IO_, HttpIOService> {

  private final HttpServiceK<IO_> serviceK;

  public HttpIOService(String name) {
    this(new HttpServiceK<>(name, monad(IO_.class)));
  }

  private HttpIOService(HttpServiceK<IO_> serviceK) {
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

  public ThenStepK<IO_, HttpIOService> preFilter(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(monad(IO_.class), handler -> addPreFilter(matcher, handler::apply));
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
  public ThenStepK<IO_, HttpIOService> when(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(monad(IO_.class), handler -> addMapping(matcher, handler::apply));
  }

  public IO<Option<HttpResponse>> execute(HttpRequest request) {
    return serviceK.execute(request).fix(toIO());
  }

  public HttpIOService combine(HttpIOService other) {
    return new HttpIOService(serviceK.combine(other.serviceK));
  }

  public HttpServiceK<IO_> build() {
    return serviceK;
  }

  public HttpIOService addMapping(Matcher1<HttpRequest> matcher, IORequestHandler handler) {
    return new HttpIOService(serviceK.addMapping(matcher, handler));
  }

  private HttpIOService addPreFilter(Matcher1<HttpRequest> matcher, IORequestHandler handler) {
    return preFilter(filter(monad(IO_.class), matcher, handler)::apply);
  }

  @Override
  public String toString() {
    return "HttpIOService(" + serviceK.name() + ")";
  }
}
