package vs;

import java.io.IOException;
import vs.lib.HTTP.HttpClientRunner;

public class User {
    private String id;
    private String name;
    private HttpClientRunner httpClientRunner;
    private String serverAddress;
    private int serverPort;

    User() throws IOException {
        this.id = System.getenv("ACCOUNT");
        this.name = "Jane Doe";
        this.serverAddress = System.getenv("SERVER_ADDRESS");
        this.serverPort = Integer.parseInt(System.getenv("SERVER_PORT"));
        this.httpClientRunner = new HttpClientRunner(serverAddress, serverPort);
        this.httpClientRunner.getHttpClient().setAccountString(System.getenv("ACCOUNT"));
    }

    // getters:
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // setters:
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void printUser() {
        System.out.println("User: " + id + " - Name: " + name);
    }

    public HttpClientRunner getHttpClientRunner() {
        return httpClientRunner;
    }
}
