/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.client;

import static java.util.Objects.requireNonNull;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.effect.ZIO;
import com.github.tonivade.purefun.effect.ZIOOf;
import com.github.tonivade.purefun.effect.ZIO_;
import com.github.tonivade.purefun.instances.ZIOInstances;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;

public class ZIOHttpClient<R> {

  private final HttpClientK<Kind<Kind<ZIO_, R>, Throwable>> client;

  public ZIOHttpClient(HttpClientK<Kind<Kind<ZIO_, R>, Throwable>> client) {
    this.client = requireNonNull(client);
  }

  public static <R> ZIOHttpClient<R> connectTo(String baseUrl) {
    return new ZIOHttpClient<>(new HttpClientK<>(baseUrl, ZIOInstances.monadDefer()));
  }

  public ZIO<R, Throwable, HttpResponse> request(HttpRequest request) {
    return client.request(request).fix(ZIOOf::narrowK);
  }
}
