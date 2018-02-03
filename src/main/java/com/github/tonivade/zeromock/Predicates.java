/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static java.util.Collections.emptyList;

import java.util.function.Predicate;

public class Predicates {

  public static Predicate<Request> method(String method) {
    return request -> request.method.equals(method);
  }
  
  public static Predicate<Request> path(String url) {
    return request -> request.path.match(url);
  }
  
  public static Predicate<Request> param(String name) {
    return request -> request.params.containsKey(name);
  }
  
  public static Predicate<Request> header(String key, String value) {
    return request -> request.headers.getOrDefault(key, emptyList()).contains(value);
  }
  
  public static Predicate<Request> get() {
    return method("GET");
  }
  
  public static Predicate<Request> put() {
    return method("PUT");
  }
  
  public static Predicate<Request> post() {
    return method("POST");
  }
  
  public static Predicate<Request> delete() {
    return method("DELETE");
  }
  
  public static Predicate<Request> patch() {
    return method("PATCH");
  }
  
  public static Predicate<Request> accept(String contentType) {
    return header("Accept", contentType);
  }
  
  public static Predicate<Request> acceptsXml() {
    return accept("text/xml");
  }
  
  public static Predicate<Request> acceptsJson() {
    return accept("application/json");
  }
}
