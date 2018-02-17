/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.Bytes.asByteBuffer;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class Handlers {
  
  private Handlers() {}

  public static <T> Function<T, HttpResponse> ok() {
    return x -> Responses.ok();
  }

  public static Function<HttpRequest, HttpResponse> ok(String body) {
    return ok(asByteBuffer(body));
  }

  public static Function<HttpRequest, HttpResponse> ok(ByteBuffer body) {
    return ok(request -> body);
  }

  public static Function<HttpRequest, HttpResponse> ok(Function<HttpRequest, ByteBuffer> handler) {
    return handler.andThen(Responses::ok);
  }
  
  public static Function<HttpRequest, HttpResponse> created(String body) {
    return created(asByteBuffer(body));
  }
  
  public static Function<HttpRequest, HttpResponse> created(ByteBuffer body) {
    return created(request -> body);
  }
  
  public static Function<HttpRequest, HttpResponse> created(Function<HttpRequest, ByteBuffer> handler) {
    return handler.andThen(Responses::created);
  }
  
  public static <T> Function<T, HttpResponse> noContent() {
    return x -> Responses.noContent();
  }
  
  public static <T> Function<T, HttpResponse> forbidden() {
    return x -> Responses.forbidden();
  }

  public static Function<HttpRequest, HttpResponse> badRequest(String body) {
    return badRequest(asByteBuffer(body));
  }

  public static Function<HttpRequest, HttpResponse> badRequest(ByteBuffer body) {
    return badRequest(request -> body);
  }

  public static Function<HttpRequest, HttpResponse> badRequest(Function<HttpRequest, ByteBuffer> handler) {
    return handler.andThen(Responses::badRequest);
  }

  public static <T> Function<T, HttpResponse> notFound() {
    return x -> Responses.notFound();
  }

  public static Function<HttpRequest, HttpResponse> notFound(String body) {
    return notFound(asByteBuffer(body));
  }

  public static Function<HttpRequest, HttpResponse> notFound(ByteBuffer body) {
    return notFound(request -> body);
  }

  public static Function<HttpRequest, HttpResponse> notFound(Function<HttpRequest, ByteBuffer> handler) {
    return handler.andThen(Responses::notFound);
  }

  public static <T> Function<T, HttpResponse> error() {
    return x -> Responses.error();
  }

  public static Function<HttpRequest, HttpResponse> error(String body) {
    return error(asByteBuffer(body));
  }

  public static Function<HttpRequest, HttpResponse> error(ByteBuffer body) {
    return error(request -> body);
  }
  
  public static Function<HttpRequest, HttpResponse> error(Function<HttpRequest, ByteBuffer> handler) {
    return handler.andThen(Responses::error);
  }
  
  public static UnaryOperator<HttpResponse> contentType(String value) {
    return response -> response.withHeader("Content-type", value);
  }
  
  public static UnaryOperator<HttpResponse> contentPlain() {
    return contentType("text/plain");
  }
  
  public static UnaryOperator<HttpResponse> contentJson() {
    return contentType("application/json");
  }
  
  public static UnaryOperator<HttpResponse> contentXml() {
    return contentType("text/xml");
  }

  public static Function<HttpRequest, HttpResponse> delegate(HttpService service) {
    return dropOneLevel().andThen(service::execute).andThen(orElse(Responses::notFound));
  }
  
  public static <T, U, R> Function<HttpRequest, BiTupple<T, U>> join(Function<HttpRequest, T> beginT, 
                                                                     Function<HttpRequest, U> beginU) {
    return request -> new BiTupple<>(beginT.apply(request), beginU.apply(request));
  }
  
  public static <T, U, R> Function<BiTupple<T, U>, R> split(BiFunction<T, U, R> function) {
    return tupple -> function.apply(tupple.get1(), tupple.get2());
  }
  
  public static <T> Function<HttpRequest, T> force(Supplier<T> supplier) {
    return value -> supplier.get();
  }
  
  public static <T> Function<T, Void> force(Consumer<T> consumer) {
    return value -> { consumer.accept(value); return null; };
  }

  public static <T> Function<Optional<T>, T> orElse(Supplier<T> supplier) {
    return optional -> optional.orElseGet(supplier);
  }

  public static <T, R> Function<Optional<T>, Optional<R>> map(Function<T, R> mapper) {
    return optional -> optional.map(mapper);
  }
  
  private static UnaryOperator<HttpRequest> dropOneLevel() {
    return request -> request.dropOneLevel();
  }
  
  private static final class BiTupple<T, U> {
    private final T t;
    private final U u;

    public BiTupple(T t, U u) {
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
