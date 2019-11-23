/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.data.ImmutableArray;
import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.data.ImmutableSet;
import com.github.tonivade.purefun.data.ImmutableTree;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public final class Deserializers {
  
  private Deserializers() {}
  
  public static Function1<Bytes, JsonElement> json() {
    return plain().andThen(asJson());
  }
  
  public static <T> Function1<Bytes, T> xmlToObject(Class<T> clazz) {
    return bytes -> Deserializers.<T>fromXml(bytes, clazz);
  }
  
  public static <T> Function1<Bytes, T> jsonToObject(Class<T> clazz) {
    return plain().andThen(fromJson(clazz));
  }
  
  public static <T> Function1<Bytes, T> jsonTo(Type type) {
    return plain().andThen(fromJson(type));
  }
  
  public static Function1<Bytes, String> plain() {
    return Bytes::asString;
  }
  
  private static Function1<String, JsonElement> asJson() {
    return new JsonParser()::parse;
  }
  
  private static <T> Function1<String, T> fromJson(Type type) {
    return json -> buildGson().fromJson(json, type);
  }

  private static Gson buildGson() {
    return new GsonBuilder()
        .registerTypeAdapter(ImmutableList.class, new ImmutableListAdapter())
        .registerTypeAdapter(ImmutableArray.class, new ImmutableArrayAdapter())
        .registerTypeAdapter(ImmutableSet.class, new ImmutableSetAdapter())
        .registerTypeAdapter(ImmutableTree.class, new ImmutableTreeAdapter())
        .create();
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
}

class ImmutableSetAdapter implements JsonDeserializer<ImmutableSet<?>> {

  @Override
  public ImmutableSet<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    Iterable<?> iterable = context.deserialize(json, Iterable.class);
    return ImmutableSet.from(iterable);
  }
}

class ImmutableArrayAdapter implements JsonDeserializer<ImmutableArray<?>> {

  @Override
  public ImmutableArray<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    Iterable<?> iterable = context.deserialize(json, Iterable.class);
    return ImmutableArray.from(iterable);
  }
}

class ImmutableTreeAdapter implements JsonDeserializer<ImmutableTree<?>> {

  @Override
  public ImmutableTree<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    Iterable<?> iterable = context.deserialize(json, Iterable.class);
    return ImmutableTree.from(iterable);
  }
}

class ImmutableListAdapter implements JsonDeserializer<ImmutableList<?>> {

  @Override
  public ImmutableList<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    Iterable<?> iterable = context.deserialize(json, Iterable.class);
    return ImmutableList.from(iterable);
  }
}