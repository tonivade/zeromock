/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.core.Matcher1;

public interface HttpRouteBuilder<T extends HttpRouteBuilder<T>> extends RouteBuilder<HttpRouteBuilder.ThenStep<T>> {

  @Override
  ThenStep<T> when(Matcher1<HttpRequest> matcher);

  @FunctionalInterface
  interface ThenStep<T> {

    T then(RequestHandler handler);

    default T ok(String body) {
      return then(Handlers.ok(body));
    }

    default T created(String body) {
      return then(Handlers.created(body));
    }

    default T error(String body) {
      return then(Handlers.error(body));
    }

    default T noContent() {
      return then(Handlers.noContent());
    }

    default T notFound() {
      return then(Handlers.notFound());
    }

    default T forbidden() {
      return then(Handlers.forbidden());
    }

    default T badRequest() {
      return then(Handlers.badRequest());
    }

    default T unauthorized() {
      return then(Handlers.unauthorized());
    }

    default T unavailable() {
      return then(Handlers.unavailable());
    }

    default T error() {
      return then(Handlers.error());
    }
  }
}
