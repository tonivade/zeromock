/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.instances.IOInstances;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.IOOf;
import com.github.tonivade.purefun.monad.IO_;

public interface IORequestHandler extends RequestHandlerK<IO_> {

  @Override
  default IO<HttpResponse> apply(HttpRequest value) {
    return RequestHandlerK.super.apply(value).fix(IOOf.toIO());
  }

  default IORequestHandler preHandle(IOPreFilter before) {
    return RequestHandlerK.super.preHandle(IOInstances.monad(), before)::apply;
  }

  default IORequestHandler postHandle(IOPostFilter after) {
    return RequestHandlerK.super.postHandle(IOInstances.monad(), after)::apply;
  }
}
