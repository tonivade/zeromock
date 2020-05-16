/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.client;

import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.effect.UIO_;
import com.github.tonivade.purefun.instances.UIOInstances;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;

import static java.util.Objects.requireNonNull;

public class UIOHttpClient {

  private final HttpClientK<UIO_> client;

  public UIOHttpClient(HttpClientK<UIO_> client) {
    this.client = requireNonNull(client);
  }

  public static UIOHttpClient connectTo(String baseUrl) {
    return new UIOHttpClient(new HttpClientK<>(baseUrl, UIOInstances.monadDefer()));
  }

  public UIO<HttpResponse> request(HttpRequest request) {
    return client.request(request).fix1(UIO_::narrowK);
  }
}
