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

public class Handlers {
  
  private Handlers() {}

  public static Function<HttpRequest, HttpResponse> ok(String body) {
    return ok(request -> body);
  }

  public static <T> Function<HttpRequest, HttpResponse> ok(Supplier<T> supplier) {
    return ok(request -> supplier.get());
  }

  public static <T> Function<HttpRequest, HttpResponse> ok(Function<HttpRequest, T> handler) {
    return handler.andThen(Responses::ok);
  }
  
  public static Function<HttpRequest, HttpResponse> created(String body) {
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

  public static Function<HttpRequest, HttpResponse> badRequest(String body) {
    return badRequest(request -> body);
  }

  public static <T> Function<HttpRequest, HttpResponse> badRequest(Function<HttpRequest, T> handler) {
    return handler.andThen(Responses::badRequest);
  }

  public static Function<HttpRequest, HttpResponse> notFound(String body) {
    return notFound(request -> body);
  }

  public static <T> Function<HttpRequest, HttpResponse> notFound(Function<HttpRequest, T> handler) {
    return handler.andThen(Responses::notFound);
  }

  public static Function<HttpRequest, HttpResponse> error(String body) {
    return error(request -> body);
  }
  
  public static <T> Function<HttpRequest, HttpResponse> error(Function<HttpRequest, T> handler) {
    return handler.andThen(Responses::error);
  }
  
  public static Function<HttpResponse, HttpResponse> contentType(String value) {
    return response -> response.withHeader("Content-type", value);
  }
  
  public static Function<HttpResponse, HttpResponse> contentJson() {
    return contentType("application/json");
  }
  
  public static Function<HttpResponse, HttpResponse> contentXml() {
    return contentType("text/xml");
  }
  
  public static Function<HttpRequest, HttpRequest> dropOneLevel() {
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
