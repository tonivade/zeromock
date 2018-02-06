/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.HttpMethod.DELETE;
import static com.github.tonivade.zeromock.HttpMethod.GET;
import static com.github.tonivade.zeromock.HttpMethod.PATCH;
import static com.github.tonivade.zeromock.HttpMethod.POST;
import static com.github.tonivade.zeromock.HttpMethod.PUT;

import java.util.function.Predicate;

public class Predicates {

  private Predicates() {}

  public static Predicate<HttpRequest> method(HttpMethod method) {
    return request -> request.method.equals(method);
  }
  
  public static Predicate<HttpRequest> path(String url) {
    return request -> request.path.match(new Path(url));
  }
  
  public static Predicate<HttpRequest> param(String name) {
    return request -> request.params.contains(name);
  }
  
  public static Predicate<HttpRequest> header(String key, String value) {
    return request -> request.headers.get(key).contains(value);
  }
  
  public static Predicate<HttpRequest> get() {
    return method(GET);
  }
  
  public static Predicate<HttpRequest> put() {
    return method(PUT);
  }
  
  public static Predicate<HttpRequest> post() {
    return method(POST);
  }
  
  public static Predicate<HttpRequest> delete() {
    return method(DELETE);
  }
  
  public static Predicate<HttpRequest> patch() {
    return method(PATCH);
  }
  
  public static Predicate<HttpRequest> accept(String contentType) {
    return header("Accept", contentType);
  }
  
  public static Predicate<HttpRequest> acceptsXml() {
    return accept("text/xml");
  }
  
  public static Predicate<HttpRequest> acceptsJson() {
    return accept("application/json");
  }
}
