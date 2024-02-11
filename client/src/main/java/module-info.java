module com.github.tonivade.zeromock.client {
  exports com.github.tonivade.zeromock.client;

  requires transitive com.github.tonivade.purefun;
  requires transitive com.github.tonivade.purefun.core;
  requires com.github.tonivade.purefun.effect;
  requires com.github.tonivade.purefun.monad;
  requires transitive com.github.tonivade.purefun.typeclasses;
  requires com.github.tonivade.purejson;
  requires transitive com.github.tonivade.zeromock.api;
  requires transitive java.net.http;
}