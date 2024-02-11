module com.github.tonivade.zeromock.junit4 {
  exports com.github.tonivade.zeromock.junit4;

  requires transitive com.github.tonivade.purefun;
  requires transitive com.github.tonivade.purefun.core;
  requires com.github.tonivade.purefun.effect;
  requires com.github.tonivade.purefun.monad;
  requires transitive com.github.tonivade.purefun.typeclasses;
  requires transitive com.github.tonivade.zeromock.api;
  requires transitive com.github.tonivade.zeromock.client;
  requires transitive com.github.tonivade.zeromock.server;
  requires transitive junit;
}