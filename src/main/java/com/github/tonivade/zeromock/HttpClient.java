/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.Bytes.asByteBuffer;
import static com.github.tonivade.zeromock.HttpStatus.BAD_REQUEST;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;

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
    if (request.body().hasRemaining()) {
      connection.setDoOutput(true);
      try (OutputStream output = connection.getOutputStream()) {
        output.write(request.body().array());
      }
    }
    return connection;
  }

  private HttpResponse processResponse(HttpURLConnection connection) throws IOException {
    HttpHeaders headers = new HttpHeaders(connection.getHeaderFields());
    ByteBuffer body = deserialize(connection);
    HttpStatus status = HttpStatus.fromCode(connection.getResponseCode());
    return new HttpResponse(status, body, headers);
  }

  private ByteBuffer deserialize(HttpURLConnection connection) throws IOException {
    ByteBuffer body = Bytes.empty();
    if (connection.getContentLength() > 0) {
      if (connection.getResponseCode() < BAD_REQUEST.code()) {
        body = asByteBuffer(connection.getInputStream());
      } else {
        body = asByteBuffer(connection.getErrorStream());
      }
    }
    return body;
  }
}
