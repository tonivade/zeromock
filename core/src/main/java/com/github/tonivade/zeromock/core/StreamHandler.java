/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.core;

import java.util.stream.Stream;

import com.github.tonivade.zeromock.core.Kind.StreamKind;

public interface StreamHandler<T, R> extends HandlerT<StreamKind.µ, T, R> {
  
  default <V> StreamHandler<T, V> map(Handler1<R, V> mapper) {
    return value -> new StreamKind<V>(unbox().handle(value).map(mapper::handle));
  }
  
  default <V> StreamHandler<T, V> flatMap(HandlerT<StreamKind.µ, R, V> mapper) {
    return value -> new StreamKind<V>(unbox().handle(value).flatMap(narrowKind(mapper)::handle));
  }
  
  default Handler1<T, Stream<R>> unbox() {
    return value -> StreamKind.narrowKind(handle(value));
  }
  
  static <T, R> Handler1<T, Stream<R>> narrowKind(HandlerT<StreamKind.µ, T, R> mapper) {
    return value -> StreamKind.narrowKind(mapper.handle(value));
  }
}
