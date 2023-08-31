package vs.lib.HTTP;

import java.io.IOException;

public class HttpClientRunner implements Runnable {
    private HttpClient httpClient;
    private int numRequests;
    private int first_wait;

    public HttpClientRunner(String address, int port) throws IOException {
        this.httpClient = new HttpClient(address, port);
        this.numRequests = Integer.parseInt(System.getenv("NUM_REQUESTS"));
        this.first_wait = Integer.parseInt(System.getenv("FIRST_WAIT"));

    }

    @Override
    public void run() {
        try {
            Thread.sleep(first_wait);

            System.out.println("HTTP Client is running");
            httpClient.makeRequest(numRequests);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setNumRequests(int numRequests) {
        this.numRequests = numRequests;
    }
}