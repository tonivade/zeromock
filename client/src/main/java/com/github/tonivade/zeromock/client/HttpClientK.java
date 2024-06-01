/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.client;

import static com.github.tonivade.purefun.core.Precondition.check;
import static com.github.tonivade.purefun.core.Precondition.checkNonNull;
import static com.github.tonivade.zeromock.api.HttpMethod.HEAD;
import static com.github.tonivade.zeromock.api.HttpMethod.OPTIONS;
import static com.github.tonivade.zeromock.api.HttpMethod.PATCH;

import java.net.URI;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import com.github.tonivade.purefun.Kind;

import com.github.tonivade.purefun.type.Try;
import com.github.tonivade.purefun.typeclasses.Async;
import com.github.tonivade.purefun.typeclasses.For;
import com.github.tonivade.zeromock.api.Bytes;
import com.github.tonivade.zeromock.api.HttpHeaders;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpStatus;

public final class HttpClientK<F extends Kind<F, ?>> implements HttpClientOf<F> {

  private final URI baseUri;
  private final Async<F> monad;

  HttpClientK(String baseUrl, Async<F> monad) {
    this.baseUri = URI.create(baseUrl);
    check(baseUri::isAbsolute);
    this.monad = checkNonNull(monad);
  }

  public static <F extends Kind<F, ?>> HttpClientK<F> connectTo(String baseUrl, Async<F> monad) {
    return new HttpClientK<>(baseUrl, monad);
  }

  @Override
  public Kind<F, HttpResponse> request(HttpRequest request) {
    return For.with(monad)
        .then(createRequest(request))
        .flatMap(this::send)
        .flatMap(this::processResponse)
        .run();
  }

  private Kind<F, java.net.http.HttpRequest> createRequest(HttpRequest request) {
    return monad.later(() -> {
        var builder = java.net.http.HttpRequest.newBuilder().uri(URI.create(baseUri.toString() + request.toUrl()));

        switch (request.method()) {
        case GET -> builder.GET();
        case DELETE -> builder.DELETE();
        case POST -> builder.POST(BodyPublishers.ofByteArray(request.body().toArray()));
        case PUT -> builder.PUT(BodyPublishers.ofByteArray(request.body().toArray()));
        case PATCH -> builder.method(PATCH.name(), BodyPublishers.ofByteArray(request.body().toArray()));
        case HEAD -> builder.method(HEAD.name(), BodyPublishers.noBody());
        case OPTIONS -> builder.method(OPTIONS.name(), BodyPublishers.noBody());
        }

        for (var header : request.headers()) {
          builder.header(header.get1(), header.get2());
        }

        return builder.build();
      });
  }

  private Kind<F, java.net.http.HttpResponse<byte[]>> send(java.net.http.HttpRequest request) {
    return monad.async(consumer -> {
      var client = java.net.http.HttpClient.newHttpClient();
      var sendAsync = client.sendAsync(request, BodyHandlers.ofByteArray());
      sendAsync.whenComplete((response, error) -> consumer.accept(
            response != null ? Try.success(response) : Try.failure(error)));
    });
  }

  private Kind<F, HttpResponse> processResponse(java.net.http.HttpResponse<byte[]> response) {
    return monad.later(() -> {
      var fromCode = HttpStatus.fromCode(response.statusCode());
      var bytes = Bytes.fromArray(response.body());
      var headers = HttpHeaders.from(response.headers().map());
      return new HttpResponse(fromCode, bytes, headers);
    });
  }
}
