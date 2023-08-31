package vs;

public class App {
    private static Bank bank;

    public static void main(String[] args) throws Exception {
        bank = new Bank();
        bank.run();
    }
}
