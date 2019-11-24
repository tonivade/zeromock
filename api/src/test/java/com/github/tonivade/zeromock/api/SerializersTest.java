/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.Bytes.asString;
import static com.github.tonivade.zeromock.api.Serializers.objectToJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.data.ImmutableArray;
import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.data.ImmutableMap;
import com.github.tonivade.purefun.data.ImmutableSet;
import com.github.tonivade.purefun.data.ImmutableTree;
import com.github.tonivade.purefun.data.ImmutableTreeMap;
import com.github.tonivade.purefun.data.Sequence;

public class SerializersTest {

  private final String expectedArray = "[\"a\",\"b\"]";
  private final String expectedObject = "{\"a\":\"b\"}";

  @Test
  public void testList() {
    Sequence<String> list = ImmutableList.of("a", "b");
    
    Bytes bytes = objectToJson().apply(list);
    
    assertEquals(expectedArray, asString(bytes));
  }

  @Test
  public void testSet() {
    Sequence<String> set = ImmutableSet.of("a", "b");
    
    Bytes bytes = objectToJson().apply(set);
    
    assertEquals(expectedArray, asString(bytes));
  }

  @Test
  public void testArray() {
    Sequence<String> array = ImmutableArray.of("a", "b");
    
    Bytes bytes = objectToJson().apply(array);
    
    assertEquals(expectedArray, asString(bytes));
  }

  @Test
  public void testTree() {
    Sequence<String> tree = ImmutableTree.of("a", "b");
    
    Bytes bytes = objectToJson().apply(tree);
    
    assertEquals(expectedArray, asString(bytes));
  }
  
  @Test
  public void testMap() {
    ImmutableMap<String, String> map = ImmutableMap.<String, String>builder().put("a", "b").build();
    
    Bytes bytes = objectToJson().apply(map);

    assertEquals(expectedObject, asString(bytes));
  }
  
  @Test
  public void testTreeMap() {
    ImmutableTreeMap<String, String> map = ImmutableTreeMap.<String, String>builder().put("a", "b").build();
    
    Bytes bytes = objectToJson().apply(map);

    assertEquals(expectedObject, asString(bytes));
  }
}
