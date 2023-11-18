module com.github.tonivade.zeromock.junit4 {
  exports com.github.tonivade.zeromock.junit4;

  requires com.github.tonivade.purefun.annotation;
  requires com.github.tonivade.purefun.core;
  requires com.github.tonivade.purefun.effect;
  requires com.github.tonivade.purefun.monad;
  requires com.github.tonivade.purefun.typeclasses;
  requires com.github.tonivade.zeromock.api;
  requires com.github.tonivade.zeromock.client;
  requires com.github.tonivade.zeromock.server;
  requires junit;
}