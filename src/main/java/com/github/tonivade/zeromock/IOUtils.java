package com.github.tonivade.zeromock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class IOUtils {

  public static String readAll(InputStream input) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    while (true) {
      int read = input.read(buffer);
      if (read > 0) {
        out.write(buffer, 0, read);
      } else break;
    }
    return new String(out.toByteArray(), Charset.forName("UTF-8"));
  }
}
