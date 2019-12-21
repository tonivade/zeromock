/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static java.util.Objects.requireNonNull;

import java.util.List;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.concurrent.Promise;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpUIOService;
import com.github.tonivade.zeromock.api.HttpUIOService.MappingBuilder;
import com.github.tonivade.zeromock.api.UIORequestHandler;
import com.github.tonivade.zeromock.server.MockHttpServerK.Builder;

public final class UIOMockHttpServer implements HttpServer {

  private final MockHttpServerK<UIO.µ> serverK;

  private UIOMockHttpServer(MockHttpServerK<UIO.µ> serverK) {
    this.serverK = requireNonNull(serverK);
  }

  public static Builder<UIO.µ> builder() {
    return new Builder<>(response -> {
      UIO<HttpResponse> future = response.fix1(UIO::narrowK);
      return Promise.<HttpResponse>make().succeeded(future.unsafeRunSync());
    });
  }

  public static UIOMockHttpServer listenAt(int port) {
    return new UIOMockHttpServer(builder().port(port).build());
  }

  public UIOMockHttpServer mount(String path, HttpUIOService other) {
    serverK.mount(path, other.build());
    return this;
  }

  public UIOMockHttpServer exec(UIORequestHandler handler) {
    serverK.exec(handler);
    return this;
  }

  public UIOMockHttpServer add(Matcher1<HttpRequest> matcher, UIORequestHandler handler) {
    serverK.add(matcher, handler);
    return this;
  }

  public MappingBuilder<UIOMockHttpServer> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::add).when(matcher);
  }

  @Override
  public UIOMockHttpServer start() {
    serverK.start();
    return this;
  }

  @Override
  public void stop() {
    serverK.stop();
  }

  @Override
  public UIOMockHttpServer verify(Matcher1<HttpRequest> matcher) {
    serverK.verify(matcher);
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
