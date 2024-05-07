/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.FutureOf;

public interface AsyncPostFilter extends PostFilterK<Future<?>> {

  @Override
  default Future<HttpResponse> apply(HttpResponse value) {
    return PostFilterK.super.apply(value).fix(FutureOf::toFuture);
  }
}

