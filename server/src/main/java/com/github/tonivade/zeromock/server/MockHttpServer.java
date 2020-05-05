/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static java.util.Objects.requireNonNull;

import java.util.List;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.concurrent.Promise;
import com.github.tonivade.purefun.instances.IdInstances;
import com.github.tonivade.purefun.type.Id;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpService;
import com.github.tonivade.zeromock.api.HttpService.MappingBuilder;
import com.github.tonivade.zeromock.api.PostFilter;
import com.github.tonivade.zeromock.api.PreFilter;
import com.github.tonivade.zeromock.api.RequestHandler;
import com.github.tonivade.zeromock.server.MockHttpServerK.Builder;

public final class MockHttpServer implements HttpServer {

  private final MockHttpServerK<Id.µ> serverK;

  private MockHttpServer(MockHttpServerK<Id.µ> serverK) {
    this.serverK = requireNonNull(serverK);
  }

  public static Builder<Id.µ> builder() {
    return new Builder<>(IdInstances.functor(), response -> {
      Promise<HttpResponse> promise = Promise.make();
      Id<HttpResponse> id = response.fix1(Id::narrowK);
      promise.succeeded(id.get());
      return promise;
    });
  }

  public static MockHttpServer listenAt(int port) {
    return new MockHttpServer(builder().port(port).build());
  }

  public MockHttpServer mount(String path, HttpService other) {
    serverK.mount(path, other.build());
    return this;
  }

  public MockHttpServer exec(RequestHandler handler) {
    serverK.exec(handler.liftId()::apply);
    return this;
  }

  public MockHttpServer preFilter(PreFilter filter) {
    serverK.preFilter(filter);
    return this;
  }

  public MockHttpServer postFilter(PostFilter filter) {
    serverK.postFilter(filter);
    return this;
  }

  public MockHttpServer add(Matcher1<HttpRequest> matcher, RequestHandler handler) {
    serverK.add(matcher, handler.liftId()::apply);
    return this;
  }

  public MappingBuilder<MockHttpServer> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::add).when(matcher);
  }

  @Override
  public MockHttpServer start() {
    serverK.start();
    return this;
  }

  @Override
  public void stop() {
    serverK.stop();
  }

  @Override
  public MockHttpServer verify(Matcher1<HttpRequest> matcher) {
    serverK.verify(matcher);
    return this;
  }

  @Override
  public MockHttpServer verifyNot(Matcher1<HttpRequest> matcher) {
    serverK.verifyNot(matcher);
    return this;
  }

  @Override
  public List<HttpRequest> getUnmatched() {
    return serverK.getUnmatched();
  }

  @Override
  public void reset() {
    serverK.reset();
  }
}
