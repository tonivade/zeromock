/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.zeromock.server.MockHttpServerK.sync;

import java.util.List;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.type.Id;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpService;
import com.github.tonivade.zeromock.api.HttpService.MappingBuilder;
import com.github.tonivade.zeromock.api.RequestHandler;

public final class MockHttpServer {

  private MockHttpServerK<Id.µ> serverK;

  private MockHttpServer(String host, int port, int threads, int backlog) {
    this.serverK = sync().host(host).port(port).threads(threads).backlog(backlog).build();
  }

  private MockHttpServer(MockHttpServerK<Id.µ> serverK) {
    this.serverK = serverK;
  }

  public static MockHttpServer listenAt(int port) {
    return new MockHttpServer(sync().port(port).build());
  }

  public MockHttpServer mount(String path, HttpService other) {
    serverK.mount(path, other.serviceK());
    return this;
  }

  public MockHttpServer exec(RequestHandler handler) {
    serverK.exec(handler.liftId()::apply);
    return this;
  }

  public MockHttpServer add(Matcher1<HttpRequest> matcher, RequestHandler handler) {
    serverK.add(matcher, handler.liftId()::apply);
    return this;
  }

  public MappingBuilder<MockHttpServer> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::add).when(matcher);
  }

  public MockHttpServer start() {
    serverK.start();
    return this;
  }

  public void stop() {
    serverK.stop();
  }

  public MockHttpServer verify(Matcher1<HttpRequest> matcher) {
    serverK.verify(matcher);
    return this;
  }

  public List<HttpRequest> getUnmatched() {
    return serverK.getUnmatched();
  }

  public void reset() {
    serverK.reset();
  }
}
