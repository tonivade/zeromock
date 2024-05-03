/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.monad.IOOf.toIO;

import com.github.tonivade.purefun.monad.IO;

public interface IOPostFilter extends PostFilterK<IO<?>> {

  @Override
  default IO<HttpResponse> apply(HttpResponse value) {
    return PostFilterK.super.apply(value).fix(toIO());
  }
}

