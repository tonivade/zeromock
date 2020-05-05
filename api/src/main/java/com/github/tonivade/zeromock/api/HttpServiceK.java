/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.Function1.fail;
import static com.github.tonivade.zeromock.api.Matchers.all;
import static com.github.tonivade.zeromock.api.Matchers.startsWith;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Function2;
import com.github.tonivade.purefun.Higher1;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.Operator1;
import com.github.tonivade.purefun.PartialFunction1;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.typeclasses.Functor;

public final class HttpServiceK<F extends Kind> {

  private final String name;
  private final Functor<F> functor;
  private final PartialFunction1<HttpRequest, Higher1<F, HttpResponse>> mappings;
  private final Operator1<HttpRequest> preFilters;
  private final Operator1<HttpResponse> postFilters;

  public HttpServiceK(String name, Functor<F> functor) {
    this(name, functor,
        PartialFunction1.of(Matcher1.never(), fail(IllegalStateException::new)),
        Function1.<HttpRequest>identity()::apply,
        Function1.<HttpResponse>identity()::apply);
  }

  private HttpServiceK(String name, Functor<F> functor,
                       PartialFunction1<HttpRequest, Higher1<F, HttpResponse>> mappings,
                       Operator1<HttpRequest> preFilters,
                       Operator1<HttpResponse> postFilters) {
    this.name = requireNonNull(name);
    this.functor = requireNonNull(functor);
    this.mappings = requireNonNull(mappings);
    this.preFilters = requireNonNull(preFilters);
    this.postFilters = requireNonNull(postFilters);
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
    return new MappingBuilderK<>(this::add).when(requireNonNull(matcher));
  }

  public HttpServiceK<F> preFilter(PreFilter filter) {
    return addPreFilter(requireNonNull(filter));
  }

  public HttpServiceK<F> postFilter(PostFilter filter) {
    return addPostFilter(requireNonNull(filter));
  }

  public Option<Higher1<F, HttpResponse>> execute(HttpRequest request) {
    return mappings.lift().apply(preFilters.apply(request))
        .map(response -> functor.map(response, postFilters::apply));
  }

  public HttpServiceK<F> combine(HttpServiceK<F> other) {
    return new HttpServiceK<>(
        this.name + "+" + other.name,
        this.functor,
        this.mappings.orElse(other.mappings),
        this.preFilters.andThen(other.preFilters)::apply,
        this.postFilters.andThen(other.postFilters)::apply
    );
  }

  private HttpServiceK<F> addMapping(Matcher1<HttpRequest> matcher, RequestHandlerK<F> handler) {
    return new HttpServiceK<>(
        this.name,
        this.functor,
        this.mappings.orElse(PartialFunction1.of(matcher, handler::apply)),
        this.preFilters,
        this.postFilters
    );
  }

  private HttpServiceK<F> addPreFilter(PreFilter filter) {
    return new HttpServiceK<>(
        this.name,
        this.functor,
        this.mappings,
        this.preFilters.andThen(filter::apply)::apply,
        this.postFilters
    );
  }

  private HttpServiceK<F> addPostFilter(PostFilter filter) {
    return new HttpServiceK<>(
        this.name,
        this.functor,
        this.mappings,
        this.preFilters,
        this.postFilters.andThen(filter::apply)::apply
    );
  }

  public static final class MappingBuilderK<F extends Kind, T> {
    private final Function2<Matcher1<HttpRequest>, RequestHandlerK<F>, T> finisher;
    private Matcher1<HttpRequest> matcher;

    public MappingBuilderK(Function2<Matcher1<HttpRequest>, RequestHandlerK<F>, T> finisher) {
      this.finisher = requireNonNull(finisher);
    }

    public MappingBuilderK<F, T> when(Matcher1<HttpRequest> matcher) {
      this.matcher = requireNonNull(matcher);
      return this;
    }

    public T then(RequestHandlerK<F> handler) {
      return finisher.apply(matcher, handler);
    }
  }
}
