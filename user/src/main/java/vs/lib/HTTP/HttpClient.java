package vs.lib.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import vs.lib.RTT;

public class HttpClient {

    private String serverAddress;
    private int serverPort;
    private String accountString;
    private RTT rttMeasurer;
    private int waitingTime;
    private int waitingBeforeResult;

    public HttpClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.rttMeasurer = new RTT();
        this.waitingTime = Integer.parseInt(System.getenv("WAITING_TIME"));

        this.waitingBeforeResult = Integer.parseInt(System.getenv("WAIT_BEFORE_RESULT"));
    }

    public void makeRequest(int numRequests) throws IOException, InterruptedException {
        for (int i = 0; i < numRequests; i++) {
            Thread.sleep(waitingTime); // Pause for a certain amount of time: look at docker-compose file
            String randomPath = generateRandomPath();
            String randomMethod = generateRandomMethod();
            String requestBody = generateRandomRequestBody();

            sendRequest(randomMethod, randomPath, requestBody);
            // Performance: Calculating the total of all RTTs:
            rttMeasurer.setTotalRTT(rttMeasurer.getTotalRTT() + rttMeasurer.getCurrRTT());
        }
        rttMeasurer.measureAverageRTT(numRequests);
        Thread.sleep(waitingBeforeResult);
        rttMeasurer.printAvg();
    }

    private String generateRandomPath() {
        // Logic to generate a random request path
        String[] paths = { "/withdraw", "/deposit" };
        return paths[new Random().nextInt(paths.length)];
    }

    private String generateRandomMethod() {
        // Logic to generate a random request method
        String[] methods = { "GET", "POST" };
        return methods[new Random().nextInt(methods.length)];
    }

    private String generateRandomRequestBody() {
        Random random = new Random();
        double amount = random.nextDouble() * 1000;

        // Create the request body string
        String requestBody = this.accountString + "=" + amount;

        return requestBody;
    }

    public void sendRequest(String method, String path, String requestBody) throws IOException {
        // RTT starts somewhere here
        rttMeasurer.setStartTime(System.currentTimeMillis());
        try (Socket socket = new Socket(serverAddress, serverPort);
                OutputStream outputStream = socket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Construct the raw HTTP request
            String request = "POST" + " " + path + " HTTP/1.1\r\n" +
                    "Host: " + serverAddress + ":" + serverPort + "\r\n" +
                    "Content-Length: " + requestBody.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                    "\r\n" +
                    requestBody;

            // Send the request
            outputStream.write(request.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            // Read the response
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }

            // Performance:
            rttMeasurer.setEndTime(System.currentTimeMillis());
            rttMeasurer.calcRTT();
            System.out.println("Response:\n" + response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // getters:
    public String getAccountString() {
        return accountString;
    }

    // setters:
    public void setAccountString(String accountString) {
        this.accountString = accountString;
    }
}
