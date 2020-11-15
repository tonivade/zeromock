/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.effect.URIOOf.toURIO;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.effect.URIO;
import com.github.tonivade.purefun.effect.URIO_;

public interface URIOPostFilter<R> extends PostFilterK<Kind<URIO_, R>> {

  @Override
  default URIO<R, HttpResponse> apply(HttpResponse value) {
    return PostFilterK.super.apply(value).fix(toURIO());
  }
}

