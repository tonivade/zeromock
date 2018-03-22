/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.core;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.github.tonivade.zeromock.core.Kind.OptionalKind;

public interface OptionalHandler<T, R> extends HandlerT<Optional<?>, T, R> {
  
  @Override
  default <V> OptionalHandler<T, V> map(Handler1<R, V> mapper) {
    return value -> new OptionalKind<V>(unbox().handle(value).map(mapper::handle));
  }
  
  @Override
  default <V> OptionalHandler<T, V> flatMap(HandlerT<Optional<?>, R, V> mapper) {
    return value -> new OptionalKind<V>(unbox().handle(value).flatMap(narrowKind(mapper)::handle));
  }
  
  @Override
  default OptionalHandler<T, R> filter(Predicate<R> predicate) {
    return value -> new OptionalKind<R>(unbox().handle(value).filter(predicate));
  }
  
  default Handler1<T, R> orElse(Supplier<R> supplier) {
    return value -> unbox().handle(value).orElseGet(supplier);
  }
  
  default Handler1<T, Optional<R>> unbox() {
    return value -> OptionalKind.narrowKind(handle(value));
  }
  
  static <T, R> Handler1<T, Optional<R>> narrowKind(HandlerT<Optional<?>, T, R> mapper) {
    return value -> OptionalKind.narrowKind(mapper.handle(value));
  }
}
