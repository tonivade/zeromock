/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.Deserializers.deserializer;
import static com.github.tonivade.zeromock.Bytes.emptyByteBuffer;
import static com.github.tonivade.zeromock.Bytes.asByteBuffer;
import static com.github.tonivade.zeromock.Responses.error;
import static com.github.tonivade.zeromock.Responses.notFound;
import static com.github.tonivade.zeromock.Serializers.serializer;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.nonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class MockHttpServer {
  
  private static final String ROOT = "/";

  private final HttpServer server;

  private final List<HttpRequest> matched = new LinkedList<>();
  private final List<HttpRequest> unmatched = new LinkedList<>();
  private final HttpService root = new HttpService("root");
  
  private MockHttpServer(int port) {
    try {
      server = HttpServer.create(new InetSocketAddress(port), 0);
      server.createContext(ROOT, this::handle);
    } catch (IOException e) {
      throw new UncheckedIOException("unable to start server at port " + port, e);
    }
  }
  
  public static MockHttpServer listenAt(int port) {
    return new MockHttpServer(port);
  }

  public MockHttpServer mount(String path, HttpService service) {
    root.mount(path, service);
    return this;
  }
  
  public MockHttpServer when(Predicate<HttpRequest> predicate, Function<HttpRequest, HttpResponse> handler) {
    root.when(predicate, handler);
    return this;
  }

  public void start() {
    server.start();
  }

  public void stop() {
    server.stop(0);
  }

  public MockHttpServer verify(Predicate<HttpRequest> predicate) {
    matched.stream()
      .filter(request -> predicate.test(request))
      .findFirst()
      .orElseThrow(() -> new AssertionError("request not found"));
    return this;
  }
  
  public List<HttpRequest> getUnmatched() {
    return unmodifiableList(unmatched);
  }

  private void handle(HttpExchange exchange) throws IOException {
    try {
      HttpRequest request = createRequest(exchange);
      Optional<HttpResponse> response = execute(request);
      if (response.isPresent()) {
        matched.add(request);
        processResponse(exchange, response.get());
      } else {
        processResponse(exchange, notFound("not found"));
        unmatched.add(request);
      }
    } catch (RuntimeException e) {
      e.printStackTrace();
      processResponse(exchange, error(e.getMessage()));
    }
  }

  private Optional<HttpResponse> execute(HttpRequest request) {
    return root.execute(request);
  }

  private HttpRequest createRequest(HttpExchange exchange) throws IOException {
    HttpMethod method = HttpMethod.valueOf(exchange.getRequestMethod());
    HttpHeaders headers = new HttpHeaders(exchange.getRequestHeaders());
    HttpParams params = new HttpParams(exchange.getRequestURI().getQuery());
    Path path = new Path(exchange.getRequestURI().getPath());
    Object body = deserializer(headers).apply(asByteBuffer(exchange.getRequestBody()));
    return new HttpRequest(method, path, body, headers, params);
  }

  private void processResponse(HttpExchange exchange, HttpResponse response) throws IOException {
    ByteBuffer bytes = serialize(response);
    response.headers().forEach((key, value) -> exchange.getResponseHeaders().add(key, value));
    exchange.sendResponseHeaders(response.status().code(), bytes.remaining());
    try (OutputStream output = exchange.getResponseBody()) {
      exchange.getResponseBody().write(bytes.array());
    }
  }

  private ByteBuffer serialize(HttpResponse response) {
    ByteBuffer bytes;
    if (nonNull(response.body())) {
      bytes = serializer(response.headers()).apply(response.body());
    } else {
      bytes = emptyByteBuffer();
    }
    return bytes;
  }
}
