package com.github.tonivade.zeromock.client;

import com.github.tonivade.purefun.annotation.Witness;

interface HttpClientOf<F extends Witness> {

  // XXX: it would be nice to define here the method request, but for HttpClient that
  // returns a naked HttpResponse it would be weird to return an Id<HttpResponse>
}
