/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.client;

import static com.github.tonivade.purefun.monad.IOOf.toIO;
import static java.util.Objects.requireNonNull;
import com.github.tonivade.purefun.instances.IOInstances;
import com.github.tonivade.purefun.monad.IO_;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;

public class HttpClient {

  // using IO here because there's no instance for MonadDefer for Id
  private final HttpClientK<IO_> client;

  public HttpClient(HttpClientK<IO_> client) {
    this.client = requireNonNull(client);
  }

  public static HttpClient connectTo(String baseUrl) {
    return new HttpClient(new HttpClientK<>(baseUrl, IOInstances.monadDefer()));
  }

  public HttpResponse request(HttpRequest request) {
    return client.request(request).fix(toIO()).unsafeRunSync();
  }
}
