/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.effect.URIO;
import com.github.tonivade.purefun.effect.URIOOf;
import com.github.tonivade.purefun.effect.URIO_;
import com.github.tonivade.purefun.type.Either;

public interface URIOPreFilter<R> extends PreFilterK<Kind<URIO_, R>> {

  @Override
  default URIO<R, Either<HttpResponse, HttpRequest>> apply(HttpRequest value) {
    return PreFilterK.super.apply(value).fix(URIOOf.toURIO());
  }
}
