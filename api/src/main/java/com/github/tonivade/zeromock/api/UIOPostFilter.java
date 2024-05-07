/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.effect.UIOOf;

public interface UIOPostFilter extends PostFilterK<UIO<?>> {

  @Override
  default UIO<HttpResponse> apply(HttpResponse value) {
    return PostFilterK.super.apply(value).fix(UIOOf::toUIO);
  }
}

