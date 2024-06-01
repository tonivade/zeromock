/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.core.Function1.identity;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.core.Function1;
import com.github.tonivade.purefun.typeclasses.Monad;

@FunctionalInterface
public interface RequestHandler extends Function1<HttpRequest, HttpResponse> {

  default RequestHandler preHandle(PreFilter before) {
    return request -> before.apply(request).fold(identity(), this::apply);
  }

  default RequestHandler postHandle(PostFilter after) {
    return andThen(after)::apply;
  }

  default <F extends Kind<F, ?>> RequestHandlerK<F> lift(Monad<F> monad) {
    return andThen(monad::pure)::apply;
  }
}
