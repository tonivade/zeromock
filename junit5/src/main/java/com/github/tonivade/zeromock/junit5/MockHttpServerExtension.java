/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit5;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import com.github.tonivade.purefun.Tuple;
import com.github.tonivade.purefun.Tuple2;
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

public class MockHttpServerExtension
    implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver {

  private com.sun.net.httpserver.HttpServer server;
  
  private HttpServer serverK;

  @Override
  public void beforeAll(ExtensionContext context) {
    Optional<ListenAt> listenAt = listenAt(context);
    int port = listenAt.map(ListenAt::value).orElse(0);
    this.server = new Builder().port(port).build();
    this.server.start();
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    try {
      server.removeContext("/");
    } catch (IllegalArgumentException e) {
      // not important
    } finally {
      serverK = null;
    }
  }

  @Override
  public void afterEach(ExtensionContext context) {
    if (!serverK.getUnmatched().isEmpty()) {
      context.publishReportEntry("UnmatchedRequests", unmatched());
    }
  }

  @Override
  public void afterAll(ExtensionContext context) {
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
      var services = findServices(extensionContext);
      HttpServer server = buildServer(type);
      mount(extensionContext, server, services);
      return server;
    }
    if (clientInstance(type)) {
      String baseUrl = "http://localhost:" + server.getAddress().getPort();
      return buildClient(type).connectTo(baseUrl);
    }
    throw new ParameterResolutionException("invalid param");
  }

  private void mount(ExtensionContext context, HttpServer server, List<Tuple2<Field, Mount>> services) {
    Optional<Method> mountMethod = Stream.of(server.getClass().getDeclaredMethods()).filter(m -> m.getName().equals("mount")).findFirst();
    services.forEach(t ->
      mountMethod.ifPresent(m -> {
        Field field = t.get1();
        field.trySetAccessible();
        Mount mount = t.get2();
        try {
          m.invoke(server, mount.value(), field.get(context.getRequiredTestInstance()));
        } catch (IllegalAccessException | IllegalArgumentException e) {
          throw new ParameterResolutionException("cannot access field " + field.getName());
        } catch (InvocationTargetException e) {
          throw new ParameterResolutionException("cannot execute method " + m.getName());
        }
      })
    );
  }

  private List<Tuple2<Field, Mount>> findServices(ExtensionContext extensionContext) {
    Optional<Class<?>> testClass = extensionContext.getTestClass();
    return testClass.map(Class::getDeclaredFields).stream().flatMap(Stream::of)
      .map(f -> Tuple.of(f, f.getAnnotation(Mount.class))).filter(t -> t.get2() != null)
      .toList();
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

  private static Optional<ListenAt> listenAt(ExtensionContext context) {
    return context.getTestClass()
        .map(clazz -> clazz.getDeclaredAnnotation(ListenAt.class));
  }

  private static boolean serverInstance(Class<?> type) {
    return type.equals(MockHttpServer.class)
        || type.equals(AsyncMockHttpServer.class)
        || type.equals(IOMockHttpServer.class)
        || type.equals(UIOMockHttpServer.class)
        || type.equals(URIOMockHttpServer.class);
  }

  private static boolean clientInstance(Class<?> type) {
    return type.equals(HttpClient.class)
        || type.equals(AsyncHttpClient.class)
        || type.equals(IOHttpClient.class)
        || type.equals(UIOHttpClient.class)
        || type.equals(TaskHttpClient.class);
  }
}
