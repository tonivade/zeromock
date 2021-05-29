/*
 * Copyright (c) 2018-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.purefun.concurrent.Future.DEFAULT_EXECUTOR;
import static com.github.tonivade.purefun.typeclasses.Instance.monad;
import static com.github.tonivade.purefun.typeclasses.Instance.runtime;
import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static java.util.Objects.requireNonNull;

import java.util.concurrent.Executor;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.IO_;
import com.github.tonivade.zeromock.api.HttpIOService;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpRouteBuilderK;
import com.github.tonivade.zeromock.api.IOPostFilter;
import com.github.tonivade.zeromock.api.IOPreFilter;
import com.github.tonivade.zeromock.api.IORequestHandler;
import com.github.tonivade.zeromock.api.PostFilter;
import com.github.tonivade.zeromock.api.PreFilter;
import com.github.tonivade.zeromock.server.MockHttpServerK.BuilderK;

public final class IOMockHttpServer implements HttpServer, HttpRouteBuilderK<IO_, IOMockHttpServer, IORequestHandler> {

  private final MockHttpServerK<IO_> serverK;

  public IOMockHttpServer(com.sun.net.httpserver.HttpServer server) {
    this(new MockHttpServerK<>(server, monad(IO_.class), ResponseInterpreterK.sync(runtime(IO_.class))));
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
    return _builder(ResponseInterpreterK.sync(runtime(IO_.class)));
  }

  public static BuilderK<IO_, IOMockHttpServer> async() {
    return async(DEFAULT_EXECUTOR);
  }

  public static BuilderK<IO_, IOMockHttpServer> async(Executor executor) {
    return _builder(ResponseInterpreterK.async(runtime(IO_.class), executor));
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

  public ThenStep<IOMockHttpServer, IORequestHandler> preFilter(Matcher1<HttpRequest> matcher) {
    return handler -> addPreFilter(matcher, handler);
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
    serverK.preFilter(filter(monad(IO_.class), matcher, handler));
    return this;
  }

  public ThenStep<IOMockHttpServer, IORequestHandler> when(Matcher1<HttpRequest> matcher) {
    return handler -> addMapping(matcher, handler);
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
  public Sequence<HttpRequest> getUnmatched() {
    return serverK.getUnmatched();
  }

  @Override
  public void reset() {
    serverK.reset();
  }

  private static BuilderK<IO_, IOMockHttpServer> _builder(ResponseInterpreterK<IO_> interpreter) {
    return new BuilderK<>(monad(IO_.class), interpreter) {
      @Override
      public IOMockHttpServer build() {
        return new IOMockHttpServer(buildK());
      }
    };
  }
}
