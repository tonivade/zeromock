/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.instances.FutureInstances;
import com.github.tonivade.purefun.instances.IdInstances;
import com.github.tonivade.purefun.type.Id;

@FunctionalInterface
public interface SyncRequestHandler extends RequestHandlerK<Id.µ> {

  @Override
  default Id<HttpResponse> apply(HttpRequest value) {
    return RequestHandlerK.super.apply(value).fix1(Id::narrowK);
  }

  default SyncRequestHandler preHandle(PreFilter before) {
    return RequestHandlerK.super.preHandle(IdInstances.monad(), before)::apply;
  }

  default SyncRequestHandler postHandle(PostFilter after) {
    return RequestHandlerK.super.postHandle(IdInstances.functor(), after)::apply;
  }
}
