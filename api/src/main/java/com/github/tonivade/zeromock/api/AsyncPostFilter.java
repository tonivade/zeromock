/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.concurrent.FutureOf.toFuture;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.FutureOf;
import com.github.tonivade.purefun.concurrent.Future_;

public interface AsyncPostFilter extends PostFilterK<Future_> {

  @Override
  default Future<HttpResponse> apply(HttpResponse value) {
    return PostFilterK.super.apply(value).fix(toFuture());
  }
}

