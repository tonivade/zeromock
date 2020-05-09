/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Higher1;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.concurrent.Promise;
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
import com.github.tonivade.zeromock.api.PostFilter;
import com.github.tonivade.zeromock.api.PreFilterK;
import com.github.tonivade.zeromock.api.RequestHandlerK;
import com.github.tonivade.zeromock.api.Responses;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

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

import static com.github.tonivade.zeromock.api.Bytes.asBytes;
import static com.github.tonivade.zeromock.api.Responses.error;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("restriction")
public abstract class MockHttpServerK<F extends Kind> implements com.github.tonivade.zeromock.server.HttpServer {

  private static final Logger LOG = Logger.getLogger(MockHttpServerK.class.getName());

  private static final String ROOT = "/";

  private final HttpServer server;
  private final Monad<F> monad;

  private final Map<Instant, HttpRequest> matched = new LimitedSizeMap<>(100);
  private final Map<Instant, HttpRequest> unmatched = new LimitedSizeMap<>(100);

  private HttpServiceK<F> service;

  private MockHttpServerK(String host, int port, int threads, int backlog, Monad<F> monad) {
    try {
      this.service = new HttpServiceK<>("root", monad);
      this.server = HttpServer.create(new InetSocketAddress(host, port), backlog);
      this.server.setExecutor(Executors.newFixedThreadPool(threads));
      this.server.createContext(ROOT, this::handle);
      this.monad = requireNonNull(monad);
    } catch (IOException e) {
      throw new UncheckedIOException("unable to start server at " + host + ":" + port, e);
    }
  }

  public MockHttpServerK<F> mount(String path, HttpServiceK<F> other) {
    service = service.mount(path, other);
    return this;
  }

  public MockHttpServerK<F> exec(RequestHandlerK<F> handler) {
    service = service.exec(handler);
    return this;
  }

  public MappingBuilderK<F, MockHttpServerK<F>> preFilter(Matcher1<HttpRequest> matcher) {
    return new MappingBuilderK<>(this::addPreFilter).when(requireNonNull(matcher));
  }

  public MockHttpServerK<F> preFilter(PreFilterK<F> filter) {
    service = service.preFilter(filter);
    return this;
  }

  public MockHttpServerK<F> postFilter(PostFilter filter) {
    service = service.postFilter(filter);
    return this;
  }

  public MockHttpServerK<F> addMapping(Matcher1<HttpRequest> matcher, RequestHandlerK<F> handler) {
    service = service.addMapping(matcher, handler);
    return this;
  }

  public MockHttpServerK<F> addPreFilter(Matcher1<HttpRequest> matcher, RequestHandlerK<F> handler) {
    service = service.addPreFilter(matcher, handler);
    return this;
  }

  public MappingBuilderK<F, MockHttpServerK<F>> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilderK<>(this::addMapping).when(requireNonNull(matcher));
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

  protected abstract Promise<HttpResponse> run(Higher1<F, HttpResponse> response);

  private void handle(HttpExchange exchange) throws IOException {
    HttpRequest request = createRequest(exchange);
    run(monad.map(execute(request), option -> fold(request, option)))
        .onSuccess(response -> processResponse(exchange, response))
        .onFailure(error -> processResponse(exchange, error(error)));
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

  private Higher1<F, Option<HttpResponse>> execute(HttpRequest request) {
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

  public static final class Builder<F extends Kind> {
    private String host = "localhost";
    private int port = 8080;
    private int threads = Runtime.getRuntime().availableProcessors();
    private int backlog = 0;
    private final Monad<F> monad;
    private final Function1<Higher1<F, HttpResponse>, Promise<HttpResponse>> run;

    public Builder(Monad<F> monad, Function1<Higher1<F, HttpResponse>, Promise<HttpResponse>> run) {
      this.monad = requireNonNull(monad);
      this.run = requireNonNull(run);
    }

    public Builder<F> host(String host) {
      this.host = requireNonNull(host);
      return this;
    }

    public Builder<F> port(int port) {
      this.port = port;
      return this;
    }

    public Builder<F> threads(int threads) {
      this.threads = threads;
      return this;
    }

    public Builder<F> backlog(int backlog) {
      this.backlog = backlog;
      return this;
    }

    public MockHttpServerK<F> build() {
      return new MockHttpServerK<F>(host, port, threads, backlog, monad) {

        @Override
        protected Promise<HttpResponse> run(Higher1<F, HttpResponse> response) {
          return run.apply(response);
        }
      };
    }
  }

  private static final class LimitedSizeMap<K, V> extends LinkedHashMap<K, V> {

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

