/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.github.tonivade.zeromock.core.Kind.OptionalKind;

public class OptionalHandlerTest {
  
  @Test
  public void mapTest() {
    OptionalHandler<String, Integer> str2int = str -> OptionalKind.of(str.length());
    
    assertEquals(Optional.of(10), str2int.map(a -> a * 2).unbox().handle("asdfg"));
  }
  
  @Test
  public void mapEmptyTest() {
    OptionalHandler<String, Integer> str2int = str -> OptionalKind.empty();
    
    assertEquals(Optional.empty(), str2int.map(a -> a * 2).unbox().handle("asdfg"));
  }
  
  @Test
  public void orElseTest() {
    OptionalHandler<String, Integer> str2int = str -> OptionalKind.empty();
    
    assertEquals(Integer.valueOf(0), str2int.orElse(() -> 0).handle("asdfg"));
  }
  
  @Test
  public void flatMapTest() {
    OptionalHandler<String, Integer> str2int = str -> OptionalKind.of(str.length());
    OptionalHandler<Integer, Integer> doubleInt = a -> OptionalKind.of(a * 2);
    
    assertEquals(Optional.of(10), str2int.flatMap(doubleInt).unbox().handle("asdfg"));
  }
  
  @Test
  public void flatMapEmptyTest() {
    OptionalHandler<String, Integer> str2int = str -> OptionalKind.of(str.length());
    OptionalHandler<Integer, Integer> empty = a -> OptionalKind.empty();
    
    assertEquals(Optional.empty(), str2int.flatMap(empty).unbox().handle("asdfg"));
  }

}
