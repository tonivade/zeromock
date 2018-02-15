/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class Handlers {
  
  private Handlers() {}

  public static <T> Function<HttpRequest, HttpResponse> ok(T body) {
    return ok(request -> body);
  }

  public static <T> Function<HttpRequest, HttpResponse> ok(Supplier<T> supplier) {
    return ok(request -> supplier.get());
  }

  public static <T> Function<HttpRequest, HttpResponse> ok(Function<HttpRequest, T> handler) {
    return handler.andThen(Responses::ok);
  }
  
  public static <T> Function<HttpRequest, HttpResponse> okOrNoContent(Function<HttpRequest, Optional<T>> handler) {
    return handler.andThen(okOrNoContent());
  }
  
  public static <T> Function<HttpRequest, HttpResponse> created(T body) {
    return created(request -> body);
  }
  
  public static <T> Function<HttpRequest, HttpResponse> created(Function<HttpRequest, T> handler) {
    return handler.andThen(Responses::created);
  }
  
  public static <T> Function<T, Void> force(Consumer<T> consumer) {
    return value -> { consumer.accept(value); return null; };
  }
  
  public static Function<HttpRequest, HttpResponse> noContent() {
    return request -> Responses.noContent();
  }
  
  public static Function<HttpRequest, HttpResponse> forbidden() {
    return request -> Responses.forbidden();
  }

  public static <T> Function<HttpRequest, HttpResponse> badRequest(T body) {
    return badRequest(request -> body);
  }

  public static <T> Function<HttpRequest, HttpResponse> badRequest(Function<HttpRequest, T> handler) {
    return handler.andThen(Responses::badRequest);
  }

  public static <T> Function<HttpRequest, HttpResponse> notFound(T body) {
    return notFound(request -> body);
  }

  public static <T> Function<HttpRequest, HttpResponse> notFound(Function<HttpRequest, T> handler) {
    return handler.andThen(Responses::notFound);
  }

  public static <T> Function<HttpRequest, HttpResponse> error(T body) {
    return error(request -> body);
  }
  
  public static <T> Function<HttpRequest, HttpResponse> error(Function<HttpRequest, T> handler) {
    return handler.andThen(Responses::error);
  }
  
  public static UnaryOperator<HttpResponse> contentType(String value) {
    return response -> response.withHeader("Content-type", value);
  }
  
  public static UnaryOperator<HttpResponse> contentJson() {
    return contentType("application/json");
  }
  
  public static UnaryOperator<HttpResponse> contentXml() {
    return contentType("text/xml");
  }
  
  public static UnaryOperator<HttpRequest> dropOneLevel() {
    return request -> request.dropOneLevel();
  }

  public static Function<HttpRequest, HttpResponse> delegate(HttpService service) {
    return dropOneLevel().andThen(service::execute).andThen(getOrNotFound());
  }

  public static Function<Optional<HttpResponse>, HttpResponse> getOrNotFound() {
    return response -> response.orElseGet(() -> Responses.notFound("no mapping found"));
  }
  
  public static <T, U, R> Function<HttpRequest, Tupple<T, U>> join(Function<HttpRequest, T> beginT, 
                                                                   Function<HttpRequest, U> beginU) {
    return request -> new Tupple<>(beginT.apply(request), beginU.apply(request));
  }
  
  public static <T, U, R> Function<Tupple<T, U>, R> split(BiFunction<T, U, R> function) {
    return tupple -> function.apply(tupple.get1(), tupple.get2());
  }

  private static <T> Function<Optional<T>, HttpResponse> okOrNoContent() {
    return optional -> optional.map(Responses::ok).orElseGet(Responses::noContent);
  }
  
  private static final class Tupple<T, U> {
    private final T t;
    private final U u;

    public Tupple(T t, U u) {
      this.t = t;
      this.u = u;
    }
    
    public T get1() {
      return t;
    }
    
    public U get2() {
      return u;
    }
  }
}
