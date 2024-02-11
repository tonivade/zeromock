module com.github.tonivade.zeromock.api {
  exports com.github.tonivade.zeromock.api;

  requires transitive com.github.tonivade.purefun;
  requires transitive com.github.tonivade.purefun.core;
  requires transitive com.github.tonivade.purefun.effect;
  requires com.github.tonivade.purefun.instances;
  requires com.github.tonivade.purefun.monad;
  requires transitive com.github.tonivade.purefun.typeclasses;
  requires com.github.tonivade.purejson;
  requires jakarta.xml.bind;
  requires transitive java.xml;
  requires json.path;
}