/*
 * Copyright (c) 2018-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static java.util.Objects.isNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.data.ImmutableArray;
import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.data.ImmutableMap;
import com.github.tonivade.purefun.data.ImmutableSet;
import com.github.tonivade.purefun.data.ImmutableTree;
import com.github.tonivade.purefun.data.ImmutableTreeMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import jakarta.xml.bind.DataBindingException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

public final class Deserializers {
  
  private Deserializers() {}
  
  public static Function1<Bytes, JsonElement> json() {
    return plain().andThen(asJson());
  }
  
  public static <T> Function1<Bytes, T> xmlToObject(Class<T> clazz) {
    return bytes -> Deserializers.fromXml(bytes, clazz);
  }
  
  public static <T> Function1<Bytes, T> jsonToObject(Function1<String, T> deserializer) {
    return plain().andThen(deserializer);
  }
  
  public static <T> Function1<Bytes, T> jsonToObject(Class<T> clazz) {
    return jsonToObject(fromJson(clazz));
  }
  
  public static <T> Function1<Bytes, T> jsonTo(Type type) {
    return jsonToObject(fromJson(type));
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
        .registerTypeAdapter(ImmutableList.class, DeserializerAdapters.IMMUTABLE_LIST)
        .registerTypeAdapter(ImmutableArray.class, DeserializerAdapters.IMMUTABLE_ARRAY)
        .registerTypeAdapter(ImmutableSet.class, DeserializerAdapters.IMMUTABLE_SET)
        .registerTypeAdapter(ImmutableTree.class, DeserializerAdapters.IMMUTABLE_TREE)
        .registerTypeAdapter(ImmutableMap.class, DeserializerAdapters.IMMUTABLE_MAP)
        .registerTypeAdapter(ImmutableTreeMap.class, DeserializerAdapters.IMMUTABLE_TREEMAP)
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

class DeserializerAdapters {

  static final JsonDeserializer<ImmutableList<?>> IMMUTABLE_LIST = 
      (json, typeOfT, context) -> ImmutableList.from(context.<List<?>>deserialize(json, as(List.class, typeOfT)));

  static final JsonDeserializer<ImmutableSet<?>> IMMUTABLE_SET = 
      (json, typeOfT, context) -> ImmutableSet.from(context.<List<?>>deserialize(json, as(List.class, typeOfT)));

  static final JsonDeserializer<ImmutableArray<?>> IMMUTABLE_ARRAY = 
      (json, typeOfT, context) -> ImmutableArray.from(context.<List<?>>deserialize(json, as(List.class, typeOfT)));

  static final JsonDeserializer<ImmutableTree<?>> IMMUTABLE_TREE = 
      (json, typeOfT, context) -> ImmutableTree.from(context.<List<?>>deserialize(json, as(List.class, typeOfT)));

  static final JsonDeserializer<ImmutableMap<?, ?>> IMMUTABLE_MAP = 
      (json, typeOfT, context) -> ImmutableMap.from(context.<Map<?, ?>>deserialize(json, as(Map.class, typeOfT)));

  static final JsonDeserializer<ImmutableTreeMap<?, ?>> IMMUTABLE_TREEMAP = 
      (json, typeOfT, context) -> ImmutableTreeMap.from(context.<NavigableMap<?, ?>>deserialize(json, as(NavigableMap.class, typeOfT)));

  private static Type as(Class<?> rawType, Type typeOfT) {
    return TypeToken.getParameterized(rawType, ((ParameterizedType) typeOfT).getActualTypeArguments()).getType();
  }
}
