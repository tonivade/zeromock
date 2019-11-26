/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Higher1;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Operator1;
import com.github.tonivade.purefun.typeclasses.Functor;

@FunctionalInterface
public interface RequestHandlerK<F extends Kind> extends Function1<HttpRequest, Higher1<F, HttpResponse>> {

  default RequestHandlerK<F> preHandle(Operator1<HttpRequest> before) {
    return compose(before)::apply;
  }

  default RequestHandlerK<F> postHandle(Functor<F> functor, Operator1<HttpResponse> after) {
    return andThen(value -> functor.map(value, after))::apply;
  }
}
