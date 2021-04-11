package com.github.tonivade.zeromock.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Type;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.type.Try;
import com.github.tonivade.purejson.TypeToken;

class JsonTest {

  private final Type type = new TypeToken<ImmutableList<Data>>() {}.getType();
  
  @Test
  void serializeDeserialize() {
    ImmutableList<Data> listOf = Sequence.listOf(new Data(1, "toni"));

    Try<Bytes> bytes = Serializers.objectToJson(type).apply(listOf);
    
    assertEquals(listOf, Deserializers.jsonTo(type).apply(bytes.getOrElseThrow()));
  }
}

class Data {
  private final int id;
  private final String name;

  public Data(int id, String name) {
    this.id = id;
    this.name = name;
  }
  
  public int getId() {
    return id;
  }
  
  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Data other = (Data) obj;
    return Objects.equals(name, other.name);
  }

  @Override
  public String toString() {
    return new StringBuilder().append("Data [id=").append(id).append(", name=").append(name).append("]").toString();
  }
}
