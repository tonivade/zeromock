/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.Function1.fail;
import static com.github.tonivade.zeromock.api.Matchers.all;
import static com.github.tonivade.zeromock.api.Matchers.startsWith;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.Function2;
import com.github.tonivade.purefun.Higher1;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.PartialFunction1;
import com.github.tonivade.purefun.type.Option;

public final class HttpServiceK<F extends Kind> {

  private final String name;
  private final PartialFunction1<HttpRequest, Higher1<F, HttpResponse>> mappings;

  public HttpServiceK(String name) {
    this(name, PartialFunction1.of(Matcher1.never(), fail(IllegalStateException::new)));
  }

  private HttpServiceK(String name, PartialFunction1<HttpRequest, Higher1<F, HttpResponse>> mappings) {
    this.name = requireNonNull(name);
    this.mappings = requireNonNull(mappings);
  }

  public String name() {
    return name;
  }

  public HttpServiceK<F> mount(String path, HttpServiceK<F> other) {
    return addMapping(
        startsWith(path).and(req -> other.mappings.isDefinedAt(req.dropOneLevel())),
        req -> other.mappings.apply(req.dropOneLevel()));
  }

  public HttpServiceK<F> exec(RequestHandlerK<F> handler) {
    return addMapping(all(), handler);
  }

  public HttpServiceK<F> add(Matcher1<HttpRequest> matcher, RequestHandlerK<F> handler) {
    return addMapping(matcher, handler);
  }

  public MappingBuilderK<F, HttpServiceK<F>> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilderK<>(this::add).when(matcher);
  }

  public Option<Higher1<F, HttpResponse>> execute(HttpRequest request) {
    return mappings.lift().apply(request);
  }

  public HttpServiceK<F> combine(HttpServiceK<F> other) {
    return new HttpServiceK<>(this.name + "+" + other.name, this.mappings.orElse(other.mappings));
  }

  private HttpServiceK<F> addMapping(Matcher1<HttpRequest> matcher, RequestHandlerK<F> handler) {
    return new HttpServiceK<>(name, mappings.orElse(PartialFunction1.of(matcher, handler::apply)));
  }

  public static final class MappingBuilderK<F extends Kind, T> {
    private final Function2<Matcher1<HttpRequest>, RequestHandlerK<F>, T> finisher;
    private Matcher1<HttpRequest> matcher;

    public MappingBuilderK(Function2<Matcher1<HttpRequest>, RequestHandlerK<F>, T> finisher) {
      this.finisher = requireNonNull(finisher);
    }

    public MappingBuilderK<F, T> when(Matcher1<HttpRequest> matcher) {
      this.matcher = matcher;
      return this;
    }

    public T then(RequestHandlerK<F> handler) {
      return finisher.apply(matcher, handler);
    }
  }
}
