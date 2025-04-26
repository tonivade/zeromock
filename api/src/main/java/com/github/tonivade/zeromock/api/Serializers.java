/*
 * Copyright (c) 2018-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purejson.JsonDSL.array;
import static com.github.tonivade.purejson.JsonDSL.entry;
import static com.github.tonivade.purejson.JsonDSL.object;
import static com.github.tonivade.purejson.JsonDSL.string;
import static com.github.tonivade.purejson.JsonNode.NULL;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.util.stream.Stream;

import com.github.tonivade.purefun.core.Function1;
import com.github.tonivade.purefun.type.Try;
import com.github.tonivade.purejson.JsonDSL;
import com.github.tonivade.purejson.JsonNode;
import com.github.tonivade.purejson.PureJson;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

public final class Serializers {

  private Serializers() {}

  public static <T> Function1<T, Try<Bytes>> empty() {
    return value -> Try.success(Bytes.empty());
  }

  public static Function1<Throwable, Try<Bytes>> throwableToJson() {
    return toJson(Serializers::toJson);
  }

  @SafeVarargs
  public static <T> Function1<T, Try<Bytes>> objectToJson(T...reified) {
    return objectToJson(getClassOf(reified));
  }

  public static <T> Function1<T, Try<Bytes>> objectToJson(Type type) {
    return toJson(value -> Serializers.toJson(value, type));
  }

  public static <T> Function1<T, Try<Bytes>> objectToJson(Function1<T, String> serializer) {
    return toJson(serializer.liftTry());
  }

  public static <T> Function1<T, Try<Bytes>> objectToXml() {
    return objectToXml(Serializers::toXml);
  }

  public static <T> Function1<T, Try<Bytes>> objectToXml(Function1<T, String> serializer) {
    return toXml(serializer.liftTry());
  }

  public static <T> Function1<T, Bytes> plain() {
    return value -> Bytes.asBytes(value.toString());
  }

  private static Try<String> toJson(Throwable error) {
    return PureJson.serialize(throwableToJson(error));
  }

  private static <T> Try<String> toJson(T value, Type type) {
    return new PureJson<>(type).toString(value);
  }

  private static <T> Function1<T, Try<Bytes>> toJson(Function1<T, Try<String>> serializer) {
    return serializer.andThen(string -> string.map(Bytes::asBytes));
  }

  private static <T> Function1<T, Try<Bytes>> toXml(Function1<T, Try<String>> serializer) {
    return serializer.andThen(string -> string.map(Bytes::asBytes));
  }

  private static <T> String toXml(T value) {
    try {
      var context = JAXBContext.newInstance(value.getClass());
      var writer = new StringWriter();
      var marshaller = context.createMarshaller();
      marshaller.marshal(value, writer);
      return writer.toString();
    } catch (JAXBException e) {
      throw new UncheckedIOException(new IOException(e));
    }
  }

  private static JsonNode throwableToJson(Throwable error) {
    return object(
        entry("type", string(error.getClass().getName())),
        entry("message", error.getMessage() != null ? string(error.getMessage()) : NULL),
        entry("trace", stacktrace(error.getStackTrace()))
    );
  }

  private static JsonNode stacktrace(StackTraceElement[] stackTrace) {
    return array(Stream.of(stackTrace).map(Object::toString).map(JsonDSL::string).toArray(JsonNode[]::new));
  }

  @SuppressWarnings("unchecked")
  private static <T> Class<T> getClassOf(T... reified) {
    if (reified.length > 0) {
      throw new IllegalArgumentException("do not pass arguments to this function, it's just a trick to get refied types");
    }
    return (Class<T>) reified.getClass().getComponentType();
  }
}