module com.github.tonivade.zeromock.junit5 {
  exports com.github.tonivade.zeromock.junit5;

  requires com.github.tonivade.zeromock.core;
  requires com.github.tonivade.zeromock.server;
  requires equalizer;
  requires gson;
  requires org.junit.jupiter.api;
  requires org.junit.platform.commons;
  requires org.opentest4j;
}
