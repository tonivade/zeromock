/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.purefun.concurrent.Future.DEFAULT_EXECUTOR;
import static com.github.tonivade.purefun.instances.UIOInstances.monad;
import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static com.github.tonivade.zeromock.server.ResponseInterpreterK.uioAsync;
import static com.github.tonivade.zeromock.server.ResponseInterpreterK.uioSync;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.concurrent.Executor;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.effect.UIO_;
import com.github.tonivade.purefun.instances.UIOInstances;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpUIOService;
import com.github.tonivade.zeromock.api.HttpUIOService.MappingBuilder;
import com.github.tonivade.zeromock.api.PostFilter;
import com.github.tonivade.zeromock.api.PreFilter;
import com.github.tonivade.zeromock.api.UIOPostFilter;
import com.github.tonivade.zeromock.api.UIOPreFilter;
import com.github.tonivade.zeromock.api.UIORequestHandler;
import com.github.tonivade.zeromock.server.MockHttpServerK.BuilderK;

public final class UIOMockHttpServer implements HttpServer {

  private final MockHttpServerK<UIO_> serverK;

  private UIOMockHttpServer(MockHttpServerK<UIO_> serverK) {
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

  public static BuilderK<UIO_> sync() {
    return new BuilderK<>(monad(), uioSync());
  }

  public static BuilderK<UIO_> async() {
    return async(DEFAULT_EXECUTOR);
  }

  public static BuilderK<UIO_> async(Executor executor) {
    return new BuilderK<>(monad(), uioAsync(executor));
  }

  public static UIOMockHttpServer listenAt(int port) {
    return new UIOMockHttpServer(sync().port(port).build());
  }

  public UIOMockHttpServer mount(String path, HttpUIOService other) {
    serverK.mount(path, other.build());
    return this;
  }

  public UIOMockHttpServer exec(UIORequestHandler handler) {
    serverK.exec(handler);
    return this;
  }

  public MappingBuilder<UIOMockHttpServer> preFilter(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::addPreFilter).when(matcher);
  }

  public UIOMockHttpServer preFilter(PreFilter filter) {
    return preFilter(filter.andThen(UIO::pure)::apply);
  }

  public UIOMockHttpServer preFilter(UIOPreFilter filter) {
    serverK.preFilter(filter);
    return this;
  }

  public UIOMockHttpServer postFilter(PostFilter filter) {
    return postFilter(filter.andThen(UIO::pure)::apply);
  }

  public UIOMockHttpServer postFilter(UIOPostFilter filter) {
    serverK.postFilter(filter);
    return this;
  }

  public UIOMockHttpServer addMapping(Matcher1<HttpRequest> matcher, UIORequestHandler handler) {
    serverK.addMapping(matcher, handler);
    return this;
  }

  public UIOMockHttpServer addPreFilter(Matcher1<HttpRequest> matcher, UIORequestHandler handler) {
    serverK.preFilter(filter(UIOInstances.monad(), matcher, handler));
    return this;
  }

  public MappingBuilder<UIOMockHttpServer> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::addMapping).when(matcher);
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
  public UIOMockHttpServer verifyNot(Matcher1<HttpRequest> matcher) {
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
