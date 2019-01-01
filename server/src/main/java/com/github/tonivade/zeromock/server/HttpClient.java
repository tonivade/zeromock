/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.zeromock.api.Bytes.asBytes;
import static com.github.tonivade.zeromock.api.HttpStatus.BAD_REQUEST;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.github.tonivade.zeromock.api.Bytes;
import com.github.tonivade.zeromock.api.HttpHeaders;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpStatus;

public class HttpClient {
  
  private final String baseUrl;
  
  public HttpClient(String baseUrl) {
    this.baseUrl = baseUrl;
  }
  
  public static HttpClient connectTo(String baseUrl) {
    return new HttpClient(baseUrl);
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
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod(request.method().name());
    request.headers().forEach(connection::setRequestProperty);
    if (!request.body().isEmpty()) {
      connection.setDoOutput(true);
      try (OutputStream output = connection.getOutputStream()) {
        output.write(request.body().toArray());
      }
    }
    return connection;
  }

  private HttpResponse processResponse(HttpURLConnection connection) throws IOException {
    HttpHeaders headers = HttpHeaders.from(connection.getHeaderFields());
    Bytes body = deserialize(connection);
    HttpStatus status = HttpStatus.fromCode(connection.getResponseCode());
    return new HttpResponse(status, body, headers);
  }

  private Bytes deserialize(HttpURLConnection connection) throws IOException {
    Bytes body = Bytes.empty();
    if (connection.getContentLength() > 0) {
      if (connection.getResponseCode() < BAD_REQUEST.code()) {
        body = asBytes(connection.getInputStream());
      } else {
        body = asBytes(connection.getErrorStream());
      }
    }
    return body;
  }
}
