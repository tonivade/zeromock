/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.monad.IOOf.toIO;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.IO_;

public interface IOPostFilter extends PostFilterK<IO_> {

  @Override
  default IO<HttpResponse> apply(HttpResponse value) {
    return PostFilterK.super.apply(value).fix(toIO());
  }
}

