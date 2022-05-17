/*
 * Copyright (c) 2018-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.client;

import static com.github.tonivade.purefun.typeclasses.Instances.async;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.IOOf;
import com.github.tonivade.purefun.monad.IO_;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;

public class IOHttpClient implements HttpClientOf<IO_> {

  private final HttpClientK<IO_> client;

  private IOHttpClient(HttpClientK<IO_> client) {
    this.client = requireNonNull(client);
  }

  public static IOHttpClient connectTo(String baseUrl) {
    return new IOHttpClient(new HttpClientK<>(baseUrl, async(IO_.class)));
  }

  public IO<HttpResponse> request(HttpRequest request) {
    return client.request(request).fix(IOOf.toIO());
  }
}
