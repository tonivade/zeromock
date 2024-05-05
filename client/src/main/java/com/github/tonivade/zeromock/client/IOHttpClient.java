/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.client;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Type;

import com.github.tonivade.purefun.core.Function1;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.IOOf;
import com.github.tonivade.purefun.typeclasses.Instances;
import com.github.tonivade.purejson.PureJson;
import com.github.tonivade.zeromock.api.Bytes;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;

public class IOHttpClient implements HttpClientOf<IO<?>> {

  private final HttpClientK<IO<?>> client;

  private IOHttpClient(HttpClientK<IO<?>> client) {
    this.client = requireNonNull(client);
  }

  public static IOHttpClient connectTo(String baseUrl) {
    return new IOHttpClient(new HttpClientK<>(baseUrl, Instances.async()));
  }

  @Override
  public IO<HttpResponse> request(HttpRequest request) {
    return client.request(request).fix(IOOf.toIO());
  }

  @SafeVarargs
  public static <T> Function1<HttpResponse, IO<T>> parse(T...reified) {
    return parse(HttpClientOf.getClassOf(reified));
  }

  public static <T> Function1<HttpResponse, IO<T>> parse(Class<T> type) {
    return parse((Type) type);
  }

  public static <T> Function1<HttpResponse, IO<T>> parse(Type type) {
    return response -> IO.fromTry(new PureJson<T>(type).fromJson(Bytes.asString(response.body())))
        .flatMap(IO::fromOption);
  }
}
