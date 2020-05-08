/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.type.Either;

public interface PreFilter extends Function1<HttpRequest, Either<HttpResponse, HttpRequest>> {

  static PreFilter filter(Matcher1<HttpRequest> matcher, RequestHandler handler) {
    return request -> matcher.match(request) ?
        Either.left(handler.apply(request)) : Either.right(request);
  }

}
