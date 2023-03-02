/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purejson.JsonDSL.array;
import static com.github.tonivade.purejson.JsonDSL.entry;
import static com.github.tonivade.purejson.JsonDSL.object;
import static com.github.tonivade.purejson.JsonDSL.string;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.util.stream.Stream;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.type.Try;
import com.github.tonivade.purejson.JsonDSL;
import com.github.tonivade.purejson.JsonNode;
import com.github.tonivade.purejson.PureJson;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

public final class Serializers {

  private Serializers() {}

  public static <T> Function1<T, Try<Bytes>> empty() {
    return value -> Try.success(Bytes.empty());
  }

  public static Function1<Throwable, Try<Bytes>> throwableToJson() {
    return _objectToJson(Serializers::toJson);
  }

  public static <T> Function1<T, Try<Bytes>> objectToJson(Type type) {
    return _objectToJson(x -> Serializers.toJson(x, type));
  }

  public static <T> Function1<T, Try<Bytes>> objectToJson(Function1<T, String> serializer) {
    return _objectToJson(serializer.liftTry());
  }

  public static <T> Function1<T, Try<Bytes>> objectToXml() {
    return objectToXml(Serializers::toXml);
  }

  public static <T> Function1<T, Try<Bytes>> objectToXml(Function1<T, String> serializer) {
    return _objectToXml(serializer.liftTry());
  }

  public static <T> Function1<T, Bytes> plain() {
    return obj -> Bytes.asBytes(obj.toString());
  }

  private static Try<String> toJson(Throwable error) {
    return PureJson.serialize(throwableToJson(error));
  }

  private static <T> Try<String> toJson(T value, Type type) {
    return new PureJson<>(type).toString(value);
  }

  private static <T> Function1<T, Try<Bytes>> _objectToJson(Function1<T, Try<String>> serializer) {
    return serializer.andThen(x -> x.map(Bytes::asBytes));
  }

  private static <T> Function1<T, Try<Bytes>> _objectToXml(Function1<T, Try<String>> serializer) {
    return serializer.andThen(x -> x.map(Bytes::asBytes));
  }

  private static <T> String toXml(T value) {
    try {
      JAXBContext context = JAXBContext.newInstance(value.getClass());
      StringWriter writer = new StringWriter();
      Marshaller marshaller = context.createMarshaller();
      marshaller.marshal(value, writer);
      return writer.toString();
    } catch (JAXBException e) {
      throw new UncheckedIOException(new IOException(e));
    }
  }

  private static JsonNode throwableToJson(Throwable error) {
    return object(
        entry("type", string(error.getClass().getName())),
        entry("message", string(error.getMessage())),
        entry("trace", stacktrace(error.getStackTrace()))
    );
  }

  private static JsonNode stacktrace(StackTraceElement[] stackTrace) {
    return array(Stream.of(stackTrace).map(Object::toString).map(JsonDSL::string).toArray(JsonNode[]::new));
  }
}