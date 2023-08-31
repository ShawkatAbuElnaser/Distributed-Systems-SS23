package vs.lib.HTTP;

import java.io.IOException;

public class HttpServerRunner implements Runnable {
    private HttpServer httpServer;

    public HttpServerRunner(int port) throws IOException {
        this.httpServer = new HttpServer(port);
    }

    @Override
    public void run() {
        try {
            httpServer.start();
        } catch (IOException e) {
            System.err.println("HTTP-Server: Error starting the server: " + e.getMessage());
        }
    }

    public HttpServer getHttpServer() {
        return httpServer;
    }
}
