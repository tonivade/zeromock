/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.zeromock.api.Bytes.asBytes;
import static com.github.tonivade.zeromock.api.Responses.error;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serial;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Nullable;
import com.github.tonivade.purefun.core.Matcher1;

import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.typeclasses.Monad;
import com.github.tonivade.zeromock.api.HttpHeaders;
import com.github.tonivade.zeromock.api.HttpMethod;
import com.github.tonivade.zeromock.api.HttpParams;
import com.github.tonivade.zeromock.api.HttpPath;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpRouteBuilderK;
import com.github.tonivade.zeromock.api.HttpServiceK;
import com.github.tonivade.zeromock.api.PostFilterK;
import com.github.tonivade.zeromock.api.PreFilterK;
import com.github.tonivade.zeromock.api.RequestHandlerK;
import com.github.tonivade.zeromock.api.Responses;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class MockHttpServerK<F> implements com.github.tonivade.zeromock.server.HttpServer, HttpRouteBuilderK<F, MockHttpServerK<F>> {

  private static final Logger LOG = LoggerFactory.getLogger(MockHttpServerK.class);

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

  @Override
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

  @Override
  public ThenStepK<F, MockHttpServerK<F>> when(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(monad, handler -> addMapping(matcher, handler));
  }

  public ThenStepK<F, MockHttpServerK<F>> preFilter(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(monad, handler -> addPreFilter(matcher, handler));
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
    LOG.info("server listening at {}", server.getAddress());
    return this;
  }

  @Override
  public void stop() {
    server.stop(0);
    LOG.info("server stopped");
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
  public Sequence<HttpRequest> getUnmatched() {
    return ImmutableList.from(unmatched.values());
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
    LOG.debug("unmatched request {}", request);
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
    var method = HttpMethod.valueOf(exchange.getRequestMethod());
    var headers = HttpHeaders.from(exchange.getRequestHeaders());
    var params = new HttpParams(exchange.getRequestURI().getQuery());
    var path = HttpPath.from(exchange.getRequestURI().getPath());
    var body = asBytes(exchange.getRequestBody());
    return new HttpRequest(method, path, body, headers, params);
  }

  private void processResponse(HttpExchange exchange, HttpResponse response) {
    try (exchange) {
      var bytes = response.body();
      response.headers().forEach((key, value) -> exchange.getResponseHeaders().add(key, value));
      exchange.sendResponseHeaders(response.status().code(), bytes.size());
      try (OutputStream output = exchange.getResponseBody()) {
        output.write(bytes.toArray());
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public abstract static class BuilderK<F, T extends com.github.tonivade.zeromock.server.HttpServer> {

    private final Monad<F> monad;
    private final ResponseInterpreterK<F> interpreter;
    private final Builder builder;

    protected BuilderK(Monad<F> monad, ResponseInterpreterK<F> interpreter) {
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

    public BuilderK<F, T> executor(Executor executor) {
      builder.executor(executor);
      return this;
    }

    public BuilderK<F, T> backlog(int backlog) {
      builder.backlog(backlog);
      return this;
    }

    public MockHttpServerK<F> buildK() {
      return new MockHttpServerK<>(builder.build(), monad, interpreter);
    }

    public abstract T build();
  }

  public static final class Builder {

    private String host = "localhost";
    private int port = 8080;
    private int backlog = 0;
    @Nullable
    private Executor executor;

    public Builder host(String host) {
      this.host = requireNonNull(host);
      return this;
    }

    public Builder port(int port) {
      this.port = port;
      return this;
    }

    public Builder backlog(int backlog) {
      this.backlog = backlog;
      return this;
    }

    public Builder executor(Executor executor) {
      this.executor = executor;
      return this;
    }

    public HttpServer build() {
      try {
        var server = HttpServer.create(new InetSocketAddress(host, port), backlog);
        if (executor != null) {
          server.setExecutor(executor);
        } else {
          server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        }
        return server;
      } catch (IOException e) {
        throw new UncheckedIOException("unable to create server at " + host + ":" + port, e);
      }
    }
  }

  private static final class LimitedSizeMap<K, V> extends LinkedHashMap<K, V> {

    @Serial
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

