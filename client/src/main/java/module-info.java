module com.github.tonivade.zeromock.client {
  exports com.github.tonivade.zeromock.client;

  requires com.github.tonivade.purefun.annotation;
  requires com.github.tonivade.purefun.core;
  requires com.github.tonivade.purefun.effect;
  requires com.github.tonivade.purefun.monad;
  requires com.github.tonivade.purefun.typeclasses;
  requires com.github.tonivade.purejson;
  requires com.github.tonivade.zeromock.api;
  requires java.net.http;
}