/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.purefun.concurrent.FutureOf.toFuture;
import static com.github.tonivade.purefun.effect.UIOOf.toUIO;
import static com.github.tonivade.purefun.effect.URIOOf.toURIO;
import static com.github.tonivade.purefun.monad.IOOf.toIO;
import static com.github.tonivade.purefun.type.IdOf.toId;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.Promise;
import com.github.tonivade.purefun.core.Producer;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.effect.URIO;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.type.Id;
import com.github.tonivade.zeromock.api.HttpResponse;

@FunctionalInterface
public interface ResponseInterpreterK<F> {

  Promise<HttpResponse> run(Kind<F, HttpResponse> response);

  static ResponseInterpreterK<IO<?>> io() {
    return response -> response.fix(toIO()).runAsync().toPromise();
  }

  static ResponseInterpreterK<UIO<?>> uio() {
    return response -> response.fix(toUIO()).runAsync().toPromise();
  }

  static <R> ResponseInterpreterK<Kind<URIO<?, ?>, R>> urio(Producer<R> env) {
    return response -> response.fix(toURIO()).runAsync(env.get()).toPromise();
  }

  static ResponseInterpreterK<Id<?>> sync() {
    return response -> Promise.<HttpResponse>make().succeeded(response.fix(toId()).value());
  }

  static ResponseInterpreterK<Future<?>> async() {
    return response -> response.fix(toFuture()).toPromise();
  }
}
