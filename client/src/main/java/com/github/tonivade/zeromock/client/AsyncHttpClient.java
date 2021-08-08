/*
 * Copyright (c) 2018-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.client;

import static com.github.tonivade.purefun.typeclasses.Instance.monadDefer;
import static java.util.Objects.requireNonNull;

import java.util.concurrent.Executor;

import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.ParOf;
import com.github.tonivade.purefun.concurrent.Par_;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;

public class AsyncHttpClient implements HttpClientOf<Par_> {

  private final HttpClientK<Par_> client;

  private AsyncHttpClient(HttpClientK<Par_> client) {
    this.client = requireNonNull(client);
  }

  public static AsyncHttpClient connectTo(String baseUrl) {
    return new AsyncHttpClient(new HttpClientK<>(baseUrl, monadDefer(Par_.class)));
  }

  public Future<HttpResponse> request(HttpRequest request) {
    return request(request, Future.DEFAULT_EXECUTOR);
  }

  public Future<HttpResponse> request(HttpRequest request, Executor executor) {
    return client.request(request).fix(ParOf.toPar()).apply(executor);
  }
}
