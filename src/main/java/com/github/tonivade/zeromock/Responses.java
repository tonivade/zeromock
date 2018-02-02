/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static java.net.HttpURLConnection.*;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;

import java.util.Map;
import java.util.function.Function;

public class Responses {

  public static Function<Request, Response> ok(String body) {
    return request -> new Response(HTTP_OK, format(body, collectParams(request)), emptyMap());
  }
  
  public static Function<Request, Response> crated(String body) {
    return request -> new Response(HTTP_CREATED, body, emptyMap());
  }
  
  public static Function<Request, Response> noContent() {
    return request -> new Response(HTTP_NO_CONTENT, null, emptyMap());
  }
  
  public static Function<Request, Response> forbidden() {
    return request -> new Response(HTTP_FORBIDDEN, null, emptyMap());
  }

  public static Function<Request, Response> badRequest(String body) {
    return request -> new Response(HTTP_BAD_REQUEST, body, emptyMap());
  }

  public static Function<Request, Response> notFound(String body) {
    return request -> new Response(HTTP_NOT_FOUND, body, emptyMap());
  }

  public static Function<Request, Response> error(String body) {
    return request -> new Response(HTTP_INTERNAL_ERROR, body, emptyMap());
  }
  
  public static Function<Response, Response> contentType(String value) {
    return response -> response.withHeader("Content-Type", value);
  }
  
  public static Function<Response, Response> contentJson() {
    return contentType("application/json");
  }
  
  public static Function<Response, Response> contentXml() {
    return contentType("text/xml");
  }

  private static Object[] collectParams(Request request) {
    return request.params.entrySet().stream().map(Map.Entry::getValue).toArray();
  }
}
