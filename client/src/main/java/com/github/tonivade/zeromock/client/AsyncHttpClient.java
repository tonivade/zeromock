/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.client;

import static com.github.tonivade.purefun.typeclasses.Instances.async;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Type;
import java.util.concurrent.Executor;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.effect.Task;
import com.github.tonivade.purefun.monad.IOOf;
import com.github.tonivade.purefun.monad.IO_;
import com.github.tonivade.purejson.PureJson;
import com.github.tonivade.zeromock.api.Bytes;
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
  
  public static <T> Function1<HttpResponse, Future<T>> parse(Class<T> type) {
    return parse((Type) type);
  }

  public static <T> Function1<HttpResponse, Future<T>> parse(Type type) {
    return response -> Task.fromTry(new PureJson<T>(type).fromJson(Bytes.asString(response.body())))
        .flatMap(Task::fromOption).runAsync();
  }
}
