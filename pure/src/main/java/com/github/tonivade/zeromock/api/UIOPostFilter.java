/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.effect.UIO;

public interface UIOPostFilter extends PostFilterK<UIO.µ> {

  @Override
  default UIO<HttpResponse> apply(HttpResponse value) {
    return PostFilterK.super.apply(value).fix1(UIO::narrowK);
  }
}

