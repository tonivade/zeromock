/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.Bytes.asByteBuffer;
import static com.github.tonivade.zeromock.Responses.error;
import static com.github.tonivade.zeromock.Responses.notFound;
import static java.util.Collections.unmodifiableList;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.tonivade.zeromock.Mappings.Mapping;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public final class MockHttpServer {
  
  private static final Logger LOG = Logger.getLogger(MockHttpServer.class.getName());
  
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
  
  public MockHttpServer when(Predicate<HttpRequest> predicate, 
                             Function<HttpRequest, HttpResponse> handler) {
    root.when(predicate, handler);
    return this;
  }
  
  public MockHttpServer when(Mapping mapping) {
    root.when(mapping);
    return this;
  }
  
  public void start() {
    server.start();
    LOG.info(() -> "server listening at " + server.getAddress());
  }

  public void stop() {
    server.stop(0);
    LOG.info(() -> "server stopped");
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

  protected void reset() {
    root.clear();
    matched.clear();
    unmatched.clear();
  }

  private void handle(HttpExchange exchange) throws IOException {
    try {
      HttpRequest request = createRequest(exchange);
      Optional<HttpResponse> response = execute(request);
      if (response.isPresent()) {
        matched.add(request);
        processResponse(exchange, response.get());
      } else {
        LOG.fine(() -> "unmatched request " + request);
        processResponse(exchange, notFound());
        unmatched.add(request);
      }
    } catch (RuntimeException e) {
      LOG.log(Level.SEVERE, "error processing request: " + exchange.getRequestURI(), e);
      processResponse(exchange, error(asByteBuffer(e.getMessage())));
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
    Bytes body = asByteBuffer(exchange.getRequestBody());
    return new HttpRequest(method, path, body, headers, params);
  }

  private void processResponse(HttpExchange exchange, HttpResponse response) throws IOException {
    Bytes bytes = response.body();
    response.headers().forEach((key, value) -> exchange.getResponseHeaders().add(key, value));
    exchange.sendResponseHeaders(response.status().code(), bytes.size());
    try (OutputStream output = exchange.getResponseBody()) {
      exchange.getResponseBody().write(bytes.toArray());
    }
  }
}
