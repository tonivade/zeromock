/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.IOOf;

public interface IOPostFilter extends PostFilterK<IO<?>> {

  @Override
  default IO<HttpResponse> apply(HttpResponse value) {
    return PostFilterK.super.apply(value).fix(IOOf::toIO);
  }
}

