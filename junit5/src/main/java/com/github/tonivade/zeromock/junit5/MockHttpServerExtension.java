/*
 * Copyright (c) 2018-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
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
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import com.github.tonivade.purefun.core.Tuple;
import com.github.tonivade.purefun.core.Tuple2;
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
import com.github.tonivade.zeromock.server.MockHttpServerK;
import com.github.tonivade.zeromock.server.UIOMockHttpServer;
import com.github.tonivade.zeromock.server.URIOMockHttpServer;

public class MockHttpServerExtension
    implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver {

  private static final String SERVER = "server";
  private static final String SERVER_K = "serverK";

  @Override
  public void beforeAll(ExtensionContext context) {
    Optional<ListenAt> listenAt = listenAt(context);
    int port = listenAt.map(ListenAt::value).orElse(0);
    var server = new MockHttpServerK.Builder().port(port).build();
    server.start();

    getStoreForClass(context).put(SERVER, server);
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    try {
      var server = getServer(context);
      server.removeContext("/");
    } catch (IllegalArgumentException e) {
      // not important
    }
  }

  @Override
  public void afterEach(ExtensionContext context) {
    var serverK = removeServerK(context);
    if (serverK != null && !serverK.getUnmatched().isEmpty()) {
      context.publishReportEntry("UnmatchedRequests", unmatched(serverK));
    }
  }

  @Override
  public void afterAll(ExtensionContext context) {
    try {
      var server = getServer(context);
      server.stop(0);
    } finally {
      getStoreForClass(context).remove(SERVER);
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    Class<?> type = parameterContext.getParameter().getType();
    return serverInstance(type) || clientInstance(type);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    Class<?> type = parameterContext.getParameter().getType();
    var server = getServer(extensionContext);
    if (serverInstance(type)) {
      var services = findServices(extensionContext);
      HttpServer serverK = buildServer(server, type);
      mount(extensionContext, serverK, services);
      getStoreForMethod(extensionContext).put(SERVER_K, serverK);
      return serverK;
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

  private HttpServer buildServer(com.sun.net.httpserver.HttpServer server, Class<?> type) {
    // TODO: please remove all this if-else-if chain
    if (type.isAssignableFrom(MockHttpServer.class)) {
      return new MockHttpServer(server);
    } else if (type.isAssignableFrom(AsyncMockHttpServer.class)) {
      return new AsyncMockHttpServer(server);
    } else if (type.isAssignableFrom(IOMockHttpServer.class)) {
      return new IOMockHttpServer(server);
    } else if (type.isAssignableFrom(UIOMockHttpServer.class)) {
      return new UIOMockHttpServer(server);
    } else if (type.isAssignableFrom(URIOMockHttpServer.class)) {
      throw new UnsupportedOperationException("urio is not supported yet!");
    }
    throw new ParameterResolutionException("invalid server param");
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
    }
    throw new ParameterResolutionException("invalid client param");
  }

  private String unmatched(HttpServer serverK) {
    if (serverK == null) {
      return "";
    }
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

  private Store getStoreForClass(ExtensionContext context) {
    return context.getStore(Namespace.create(context.getRequiredTestClass()));
  }

  private Store getStoreForMethod(ExtensionContext context) {
    return context.getStore(Namespace.create(context.getRequiredTestMethod()));
  }

  private com.sun.net.httpserver.HttpServer getServer(ExtensionContext context) {
    return getStoreForClass(context).get(SERVER, com.sun.net.httpserver.HttpServer.class);
  }

  private HttpServer removeServerK(ExtensionContext context) {
    return getStoreForMethod(context).remove(SERVER_K, HttpServer.class);
  }
}
