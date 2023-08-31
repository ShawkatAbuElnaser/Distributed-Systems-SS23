package vs;

import java.io.IOException;

public class App {

    public static void main(String[] args) throws IOException {
        User user = new User();
        user.printUser();
        Thread httpClientThread = new Thread(user.getHttpClientRunner());
        httpClientThread.start();
        // user.getHttpClientRunner().run();
    }
}
