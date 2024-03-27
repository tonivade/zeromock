/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

import com.github.tonivade.purefun.core.Function1;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.type.Try;
import com.github.tonivade.purejson.JsonNode;
import com.github.tonivade.purejson.PureJson;

import jakarta.xml.bind.DataBindingException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

public final class Deserializers {

  private Deserializers() {}

  public static Function1<Bytes, Try<JsonNode>> json() {
    return plain().andThen(asJson());
  }

  @SafeVarargs
  public static <T> Function1<Bytes, Try<T>> xmlToObject(T...reified) {
    return xmlToObject(getClassOf(reified));
  }

  public static <T> Function1<Bytes, Try<T>> xmlToObject(Class<T> clazz) {
    return bytes -> Try.of(() -> Deserializers.fromXml(bytes, clazz));
  }

  public static <T> Function1<Bytes, Try<T>> jsonToObject(Function1<String, T> deserializer) {
    return toObject(deserializer.liftTry());
  }

  @SafeVarargs
  public static <T> Function1<Bytes, Try<Option<T>>> jsonToObject(T... reified) {
    return jsonToObject(getClassOf(reified));
  }

  public static <T> Function1<Bytes, Try<Option<T>>> jsonToObject(Class<T> clazz) {
    return toObject(fromJson(clazz));
  }

  public static <T> Function1<Bytes, Try<Option<T>>> jsonTo(Type type) {
    return toObject(fromJson(type));
  }

  public static Function1<Bytes, String> plain() {
    return Bytes::asString;
  }

  private static Function1<String, Try<JsonNode>> asJson() {
    return PureJson::parse;
  }

  private static <T> Function1<String, Try<Option<T>>> fromJson(Type type) {
    return json -> fromJson(json, type);
  }

  public static <T> Function1<Bytes, Try<T>> toObject(Function1<String, Try<T>> deserializer) {
    return plain().andThen(deserializer);
  }

  @SuppressWarnings("unchecked")
  private static <T> T fromXml(Bytes bytes, Class<T> clazz) {
    try (InputStream input = new ByteArrayInputStream(bytes.toArray())) {
      JAXBContext context = JAXBContext.newInstance(clazz);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      return (T) unmarshaller.unmarshal(input);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (JAXBException e) {
      throw new DataBindingException(e);
    }
  }

  private static <T> Try<Option<T>> fromJson(String json, Type type) {
    return new PureJson<T>(type).fromJson(json);
  }

  @SuppressWarnings("unchecked")
  private static <T> Class<T> getClassOf(T... reified) {
    if (reified.length > 0) {
      throw new IllegalArgumentException("do not pass arguments to this function, it's just a trick to get refied types");
    }
    return (Class<T>) reified.getClass().getComponentType();
  }
}
