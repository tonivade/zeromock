/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.effect.URIOOf.toURIO;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.effect.URIO;

public interface URIOPostFilter<R> extends PostFilterK<Kind<URIO<?, ?>, R>> {

  @Override
  default URIO<R, HttpResponse> apply(HttpResponse value) {
    return PostFilterK.super.apply(value).fix(toURIO());
  }
}

