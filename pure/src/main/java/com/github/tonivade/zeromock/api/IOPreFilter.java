/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.type.Either;

public interface IOPreFilter extends PreFilterK<IO.µ> {

  @Override
  default IO<Either<HttpResponse, HttpRequest>> apply(HttpRequest value) {
    return PreFilterK.super.apply(value).fix1(IO::narrowK);
  }
}
