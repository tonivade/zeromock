/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.client;

import com.github.tonivade.purefun.instances.IOInstances;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;

import static java.util.Objects.requireNonNull;

public class IOHttpClient {

  private final HttpClientK<IO.µ> client;

  public IOHttpClient(HttpClientK<IO.µ> client) {
    this.client = requireNonNull(client);
  }

  public static IOHttpClient connectTo(String baseUrl) {
    return new IOHttpClient(new HttpClientK<>(baseUrl, IOInstances.monadDefer()));
  }

  public IO<HttpResponse> request(HttpRequest request) {
    return client.request(request).fix1(IO::narrowK);
  }
}
