package com.github.tonivade.zeromock.core;

import java.util.Optional;
import java.util.stream.Stream;

public interface Kind<M, T> {
  
  public static class OptionalKind<T> implements Kind<OptionalKind.µ, T> {
    
    private final Optional<T> value;
    
    public interface µ {};
    
    public OptionalKind(Optional<T> value) {
      this.value = value;
    }
    
    public static <V> OptionalKind<V> of(V value) {
      return new OptionalKind<>(Optional.of(value));
    }
    
    public static <V> OptionalKind<V> empty() {
      return new OptionalKind<>(Optional.empty());
    }
    
    public static <T> Optional<T> narrowKind(Kind<OptionalKind.µ, T> kind) {
      return ((OptionalKind<T>) kind).value;
    }
  }
  
  public static class StreamKind<T> implements Kind<StreamKind.µ, T> {
    private Stream<T> value;
    
    public interface µ {};
    
    public StreamKind(Stream<T> value) {
      this.value = value;
    }
    
    public static <V> StreamKind<V> of(V value) {
      return new StreamKind<>(Stream.of(value));
    }
    
    public static <V> StreamKind<V> empty() {
      return new StreamKind<>(Stream.empty());
    }
    
    public static <T> Stream<T> narrowKind(Kind<StreamKind.µ, T> kind) {
      return ((StreamKind<T>) kind).value;
    }
  }
}
