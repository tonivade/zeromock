/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import java.util.function.Function;

public class Handlers {
  
  private Handlers() {}

  public static Function<HttpRequest, HttpResponse> ok(String body) {
    return request -> Responses.ok(body);
  }

  public static Function<HttpRequest, HttpResponse> ok(Function<HttpRequest, Object> handler) {
    return request -> Responses.ok(handler.apply(request));
  }
  
  public static Function<HttpRequest, HttpResponse> created(String body) {
    return request -> Responses.created(body);
  }
  
  public static Function<HttpRequest, HttpResponse> created(Function<HttpRequest, Object> handler) {
    return request -> Responses.created(handler.apply(request));
  }
  
  public static Function<HttpRequest, HttpResponse> noContent() {
    return request -> Responses.noContent();
  }
  
  public static Function<HttpRequest, HttpResponse> forbidden() {
    return request -> Responses.forbidden();
  }

  public static Function<HttpRequest, HttpResponse> badRequest(String body) {
    return request -> Responses.badRequest(body);
  }

  public static Function<HttpRequest, HttpResponse> notFound(String body) {
    return request -> Responses.notFound(body);
  }

  public static Function<HttpRequest, HttpResponse> error(String body) {
    return request -> Responses.error(body);
  }
  
  public static Function<HttpResponse, HttpResponse> contentType(String value) {
    return response -> response.withHeader("Content-type", value);
  }
  
  public static Function<HttpResponse, HttpResponse> contentJson() {
    return contentType("application/json");
  }
  
  public static Function<HttpResponse, HttpResponse> contentXml() {
    return contentType("text/xml");
  }

  public static Function<HttpRequest, HttpResponse> delegate(HttpService service) {
    return request -> service.handle(request.dropOneLevel()).orElse(Responses.notFound("not found"));
  }
}
