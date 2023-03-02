/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.client;

import static com.github.tonivade.purefun.typeclasses.Instances.async;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Type;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.IOOf;
import com.github.tonivade.purefun.monad.IO_;
import com.github.tonivade.purejson.PureJson;
import com.github.tonivade.zeromock.api.Bytes;
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
  
  public static <T> Function1<HttpResponse, IO<T>> parse(Class<T> type) {
    return parse((Type) type);
  }

  public static <T> Function1<HttpResponse, IO<T>> parse(Type type) {
    return response -> IO.fromTry(new PureJson<T>(type).fromJson(Bytes.asString(response.body())))
        .flatMap(IO::fromOption);
  }
}
