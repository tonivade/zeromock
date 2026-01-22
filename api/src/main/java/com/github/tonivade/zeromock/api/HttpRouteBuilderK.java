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

    public R then(Kind<F, HttpResponse> response) {
      return then(RequestHandlerK.cons(response));
    }

    public R lift(HttpResponse response) {
      return then(monad.pure(response));
    }

    public R lift(RequestHandler handler) {
      return then(handler.lift(monad));
    }

    public R ok(String body) {
      return lift(Handlers.ok(body));
    }

    public R created(String body) {
      return lift(Handlers.created(body));
    }

    public R error(String body) {
      return lift(Handlers.error(body));
    }

    public R noContent() {
      return lift(Handlers.noContent());
    }

    public R notFound() {
      return lift(Handlers.notFound());
    }

    public R forbidden() {
      return lift(Handlers.forbidden());
    }

    public R badRequest() {
      return lift(Handlers.badRequest());
    }

    public R unauthorized() {
      return lift(Handlers.unauthorized());
    }

    public R unavailable() {
      return lift(Handlers.unavailable());
    }

    public R error() {
      return lift(Handlers.error());
    }
  }
}
