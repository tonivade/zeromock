/*
 * Copyright (c) 2018-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.client;

import static com.github.tonivade.zeromock.api.Bytes.asBytes;
import static com.github.tonivade.zeromock.api.HttpStatus.BAD_REQUEST;
import static java.util.Objects.requireNonNull;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Witness;
import com.github.tonivade.purefun.data.NonEmptyString;
import com.github.tonivade.purefun.typeclasses.For;
import com.github.tonivade.purefun.typeclasses.MonadDefer;
import com.github.tonivade.zeromock.api.Bytes;
import com.github.tonivade.zeromock.api.HttpHeaders;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpStatus;

public class HttpClientK<F extends Witness> {

  private final NonEmptyString baseUrl;
  private final MonadDefer<F> monad;

  protected HttpClientK(String baseUrl, MonadDefer<F> monad) {
    this.baseUrl = NonEmptyString.of(baseUrl);
    this.monad = requireNonNull(monad);
  }

  public static <F extends Witness> HttpClientK<F> connectTo(String baseUrl, MonadDefer<F> monad) {
    return new HttpClientK<>(baseUrl, monad);
  }

  public Kind<F, HttpResponse> request(HttpRequest request) {
    return For.with(monad)
        .then(createConnection(request))
        .flatMap(this::connect)
        .flatMap(this::processResponse)
        .run();
  }

  private Kind<F, HttpURLConnection> connect(HttpURLConnection connection) {
    return monad.later(() -> {
      connection.connect();
      return connection;
    });
  }

  private Kind<F, HttpURLConnection> createConnection(HttpRequest request) {
    return monad.later(() -> {
        URL url = new URL(baseUrl.get() + request.toUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(request.method().name());
        request.headers().forEach(connection::setRequestProperty);
        if (!request.body().isEmpty()) {
          connection.setDoOutput(true);
          try (OutputStream output = connection.getOutputStream()) {
            output.write(request.body().toArray());
          }
        }
        return connection;
      });
  }

  private Kind<F, HttpResponse> processResponse(HttpURLConnection connection) {
    return For.with(monad)
        .then(status(connection))
        .then(body(connection))
        .then(headers(connection))
        .apply(HttpResponse::new);
  }

  private Kind<F, HttpHeaders> headers(HttpURLConnection connection) {
    return monad.later(() -> HttpHeaders.from(connection.getHeaderFields()));
  }

  private Kind<F, HttpStatus> status(HttpURLConnection connection) {
    return monad.later(() -> HttpStatus.fromCode(connection.getResponseCode()));
  }

  private Kind<F, Bytes> body(HttpURLConnection connection) {
    return monad.later(() -> {
      Bytes body = Bytes.empty();
      if (connection.getContentLength() > 0) {
        if (connection.getResponseCode() < BAD_REQUEST.code()) {
          body = asBytes(connection.getInputStream());
        } else {
          body = asBytes(connection.getErrorStream());
        }
      }
      return body;
    });
  }
}
