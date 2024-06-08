module com.github.tonivade.zeromock.api {
  exports com.github.tonivade.zeromock.api;

  requires transitive com.github.tonivade.purefun;
  requires transitive com.github.tonivade.purefun.core;
  requires static com.github.tonivade.purefun.effect;
  requires static com.github.tonivade.purefun.monad;
  requires transitive com.github.tonivade.purefun.typeclasses;
  requires transitive com.github.tonivade.purejson;
  requires static jakarta.xml.bind;
  requires transitive java.xml;
  requires static json.path;
}