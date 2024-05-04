/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.monad.IOOf.toIO;

import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.type.Either;

public interface IOPreFilter extends PreFilterK<IO<?>> {

  @Override
  default IO<Either<HttpResponse, HttpRequest>> apply(HttpRequest value) {
    return PreFilterK.super.apply(value).fix(toIO());
  }
}
