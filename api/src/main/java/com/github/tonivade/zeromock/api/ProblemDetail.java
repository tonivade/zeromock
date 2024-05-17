/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.core.Precondition.checkNonNull;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.github.tonivade.purefun.Nullable;
import com.github.tonivade.purefun.data.ImmutableMap;

public record ProblemDetail(
    HttpStatus status,
    @Nullable
    String detail,
    @Nullable
    URI type,
    @Nullable
    String title,
    @Nullable
    URI instance,
    ImmutableMap<String, Object> properties) {

  public ProblemDetail {
    checkNonNull(status);
    checkNonNull(properties);
  }

  public static Builder builder(HttpStatus status) {
    return new Builder(status);
  }

  public static final class Builder {
    private final HttpStatus status;
    @Nullable
    private String detail;
    @Nullable
    private URI type;
    @Nullable
    private String title;
    @Nullable
    private URI instance;
    private Map<String, Object> properties = new HashMap<>();

    public Builder(HttpStatus status) {
      this.status = checkNonNull(status);
    }

    public Builder detail(String detail) {
      this.detail = detail;
      return this;
    }

    public Builder instance(URI instance) {
      this.instance = instance;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder type(URI type) {
      this.type = type;
      return this;
    }

    public Builder property(String key, Object value) {
      properties.put(key, value);
      return this;
    }

    public ProblemDetail build() {
      return new ProblemDetail(status, detail, type, title, instance, ImmutableMap.from(properties));
    }
  }
}