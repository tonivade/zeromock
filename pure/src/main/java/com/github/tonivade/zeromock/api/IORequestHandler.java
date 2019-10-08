/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.instances.IOInstances;
import com.github.tonivade.purefun.monad.IO;

public interface IORequestHandler extends RequestHandlerK<IO.µ> {

  @Override
  IO<HttpResponse> run(HttpRequest value);

  default IORequestHandler postHandle(Function1<HttpResponse, HttpResponse> after) {
    return postHandle(IOInstances.functor(), after).andThen(IO::narrowK)::apply;
  }
}
