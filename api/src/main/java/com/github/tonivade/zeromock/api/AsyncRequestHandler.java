/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.FutureOf;
import com.github.tonivade.purefun.typeclasses.Instances;

public interface AsyncRequestHandler extends RequestHandlerK<Future<?>> {

  @Override
  default Future<HttpResponse> apply(HttpRequest value) {
    return RequestHandlerK.super.apply(value).fix(FutureOf::toFuture);
  }

  default AsyncRequestHandler preHandle(AsyncPreFilter before) {
    return RequestHandlerK.super.preHandle(Instances.monad(), before)::apply;
  }

  default AsyncRequestHandler postHandle(AsyncPostFilter after) {
    return RequestHandlerK.super.postHandle(Instances.monad(), after)::apply;
  }
}
