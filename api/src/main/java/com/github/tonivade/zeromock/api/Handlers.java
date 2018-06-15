/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.Bytes.asBytes;

import com.github.tonivade.zeromock.core.Producer;
import com.github.tonivade.zeromock.core.Function1;

public final class Handlers {
  
  private Handlers() {}

  public static <T> Function1<T, HttpResponse> ok() {
    return asFunction(Responses::ok);
  }

  public static RequestHandler ok(String body) {
    return ok(asBytes(body));
  }

  public static RequestHandler ok(Bytes body) {
    return ok(request -> body);
  }

  public static RequestHandler ok(Function1<HttpRequest, Bytes> handler) {
    return handler.andThen(Responses::ok)::apply;
  }
  
  public static RequestHandler created(String body) {
    return created(asBytes(body));
  }
  
  public static RequestHandler created(Bytes body) {
    return created(request -> body);
  }
  
  public static RequestHandler created(Function1<HttpRequest, Bytes> handler) {
    return handler.andThen(Responses::created)::apply;
  }
  
  public static <T> Function1<T, HttpResponse> noContent() {
    return asFunction(Responses::noContent);
  }
  
  public static <T> Function1<T, HttpResponse> forbidden() {
    return asFunction(Responses::forbidden);
  }

  public static RequestHandler badRequest() {
    return asFunction(Responses::badRequest)::apply;
  }

  public static RequestHandler badRequest(String body) {
    return badRequest(asBytes(body));
  }

  public static RequestHandler badRequest(Bytes body) {
    return badRequest(request -> body);
  }

  public static RequestHandler badRequest(Function1<HttpRequest, Bytes> handler) {
    return handler.andThen(Responses::badRequest)::apply;
  }

  public static <T> Function1<T, HttpResponse> notFound() {
    return asFunction(Responses::notFound);
  }

  public static RequestHandler notFound(String body) {
    return notFound(asBytes(body));
  }

  public static RequestHandler notFound(Bytes body) {
    return notFound(request -> body);
  }

  public static RequestHandler notFound(Function1<HttpRequest, Bytes> handler) {
    return handler.andThen(Responses::notFound)::apply;
  }

  public static <T> Function1<T, HttpResponse> error() {
    return asFunction(Responses::error);
  }

  public static RequestHandler error(String body) {
    return error(asBytes(body));
  }

  public static RequestHandler error(Bytes body) {
    return error(request -> body);
  }
  
  public static RequestHandler error(Function1<HttpRequest, Bytes> handler) {
    return handler.andThen(Responses::error)::apply;
  }
  
  private static <T> Function1<T, HttpResponse> asFunction(Producer<HttpResponse> supplier) {
    return supplier.asFunction();
  }
}
