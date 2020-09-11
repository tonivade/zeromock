/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import java.util.concurrent.Executor;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Nothing;
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.Witness;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.FutureOf;
import com.github.tonivade.purefun.concurrent.Future_;
import com.github.tonivade.purefun.concurrent.Promise;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.effect.UIOOf;
import com.github.tonivade.purefun.effect.UIO_;
import com.github.tonivade.purefun.effect.ZIO;
import com.github.tonivade.purefun.effect.ZIOOf;
import com.github.tonivade.purefun.effect.ZIO_;
import com.github.tonivade.purefun.instances.FutureInstances;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.IOOf;
import com.github.tonivade.purefun.monad.IO_;
import com.github.tonivade.purefun.type.Id;
import com.github.tonivade.purefun.type.IdOf;
import com.github.tonivade.purefun.type.Id_;
import com.github.tonivade.zeromock.api.HttpResponse;

@FunctionalInterface
public interface ResponseInterpreterK<F extends Witness> {
  
  Promise<HttpResponse> run(Kind<F, HttpResponse> response);
  
  static ResponseInterpreterK<Future_> async() {
    return response -> {
      Future<HttpResponse> future = response.fix(FutureOf::narrowK);
      return future.toPromise();
    };
  }
  
  static ResponseInterpreterK<Id_> sync() {
    return response -> {
      Id<HttpResponse> value = response.fix(IdOf::narrowK);
      return Promise.<HttpResponse>make().succeeded(value.get());
    };
  }
  
  static ResponseInterpreterK<IO_> ioSync() {
    return response -> {
      IO<HttpResponse> value = response.fix(IOOf::narrowK);
      return Promise.<HttpResponse>make().succeeded(value.unsafeRunSync());
    };
  }
  
  static ResponseInterpreterK<IO_> ioAsync(Executor executor) {
    return response -> {
      IO<HttpResponse> value = response.fix(IOOf::narrowK);
      Kind<Future_, HttpResponse> future = value.foldMap(FutureInstances.monadDefer(executor));
      return future.fix(FutureOf::narrowK).toPromise();
    };
  }
  
  static ResponseInterpreterK<UIO_> uioSync() {
    return response -> {
      UIO<HttpResponse> value = response.fix(UIOOf::narrowK);
      return Promise.<HttpResponse>make().succeeded(value.unsafeRunSync());
    };
  }
  
  static ResponseInterpreterK<UIO_> uioAsync(Executor executor) {
    return response -> {
      UIO<HttpResponse> value = response.fix(UIOOf::narrowK);
      Kind<Future_, HttpResponse> future = value.foldMap(FutureInstances.monadDefer(executor));
      return future.fix(FutureOf::narrowK).toPromise();
    };
  }
  
  static <R> ResponseInterpreterK<Kind<Kind<ZIO_, R>, Nothing>> zioSync(Producer<R> factory) {
    return response -> {
      ZIO<R, Nothing, HttpResponse> future = response.fix(ZIOOf::narrowK);
      return Promise.<HttpResponse>make().succeeded(future.provide(factory.get()).get());
    };
  }
  
  static <R> ResponseInterpreterK<Kind<Kind<ZIO_, R>, Nothing>> zioAsync(
      Producer<R> factory, Executor executor) {
    return response -> {
      ZIO<R, Nothing, HttpResponse> effect = response.fix(ZIOOf::narrowK);
      Kind<Future_, HttpResponse> future = effect.foldMap(factory.get(), FutureInstances.monadDefer(executor));
      return future.fix(FutureOf::narrowK).toPromise();
    };
  }
}
