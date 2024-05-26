/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.effect.URIO;
import com.github.tonivade.purefun.effect.URIOOf;
import com.github.tonivade.purefun.typeclasses.Instances;

public interface URIORequestHandler<R> extends RequestHandlerK<URIO<R, ?>> {

  @Override
  default URIO<R, HttpResponse> apply(HttpRequest value) {
    return RequestHandlerK.super.apply(value).fix(URIOOf::toURIO);
  }

  default URIORequestHandler<R> preHandle(URIOPreFilter<R> before) {
    return RequestHandlerK.super.preHandle(Instances.<URIO<R, ?>>monad(), before)::apply;
  }

  default URIORequestHandler<R> postHandle(URIOPostFilter<R> after) {
    return RequestHandlerK.super.postHandle(Instances.<URIO<R, ?>>monad(), after)::apply;
  }
}
