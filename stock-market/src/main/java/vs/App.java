package vs;

import java.io.IOException;

public class App {
    private static int first_wait;

    public static void main(String[] args) throws IOException, InterruptedException {
        first_wait = Integer.parseInt(System.getenv("FIRST_WAIT")); // WAIT TILL THE UDP SERVERS START RUNNING
        Thread.sleep(first_wait);

        stockMarket stockMarket = new stockMarket(1, System.getenv("STOCKMARKET_NAME"));
        stockMarket.run();
    }
}
