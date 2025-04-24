import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * <p>作者： zcq</p>
 * <p>文件名称: SimpleHttpServer </p>
 * <p>描述: [类型描述] </p>
 * <p>创建时间: 2025/4/23 </p>
 *
 * @author <a href="mail to: 2928235428@qq.com" rel="nofollow">作者</a>
 * @version 1.0
 **/
public class SimpleHttpServer implements HttpHandler, AutoCloseable {


  public static void main(String[] args) {
    String host = "0.0.0.0";
    int port = 8080;
    try (SimpleHttpServer connector = new SimpleHttpServer(host, port)) {
      for (;;) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  final HttpServer httpServer;
  final String host;
  final int port;

  public SimpleHttpServer(String host, int port) throws IOException {
    this.host = host;
    this.port = port;
    this.httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);
    this.httpServer.createContext("/", this);
    this.httpServer.start();
  }

  @Override
  public void close() {
    this.httpServer.stop(3);
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String method = exchange.getRequestMethod();
    URI uri = exchange.getRequestURI();
    String path = uri.getPath();
    String query = uri.getRawQuery();
    Headers respHeaders = exchange.getResponseHeaders();
    respHeaders.set("Content-Type", "text/html; charset=utf-8");
    respHeaders.set("Cache-Control", "no-cache");
    // 设置200响应:
    exchange.sendResponseHeaders(200, 0);
    String s = "<h1>Hello, world.</h1><p>" + LocalDateTime.now().withNano(0) + "</p>";
    try (OutputStream out = exchange.getResponseBody()) {
      out.write(s.getBytes(StandardCharsets.UTF_8));
    }
  }
}
