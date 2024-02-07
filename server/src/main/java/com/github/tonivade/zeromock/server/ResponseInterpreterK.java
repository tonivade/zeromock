/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.purefun.concurrent.FutureOf.toFuture;
import static com.github.tonivade.purefun.effect.UIOOf.toUIO;
import static com.github.tonivade.purefun.effect.URIOOf.toURIO;
import static com.github.tonivade.purefun.monad.IOOf.toIO;
import static com.github.tonivade.purefun.type.IdOf.toId;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.Witness;
import com.github.tonivade.purefun.concurrent.Future_;
import com.github.tonivade.purefun.concurrent.Promise;
import com.github.tonivade.purefun.effect.UIO_;
import com.github.tonivade.purefun.effect.URIO_;
import com.github.tonivade.purefun.monad.IO_;
import com.github.tonivade.purefun.type.Id_;
import com.github.tonivade.zeromock.api.HttpResponse;

@FunctionalInterface
public interface ResponseInterpreterK<F extends Witness> {

  Promise<HttpResponse> run(Kind<F, HttpResponse> response);

  static ResponseInterpreterK<IO_> io() {
    return response -> response.fix(toIO()).runAsync().toPromise();
  }

  static ResponseInterpreterK<UIO_> uio() {
    return response -> response.fix(toUIO()).runAsync().toPromise();
  }

  static <R> ResponseInterpreterK<Kind<URIO_, R>> urio(Producer<R> env) {
    return response -> response.fix(toURIO()).runAsync(env.get()).toPromise();
  }

  static ResponseInterpreterK<Id_> sync() {
    return response -> Promise.<HttpResponse>make().succeeded(response.fix(toId()).value());
  }

  static ResponseInterpreterK<Future_> async() {
    return response -> response.fix(toFuture()).toPromise();
  }
}
