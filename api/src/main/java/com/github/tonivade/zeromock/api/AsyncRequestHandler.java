/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.concurrent.FutureOf.toFuture;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.Future_;
import com.github.tonivade.purefun.instances.FutureInstances;

@FunctionalInterface
public interface AsyncRequestHandler extends RequestHandlerK<Future_> {

  @Override
  default Future<HttpResponse> apply(HttpRequest value) {
    return RequestHandlerK.super.apply(value).fix(toFuture());
  }

  default AsyncRequestHandler preHandle(AsyncPreFilter before) {
    return RequestHandlerK.super.preHandle(FutureInstances.monad(), before)::apply;
  }

  default AsyncRequestHandler postHandle(AsyncPostFilter after) {
    return RequestHandlerK.super.postHandle(FutureInstances.monad(), after)::apply;
  }
}
