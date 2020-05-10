/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.PostFilterK;

public interface IOPostFilter extends PostFilterK<IO.µ> {

  @Override
  default IO<HttpResponse> apply(HttpResponse value) {
    return PostFilterK.super.apply(value).fix1(IO::narrowK);
  }
}

