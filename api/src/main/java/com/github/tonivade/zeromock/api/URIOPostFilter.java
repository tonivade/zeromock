/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.effect.URIO;
import com.github.tonivade.purefun.effect.URIOOf;

public interface URIOPostFilter<R> extends PostFilterK<Kind<URIO<?, ?>, R>> {

  @Override
  default URIO<R, HttpResponse> apply(HttpResponse value) {
    return PostFilterK.super.apply(value).fix(URIOOf::toURIO);
  }
}

