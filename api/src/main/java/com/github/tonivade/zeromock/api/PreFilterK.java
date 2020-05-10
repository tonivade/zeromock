/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Higher1;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.purefun.typeclasses.Monad;

public interface PreFilterK<F extends Kind> extends Function1<HttpRequest, Higher1<F, Either<HttpResponse, HttpRequest>>> {

  static <F extends Kind> PreFilterK<F> filter(Monad<F> monad, Matcher1<HttpRequest> matcher, RequestHandlerK<F> handler) {
    return request -> matcher.match(request) ?
        monad.map(handler.apply(request), Either::left) : monad.pure(Either.right(request));
  }
}
