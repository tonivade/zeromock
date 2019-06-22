/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.zio.ZIO.accessM;
import static com.github.tonivade.zeromock.api.Deserializers.json;

import com.github.tonivade.purefun.zio.ZIO;
import com.github.tonivade.zeromock.api.Bytes;
import com.github.tonivade.zeromock.api.HttpHeaders;
import com.github.tonivade.zeromock.api.HttpMethod;
import com.github.tonivade.zeromock.api.HttpParams;
import com.github.tonivade.zeromock.api.HttpPath;
import com.github.tonivade.zeromock.api.HttpRequest;

public interface HasHttpRequest {

  Service request();

  static <E extends HasHttpRequest> ZIO<E, Throwable, String> pathParam(int index) {
    return accessM(env -> env.request().pathParam(index));
  }

  static <E extends HasHttpRequest> ZIO<E, Throwable, String> param(String name) {
    return accessM(env -> env.request().param(name));
  }

  static <E extends HasHttpRequest> ZIO<E, Throwable, Bytes> body() {
    return accessM(env -> env.request().body());
  }

  static <E extends HasHttpRequest> ZIO<E, Throwable, HttpMethod> method() {
    return accessM(env -> env.request().method());
  }

  static <E extends HasHttpRequest> ZIO<E, Throwable, HttpParams> params() {
    return accessM(env -> env.request().params());
  }

  static <E extends HasHttpRequest> ZIO<E, Throwable, HttpHeaders> headers() {
    return accessM(env -> env.request().headers());
  }

  static <E extends HasHttpRequest> ZIO<E, Throwable, HttpPath> path() {
    return accessM(env -> env.request().path());
  }

  static <E extends HasHttpRequest> ZIO<E, Throwable, String> toUrl() {
    return accessM(env -> env.request().toUrl());
  }

  static <E extends HasHttpRequest> ZIO<E, Throwable, Integer> asInteger(String value) {
    return ZIO.from(() -> Integer.parseInt(value));
  }

  static <E extends HasHttpRequest> ZIO<E, Throwable, Long> asLong(String value) {
    return ZIO.from(() -> Long.parseLong(value));
  }

  static <E extends HasHttpRequest> ZIO<E, Throwable, Double> asDouble(String value) {
    return ZIO.from(() -> Double.parseDouble(value));
  }

  static <E extends HasHttpRequest, T> ZIO<E, Throwable, T> fromJson(Bytes body, Class<T> clazz) {
    return ZIO.from(() -> json(clazz).apply(body));
  }

  static Service use(HttpRequest request) {
    return new Service() {

      @Override
      public <E extends HasHttpRequest> ZIO<E, Throwable, String> pathParam(int index) {
        return ZIO.from(() -> request.pathParam(index));
      }

      @Override
      public <E extends HasHttpRequest> ZIO<E, Throwable, String> param(String name) {
        return ZIO.from(() -> request.param(name));
      }

      @Override
      public <E extends HasHttpRequest> ZIO<E, Throwable, Bytes> body() {
        return ZIO.from(request::body);
      }

      @Override
      public <E extends HasHttpRequest> ZIO<E, Throwable, HttpMethod> method() {
        return ZIO.from(request::method);
      }

      @Override
      public <E extends HasHttpRequest> ZIO<E, Throwable, HttpParams> params() {
        return ZIO.from(request::params);
      }

      @Override
      public <E extends HasHttpRequest> ZIO<E, Throwable, HttpHeaders> headers() {
        return ZIO.from(request::headers);
      }

      @Override
      public <E extends HasHttpRequest> ZIO<E, Throwable, HttpPath> path() {
        return ZIO.from(request::path);
      }

      @Override
      public <E extends HasHttpRequest> ZIO<E, Throwable, String> toUrl() {
        return ZIO.from(request::toUrl);
      }
    };
  }

  interface Service {
    <E extends HasHttpRequest> ZIO<E, Throwable, String> param(String name);
    <E extends HasHttpRequest> ZIO<E, Throwable, String> pathParam(int index);
    <E extends HasHttpRequest> ZIO<E, Throwable, Bytes> body();
    <E extends HasHttpRequest> ZIO<E, Throwable, HttpHeaders> headers();
    <E extends HasHttpRequest> ZIO<E, Throwable, HttpMethod> method();
    <E extends HasHttpRequest> ZIO<E, Throwable, HttpPath> path();
    <E extends HasHttpRequest> ZIO<E, Throwable, HttpParams> params();
    <E extends HasHttpRequest> ZIO<E, Throwable, String> toUrl();
  }
}