/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.instances.IOInstances;
import com.github.tonivade.purefun.monad.IO;

public interface IORequestHandler extends RequestHandlerK<IO.µ> {

  @Override
  default IO<HttpResponse> apply(HttpRequest value) {
    return RequestHandlerK.super.apply(value).fix1(IO::narrowK);
  }

  default IORequestHandler preHandle(PreFilter before) {
    return RequestHandlerK.super.preHandle(IOInstances.monad(), before)::apply;
  }

  default IORequestHandler postHandle(PostFilter after) {
    return postHandle(IOInstances.functor(), after).andThen(IO::narrowK)::apply;
  }
}
