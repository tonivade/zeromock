package com.github.tonivade.zeromock.client;

import com.github.tonivade.purefun.Kind;

import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;

interface HttpClientOf<F extends Kind<F, ?>> {

  Kind<F, HttpResponse> request(HttpRequest request);

  // XXX: it would be nice to define here the method request, but for HttpClient that
  // returns a naked HttpResponse it would be weird to return an Id<HttpResponse>

  @SuppressWarnings("unchecked")
  static <T> Class<T> getClassOf(T... reified) {
    if (reified.length > 0) {
      throw new IllegalArgumentException("do not pass arguments to this function, it's just a trick to get refied types");
    }
    return (Class<T>) reified.getClass().getComponentType();
  }
}
