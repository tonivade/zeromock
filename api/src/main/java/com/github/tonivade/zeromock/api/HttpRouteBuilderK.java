/*
 * Copyright (c) 2018-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.Witness;

public interface HttpRouteBuilderK<F extends Witness, T extends HttpRouteBuilderK<F, T, R>, R extends RequestHandlerK<F>> extends RouteBuilder<HttpRouteBuilderK.ThenStep<T, R>> {

  ThenStep<T, R> when(Matcher1<HttpRequest> matcher);
  
  @FunctionalInterface
  interface ThenStep<T, R> {

    T then(R handler);
  }
}
