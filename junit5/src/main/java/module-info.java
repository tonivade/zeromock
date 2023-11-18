module com.github.tonivade.zeromock.junit5 {
  exports com.github.tonivade.zeromock.junit5;

  requires com.github.tonivade.purefun.annotation;
  requires com.github.tonivade.purefun.core;
  requires com.github.tonivade.purefun.effect;
  requires com.github.tonivade.purefun.monad;
  requires com.github.tonivade.purefun.typeclasses;
  requires com.github.tonivade.zeromock.api;
  requires com.github.tonivade.zeromock.client;
  requires com.github.tonivade.zeromock.server;
  requires jdk.httpserver;
  requires org.junit.jupiter.api;
  requires org.junit.platform.commons;
  requires org.opentest4j;
}