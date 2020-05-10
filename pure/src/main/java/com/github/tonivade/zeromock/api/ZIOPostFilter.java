/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Higher1;
import com.github.tonivade.purefun.Nothing;
import com.github.tonivade.purefun.effect.ZIO;

public interface ZIOPostFilter<R> extends PostFilterK<Higher1<Higher1<ZIO.µ, R>, Nothing>> {

  @Override
  default ZIO<R, Nothing, HttpResponse> apply(HttpResponse value) {
    return PostFilterK.super.apply(value).fix1(ZIO::narrowK);
  }
}

