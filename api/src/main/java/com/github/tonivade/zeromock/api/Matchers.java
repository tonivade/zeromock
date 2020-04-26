/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.Bytes.asString;
import static com.github.tonivade.zeromock.api.HttpMethod.DELETE;
import static com.github.tonivade.zeromock.api.HttpMethod.GET;
import static com.github.tonivade.zeromock.api.HttpMethod.OPTIONS;
import static com.github.tonivade.zeromock.api.HttpMethod.PATCH;
import static com.github.tonivade.zeromock.api.HttpMethod.POST;
import static com.github.tonivade.zeromock.api.HttpMethod.PUT;

import java.lang.reflect.Type;

import com.github.tonivade.purefun.Matcher1;

public final class Matchers {

  private Matchers() {}
  
  public static Matcher1<HttpRequest> all() {
    return request -> true;
  }

  public static Matcher1<HttpRequest> method(HttpMethod method) {
    return request -> request.method().equals(method);
  }
  
  public static Matcher1<HttpRequest> path(String url) {
    return request -> request.path().match(HttpPath.from(url));
  }
  
  public static Matcher1<HttpRequest> startsWith(String url) {
    return request -> request.path().startsWith(HttpPath.from(url));
  }
  
  public static Matcher1<HttpRequest> param(String name) {
    return request -> request.params().contains(name);
  }
  
  public static Matcher1<HttpRequest> param(String name, String value) {
    return request -> request.params().get(name).map(value::equals).getOrElse(false);
  }
  
  public static Matcher1<HttpRequest> header(String key, String value) {
    return request -> request.headers().get(key).contains(value);
  }
  
  public static Matcher1<HttpRequest> get() {
    return method(GET);
  }
  
  public static Matcher1<HttpRequest> put() {
    return method(PUT);
  }
  
  public static Matcher1<HttpRequest> post() {
    return method(POST);
  }
  
  public static Matcher1<HttpRequest> delete() {
    return method(DELETE);
  }

  public static Matcher1<HttpRequest>patch() {
    return method(PATCH);
  }

  public static Matcher1<HttpRequest>options() {
    return method(OPTIONS);
  }
  
  public static <T> Matcher1<HttpRequest> equalTo(T value) {
    return request -> json(request, value.getClass()).equals(value);
  }
  
  public static Matcher1<HttpRequest> body(String body) {
    return request -> asString(request.body()).equals(body);
  }
  
  public static Matcher1<HttpRequest> accept(String contentType) {
    return header("Accept", contentType);
  }
  
  public static Matcher1<HttpRequest> acceptsXml() {
    return accept("text/xml");
  }
  
  public static Matcher1<HttpRequest> acceptsJson() {
    return accept("application/json");
  }
  
  public static Matcher1<HttpRequest> get(String path) {
    return get().and(path(path));
  }
  
  public static Matcher1<HttpRequest> put(String path) {
    return put().and(path(path));
  }
  
  public static Matcher1<HttpRequest> post(String path) {
    return post().and(path(path));
  }
  
  public static Matcher1<HttpRequest> patch(String path) {
    return patch().and(path(path));
  }

  public static Matcher1<HttpRequest> delete(String path) {
    return delete().and(path(path));
  }

  public static Matcher1<HttpRequest> options(String path) {
    return options().and(path(path));
  }

  private static <T> T json(HttpRequest request, Type type) {
    return Extractors.body().andThen(Deserializers.<T>jsonTo(type)).apply(request);
  }
}
