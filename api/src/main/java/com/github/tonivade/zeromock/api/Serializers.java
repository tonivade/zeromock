/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.core.Handler1.adapt;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.github.tonivade.zeromock.core.Handler1;
import com.google.gson.GsonBuilder;

public final class Serializers {
  
  private Serializers() {}
  
  public static <T> Handler1<T, Bytes> empty() {
    return adapt(Bytes::empty);
  }
  
  public static <T> Handler1<T, Bytes> json() {
    return Serializers.<T>asJson().andThen(Bytes::asBytes);
  }
  
  public static <T> Handler1<T, Bytes> xml() {
    return Serializers.<T>asXml().andThen(Bytes::asBytes);
  }

  public static <T> Handler1<T, Bytes> plain() {
    return Serializers.<T>asString().andThen(Bytes::asBytes);
  }
  
  private static <T> Handler1<T, String> asJson() {
    return value -> new GsonBuilder().create().toJson(value);
  }
  
  private static <T> Handler1<T, String> asXml() {
    return Serializers::toXml;
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
  
  private static <T> Handler1<T, String> asString() {
    return Object::toString;
  }
}