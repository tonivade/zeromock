/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.concurrent.FutureOf.toFuture;
import com.github.tonivade.purefun.concurrent.Future;

public interface AsyncPostFilter extends PostFilterK<Future<?>> {

  @Override
  default Future<HttpResponse> apply(HttpResponse value) {
    return PostFilterK.super.apply(value).fix(toFuture());
  }
}

