/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.core.Function1;
import com.github.tonivade.purefun.Kind;

import com.github.tonivade.purefun.typeclasses.Monad;

@FunctionalInterface
public interface RequestHandlerK<F> extends Function1<HttpRequest, Kind<F, HttpResponse>> {

  default RequestHandlerK<F> preHandle(Monad<F> monad, PreFilterK<F> before) {
    return request ->
        monad.flatMap(before.apply(request), either -> either.fold(monad::<HttpResponse>pure, this::apply));
  }

  default RequestHandlerK<F> postHandle(Monad<F> monad, PostFilterK<F> after) {
    return andThen(value -> monad.flatMap(value, after))::apply;
  }
}
