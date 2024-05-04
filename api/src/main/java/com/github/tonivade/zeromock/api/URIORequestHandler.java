/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.effect.URIOOf.toURIO;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.effect.URIO;
import com.github.tonivade.purefun.instances.URIOInstances;

public interface URIORequestHandler<R> extends RequestHandlerK<Kind<URIO<?, ?>, R>> {

  @Override
  default URIO<R, HttpResponse> apply(HttpRequest value) {
    return RequestHandlerK.super.apply(value).fix(toURIO());
  }

  default URIORequestHandler<R> preHandle(URIOPreFilter<R> before) {
    return RequestHandlerK.super.preHandle(URIOInstances.monad(), before)::apply;
  }

  default URIORequestHandler<R> postHandle(URIOPostFilter<R> after) {
    return RequestHandlerK.super.postHandle(URIOInstances.monad(), after)::apply;
  }
}
