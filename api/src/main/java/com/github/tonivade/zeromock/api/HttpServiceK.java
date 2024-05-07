/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.core.Function1.fail;
import static com.github.tonivade.purefun.core.Matcher1.never;
import static com.github.tonivade.purefun.core.Precondition.checkNonNull;
import static com.github.tonivade.zeromock.api.Matchers.all;
import static com.github.tonivade.zeromock.api.Matchers.startsWith;
import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static com.github.tonivade.zeromock.api.Responses.notFound;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.core.Function1;
import com.github.tonivade.purefun.core.Matcher1;
import com.github.tonivade.purefun.core.PartialFunction1;
import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.type.OptionOf;
import com.github.tonivade.purefun.typeclasses.For;
import com.github.tonivade.purefun.typeclasses.Instances;
import com.github.tonivade.purefun.typeclasses.Monad;

public final class HttpServiceK<F> implements HttpRouteBuilderK<F, HttpServiceK<F>> {

  private final String name;
  private final Monad<F> monad;
  private final PartialFunction1<HttpRequest, Kind<F, HttpResponse>> mappings;
  private final Function1<HttpRequest, Kind<F, Either<HttpResponse, HttpRequest>>> preFilters;
  private final Function1<HttpResponse, Kind<F, HttpResponse>> postFilters;

  public HttpServiceK(String name, Monad<F> monad) {
    this(name, monad,
        PartialFunction1.of(never(), fail(IllegalStateException::new)),
        request -> monad.pure(Either.right(request)),
        monad::<HttpResponse>pure);
  }

  private HttpServiceK(String name, Monad<F> monad,
                       PartialFunction1<HttpRequest, Kind<F, HttpResponse>> mappings,
                       Function1<HttpRequest, Kind<F, Either<HttpResponse, HttpRequest>>> preFilters,
                       Function1<HttpResponse, Kind<F, HttpResponse>> postFilters) {
    this.name = checkNonNull(name);
    this.monad = checkNonNull(monad);
    this.mappings = checkNonNull(mappings);
    this.preFilters = checkNonNull(preFilters);
    this.postFilters = checkNonNull(postFilters);
  }

  public String name() {
    return name;
  }

  Monad<F> monad() {
    return monad;
  }

  public HttpServiceK<F> mount(String path, HttpServiceK<F> other) {
    checkNonNull(path);
    checkNonNull(other);
    return addMapping(
        startsWith(path).and(req -> other.mappings.isDefinedAt(req.dropOneLevel())),
        req -> monad.map(other.execute(req.dropOneLevel()), option -> option.getOrElse(notFound())));
  }

  public HttpServiceK<F> exec(RequestHandlerK<F> handler) {
    return addMapping(all(), handler);
  }

  @Override
  public ThenStepK<F, HttpServiceK<F>> when(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(monad, handler -> addMapping(matcher, handler));
  }

  public ThenStepK<F, HttpServiceK<F>> preFilter(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(monad, handler -> addPreFilter(matcher, handler));
  }

  public HttpServiceK<F> preFilter(PreFilterK<F> filter) {
    return addPreFilter(checkNonNull(filter));
  }

  public HttpServiceK<F> postFilter(PostFilterK<F> filter) {
    return addPostFilter(checkNonNull(filter));
  }

  public Kind<F, Option<HttpResponse>> execute(HttpRequest request) {
    Function1<HttpRequest, Option<Kind<F, HttpResponse>>> mappingsWithPostFilters =
        mappings.andThen(value -> monad.flatMap(value, postFilters::apply)).lift();

    return For.with(monad)
        .then(preFilters.apply(request))
        .flatMap(either -> either.fold(
            res -> monad.pure(Option.some(res)),
            mappingsWithPostFilters.andThen(option -> Instances.<Option<?>>traverse().sequence(monad, option))))
        .map(OptionOf::toOption)
        .run();
  }

  public HttpServiceK<F> combine(HttpServiceK<F> other) {
    checkNonNull(other);
    return new HttpServiceK<>(
        this.name + "+" + other.name,
        this.monad,
        this.mappings.orElse(other.mappings),
        this.preFilters.andThen(
            value -> monad.flatMap(value,
                either -> either.fold(
                    response -> monad.pure(Either.left(response)), other.preFilters))),
        this.postFilters.andThen(value -> monad.flatMap(value, other.postFilters))::apply
    );
  }

  public HttpServiceK<F> addMapping(Matcher1<HttpRequest> matcher, RequestHandlerK<F> handler) {
    checkNonNull(matcher);
    checkNonNull(handler);
    return new HttpServiceK<>(
        this.name,
        this.monad,
        this.mappings.orElse(PartialFunction1.of(matcher, handler::apply)),
        this.preFilters,
        this.postFilters
    );
  }

  public HttpServiceK<F> addPreFilter(Matcher1<HttpRequest> matcher, RequestHandlerK<F> handler) {
    return addPreFilter(filter(monad, matcher, handler));
  }

  private HttpServiceK<F> addPreFilter(PreFilterK<F> filter) {
    checkNonNull(filter);
    return new HttpServiceK<>(
        this.name,
        this.monad,
        this.mappings,
        this.preFilters.andThen(
            value -> monad.flatMap(value,
                either -> either.fold(
                    response -> monad.pure(Either.left(response)), filter))),
        this.postFilters
    );
  }

  private HttpServiceK<F> addPostFilter(PostFilterK<F> filter) {
    checkNonNull(filter);
    return new HttpServiceK<>(
        this.name,
        this.monad,
        this.mappings,
        this.preFilters,
        this.postFilters.andThen(value -> monad.flatMap(value, filter))::apply
    );
  }
}
