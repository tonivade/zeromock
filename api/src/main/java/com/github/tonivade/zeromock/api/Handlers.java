/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.Bytes.asBytes;

import com.github.tonivade.purefun.core.Function1;
import com.github.tonivade.purefun.core.Producer;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.type.Try;

public final class Handlers {

  private Handlers() {}

  public static RequestHandler ok() {
    return asFunction(Responses::ok);
  }

  public static RequestHandler fromOption(Function1<HttpRequest, Option<Bytes>> handler) {
    return handler.andThen(Responses::fromOption)::apply;
  }

  public static RequestHandler fromTry(Function1<HttpRequest, Try<Bytes>> handler) {
    return handler.andThen(Responses::fromTry)::apply;
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

  public static RequestHandler noContent() {
    return asFunction(Responses::noContent);
  }

  public static RequestHandler forbidden() {
    return asFunction(Responses::forbidden);
  }

  public static RequestHandler badRequest() {
    return asFunction(Responses::badRequest);
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

  public static RequestHandler notFound() {
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

  public static RequestHandler unauthorized() {
    return asFunction(Responses::unauthorized);
  }

  public static RequestHandler unauthorized(String body) {
    return unauthorized(asBytes(body));
  }

  public static RequestHandler unauthorized(Bytes body) {
    return unauthorized(request -> body);
  }

  public static RequestHandler unauthorized(Function1<HttpRequest, Bytes> handler) {
    return handler.andThen(Responses::unauthorized)::apply;
  }

  public static RequestHandler error() {
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

  public static RequestHandler unavailable() {
    return asFunction(Responses::unavailable);
  }

  public static RequestHandler unavailable(String body) {
    return unavailable(asBytes(body));
  }

  public static RequestHandler unavailable(Bytes body) {
    return unavailable(request -> body);
  }

  public static RequestHandler unavailable(Function1<HttpRequest, Bytes> handler) {
    return handler.andThen(Responses::unavailable)::apply;
  }

  private static RequestHandler asFunction(Producer<HttpResponse> producer) {
    return producer.asFunction()::apply;
  }
}
