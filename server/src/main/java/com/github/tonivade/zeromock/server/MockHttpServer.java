/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.zeromock.core.Bytes.asBytes;
import static com.github.tonivade.zeromock.core.Matchers.get;
import static com.github.tonivade.zeromock.core.Responses.error;
import static com.github.tonivade.zeromock.core.Responses.notFound;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.tonivade.zeromock.core.Bytes;
import com.github.tonivade.zeromock.core.HttpHeaders;
import com.github.tonivade.zeromock.core.HttpMethod;
import com.github.tonivade.zeromock.core.HttpParams;
import com.github.tonivade.zeromock.core.HttpPath;
import com.github.tonivade.zeromock.core.HttpRequest;
import com.github.tonivade.zeromock.core.HttpResponse;
import com.github.tonivade.zeromock.core.HttpService;
import com.github.tonivade.zeromock.core.HttpService.MappingBuilder;
import com.github.tonivade.zeromock.core.Matcher;
import com.github.tonivade.zeromock.core.RequestHandler;
import com.github.tonivade.zeromock.server.admin.AdminAPI;
import com.github.tonivade.zeromock.server.admin.AdminService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public final class MockHttpServer {
  
  private static final Logger LOG = Logger.getLogger(MockHttpServer.class.getName());
  
  private static final String ROOT = "/";
  private static final String ADMIN = "/__admin";

  private final HttpServer server;

  private final AdminService adminService = new AdminService();
  private final AdminAPI adminAPI = new AdminAPI(adminService);
  private final HttpService root = new HttpService("root");
  private final HttpService admin = new HttpService("admin")
      .when(get("/unmatched")).then(adminAPI.unmatched())
      .when(get("/matched")).then(adminAPI.matched());
  
  private MockHttpServer(String host, int port, int threads, int backlog) {
    try {
      server = HttpServer.create(new InetSocketAddress(host, port), backlog);
      server.setExecutor(Executors.newFixedThreadPool(threads));
      server.createContext(ROOT, this::handle);
      root.mount(ADMIN, admin);
    } catch (IOException e) {
      throw new UncheckedIOException("unable to start server at " + host + ":" + port, e);
    }
  }
  
  public static MockHttpServer listenAt(int port) {
    return builder().port(port).build();
  }
  
  public static Builder builder() {
    return new Builder();
  }

  public MockHttpServer mount(String path, HttpService service) {
    root.mount(path, service);
    return this;
  }
  
  public MockHttpServer exec(RequestHandler handler) {
    root.exec(handler);
    return this;
  }
  
  public MockHttpServer add(Matcher matcher, RequestHandler handler) {
    root.add(matcher, handler);
    return this;
  }
  
  public MappingBuilder<MockHttpServer> when(Matcher matcher) {
    return new MappingBuilder<>(this::add).when(matcher);
  }
  
  public MockHttpServer start() {
    server.start();
    LOG.info(() -> "server listening at " + server.getAddress());
    return this;
  }

  public void stop() {
    server.stop(0);
    LOG.info(() -> "server stopped");
  }

  public MockHttpServer verify(Matcher matcher) {
    adminService.getMatched().stream()
      .filter(matcher::match)
      .findFirst()
      .orElseThrow(() -> new AssertionError("request not found"));
    return this;
  }
  
  public List<HttpRequest> getUnmatched() {
    return adminService.getUnmatched();
  }

  public void reset() {
    root.clear();
    adminService.clear();
  }

  private void handle(HttpExchange exchange) throws IOException {
    try {
      HttpRequest request = createRequest(exchange);
      Optional<HttpResponse> response = execute(request);
      if (response.isPresent()) {
        adminService.addMatched(request);
        processResponse(exchange, response.get());
      } else {
        LOG.fine(() -> "unmatched request " + request);
        processResponse(exchange, notFound());
        adminService.addUnmatched(request);
      }
    } catch (RuntimeException e) {
      LOG.log(Level.SEVERE, "error processing request: " + exchange.getRequestURI(), e);
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
    HttpPath path = HttpPath.from(exchange.getRequestURI().getPath());
    Bytes body = asBytes(exchange.getRequestBody());
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
  
  public static final class Builder {
    private String host = "localhost";
    private int port = 8080;
    private int threads = Runtime.getRuntime().availableProcessors();
    private int backlog = 0;
    
    public Builder host(String host) {
      this.host = host;
      return this;
    }
    
    public Builder port(int port) {
      this.port = port;
      return this;
    }
    
    public Builder threads(int threads) {
      this.threads = threads;
      return this;
    }
    
    public Builder backlog(int backlog) {
      this.backlog = backlog;
      return this;
    }
    
    public MockHttpServer build() {
      return new MockHttpServer(host, port, threads, backlog);
    }
  }
}
