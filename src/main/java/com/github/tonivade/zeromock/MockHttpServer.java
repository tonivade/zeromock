/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.IOUtils.readAll;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class MockHttpServer {
  private final HttpServer server;

  private Map<String, Request> requests = new HashMap<>();
  private Map<String, Resource> mappings = new HashMap<>();
  
  private MockHttpServer(int port) {
    try {
      server = HttpServer.create(new InetSocketAddress(port), 0);
      server.createContext("/", this::handle);
    } catch (IOException e) {
      throw new UncheckedIOException("unable to start server at port " + port, e);
    }
  }
  
  public static MockHttpServer listenAt(int port) {
    return new MockHttpServer(port);
  }

  public MockHttpServer mount(String path, Resource resource) {
    mappings.put(path, resource);
    return this;
  }

  public void start() {
    server.start();
  }

  public void stop() {
    server.stop(0);
  }

  public Request getRequest(String url) {
    return requests.get(url);
  }

  private void handle(HttpExchange exchange) throws IOException {
    Request request = createRequest(exchange);
    Resource resource = findResource(request);
    processResponse(exchange, resource.handle(request.dropOneLevel()));
  }

  private Resource findResource(Request request) {
    return mappings.get("/" + request.path.get(0));
  }

  private Request createRequest(HttpExchange exchange) throws IOException {
    return new Request(exchange.getRequestMethod(),
                       new Path(exchange.getRequestURI().getPath()), 
                       readAll(exchange.getRequestBody()),
                       exchange.getRequestHeaders(),
                       queryToMap(exchange.getRequestURI().getQuery()));
  }
  
  private Map<String, String> queryToMap(String query) {
    Map<String, String> result = new HashMap<>();
    if (query != null) {
      for (String param : query.split("&")) {
        String[] pair = param.split("=");
        if (pair.length > 1) {
          result.put(pair[0], pair[1]);
        } else {
          result.put(pair[0], "");
        }
      }
    }
    return result;
  }

  private void processResponse(HttpExchange exchange, Response response) throws IOException {
    byte[] bytes = response.getBytes();
    response.headers.forEach((key, values) -> values.forEach(value -> exchange.getResponseHeaders().add(key, value)));
    exchange.sendResponseHeaders(response.statusCode, bytes.length);
    exchange.getResponseBody().write(bytes);
    exchange.getResponseBody().close();
  }
}
