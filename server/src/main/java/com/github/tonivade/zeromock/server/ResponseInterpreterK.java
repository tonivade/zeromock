/*
 * Copyright (c) 2018-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.purefun.concurrent.FutureOf.toFuture;
import static com.github.tonivade.purefun.type.IdOf.toId;

import java.util.concurrent.Executor;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Witness;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.Future_;
import com.github.tonivade.purefun.concurrent.Promise;
import com.github.tonivade.purefun.type.Id_;
import com.github.tonivade.purefun.typeclasses.Runtime;
import com.github.tonivade.zeromock.api.HttpResponse;

@FunctionalInterface
public interface ResponseInterpreterK<F extends Witness> {
  
  Promise<HttpResponse> run(Kind<F, HttpResponse> response);
  
  static <F extends Witness> ResponseInterpreterK<F> async(Runtime<F> runtime) {
    return async(runtime, Future.DEFAULT_EXECUTOR);
  }

  static <F extends Witness> ResponseInterpreterK<F> async(Runtime<F> runtime, Executor executor) {
    return response -> runtime.parRun(response, executor).toPromise();
  }

  static ResponseInterpreterK<Id_> sync() {
    return response -> Promise.<HttpResponse>make().succeeded(response.fix(toId()).get());
  }

  static ResponseInterpreterK<Future_> async() {
    return response -> response.fix(toFuture()).toPromise();
  }
}
