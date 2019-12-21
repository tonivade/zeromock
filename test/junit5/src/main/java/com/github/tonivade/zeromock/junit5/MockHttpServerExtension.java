/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit5;

import static com.github.tonivade.zeromock.junit5.ListenAt.Server.SYNC;
import static java.util.stream.Collectors.joining;

import java.util.Optional;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import com.github.tonivade.zeromock.junit5.ListenAt.Server;
import com.github.tonivade.zeromock.server.AsyncMockHttpServer;
import com.github.tonivade.zeromock.server.HttpServer;
import com.github.tonivade.zeromock.server.IOMockHttpServer;
import com.github.tonivade.zeromock.server.MockHttpServer;
import com.github.tonivade.zeromock.server.UIOMockHttpServer;
import com.github.tonivade.zeromock.server.ZIOMockHttpServer;

public class MockHttpServerExtension
    implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver {

  private HttpServer server;

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    Optional<ListenAt> listenAt = listenAt(context);
    Server type = listenAt.map(ListenAt::type).orElse(SYNC);
    Integer port = listenAt.map(ListenAt::value).orElse(8080);
    switch (type) {
    case SYNC:
      server = MockHttpServer.listenAt(port);
      break;
    case ASYNC:
      server = AsyncMockHttpServer.listenAt(port);
      break;
    case IO:
      server = IOMockHttpServer.listenAt(port);
      break;
    case UIO:
      server = UIOMockHttpServer.listenAt(port);
      break;
    case ZIO:
      throw new UnsupportedOperationException("ZIO not supported yet :(");
    }
    server.start();
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    server.reset();
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    if (!server.getUnmatched().isEmpty()) {
      context.publishReportEntry("UnmatchedRequests", unmatched());
    }
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    server.stop();
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    Class<?> type = parameterContext.getParameter().getType();
    return type.equals(MockHttpServer.class) 
        || type.equals(AsyncMockHttpServer.class)
        || type.equals(IOMockHttpServer.class)
        || type.equals(UIOMockHttpServer.class)
        || type.equals(ZIOMockHttpServer.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return server;
  }

  private String unmatched() {
    return server.getUnmatched().stream().map(Object::toString).collect(joining(",", "[", "]"));
  }

  private Optional<ListenAt> listenAt(ExtensionContext context) {
    return context.getTestClass()
        .map(clazz -> clazz.getDeclaredAnnotation(ListenAt.class));
  }
}
