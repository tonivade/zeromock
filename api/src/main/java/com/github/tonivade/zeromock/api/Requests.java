/*
 * Copyright (c) 2018-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.HttpMethod.DELETE;
import static com.github.tonivade.zeromock.api.HttpMethod.GET;
import static com.github.tonivade.zeromock.api.HttpMethod.HEAD;
import static com.github.tonivade.zeromock.api.HttpMethod.OPTIONS;
import static com.github.tonivade.zeromock.api.HttpMethod.PATCH;
import static com.github.tonivade.zeromock.api.HttpMethod.POST;
import static com.github.tonivade.zeromock.api.HttpMethod.PUT;

public final class Requests {

  private Requests() {}

  public static HttpRequest head(String path) {
    return new HttpRequest(HEAD, HttpPath.from(path));
  }

  public static HttpRequest get(String path) {
    return new HttpRequest(GET, HttpPath.from(path));
  }

  public static HttpRequest post(String path) {
    return new HttpRequest(POST, HttpPath.from(path));
  }

  public static HttpRequest put(String path) {
    return new HttpRequest(PUT, HttpPath.from(path));
  }

  public static HttpRequest delete(String path) {
    return new HttpRequest(DELETE, HttpPath.from(path));
  }

  public static HttpRequest patch(String path) {
    return new HttpRequest(PATCH, HttpPath.from(path));
  }

  public static HttpRequest options(String path) {
    return new HttpRequest(OPTIONS, HttpPath.from(path));
  }
}
