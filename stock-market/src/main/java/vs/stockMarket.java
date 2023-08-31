package vs;

import java.io.IOException;
import vs.lib.udpClient;
import vs.lib.stockSimulator;

public class stockMarket {

    private int id;
    private String name;
    private udpClient udpclient;
    private stockSimulator simulator;

    stockMarket(int id, String n) throws IOException {
        System.out.println("stock-market app is running ...");
        this.id = id;
        this.name = n;
        this.udpclient = new udpClient();
        this.simulator = new stockSimulator();
    }

    // getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void sendHello() throws IOException {
        // send a simple Hello to test the UDP-connection
        udpclient.sendUdpHello();
    }

    public void run() throws IOException, InterruptedException {
        for (int i = 0; i < this.udpclient.getPacketsNum(); i++) { // set to 10
            // printing out stock exchange info
            this.simulator.printStockInfos();

            // sending the stock information
            sendStockInfo();

            // Generating new values for the sent stock
            generateStockInfo();
        }

        // for Performance
        udpclient.calcpacketSizeAvg();

        this.udpclient.getClientSocket().close();
    }

    public void sendStockInfo() throws IOException, InterruptedException {
        // Praktikum 1: UDP
        // Convert the Stock attributes to a string
        String stockStr = String.format("%s,%.3f,%d", this.simulator.getCurrentStock().getName(),
                this.simulator.getCurrentStock().getCurrentValue(),
                this.simulator.getCurrentStock().getQuantity());
        // Convert the string to a byte array
        byte[] data = stockStr.getBytes();

        this.udpclient.sendUdpMsg(data);
        this.udpclient.calcPacketSizeSum(udpclient.getSendPacket().getLength());
    }

    public void generateStockInfo() {
        // Generate stock name randomly (out of three different stocks)
        simulator.generateStockName();

        // Generate stock value randomly (with a reasonable range)
        simulator.generateStockValue();

        // Generate stock quantaty randomly (with reasonable range)
        simulator.generateStockQuantity();
    }
}
