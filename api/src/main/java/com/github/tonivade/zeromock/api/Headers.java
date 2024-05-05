/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

public final class Headers {

  private Headers() {}

  public static PostFilter enableCors() {
    return response ->
        response.withHeader("Access-Control-Allow-Origin", "*")
          .withHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PATCH, PUT")
          .withHeader("Access-Control-Max-Age", "3600")
          .withHeader("Access-Control-Allow-Headers", "x-requested-with, origin, content-type, accept");
  }
  
  public static PostFilter contentType(String value) {
    return response -> response.withHeader("Content-type", value);
  }
  
  public static PostFilter contentPlain() {
    return contentType("text/plain");
  }
  
  public static PostFilter contentJson() {
    return contentType("application/json");
  }
  
  public static PostFilter contentXml() {
    return contentType("text/xml");
  }
}
