/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.core;

import static com.github.tonivade.zeromock.core.Sequence.listOf;

import java.util.Optional;
import java.util.stream.Stream;

@FunctionalInterface
public interface Function1<T, R> {

  R apply(T value);
  
  default <V> Function1<T, V> andThen(Function1<R, V> after) {
    return (T value) -> after.apply(apply(value));
  }
  
  default <V> Function1<V, R> compose(Function1<V, T> before) {
    return (V value) -> apply(before.apply(value));
  }
  
  default OptionalHandler<T, R> liftOptional() {
    return value -> Optional.ofNullable(apply(value));
  }
  
  @SuppressWarnings("unchecked")
  default OptionalHandler<T, R> asOptional() {
    return value -> (Optional<R>) apply(value);
  }
  
  default OptionHandler<T, R> liftOption() {
    return value -> Option.of(() -> apply(value));
  }
  
  @SuppressWarnings("unchecked")
  default OptionHandler<T, R> asOption() {
    return value -> (Option<R>) apply(value);
  }
  
  default TryHandler<T, R> liftTry() {
    return value -> Try.of(() -> apply(value));
  }
  
  @SuppressWarnings("unchecked")
  default TryHandler<T, R> asTry() {
    return value -> (Try<R>) apply(value);
  }
  
  default <L> EitherHandler<T, L, R> liftRight() {
    return value -> Either.right(apply(value));
  }
  
  default <L> EitherHandler<T, R, L> liftLeft() {
    return value -> Either.left(apply(value));
  }
  
  default SequenceHandler<T, R> liftSequence() {
    return value -> listOf(apply(value));
  }
  
  @SuppressWarnings("unchecked")
  default SequenceHandler<T, R> asSequence() {
    return value -> (Sequence<R>) apply(value);
  }
  
  default StreamHandler<T, R> stream() {
    return value -> Stream.of(apply(value));
  }
  
  static <T> Function1<T, T> identity() {
    return value -> value;
  }
}