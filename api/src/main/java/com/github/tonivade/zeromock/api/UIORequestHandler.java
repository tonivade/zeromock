/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.effect.UIOOf;
import com.github.tonivade.purefun.typeclasses.Instances;

public interface UIORequestHandler extends RequestHandlerK<UIO<?>> {

  @Override
  default UIO<HttpResponse> apply(HttpRequest value) {
    return RequestHandlerK.super.apply(value).fix(UIOOf::toUIO);
  }

  default UIORequestHandler preHandle(UIOPreFilter before) {
    return RequestHandlerK.super.preHandle(Instances.monad(), before)::apply;
  }

  default UIORequestHandler postHandle(UIOPostFilter after) {
    return RequestHandlerK.super.postHandle(Instances.monad(), after)::apply;
  }
}
