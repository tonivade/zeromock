/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.Bytes.asString;
import static com.github.tonivade.zeromock.api.HttpMethod.DELETE;
import static com.github.tonivade.zeromock.api.HttpMethod.GET;
import static com.github.tonivade.zeromock.api.HttpMethod.PATCH;
import static com.github.tonivade.zeromock.api.HttpMethod.POST;
import static com.github.tonivade.zeromock.api.HttpMethod.PUT;
import static com.github.tonivade.zeromock.core.Producer.unit;

import java.lang.reflect.Type;

import com.github.tonivade.zeromock.core.Matcher;

public final class Matchers {

  private Matchers() {}
  
  public static Matcher<HttpRequest> all() {
    return request -> true;
  }

  public static Matcher<HttpRequest> method(HttpMethod method) {
    return request -> request.method().equals(method);
  }
  
  public static Matcher<HttpRequest> path(String url) {
    return request -> request.path().match(HttpPath.from(url));
  }
  
  public static Matcher<HttpRequest> startsWith(String url) {
    return request -> request.path().startsWith(HttpPath.from(url));
  }
  
  public static Matcher<HttpRequest> param(String name) {
    return request -> request.params().contains(name);
  }
  
  public static Matcher<HttpRequest> param(String name, String value) {
    return request -> request.params().get(name).map(value::equals).orElse(unit(false));
  }
  
  public static Matcher<HttpRequest> header(String key, String value) {
    return request -> request.headers().get(key).contains(value);
  }
  
  public static Matcher<HttpRequest> get() {
    return method(GET);
  }
  
  public static Matcher<HttpRequest> put() {
    return method(PUT);
  }
  
  public static Matcher<HttpRequest> post() {
    return method(POST);
  }
  
  public static Matcher<HttpRequest> delete() {
    return method(DELETE);
  }
  
  public static Matcher<HttpRequest>patch() {
    return method(PATCH);
  }
  
  public static <T> Matcher<HttpRequest> equalTo(T value) {
    return request -> json(request, value.getClass()).equals(value);
  }
  
  public static Matcher<HttpRequest> body(String body) {
    return request -> asString(request.body()).equals(body);
  }
  
  public static Matcher<HttpRequest> accept(String contentType) {
    return header("Accept", contentType);
  }
  
  public static Matcher<HttpRequest> acceptsXml() {
    return accept("text/xml");
  }
  
  public static Matcher<HttpRequest> acceptsJson() {
    return accept("application/json");
  }
  
  public static Matcher<HttpRequest> get(String path) {
    return get().and(path(path));
  }
  
  public static Matcher<HttpRequest> put(String path) {
    return put().and(path(path));
  }
  
  public static Matcher<HttpRequest> post(String path) {
    return post().and(path(path));
  }
  
  public static Matcher<HttpRequest> patch(String path) {
    return patch().and(path(path));
  }
  
  public static Matcher<HttpRequest> delete(String path) {
    return delete().and(path(path));
  }

  private static <T> T json(HttpRequest request, Type type) {
    return Extractors.body().andThen(Deserializers.<T>json(type)).handle(request);
  }
}
