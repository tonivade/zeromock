/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static java.util.function.Function.identity;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class Handlers {
  
  private Handlers() {}

  public static Function<HttpRequest, HttpResponse> ok(String body) {
    return compose(identity(), request -> body, Responses::ok);
  }

  public static Function<HttpRequest, HttpResponse> ok(Function<HttpRequest, Object> handler) {
    return compose(identity(), handler, Responses::ok);
  }

  public static Function<HttpRequest, HttpResponse> ok(Supplier<Object> supplier) {
    return compose(identity(), request -> supplier.get(), Responses::ok);
  }

  public static <T, R> Function<HttpRequest, HttpResponse> ok(Function<HttpRequest, T> begin, Function<T, R> exec) {
    return compose(begin, exec, Responses::ok);
  }

  public static <U, T, R> Function<HttpRequest, HttpResponse> ok(Function<HttpRequest, U> beginT, 
                                                                 Function<HttpRequest, T> beginU, 
                                                                 BiFunction<U, T, R> exec) {
    return compose(beginT, beginU, exec, Responses::ok);
  }
  
  public static Function<HttpRequest, HttpResponse> created(String body) {
    return compose(identity(), request -> body, Responses::created);
  }
  
  public static Function<HttpRequest, HttpResponse> created(Function<HttpRequest, Object> handler) {
    return compose(identity(), handler, Responses::created);
  }

  public static <T, R> Function<HttpRequest, HttpResponse> created(Function<HttpRequest, T> begin, 
                                                                   Function<T, R> exec) {
    return compose(begin, exec, Responses::created);
  }

  public static <U, T, R> Function<HttpRequest, HttpResponse> created(Function<HttpRequest, U> beginT, 
                                                                      Function<HttpRequest, T> beginU, 
                                                                      BiFunction<U, T, R> exec) {
    return compose(beginT, beginU, exec, Responses::created);
  }
  
  public static Function<HttpRequest, HttpResponse> noContent() {
    return compose(identity(), identity(), request -> Responses.noContent());
  }
  
  public static Function<HttpRequest, HttpResponse> forbidden() {
    return compose(identity(), identity(), request -> Responses.forbidden());
  }

  public static Function<HttpRequest, HttpResponse> badRequest(String body) {
    return compose(identity(), request -> body, Responses::badRequest);
  }

  public static Function<HttpRequest, HttpResponse> notFound(String body) {
    return compose(identity(), request -> body, Responses::notFound);
  }

  public static Function<HttpRequest, HttpResponse> error(String body) {
    return compose(identity(), request -> body, Responses::error);
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

  public static Function<HttpRequest, HttpResponse> delegate(HttpService service) {
    return request -> service.execute(request.dropOneLevel()).orElse(Responses.notFound("not found"));
  }
  
  public static <T, R> Function<HttpRequest, HttpResponse> compose(Function<HttpRequest, T> begin, 
                                                                   Function<T, R> exec, 
                                                                   Function<R, HttpResponse> end) {
    return begin.andThen(exec).andThen(end);
  }
  
  public static <T, U, R> Function<HttpRequest, HttpResponse> compose(Function<HttpRequest, T> beginT, 
                                                                      Function<HttpRequest, U> beginU, 
                                                                      BiFunction<T, U, R> exec, 
                                                                      Function<R, HttpResponse> end) {
    return tupple(beginT, beginU).andThen(untupple(exec)).andThen(end);
  }
  
  private static <T, U, R> Function<HttpRequest, Tupple<T, U>> tupple(Function<HttpRequest, T> beginT, 
                                                                      Function<HttpRequest, U> beginU) {
    return request -> new Tupple<>(beginT.apply(request), beginU.apply(request));
  }
  
  private static <T, U, R> Function<Tupple<T, U>, R> untupple(BiFunction<T, U, R> function) {
    return tupple -> function.apply(tupple._1(), tupple._2());
  }
  
  private static final class Tupple<T, U> {
    private final T t;
    private final U u;

    public Tupple(T t, U u) {
      this.t = t;
      this.u = u;
    }
    
    public T _1() {
      return t;
    }
    
    public U _2() {
      return u;
    }
  }
}
