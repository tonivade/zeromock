/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.zeromock.api.Bytes.asBytes;
import static com.github.tonivade.zeromock.api.Responses.error;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.Witness;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.typeclasses.Monad;
import com.github.tonivade.zeromock.api.Bytes;
import com.github.tonivade.zeromock.api.HttpHeaders;
import com.github.tonivade.zeromock.api.HttpMethod;
import com.github.tonivade.zeromock.api.HttpParams;
import com.github.tonivade.zeromock.api.HttpPath;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpServiceK;
import com.github.tonivade.zeromock.api.HttpServiceK.MappingBuilderK;
import com.github.tonivade.zeromock.api.PostFilterK;
import com.github.tonivade.zeromock.api.PreFilterK;
import com.github.tonivade.zeromock.api.RequestHandlerK;
import com.github.tonivade.zeromock.api.Responses;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class MockHttpServerK<F extends Witness> implements com.github.tonivade.zeromock.server.HttpServer {

  private static final Logger LOG = Logger.getLogger(MockHttpServerK.class.getName());

  private static final String ROOT = "/";

  private final HttpServer server;
  private final HttpContext context;
  private final Monad<F> monad;
  private final ResponseInterpreterK<F> interpreter;

  private final Map<Instant, HttpRequest> matched = new LimitedSizeMap<>(100);
  private final Map<Instant, HttpRequest> unmatched = new LimitedSizeMap<>(100);

  private HttpServiceK<F> service;

  protected MockHttpServerK(HttpServer server, Monad<F> monad, ResponseInterpreterK<F> interpreter) {
    this.server = requireNonNull(server);
    this.monad = requireNonNull(monad);
    this.interpreter = requireNonNull(interpreter);
    this.service = new HttpServiceK<>("root", monad);
    this.context = server.createContext(ROOT, this::handle);
  }

  @Override
  public int getPort() {
    return server.getAddress().getPort();
  }
  
  public String getPath() {
    return context.getPath();
  }

  public MockHttpServerK<F> mount(String path, HttpServiceK<F> other) {
    service = service.mount(path, other);
    return this;
  }

  public MockHttpServerK<F> exec(RequestHandlerK<F> handler) {
    service = service.exec(handler);
    return this;
  }

  public MappingBuilderK<F, MockHttpServerK<F>> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilderK<>(this::addMapping).when(requireNonNull(matcher));
  }

  public MappingBuilderK<F, MockHttpServerK<F>> preFilter(Matcher1<HttpRequest> matcher) {
    return new MappingBuilderK<>(this::addPreFilter).when(requireNonNull(matcher));
  }

  public MockHttpServerK<F> preFilter(PreFilterK<F> filter) {
    service = service.preFilter(filter);
    return this;
  }

  public MockHttpServerK<F> postFilter(PostFilterK<F> filter) {
    service = service.postFilter(filter);
    return this;
  }

  @Override
  public MockHttpServerK<F> start() {
    server.start();
    LOG.info(() -> "server listening at " + server.getAddress());
    return this;
  }

  @Override
  public void stop() {
    server.stop(0);
    LOG.info(() -> "server stopped");
  }

  @Override
  public MockHttpServerK<F> verify(Matcher1<HttpRequest> matcher) {
    if (!matches(matcher)) {
      throw new AssertionError("request not found");
    }
    return this;
  }

  @Override
  public MockHttpServerK<F> verifyNot(Matcher1<HttpRequest> matcher) {
    if (matches(matcher)) {
      throw new AssertionError("request not found");
    }
    return this;
  }

  @Override
  public List<HttpRequest> getUnmatched() {
    return unmodifiableList(new ArrayList<>(unmatched.values()));
  }

  @Override
  public void reset() {
    service = new HttpServiceK<>("root", monad);
    matched.clear();
    unmatched.clear();
  }

  protected MockHttpServerK<F> addMapping(Matcher1<HttpRequest> matcher, RequestHandlerK<F> handler) {
    service = service.addMapping(matcher, handler);
    return this;
  }

  protected MockHttpServerK<F> addPreFilter(Matcher1<HttpRequest> matcher, RequestHandlerK<F> handler) {
    service = service.addPreFilter(matcher, handler);
    return this;
  }

  private void handle(HttpExchange exchange) throws IOException {
    HttpRequest request = createRequest(exchange);
    try {
      Kind<F, HttpResponse> response = monad.map(execute(request), option -> fold(request, option));
      interpreter.run(response)
        .onSuccess(res -> processResponse(exchange, res))
        .onFailure(err -> processResponse(exchange, error(err)));
    } catch (Exception e) {
      processResponse(exchange, error(e));
    }
  }

  private HttpResponse fold(HttpRequest request, Option<HttpResponse> option) {
    return option
        .ifPresent(response -> matched(request))
        .ifEmpty(() -> unmatched(request))
        .getOrElse(Responses::notFound);
  }

  private void unmatched(HttpRequest request) {
    LOG.fine(() -> "unmatched request " + request);
    unmatched.put(Instant.now(), request);
  }

  private void matched(HttpRequest request) {
    matched.put(Instant.now(), request);
  }

  private boolean matches(Matcher1<HttpRequest> matcher) {
    return matched.values().stream().anyMatch(matcher::match);
  }

  private Kind<F, Option<HttpResponse>> execute(HttpRequest request) {
    return service.execute(request);
  }

  private HttpRequest createRequest(HttpExchange exchange) throws IOException {
    HttpMethod method = HttpMethod.valueOf(exchange.getRequestMethod());
    HttpHeaders headers = HttpHeaders.from(exchange.getRequestHeaders());
    HttpParams params = new HttpParams(exchange.getRequestURI().getQuery());
    HttpPath path = HttpPath.from(exchange.getRequestURI().getPath());
    Bytes body = asBytes(exchange.getRequestBody());
    return new HttpRequest(method, path, body, headers, params);
  }

  private void processResponse(HttpExchange exchange, HttpResponse response) {
    try {
      Bytes bytes = response.body();
      response.headers().forEach((key, value) -> exchange.getResponseHeaders().add(key, value));
      exchange.sendResponseHeaders(response.status().code(), bytes.size());
      try (OutputStream output = exchange.getResponseBody()) {
        output.write(bytes.toArray());
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } finally {
      exchange.close();
    }
  }

  public static abstract class BuilderK<F extends Witness, T> {
    
    private final Monad<F> monad;
    private final ResponseInterpreterK<F> interpreter;
    private final Builder builder;
    
    public BuilderK(Monad<F> monad, ResponseInterpreterK<F> interpreter) {
      this.builder = new Builder();
      this.monad = requireNonNull(monad);
      this.interpreter = requireNonNull(interpreter);
    }

    public BuilderK<F, T> host(String host) {
      builder.host(host);
      return this;
    }

    public BuilderK<F, T> port(int port) {
      builder.port(port);
      return this;
    }

    public BuilderK<F, T> threads(int threads) {
      builder.threads(threads);
      return this;
    }

    public BuilderK<F, T> backlog(int backlog) {
      builder.backlog(backlog);
      return this;
    }

    public MockHttpServerK<F> buildK() {
      return new MockHttpServerK<F>(builder.build(), monad, interpreter);
    }
    
    public abstract T build();
  }

  public static final class Builder {

    private String host = "localhost";
    private int port = 8080;
    private int threads = Runtime.getRuntime().availableProcessors();
    private int backlog = 0;

    public Builder host(String host) {
      this.host = requireNonNull(host);
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

    public HttpServer build() {
      try {
        HttpServer server = HttpServer.create(new InetSocketAddress(host, port), backlog);
        server.setExecutor(Executors.newFixedThreadPool(threads));
        return server;
      } catch (IOException e) {
        throw new UncheckedIOException("unable to create server at " + host + ":" + port, e);
      }
    }
  }

  private static final class LimitedSizeMap<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = 1L;

    private final int maxSize;

    private LimitedSizeMap(int maxSize) {
      super(maxSize);
      this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
      return size() > maxSize;
    }
  }
}

