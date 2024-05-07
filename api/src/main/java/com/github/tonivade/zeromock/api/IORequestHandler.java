/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.IOOf;
import com.github.tonivade.purefun.typeclasses.Instances;

public interface IORequestHandler extends RequestHandlerK<IO<?>> {

  @Override
  default IO<HttpResponse> apply(HttpRequest value) {
    return RequestHandlerK.super.apply(value).fix(IOOf::toIO);
  }

  default IORequestHandler preHandle(IOPreFilter before) {
    return RequestHandlerK.super.preHandle(Instances.monad(), before)::apply;
  }

  default IORequestHandler postHandle(IOPostFilter after) {
    return RequestHandlerK.super.postHandle(Instances.monad(), after)::apply;
  }
}
