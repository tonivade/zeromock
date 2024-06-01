/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.client;

import static java.util.Objects.requireNonNull;
import com.github.tonivade.purefun.core.Function1;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.effect.UIOOf;
import com.github.tonivade.purefun.typeclasses.Instances;
import com.github.tonivade.purejson.PureJson;
import com.github.tonivade.zeromock.api.Bytes;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import java.lang.reflect.Type;

public class UIOHttpClient implements HttpClientOf<UIO<?>> {

  private final HttpClientK<UIO<?>> client;

  private UIOHttpClient(HttpClientK<UIO<?>> client) {
    this.client = requireNonNull(client);
  }

  public static UIOHttpClient connectTo(String baseUrl) {
    return new UIOHttpClient(new HttpClientK<>(baseUrl, Instances.<UIO<?>>async()));
  }

  @Override
  public UIO<HttpResponse> request(HttpRequest request) {
    return client.request(request).fix(UIOOf::toUIO);
  }

  @SafeVarargs
  public static <T> Function1<HttpResponse, UIO<T>> parse(T... reified) {
    return parse(HttpClientOf.getClassOf(reified));
  }

  public static <T> Function1<HttpResponse, UIO<T>> parse(Class<T> type) {
    return parse((Type) type);
  }

  public static <T> Function1<HttpResponse, UIO<T>> parse(Type type) {
    return response -> UIO.fromTry(new PureJson<T>(type).fromJson(Bytes.asString(response.body())))
        .flatMap(UIO::fromOption);
  }
}
