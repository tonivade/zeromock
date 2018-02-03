/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.IOUtils.readAll;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HttpClient {
  
  private String baseUrl;
  
  public HttpClient(String baseUrl) {
    this.baseUrl = baseUrl;
  }
  
  public Response request(Request request) throws IOException {
    URL url = new URL(baseUrl + request.toUrl());
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    request.headers.forEach((key, values) -> values.forEach(value -> con.setRequestProperty(key, value)));
    con.setRequestMethod(request.method);
    int responseCode = con.getResponseCode();
    Map<String, List<String>> headers = con.getHeaderFields();
    String body = null;
    if (responseCode < 400) {
      body = readAll(con.getInputStream());
    }
    return new Response(responseCode, body, headers);
  }
}
