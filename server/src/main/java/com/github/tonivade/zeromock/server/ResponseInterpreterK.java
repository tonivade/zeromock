/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.FutureOf;
import com.github.tonivade.purefun.concurrent.Promise;
import com.github.tonivade.purefun.core.Producer;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.effect.UIOOf;
import com.github.tonivade.purefun.effect.URIO;
import com.github.tonivade.purefun.effect.URIOOf;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.IOOf;
import com.github.tonivade.purefun.type.Id;
import com.github.tonivade.purefun.type.IdOf;
import com.github.tonivade.zeromock.api.HttpResponse;

@FunctionalInterface
public interface ResponseInterpreterK<F> {

  Promise<HttpResponse> run(Kind<F, HttpResponse> response);

  static ResponseInterpreterK<IO<?>> io() {
    return response -> response.fix(IOOf::<HttpResponse>toIO).runAsync().toPromise();
  }

  static ResponseInterpreterK<UIO<?>> uio() {
    return response -> response.fix(UIOOf::<HttpResponse>toUIO).runAsync().toPromise();
  }

  static <R> ResponseInterpreterK<URIO<R, ?>> urio(Producer<R> env) {
    return response -> response.fix(URIOOf::<R, HttpResponse>toURIO).runAsync(env.get()).toPromise();
  }

  static ResponseInterpreterK<Id<?>> sync() {
    return response -> Promise.<HttpResponse>make().succeeded(response.fix(IdOf::toId).value());
  }

  static ResponseInterpreterK<Future<?>> async() {
    return response -> response.fix(FutureOf::<HttpResponse>toFuture).toPromise();
  }
}
