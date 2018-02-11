/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.HttpStatus.BAD_REQUEST;
import static com.github.tonivade.zeromock.IOUtils.readAll;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClient {
  
  private final String baseUrl;
  
  public HttpClient(String baseUrl) {
    this.baseUrl = baseUrl;
  }
  
  public HttpResponse request(HttpRequest request) {
    try {
      HttpURLConnection connection = createConnection(request);
      connection.connect();
      return processResponse(connection);
    } catch (IOException e) {
      throw new UncheckedIOException("request error: " + request, e);
    }
  }

  private HttpURLConnection createConnection(HttpRequest request) throws IOException {
    URL url = new URL(baseUrl + request.toUrl());
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod(request.method.name());
    request.headers.forEach(con::setRequestProperty);
    if (request.body != null) {
      con.setDoOutput(true);
      try (OutputStream output = con.getOutputStream()) {
        output.write(Serializers.plain().apply(request.body).array());
      }
    }
    return con;
  }

  private HttpResponse processResponse(HttpURLConnection connection) throws IOException {
    HttpHeaders headers = new HttpHeaders(connection.getHeaderFields());
    Object body = deserialize(connection);
    HttpStatus status = HttpStatus.fromCode(connection.getResponseCode());
    return new HttpResponse(status, body, headers);
  }

  private Object deserialize(HttpURLConnection connection) throws IOException {
    Object body = null;
    if (connection.getContentLength() > 0) {
      if (connection.getResponseCode() < BAD_REQUEST.code) {
        body = Deserializers.plain().apply(readAll(connection.getInputStream()));
      } else {
        body = Deserializers.plain().apply(readAll(connection.getErrorStream()));
      }
    }
    return body;
  }
}
