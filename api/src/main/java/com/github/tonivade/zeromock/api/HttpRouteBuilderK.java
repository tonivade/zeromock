/*
 * Copyright (c) 2018-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.core.Precondition.checkNonNull;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.core.Function1;
import com.github.tonivade.purefun.core.Matcher1;
import com.github.tonivade.purefun.typeclasses.Monad;

public interface HttpRouteBuilderK<F extends Kind<F, ?>, R extends HttpRouteBuilderK<F, R>> extends RouteBuilder<HttpRouteBuilderK.ThenStepK<F, R>> {

  @Override
  ThenStepK<F, R> when(Matcher1<HttpRequest> matcher);

  class ThenStepK<F extends Kind<F, ?>, R extends HttpRouteBuilderK<F, R>> {

    private final Monad<F> monad;
    private final Function1<RequestHandlerK<F>, R> then;

    public ThenStepK(Monad<F> monad, Function1<RequestHandlerK<F>, R> then) {
      this.monad = checkNonNull(monad);
      this.then = checkNonNull(then);
    }

    public Monad<F> monad() {
      return monad;
    }

    public R then(RequestHandlerK<F> handler) {
      return then.apply(handler);
    }

    public R ok(String body) {
      return then(Handlers.ok(body).lift(monad));
    }

    public R created(String body) {
      return then(Handlers.created(body).lift(monad));
    }

    public R error(String body) {
      return then(Handlers.error(body).lift(monad));
    }

    public R noContent() {
      return then(Handlers.noContent().lift(monad));
    }

    public R notFound() {
      return then(Handlers.notFound().lift(monad));
    }

    public R forbidden() {
      return then(Handlers.forbidden().lift(monad));
    }

    public R badRequest() {
      return then(Handlers.badRequest().lift(monad));
    }

    public R unauthorized() {
      return then(Handlers.unauthorized().lift(monad));
    }

    public R unavailable() {
      return then(Handlers.unavailable().lift(monad));
    }

    public R error() {
      return then(Handlers.error().lift(monad));
    }
  }
}
