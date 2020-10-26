/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.data.ImmutableArray;
import com.github.tonivade.purefun.data.ImmutableArray.JavaBasedImmutableArray;
import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.data.ImmutableList.JavaBasedImmutableList;
import com.github.tonivade.purefun.data.ImmutableMap;
import com.github.tonivade.purefun.data.ImmutableMap.JavaBasedImmutableMap;
import com.github.tonivade.purefun.data.ImmutableSet;
import com.github.tonivade.purefun.data.ImmutableSet.JavaBasedImmutableSet;
import com.github.tonivade.purefun.data.ImmutableTree;
import com.github.tonivade.purefun.data.ImmutableTree.JavaBasedImmutableTree;
import com.github.tonivade.purefun.data.ImmutableTreeMap;
import com.github.tonivade.purefun.data.ImmutableTreeMap.JavaBasedImmutableTreeMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;

public final class Serializers {

  private Serializers() {}

  public static <T> Function1<T, Bytes> empty() {
    return Function1.cons(Bytes.empty());
  }

  public static Function1<Throwable, Bytes> throwableToJson() {
    return throwableToJson(Serializers::toJson);
  }

  public static Function1<Throwable, Bytes> throwableToJson(Function1<Throwable, String> serializer) {
    return serializer.andThen(Bytes::asBytes);
  }

  public static <T> Function1<T, Bytes> objectToJson() {
    return objectToJson(Serializers::toJson);
  }

  public static <T> Function1<T, Bytes> objectToJson(Function1<T, String> serializer) {
    return serializer.andThen(Bytes::asBytes);
  }

  public static <T> Function1<T, Bytes> objectToXml() {
    return objectToXml(Serializers::toXml);
  }

  public static <T> Function1<T, Bytes> objectToXml(Function1<T, String> serializer) {
    return serializer.andThen(Bytes::asBytes);
  }

  public static <T> Function1<T, Bytes> plain() {
    return Function1.<T, String>of(Object::toString).andThen(Bytes::asBytes);
  }

  private static <T> String toJson(T value) {
    return buildGson().toJson(value);
  }

  private static String toJson(Throwable error) {
    return toJson(throwableToJson(error));
  }

  private static JsonObject throwableToJson(Throwable error) {
    JsonObject json = new JsonObject();
    json.addProperty("type", error.getClass().getName());
    json.addProperty("message", error.getMessage());
    json.add("trace", stacktrace(error.getStackTrace()));
    return json;
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

  private static JsonElement stacktrace(StackTraceElement[] stackTrace) {
    JsonArray array = new JsonArray();
    for (StackTraceElement stackTraceElement : stackTrace) {
      array.add(stackTraceElement.toString());
    }
    return array;
  }

  private static Gson buildGson() {
    return new GsonBuilder()
        .registerTypeAdapter(JavaBasedImmutableList.class, SerializerAdapters.IMMUTABLE_LIST)
        .registerTypeAdapter(JavaBasedImmutableArray.class, SerializerAdapters.IMMUTABLE_ARRAY)
        .registerTypeAdapter(JavaBasedImmutableSet.class, SerializerAdapters.IMMUTABLE_SET)
        .registerTypeAdapter(JavaBasedImmutableTree.class, SerializerAdapters.IMMUTABLE_TREE)
        .registerTypeAdapter(JavaBasedImmutableMap.class, SerializerAdapters.IMMUTABLE_MAP)
        .registerTypeAdapter(JavaBasedImmutableTreeMap.class, SerializerAdapters.IMMUTABLE_TREEMAP)
        .create();
  }
}

class SerializerAdapters {

  static final JsonSerializer<ImmutableList<?>> IMMUTABLE_LIST = 
      (src, type, context) -> context.serialize(src.toList());

  static final JsonSerializer<ImmutableSet<?>> IMMUTABLE_SET = 
      (src, type, context) -> context.serialize(src.toSet());

  static final JsonSerializer<ImmutableArray<?>> IMMUTABLE_ARRAY = 
      (src, type, context) -> context.serialize(src.toList());

  static final JsonSerializer<ImmutableTree<?>> IMMUTABLE_TREE = 
      (src, type, context) -> context.serialize(src.toNavigableSet());

  static final JsonSerializer<ImmutableMap<?, ?>> IMMUTABLE_MAP = 
      (src, type, context) -> context.serialize(src.toMap());

  static final JsonSerializer<ImmutableTreeMap<?, ?>> IMMUTABLE_TREEMAP = 
      (src, type, context) -> context.serialize(src.toNavigableMap());
}