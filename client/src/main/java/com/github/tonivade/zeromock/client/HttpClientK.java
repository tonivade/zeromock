/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.client;

import static com.github.tonivade.purefun.Precondition.check;
import static com.github.tonivade.purefun.Precondition.checkNonNull;
import static com.github.tonivade.zeromock.api.HttpMethod.HEAD;
import static com.github.tonivade.zeromock.api.HttpMethod.OPTIONS;
import static com.github.tonivade.zeromock.api.HttpMethod.PATCH;

import java.net.URI;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Witness;
import com.github.tonivade.purefun.type.Try;
import com.github.tonivade.purefun.typeclasses.Async;
import com.github.tonivade.purefun.typeclasses.For;
import com.github.tonivade.zeromock.api.Bytes;
import com.github.tonivade.zeromock.api.HttpHeaders;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpStatus;

public class HttpClientK<F extends Witness> {

  private final URI baseUri;
  private final Async<F> monad;

  protected HttpClientK(String baseUrl, Async<F> monad) {
    this.baseUri = URI.create(baseUrl);
    check(baseUri::isAbsolute);
    this.monad = checkNonNull(monad);
  }

  public static <F extends Witness> HttpClientK<F> connectTo(String baseUrl, Async<F> monad) {
    return new HttpClientK<>(baseUrl, monad);
  }

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
        case GET:
          builder = builder.GET();
          break;
        case DELETE:
          builder = builder.DELETE();
          break;
        case POST:
          builder = builder.POST(BodyPublishers.ofByteArray(request.body().toArray()));
          break;
        case PUT:
          builder = builder.PUT(BodyPublishers.ofByteArray(request.body().toArray()));
          break;
        case PATCH:
          builder = builder.method(PATCH.name(), BodyPublishers.ofByteArray(request.body().toArray()));
          break;
        case HEAD:
          builder = builder.method(HEAD.name(), BodyPublishers.noBody());
          break;
        case OPTIONS:
          builder = builder.method(OPTIONS.name(), BodyPublishers.noBody());
          break;
        }
        
        for (var header : request.headers()) {
          builder = builder.header(header.get1(), header.get2());
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
