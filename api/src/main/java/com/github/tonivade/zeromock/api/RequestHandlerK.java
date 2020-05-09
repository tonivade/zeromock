/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Higher1;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.typeclasses.Functor;
import com.github.tonivade.purefun.typeclasses.Monad;

@FunctionalInterface
public interface RequestHandlerK<F extends Kind> extends Function1<HttpRequest, Higher1<F, HttpResponse>> {

  default RequestHandlerK<F> preHandle(Monad<F> monad, PreFilterK<F> before) {
    return request ->
        monad.flatMap(before.apply(request), either -> either.fold(monad::<HttpResponse>pure, this::apply));
  }

  default RequestHandlerK<F> postHandle(Functor<F> functor, PostFilter after) {
    return andThen(value -> functor.map(value, after))::apply;
  }
}
