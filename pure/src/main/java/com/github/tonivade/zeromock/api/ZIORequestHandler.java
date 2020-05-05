/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Higher1;
import com.github.tonivade.purefun.Nothing;
import com.github.tonivade.purefun.effect.ZIO;
import com.github.tonivade.purefun.instances.ZIOInstances;

public interface ZIORequestHandler<R> extends RequestHandlerK<Higher1<Higher1<ZIO.µ, R>, Nothing>> {

  @Override
  default ZIO<R, Nothing, HttpResponse> apply(HttpRequest value) {
    return RequestHandlerK.super.apply(value).fix1(ZIO::narrowK);
  }

  default ZIORequestHandler<R> preHandle(PreFilter before) {
    return RequestHandlerK.super.preHandle(ZIOInstances.<R, Nothing>monad(), before)::apply;
  }

  default ZIORequestHandler<R> postHandle(PostFilter after) {
    return postHandle(ZIOInstances.functor(), after).andThen(ZIO::narrowK)::apply;
  }
}
