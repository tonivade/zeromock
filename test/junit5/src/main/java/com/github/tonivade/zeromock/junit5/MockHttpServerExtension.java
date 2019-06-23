/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit5;

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

import com.github.tonivade.zeromock.server.AsyncMockHttpServer;
import com.github.tonivade.zeromock.server.HttpServer;
import com.github.tonivade.zeromock.server.MockHttpServer;

public class MockHttpServerExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver {

  private HttpServer server;

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    Optional<ListenAt> listenAt = listenAt(context);
    if (listenAt.map(ListenAt::async).orElse(false)) {
      server = AsyncMockHttpServer.listenAt(listenAt.map(ListenAt::value).orElse(8080));
    } else {
      server = MockHttpServer.listenAt(listenAt.map(ListenAt::value).orElse(8080));
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
    return type.equals(MockHttpServer.class) || type.equals(AsyncMockHttpServer.class);
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
