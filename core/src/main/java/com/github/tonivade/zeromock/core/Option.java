/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.core;

import static com.github.tonivade.zeromock.core.Equal.comparing;
import static com.github.tonivade.zeromock.core.Equal.equal;
import static com.github.tonivade.zeromock.core.OptionHandler.identity;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public interface Option<T> extends Functor<T>, Filterable<T>, Holder<T> {
  
  static <T> Option<T> some(T value) {
    return new Some<>(value);
  }
  
  @SuppressWarnings("unchecked")
  static <T> Option<T> none() {
    return (Option<T>) None.INSTANCE;
  }

  static <T> Option<T> of(Producer<T> producer) {
    T value = producer.get();
    if (nonNull(value)) {
      return some(value);
    }
    return none();
  }

  static <T> Option<T> from(Optional<T> optional) {
    return optional.map(Option::some).orElseGet(Option::none);
  }
  
  boolean isPresent();
  boolean isEmpty();
  
  @Override
  default <R> Option<R> map(Function1<T, R> mapper) {
    if (isPresent()) {
      return some(mapper.apply(get()));
    }
    return none();
  }

  default <R> Option<R> flatMap(OptionHandler<T, R> mapper) {
    if (isPresent()) {
      return mapper.apply(get());
    }
    return none();
  }

  default Option<T> ifPresent(Consumer1<T> consumer) {
    if (isPresent()) {
      consumer.accept(get());
    }
    return this;
  }
  
  default Option<T> ifEmpty(Runnable run) {
    if (isEmpty()) {
      run.run();
    }
    return this;
  }

  @Override
  default Option<T> filter(Matcher<T> matcher) {
    if (isPresent() && matcher.match(get())) {
      return this;
    }
    return none();
  }

  default T orElse(T value) {
    return orElse(Producer.unit(value));
  }

  default T orElse(Producer<T> producer) {
    if (isEmpty()) {
      return producer.get();
    }
    return get();
  }

  default <X extends Throwable> T orElseThrow(Producer<X> producer) throws X { 
    if (isEmpty()) {
      throw producer.get();
    }
    return get();
  }
  
  default <U> U fold(Producer<U> orElse, Function1<T, U> mapper) {
    if (isPresent()) {
      return mapper.apply(get());
    }
    return orElse.get();
  }

  default Stream<T> stream() {
    if (isPresent()) {
      return Stream.of(get());
    }
    return Stream.empty();
  }
  
  default Optional<T> toOptional() {
    if (isPresent()) {
      return Optional.of(get());
    }
    return Optional.empty();
  }
  
  @SuppressWarnings("unchecked")
  default <V> Option<V> flatten() {
    try {
      return ((Option<Option<V>>) this).flatMap(identity());
    } catch (ClassCastException e) {
      throw new UnsupportedOperationException("cannot be flattened");
    }
  }

  final class Some<T> implements Option<T> {
    private final T value;
    
    private Some(T value) {
      this.value = requireNonNull(value);
    }
    
    @Override
    public T get() {
      return value;
    }
    
    @Override
    public boolean isEmpty() {
      return false;
    }
    
    @Override
    public boolean isPresent() {
      return true;
    }
    
    @Override
    public int hashCode() {
      return Objects.hash(value);
    }
    
    @Override
    public boolean equals(Object obj) {
      return equal(this)
          .append(comparing(Option::get))
          .applyTo(obj);
    }
    
    @Override
    public String toString() {
      return "Some(" + value + ")";
    }
  }

  final class None<T> implements Option<T> {
    
    private static final None<?> INSTANCE = new None<>();

    private None() { }

    @Override
    public T get() {
      throw new NoSuchElementException("get() in none");
    }
    
    @Override
    public boolean isEmpty() {
      return true;
    }
    
    @Override
    public boolean isPresent() {
      return false;
    }
    
    @Override
    public int hashCode() {
      return 1;
    }
    
    @Override
    public boolean equals(Object obj) {
      return this == obj;
    }
    
    @Override
    public String toString() {
      return "None";
    }
  }
}
