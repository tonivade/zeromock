/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.purefun.concurrent.FutureOf.toFuture;
import static com.github.tonivade.purefun.effect.UIOOf.toUIO;
import static com.github.tonivade.purefun.effect.URIOOf.toURIO;
import static com.github.tonivade.purefun.monad.IOOf.toIO;
import static com.github.tonivade.purefun.type.IdOf.toId;

import java.util.concurrent.Executor;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.Witness;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.Future_;
import com.github.tonivade.purefun.concurrent.Promise;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.effect.UIO_;
import com.github.tonivade.purefun.effect.URIO;
import com.github.tonivade.purefun.effect.URIO_;
import com.github.tonivade.purefun.instances.FutureInstances;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.IO_;
import com.github.tonivade.purefun.type.Id;
import com.github.tonivade.purefun.type.Id_;
import com.github.tonivade.zeromock.api.HttpResponse;

@FunctionalInterface
public interface ResponseInterpreterK<F extends Witness> {
  
  Promise<HttpResponse> run(Kind<F, HttpResponse> response);
  
  static ResponseInterpreterK<Future_> async() {
    return response -> {
      Future<HttpResponse> future = response.fix(toFuture());
      return future.toPromise();
    };
  }
  
  static ResponseInterpreterK<Id_> sync() {
    return response -> {
      Id<HttpResponse> value = response.fix(toId());
      return Promise.<HttpResponse>make().succeeded(value.get());
    };
  }
  
  static ResponseInterpreterK<IO_> ioSync() {
    return response -> {
      IO<HttpResponse> value = response.fix(toIO());
      return Promise.<HttpResponse>make().succeeded(value.unsafeRunSync());
    };
  }
  
  static ResponseInterpreterK<IO_> ioAsync(Executor executor) {
    return response -> {
      IO<HttpResponse> value = response.fix(toIO());
      Kind<Future_, HttpResponse> future = value.foldMap(FutureInstances.async(executor));
      return future.fix(toFuture()).toPromise();
    };
  }
  
  static ResponseInterpreterK<UIO_> uioSync() {
    return response -> {
      UIO<HttpResponse> value = response.fix(toUIO());
      return Promise.<HttpResponse>make().succeeded(value.unsafeRunSync());
    };
  }
  
  static ResponseInterpreterK<UIO_> uioAsync(Executor executor) {
    return response -> {
      UIO<HttpResponse> value = response.fix(toUIO());
      Kind<Future_, HttpResponse> future = value.foldMap(FutureInstances.async(executor));
      return future.fix(toFuture()).toPromise();
    };
  }
  
  static <R> ResponseInterpreterK<Kind<URIO_, R>> urioSync(Producer<R> factory) {
    return response -> {
      URIO<R, HttpResponse> future = response.fix(toURIO());
      return Promise.<HttpResponse>make().succeeded(future.unsafeRunSync(factory.get()));
    };
  }
  
  static <R> ResponseInterpreterK<Kind<URIO_, R>> urioAsync(
      Producer<R> factory, Executor executor) {
    return response -> {
      URIO<R, HttpResponse> effect = response.fix(toURIO());
      Kind<Future_, HttpResponse> future = effect.foldMap(factory.get(), FutureInstances.async(executor));
      return future.fix(toFuture()).toPromise();
    };
  }
}
