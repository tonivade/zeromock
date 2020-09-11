/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Nothing;
import com.github.tonivade.purefun.effect.ZIO;
import com.github.tonivade.purefun.effect.ZIOOf;
import com.github.tonivade.purefun.effect.ZIO_;

public interface ZIOPostFilter<R> extends PostFilterK<Kind<Kind<ZIO_, R>, Nothing>> {

  @Override
  default ZIO<R, Nothing, HttpResponse> apply(HttpResponse value) {
    return PostFilterK.super.apply(value).fix(ZIOOf::narrowK);
  }
}

