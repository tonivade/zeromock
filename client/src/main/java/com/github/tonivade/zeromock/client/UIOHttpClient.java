/*
 * Copyright (c) 2018-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.client;

import static com.github.tonivade.purefun.effect.UIOOf.toUIO;
import static com.github.tonivade.purefun.typeclasses.Instances.async;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.effect.UIO_;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;

public class UIOHttpClient implements HttpClientOf<UIO_> {

  private final HttpClientK<UIO_> client;

  private UIOHttpClient(HttpClientK<UIO_> client) {
    this.client = requireNonNull(client);
  }

  public static UIOHttpClient connectTo(String baseUrl) {
    return new UIOHttpClient(new HttpClientK<>(baseUrl, async(UIO_.class)));
  }

  public UIO<HttpResponse> request(HttpRequest request) {
    return client.request(request).fix(toUIO());
  }
}
