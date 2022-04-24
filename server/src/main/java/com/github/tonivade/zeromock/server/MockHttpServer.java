/*
 * Copyright (c) 2018-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.purefun.typeclasses.Instance.monad;
import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static com.github.tonivade.zeromock.server.ResponseInterpreterK.sync;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.type.Id_;
import com.github.tonivade.purefun.typeclasses.Instance;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpRouteBuilder;
import com.github.tonivade.zeromock.api.HttpService;
import com.github.tonivade.zeromock.api.PostFilter;
import com.github.tonivade.zeromock.api.PreFilter;
import com.github.tonivade.zeromock.api.RequestHandler;
import com.github.tonivade.zeromock.server.MockHttpServerK.BuilderK;

public final class MockHttpServer implements HttpServer, HttpRouteBuilder<MockHttpServer> {

  private final MockHttpServerK<Id_> serverK;

  public MockHttpServer(com.sun.net.httpserver.HttpServer server) {
    this(new MockHttpServerK<>(server, monad(Id_.class), sync()));
  }

  private MockHttpServer(MockHttpServerK<Id_> serverK) {
    this.serverK = requireNonNull(serverK);
  }

  @Override
  public int getPort() {
    return serverK.getPort();
  }
  
  @Override
  public String getPath() {
    return serverK.getPath();
  }

  public static BuilderK<Id_, MockHttpServer> builder() {
    return new BuilderK<>(monad(Id_.class), sync()) {
      @Override
      public MockHttpServer build() {
        return new MockHttpServer(buildK());
      }
    };
  }

  public static MockHttpServer listenAt(int port) {
    return builder().port(port).build();
  }

  public static MockHttpServer listenAt(String host, int port) {
    return builder().host(host).port(port).build();
  }

  public MockHttpServer mount(String path, HttpService other) {
    serverK.mount(path, other.build());
    return this;
  }

  public MockHttpServer exec(RequestHandler handler) {
    serverK.exec(handler.liftId()::apply);
    return this;
  }

  public ThenStep<MockHttpServer> preFilter(Matcher1<HttpRequest> matcher) {
    return handler -> addPreFilter(matcher, handler);
  }

  public MockHttpServer preFilter(PreFilter filter) {
    serverK.preFilter(filter.liftId()::apply);
    return this;
  }

  public MockHttpServer postFilter(PostFilter filter) {
    serverK.postFilter(filter.liftId()::apply);
    return this;
  }

  protected MockHttpServer addMapping(Matcher1<HttpRequest> matcher, RequestHandler handler) {
    serverK.addMapping(matcher, handler.liftId()::apply);
    return this;
  }

  protected MockHttpServer addPreFilter(Matcher1<HttpRequest> matcher, RequestHandler handler) {
    serverK.preFilter(filter(Instance.monad(Id_.class), matcher, handler.liftId()::apply)::apply);
    return this;
  }

  public ThenStep<MockHttpServer> when(Matcher1<HttpRequest> matcher) {
    return handler -> addMapping(matcher, handler);
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
  public Sequence<HttpRequest> getUnmatched() {
    return serverK.getUnmatched();
  }

  @Override
  public void reset() {
    serverK.reset();
  }
}
