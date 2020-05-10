/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static java.util.Objects.isNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.data.ImmutableArray;
import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.data.ImmutableMap;
import com.github.tonivade.purefun.data.ImmutableSet;
import com.github.tonivade.purefun.data.ImmutableTree;
import com.github.tonivade.purefun.data.ImmutableTreeMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public final class Deserializers {
  
  private Deserializers() {}
  
  public static Function1<Bytes, JsonElement> json() {
    return plain().andThen(asJson());
  }
  
  public static <T> Function1<Bytes, T> xmlToObject(Class<T> clazz) {
    return bytes -> Deserializers.fromXml(bytes, clazz);
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
    return JsonParser::parseString;
  }
  
  private static <T> Function1<String, T> fromJson(Type type) {
    return json -> fromJson(json, type);
  }

  private static Gson buildGson() {
    return new GsonBuilder()
        .registerTypeAdapter(ImmutableList.class, new ImmutableListDeserializerAdapter())
        .registerTypeAdapter(ImmutableArray.class, new ImmutableArrayDeserializerAdapter())
        .registerTypeAdapter(ImmutableSet.class, new ImmutableSetDeserializerAdapter())
        .registerTypeAdapter(ImmutableTree.class, new ImmutableTreeDeserializerAdapter())
        .registerTypeAdapter(ImmutableMap.class, new ImmutableMapDeserializerAdapter())
        .registerTypeAdapter(ImmutableTreeMap.class, new ImmutableTreeMapDeserializerAdapter())
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

  private static <T> T fromJson(String json, Type type) {
    if (isNull(json) || json.isEmpty()) {
      throw new JsonSyntaxException("body cannot be null or empty");
    }
    return buildGson().fromJson(json, type);
  }
}

class ImmutableSetDeserializerAdapter implements JsonDeserializer<ImmutableSet<?>> {

  @Override
  public ImmutableSet<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    List<?> list = context.deserialize(json, List.class);
    return ImmutableSet.from(list);
  }
}

class ImmutableArrayDeserializerAdapter implements JsonDeserializer<ImmutableArray<?>> {

  @Override
  public ImmutableArray<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    List<?> list = context.deserialize(json, List.class);
    return ImmutableArray.from(list);
  }
}

class ImmutableTreeDeserializerAdapter implements JsonDeserializer<ImmutableTree<?>> {

  @Override
  public ImmutableTree<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    List<?> list = context.deserialize(json, List.class);
    return ImmutableTree.from(list);
  }
}

class ImmutableListDeserializerAdapter implements JsonDeserializer<ImmutableList<?>> {

  @Override
  public ImmutableList<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    List<?> list = context.deserialize(json, List.class);
    return ImmutableList.from(list);
  }
}

class ImmutableMapDeserializerAdapter implements JsonDeserializer<ImmutableMap<?, ?>> {

  @Override
  public ImmutableMap<?, ?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    Map<?, ?> map = context.deserialize(json, Map.class);
    return ImmutableMap.from(map);
  }
}

class ImmutableTreeMapDeserializerAdapter implements JsonDeserializer<ImmutableTreeMap<?, ?>> {

  @Override
  public ImmutableTreeMap<?, ?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    NavigableMap<?, ?> map = context.deserialize(json, NavigableMap.class);
    return ImmutableTreeMap.from(map);
  }
}