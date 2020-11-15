/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit5;

import java.util.Optional;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import com.github.tonivade.zeromock.client.AsyncHttpClient;
import com.github.tonivade.zeromock.client.HttpClient;
import com.github.tonivade.zeromock.client.HttpClientBuilder;
import com.github.tonivade.zeromock.client.IOHttpClient;
import com.github.tonivade.zeromock.client.TaskHttpClient;
import com.github.tonivade.zeromock.client.UIOHttpClient;
import com.github.tonivade.zeromock.server.AsyncMockHttpServer;
import com.github.tonivade.zeromock.server.HttpServer;
import com.github.tonivade.zeromock.server.IOMockHttpServer;
import com.github.tonivade.zeromock.server.MockHttpServer;
import com.github.tonivade.zeromock.server.MockHttpServerK.Builder;
import com.github.tonivade.zeromock.server.UIOMockHttpServer;
import com.github.tonivade.zeromock.server.URIOMockHttpServer;

@SuppressWarnings("restriction")
public class MockHttpServerExtension
    implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver {

  private com.sun.net.httpserver.HttpServer server;
  
  private HttpServer serverK;

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    Optional<ListenAt> listenAt = listenAt(context);
    int port = listenAt.map(ListenAt::value).orElse(0);
    this.server = new Builder().port(port).build();
    this.server.start();
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    try {
      server.removeContext("/");
    } catch (IllegalArgumentException e) {
      // not important
    } finally {
      serverK = null;
    }
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    if (!serverK.getUnmatched().isEmpty()) {
      context.publishReportEntry("UnmatchedRequests", unmatched());
    }
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    server.stop(0);
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    Class<?> type = parameterContext.getParameter().getType();
    return serverInstance(type) || clientInstance(type);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    Class<?> type = parameterContext.getParameter().getType();
    if (serverInstance(type)) {
      return buildServer(type);
    }
    if (clientInstance(type)) {
      String baseUrl = "http://localhost:" + server.getAddress().getPort();
      return buildClient(type).connectTo(baseUrl);
    }
    throw new ParameterResolutionException("invalid param");
  }

  private HttpServer buildServer(Class<?> type) {
    // TODO: please remove all this if-else-if chain
    if (type.isAssignableFrom(MockHttpServer.class)) {
      this.serverK = new MockHttpServer(server);
    } else if (type.isAssignableFrom(AsyncMockHttpServer.class)) {
      this.serverK = new AsyncMockHttpServer(server);
    } else if (type.isAssignableFrom(IOMockHttpServer.class)) {
      this.serverK = new IOMockHttpServer(server);
    } else if (type.isAssignableFrom(UIOMockHttpServer.class)) {
      this.serverK = new UIOMockHttpServer(server);
    } else if (type.isAssignableFrom(URIOMockHttpServer.class)) {
      throw new UnsupportedOperationException("urio is not supported yet!");
    } else {
      throw new ParameterResolutionException("invalid server param");
    }
    return serverK;
  }

  private HttpClientBuilder<?> buildClient(Class<?> type) {
    // TODO: please remove all this if-else-if chain
    if (type.isAssignableFrom(HttpClient.class)) {
      return HttpClientBuilder.client();
    } else if (type.isAssignableFrom(AsyncHttpClient.class)) {
      return HttpClientBuilder.asyncClient();
    } else if (type.isAssignableFrom(IOHttpClient.class)) {
      return HttpClientBuilder.ioClient();
    } else if (type.isAssignableFrom(UIOHttpClient.class)) {
      return HttpClientBuilder.uioClient();
    } else if (type.isAssignableFrom(TaskHttpClient.class)) {
      return HttpClientBuilder.taskClient();
    } else {
      throw new ParameterResolutionException("invalid client param");
    }
  }

  private String unmatched() {
    return serverK.getUnmatched().join(",", "[", "]");
  }

  private Optional<ListenAt> listenAt(ExtensionContext context) {
    return context.getTestClass()
        .map(clazz -> clazz.getDeclaredAnnotation(ListenAt.class));
  }

  private boolean serverInstance(Class<?> type) {
    return type.equals(MockHttpServer.class)
        || type.equals(AsyncMockHttpServer.class)
        || type.equals(IOMockHttpServer.class)
        || type.equals(UIOMockHttpServer.class)
        || type.equals(URIOMockHttpServer.class);
  }

  private boolean clientInstance(Class<?> type) {
    return type.equals(HttpClient.class)
        || type.equals(AsyncHttpClient.class)
        || type.equals(IOHttpClient.class)
        || type.equals(UIOHttpClient.class)
        || type.equals(TaskHttpClient.class);
  }
}
