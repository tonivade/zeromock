/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.core;

import java.util.function.Predicate;

public interface HandlerT<M, T, R> extends Handler1<T, Kind<M, R>> {
  <V> HandlerT<M, T, V> map(Handler1<R, V> mapper);
  
  <V> HandlerT<M, T, V> flatMap(HandlerT<M, R, V> mapper);
  
  HandlerT<M, T, R> filter(Predicate<R> predicate);
}
