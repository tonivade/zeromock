/*
 * Copyright (c) 2018-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

import com.github.tonivade.purefun.Function1;
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
  
  public static <T> Function1<Bytes, Try<T>> xmlToObject(Class<T> clazz) {
    return bytes -> Try.of(() -> Deserializers.fromXml(bytes, clazz));
  }
  
  public static <T> Function1<Bytes, Try<T>> jsonToObject(Function1<String, T> deserializer) {
    return _jsonToObject(deserializer.liftTry());
  }
  
  public static <T> Function1<Bytes, Try<Option<T>>> jsonToObject(Class<T> clazz) {
    return _jsonToObject(fromJson(clazz));
  }
  
  public static <T> Function1<Bytes, Try<Option<T>>> jsonTo(Type type) {
    return _jsonToObject(fromJson(type));
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
  
  public static <T> Function1<Bytes, Try<T>> _jsonToObject(Function1<String, Try<T>> deserializer) {
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
}
