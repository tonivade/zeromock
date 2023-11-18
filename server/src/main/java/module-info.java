module com.github.tonivade.zeromock.server {
  exports com.github.tonivade.zeromock.server;

  requires com.github.tonivade.purefun.annotation;
  requires com.github.tonivade.purefun.core;
  requires com.github.tonivade.purefun.effect;
  requires com.github.tonivade.purefun.instances;
  requires com.github.tonivade.purefun.monad;
  requires com.github.tonivade.purefun.typeclasses;
  requires com.github.tonivade.zeromock.api;
  requires jakarta.xml.bind;
  requires jdk.httpserver;
  requires org.slf4j;
}