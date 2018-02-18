/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class MockHttpServerExtension implements BeforeAllCallback, AfterAllCallback, ParameterResolver {
  
  private final MockHttpServer server;

  public MockHttpServerExtension() {
    // TODO: listen port as parameter
    this.server = MockHttpServer.listenAt(8080);
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    server.start();
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
}
