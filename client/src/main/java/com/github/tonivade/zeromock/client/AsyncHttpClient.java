/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.client;

import static com.github.tonivade.purefun.concurrent.FutureOf.toFuture;
import static java.util.Objects.requireNonNull;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.Future_;
import com.github.tonivade.purefun.instances.FutureInstances;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;

public class AsyncHttpClient implements HttpClientOf<Future_> {

  private final HttpClientK<Future_> client;

  private AsyncHttpClient(HttpClientK<Future_> client) {
    this.client = requireNonNull(client);
  }

  public static AsyncHttpClient connectTo(String baseUrl) {
    return new AsyncHttpClient(new HttpClientK<>(baseUrl, FutureInstances.monadDefer()));
  }

  public Future<HttpResponse> request(HttpRequest request) {
    return client.request(request).fix(toFuture());
  }
}
