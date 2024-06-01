/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.core.Function1;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.core.Matcher1;

import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.purefun.typeclasses.Monad;

public interface PreFilterK<F extends Kind<F, ?>> extends Function1<HttpRequest, Kind<F, Either<HttpResponse, HttpRequest>>> {

  static <F extends Kind<F, ?>> PreFilterK<F> filter(Monad<F> monad, Matcher1<HttpRequest> matcher, RequestHandlerK<F> handler) {
    return request -> matcher.match(request) ?
        monad.map(handler.apply(request), Either::left) : monad.pure(Either.right(request));
  }
}
