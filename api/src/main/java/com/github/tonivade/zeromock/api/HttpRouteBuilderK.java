/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.Precondition.checkNonNull;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.Witness;
import com.github.tonivade.purefun.typeclasses.Monad;

public interface HttpRouteBuilderK<F extends Witness, R extends HttpRouteBuilderK<F, R>> extends RouteBuilder<HttpRouteBuilderK.ThenStepK<F, R>> {

  ThenStepK<F, R> when(Matcher1<HttpRequest> matcher);
  
  class ThenStepK<F extends Witness, R extends HttpRouteBuilderK<F, R>> {
    
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
      return then(Handlers.ok(body).lift(monad()));
    }
    
    public R created(String body) {
      return then(Handlers.created(body).lift(monad()));
    }
    
    public R error(String body) {
      return then(Handlers.error(body).lift(monad()));
    }
    
    public R noContent() {
      return then(Handlers.noContent().lift(monad()));
    }
    
    public R notFound() {
      return then(Handlers.notFound().lift(monad()));
    }
    
    public R forbidden() {
      return then(Handlers.forbidden().lift(monad()));
    }
    
    public R badRequest() {
      return then(Handlers.badRequest().lift(monad()));
    }
    
    public R unauthorized() {
      return then(Handlers.unauthorized().lift(monad()));
    }
    
    public R unavailable() {
      return then(Handlers.unavailable().lift(monad()));
    }
    
    public R error() {
      return then(Handlers.error().lift(monad()));
    }
  }
}
