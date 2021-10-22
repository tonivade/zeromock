/*
 * Copyright (c) 2018-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.client;

import static com.github.tonivade.purefun.typeclasses.Instance.async;
import static java.util.Objects.requireNonNull;

import java.util.concurrent.Executor;

import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.monad.IOOf;
import com.github.tonivade.purefun.monad.IO_;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;

public class AsyncHttpClient implements HttpClientOf<IO_> {

  // using IO here because there's no instance for MonadDefer for Future
  private final HttpClientK<IO_> client;

  private AsyncHttpClient(HttpClientK<IO_> client) {
    this.client = requireNonNull(client);
  }

  public static AsyncHttpClient connectTo(String baseUrl) {
    return new AsyncHttpClient(new HttpClientK<>(baseUrl, async(IO_.class)));
  }

  public Future<HttpResponse> request(HttpRequest request) {
    return request(request, Future.DEFAULT_EXECUTOR);
  }

  public Future<HttpResponse> request(HttpRequest request, Executor executor) {
    return client.request(request).fix(IOOf.toIO()).runAsync(executor);
  }
}
