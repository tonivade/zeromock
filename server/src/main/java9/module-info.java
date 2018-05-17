module com.github.tonivade.zeromock.server {
  exports com.github.tonivade.zeromock.server.admin;
  exports com.github.tonivade.zeromock.server;

  requires com.github.tonivade.zeromock.core;
  requires equalizer;
  requires gson;
  requires java.logging;
  requires java.xml.bind;
  requires jdk.httpserver;
  requires org.junit.jupiter.api;
  requires org.opentest4j;
}
