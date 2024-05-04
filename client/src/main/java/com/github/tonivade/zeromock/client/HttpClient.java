/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.client;

import static com.github.tonivade.purefun.monad.IOOf.toIO;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Type;

import com.github.tonivade.purefun.core.Function1;
import com.github.tonivade.purefun.effect.Task;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.type.Try;
import com.github.tonivade.purefun.typeclasses.Instances;
import com.github.tonivade.purejson.PureJson;
import com.github.tonivade.zeromock.api.Bytes;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;

public class HttpClient {

  // using IO here because there's no instance for MonadDefer for Id
  private final HttpClientK<IO<?>> client;

  private HttpClient(HttpClientK<IO<?>> client) {
    this.client = requireNonNull(client);
  }

  public static HttpClient connectTo(String baseUrl) {
    return new HttpClient(new HttpClientK<>(baseUrl, Instances.async()));
  }

  public HttpResponse request(HttpRequest request) {
    return client.request(request).fix(toIO()).unsafeRunSync();
  }

  @SafeVarargs
  public static <T> Function1<HttpResponse, Try<T>> parse(T...reified) {
    return parse(HttpClientOf.getClassOf(reified));
  }

  public static <T> Function1<HttpResponse, Try<T>> parse(Class<T> type) {
    return parse((Type) type);
  }

  public static <T> Function1<HttpResponse, Try<T>> parse(Type type) {
    return response -> Task.fromTry(new PureJson<T>(type).fromJson(Bytes.asString(response.body())))
        .flatMap(Task::fromOption).safeRunSync();
  }
}
