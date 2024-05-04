/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.client;

@FunctionalInterface
public interface HttpClientBuilder<T> {

  T connectTo(String url);

  static HttpClientBuilder<HttpClient> client() {
    return HttpClient::connectTo;
  }

  static HttpClientBuilder<AsyncHttpClient> asyncClient() {
    return AsyncHttpClient::connectTo;
  }

  static HttpClientBuilder<IOHttpClient> ioClient() {
    return IOHttpClient::connectTo;
  }

  static HttpClientBuilder<UIOHttpClient> uioClient() {
    return UIOHttpClient::connectTo;
  }

  static HttpClientBuilder<TaskHttpClient> taskClient() {
    return TaskHttpClient::connectTo;
  }
}
