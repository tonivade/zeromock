/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.effect.UIOOf.toUIO;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.instances.UIOInstances;

public interface UIORequestHandler extends RequestHandlerK<UIO<?>> {

  @Override
  default UIO<HttpResponse> apply(HttpRequest value) {
    return RequestHandlerK.super.apply(value).fix(toUIO());
  }

  default UIORequestHandler preHandle(UIOPreFilter before) {
    return RequestHandlerK.super.preHandle(UIOInstances.monad(), before)::apply;
  }

  default UIORequestHandler postHandle(UIOPostFilter after) {
    return RequestHandlerK.super.postHandle(UIOInstances.monad(), after)::apply;
  }
}
