/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.concurrent.FutureOf.toFuture;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.type.Either;

public interface AsyncPreFilter extends PreFilterK<Future<?>> {

  @Override
  default Future<Either<HttpResponse, HttpRequest>> apply(HttpRequest value) {
    return PreFilterK.super.apply(value).fix(toFuture());
  }
}
