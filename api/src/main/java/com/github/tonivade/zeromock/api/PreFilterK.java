/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.core.Function1;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.core.Matcher1;
import com.github.tonivade.purefun.Witness;
import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.purefun.typeclasses.Monad;

public interface PreFilterK<F extends Witness> extends Function1<HttpRequest, Kind<F, Either<HttpResponse, HttpRequest>>> {

  static <F extends Witness> PreFilterK<F> filter(Monad<F> monad, Matcher1<HttpRequest> matcher, RequestHandlerK<F> handler) {
    return request -> matcher.match(request) ?
        monad.map(handler.apply(request), Either::left) : monad.pure(Either.right(request));
  }
}
