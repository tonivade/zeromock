/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Operator1;

public final class Headers {

  private Headers() {}

  public static Operator1<HttpResponse> enableCors() {
    return response ->
        response.withHeader("Access-Control-Allow-Origin", "*")
          .withHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PATCH, PUT")
          .withHeader("Access-Control-Max-Age", "3600")
          .withHeader("Access-Control-Allow-Headers", "x-requested-with, origin, content-type, accept");
  }
  
  public static Operator1<HttpResponse> contentType(String value) {
    return response -> response.withHeader("Content-type", value);
  }
  
  public static Operator1<HttpResponse> contentPlain() {
    return contentType("text/plain");
  }
  
  public static Operator1<HttpResponse> contentJson() {
    return contentType("application/json");
  }
  
  public static Operator1<HttpResponse> contentXml() {
    return contentType("text/xml");
  }
}
