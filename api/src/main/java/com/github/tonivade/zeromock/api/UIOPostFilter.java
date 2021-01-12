/*
 * Copyright (c) 2018-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.effect.UIOOf.toUIO;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.effect.UIO_;

public interface UIOPostFilter extends PostFilterK<UIO_> {

  @Override
  default UIO<HttpResponse> apply(HttpResponse value) {
    return PostFilterK.super.apply(value).fix(toUIO());
  }
}

