/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import org.junit.rules.ExternalResource;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpZIOService;
import com.github.tonivade.zeromock.api.HttpZIOService.MappingBuilder;
import com.github.tonivade.zeromock.api.ZIORequestHandler;
import com.github.tonivade.zeromock.server.ZIOMockHttpServer;

public class ZIOMockHttpServerRule<R> extends ExternalResource {

  private final ZIOMockHttpServer<R> server;

  public ZIOMockHttpServerRule(R env, int port) {
    this.server = ZIOMockHttpServer.listenAt(env, port);
  }

  @Override
  protected void before() throws Throwable {
    server.start();
  }

  @Override
  protected void after() {
    server.stop();
  }

  public ZIOMockHttpServerRule<R> verify(Matcher1<HttpRequest> matcher) {
    server.verify(matcher);
    return this;
  }

  public ZIOMockHttpServerRule<R> add(Matcher1<HttpRequest> matcher, ZIORequestHandler<R> handler) {
    server.add(matcher, handler);
    return this;
  }

  public MappingBuilder<R, ZIOMockHttpServerRule<R>> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::add).when(matcher);
  }

  public ZIOMockHttpServerRule<R> mount(String path, HttpZIOService<R> service) {
    server.mount(path, service);
    return this;
  }
}
