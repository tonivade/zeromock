/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.instances.FutureInstances;

@FunctionalInterface
public interface AsyncRequestHandler extends RequestHandlerK<Future.µ> {

  @Override
  default Future<HttpResponse> apply(HttpRequest value) {
    return RequestHandlerK.super.apply(value).fix1(Future::narrowK);
  }

  default AsyncRequestHandler preHandle(PreFilter before) {
    return RequestHandlerK.super.preHandle(FutureInstances.monad(), before)::apply;
  }

  default AsyncRequestHandler postHandle(PostFilter after) {
    return RequestHandlerK.super.postHandle(FutureInstances.functor(), after).andThen(Future::narrowK)::apply;
  }
}
