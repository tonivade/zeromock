/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.core;

import java.util.function.Predicate;
import java.util.stream.Stream;

import com.github.tonivade.zeromock.core.Kind.StreamKind;

public interface StreamHandler<T, R> extends HandlerT<Stream<?>, T, R> {
  
  @Override
  default <V> StreamHandler<T, V> map(Handler1<R, V> mapper) {
    return value -> new StreamKind<V>(unbox().handle(value).map(mapper::handle));
  }
  
  @Override
  default <V> StreamHandler<T, V> flatMap(HandlerT<Stream<?>, R, V> mapper) {
    return value -> new StreamKind<V>(unbox().handle(value).flatMap(narrowKind(mapper)::handle));
  }
  
  @Override
  default StreamHandler<T, R> filter(Predicate<R> predicate) {
    return value -> new StreamKind<R>(unbox().handle(value).filter(predicate));
  }
  
  default Handler1<T, Stream<R>> unbox() {
    return value -> StreamKind.narrowKind(handle(value));
  }
  
  static <T, R> Handler1<T, Stream<R>> narrowKind(HandlerT<Stream<?>, T, R> mapper) {
    return value -> StreamKind.narrowKind(mapper.handle(value));
  }
}
