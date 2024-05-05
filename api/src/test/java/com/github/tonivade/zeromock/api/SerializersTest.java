/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.Bytes.asString;
import static com.github.tonivade.zeromock.api.Serializers.objectToJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Type;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.data.ImmutableArray;
import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.data.ImmutableMap;
import com.github.tonivade.purefun.data.ImmutableSet;
import com.github.tonivade.purefun.data.ImmutableTree;
import com.github.tonivade.purefun.data.ImmutableTreeMap;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.type.Try;
import com.github.tonivade.purejson.TypeToken;

public class SerializersTest {

  private final String expectedArray = "[\"a\",\"b\"]";
  private final String expectedObject = "{\"a\":\"b\"}";

  private final Type sequenceOfStrings = new TypeToken<ImmutableList<String>>() {}.getType();
  private final Type mapOfStrings = new TypeToken<ImmutableMap<String, String>>() {}.getType();

  @Test
  public void testList() {
    Sequence<String> list = ImmutableList.of("a", "b");
    
    Try<Bytes> bytes = objectToJson(sequenceOfStrings).apply(list);
    
    assertEquals(expectedArray, asString(bytes.getOrElseThrow()));
  }

  @Test
  public void testSet() {
    Sequence<String> set = ImmutableSet.of("a", "b");
    
    Try<Bytes> bytes = objectToJson(sequenceOfStrings).apply(set);
    
    assertEquals(expectedArray, asString(bytes.getOrElseThrow()));
  }

  @Test
  public void testArray() {
    Sequence<String> array = ImmutableArray.of("a", "b");
    
    Try<Bytes> bytes = objectToJson(sequenceOfStrings).apply(array);
    
    assertEquals(expectedArray, asString(bytes.getOrElseThrow()));
  }

  @Test
  public void testTree() {
    Sequence<String> tree = ImmutableTree.of("a", "b");
    
    Try<Bytes> bytes = objectToJson(sequenceOfStrings).apply(tree);
    
    assertEquals(expectedArray, asString(bytes.getOrElseThrow()));
  }
  
  @Test
  public void testMap() {
    ImmutableMap<String, String> map = ImmutableMap.<String, String>builder().put("a", "b").build();
    
    Try<Bytes> bytes = objectToJson(mapOfStrings).apply(map);

    assertEquals(expectedObject, asString(bytes.getOrElseThrow()));
  }
  
  @Test
  public void testTreeMap() {
    ImmutableTreeMap<String, String> map = ImmutableTreeMap.<String, String>builder().put("a", "b").build();
    
    Try<Bytes> bytes = objectToJson(mapOfStrings).apply(map);

    assertEquals(expectedObject, asString(bytes.getOrElseThrow()));
  }
}
