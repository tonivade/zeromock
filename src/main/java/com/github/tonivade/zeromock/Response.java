/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Response {

  final int statusCode;
  final Object body;
  final Map<String, List<String>> headers;
  
  public Response(int statusCode, Object body, Map<String, List<String>> headers) {
    this.statusCode = statusCode;
    this.body = body;
    this.headers = unmodifiableMap(headers);
  }

  public Response withHeader(String string, String value) {
    Map<String, List<String>> newHeaders = new HashMap<>(headers);
    newHeaders.merge(string, singletonList(value), (oldValue, newValue) -> {
      List<String> newList = new ArrayList<>(oldValue);
      newList.addAll(newValue);
      return newList;
    });
    return new Response(statusCode, body, newHeaders);
  }
}
