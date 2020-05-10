/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.concurrent.Future;

public interface AsyncPostFilter extends PostFilterK<Future.µ> {

  @Override
  default Future<HttpResponse> apply(HttpResponse value) {
    return PostFilterK.super.apply(value).fix1(Future::narrowK);
  }
}

