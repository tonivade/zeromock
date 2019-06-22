/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.instances.IdInstances;
import com.github.tonivade.purefun.type.Id;

@FunctionalInterface
public interface SyncRequestHandler extends RequestHandlerK<Id.µ> {

  @Override
  Id<HttpResponse> apply(HttpRequest value);

  default SyncRequestHandler postHandle(Function1<HttpResponse, HttpResponse> after) {
    return postHandle(IdInstances.functor(), after).andThen(Id::narrowK)::apply;
  }
}
