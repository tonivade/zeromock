module com.github.tonivade.zeromock.server {
  exports com.github.tonivade.zeromock.server;

  requires transitive com.github.tonivade.purefun;
  requires transitive com.github.tonivade.purefun.core;
  requires com.github.tonivade.purefun.effect;
  requires com.github.tonivade.purefun.instances;
  requires com.github.tonivade.purefun.monad;
  requires transitive com.github.tonivade.purefun.typeclasses;
  requires transitive com.github.tonivade.zeromock.api;
  requires transitive jakarta.xml.bind;
  requires transitive jdk.httpserver;
  requires transitive org.slf4j;
}