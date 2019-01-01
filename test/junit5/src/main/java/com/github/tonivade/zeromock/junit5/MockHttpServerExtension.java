/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit5;

import static java.util.stream.Collectors.joining;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import com.github.tonivade.zeromock.server.MockHttpServer;

public class MockHttpServerExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver {
  
  private MockHttpServer server;

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    server = MockHttpServer.listenAt(port(context));
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
    return parameterContext.getParameter().getType().equals(MockHttpServer.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return server;
  }

  private String unmatched() {
    return server.getUnmatched().stream().map(Object::toString).collect(joining(",", "[", "]"));
  }

  private int port(ExtensionContext context) {
    return context.getTestClass()
        .map(clazz -> clazz.getDeclaredAnnotation(ListenAt.class))
        .map(ListenAt::value)
        .orElse(8080);
  }
}
