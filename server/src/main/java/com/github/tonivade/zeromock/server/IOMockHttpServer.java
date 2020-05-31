/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.purefun.concurrent.Future.DEFAULT_EXECUTOR;
import static com.github.tonivade.purefun.instances.IOInstances.monad;
import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static com.github.tonivade.zeromock.server.ResponseInterpreterK.ioAsync;
import static com.github.tonivade.zeromock.server.ResponseInterpreterK.ioSync;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.concurrent.Executor;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.instances.IOInstances;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.IO_;
import com.github.tonivade.zeromock.api.HttpIOService;
import com.github.tonivade.zeromock.api.HttpIOService.MappingBuilder;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.IOPostFilter;
import com.github.tonivade.zeromock.api.IOPreFilter;
import com.github.tonivade.zeromock.api.IORequestHandler;
import com.github.tonivade.zeromock.api.PostFilter;
import com.github.tonivade.zeromock.api.PreFilter;
import com.github.tonivade.zeromock.server.MockHttpServerK.BuilderK;

public final class IOMockHttpServer implements HttpServer {

  private final MockHttpServerK<IO_> serverK;

  @SuppressWarnings("restriction")
  public IOMockHttpServer(com.sun.net.httpserver.HttpServer server) {
    this(new MockHttpServerK<IO_>(server, monad(), ioSync()));
  }

  private IOMockHttpServer(MockHttpServerK<IO_> serverK) {
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

  public static BuilderK<IO_, IOMockHttpServer> sync() {
    return _builder(ioSync());
  }

  public static BuilderK<IO_, IOMockHttpServer> async() {
    return async(DEFAULT_EXECUTOR);
  }

  public static BuilderK<IO_, IOMockHttpServer> async(Executor executor) {
    return _builder(ioAsync(executor));
  }

  public static IOMockHttpServer listenAt(int port) {
    return sync().port(port).build();
  }

  public IOMockHttpServer mount(String path, HttpIOService other) {
    serverK.mount(path, other.build());
    return this;
  }

  public IOMockHttpServer exec(IORequestHandler handler) {
    serverK.exec(handler);
    return this;
  }

  public MappingBuilder<IOMockHttpServer> preFilter(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::addMapping).when(matcher);
  }

  public IOMockHttpServer preFilter(PreFilter filter) {
    return preFilter(filter.andThen(IO::pure)::apply);
  }

  public IOMockHttpServer preFilter(IOPreFilter filter) {
    serverK.preFilter(filter);
    return this;
  }

  public IOMockHttpServer postFilter(PostFilter filter) {
    return postFilter(filter.andThen(IO::pure)::apply);
  }

  public IOMockHttpServer postFilter(IOPostFilter filter) {
    serverK.postFilter(filter);
    return this;
  }

  public IOMockHttpServer addMapping(Matcher1<HttpRequest> matcher, IORequestHandler handler) {
    serverK.addMapping(matcher, handler);
    return this;
  }

  public IOMockHttpServer addPreFilter(Matcher1<HttpRequest> matcher, IORequestHandler handler) {
    serverK.preFilter(filter(IOInstances.monad(), matcher, handler));
    return this;
  }

  public MappingBuilder<IOMockHttpServer> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::addMapping).when(matcher);
  }

  @Override
  public IOMockHttpServer start() {
    serverK.start();
    return this;
  }

  @Override
  public void stop() {
    serverK.stop();
  }

  @Override
  public IOMockHttpServer verify(Matcher1<HttpRequest> matcher) {
    serverK.verify(matcher);
    return this;
  }

  @Override
  public IOMockHttpServer verifyNot(Matcher1<HttpRequest> matcher) {
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

  private static BuilderK<IO_, IOMockHttpServer> _builder(ResponseInterpreterK<IO_> interpreter) {
    return new BuilderK<IO_, IOMockHttpServer>(monad(), interpreter) {
      @Override
      public IOMockHttpServer build() {
        return new IOMockHttpServer(buildK());
      }
    };
  }
}
