/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Higher1;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.typeclasses.Applicative;
import com.github.tonivade.purefun.typeclasses.Functor;

@FunctionalInterface
public interface RequestHandlerK<F extends Kind> extends Function1<HttpRequest, Higher1<F, HttpResponse>> {

  default RequestHandlerK<F> preHandle(Applicative<F> applicative, PreFilter before) {
    return request -> before.apply(request).fold(applicative::pure, this::apply);
  }

  default RequestHandlerK<F> postHandle(Functor<F> functor, PostFilter after) {
    return andThen(value -> functor.map(value, after))::apply;
  }
}
