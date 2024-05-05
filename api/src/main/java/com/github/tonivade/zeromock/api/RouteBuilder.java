/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.core.Matcher1;

public interface RouteBuilder<T> {

  T when(Matcher1<HttpRequest> matcher);

  default T get(String path) {
    return when(Matchers.get(path));
  }

  default T post(String path) {
    return when(Matchers.post(path));
  }

  default T put(String path) {
    return when(Matchers.put(path));
  }

  default T delete(String path) {
    return when(Matchers.delete(path));
  }

  default T patch(String path) {
    return when(Matchers.patch(path));
  }

  default T head(String path) {
    return when(Matchers.head(path));
  }

  default T options(String path) {
    return when(Matchers.options(path));
  }
}
