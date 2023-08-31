package vs.lib.UDP;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import vs.Bank;
import vs.lib.Stock;

public class udpServerRunner implements Runnable {

    private udpServer udpServer;
    private udpPerformanceTester performanceTester;

    public udpServerRunner() throws SocketException, UnknownHostException {
        this.udpServer = new udpServer();
        this.performanceTester = new udpPerformanceTester();
    }

    @Override
    public void run() {

        try {
            this.receiveStockInfo();

            // analyzing udp performance
            this.performanceTester.printResult();
            Thread.sleep(Bank.getWaitingBeforeResult());
            this.performanceTester.readResults();

        } catch (InterruptedException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void receiveHello() throws Exception {
        // receive a simple hello to test the UDP-connection
        this.udpServer.receiveUdpHello();
    }

    public void receiveStockInfo() throws Exception {
        // Praktikum 1: receive stock info with UDP
        try {
            this.udpServer.getServerSocket().setSoTimeout(Bank.getTimeout()); // set timeout to 5 seconds
            while (true) {
                try {
                    // receiving the stock information
                    this.udpServer.receiveUdpMsg();

                    // for performance:
                    performanceTester.accPacketsReceived();
                    performanceTester.calcPacketSizeSum(udpServer.getReceivePacket().getLength());

                    // Convert the string data back into a Stock object
                    String[] fields = this.udpServer.getStockStr().split(",");
                    Stock stock = new Stock(fields[0], Double.parseDouble(fields[1]), Integer.parseInt(fields[2]));
                    System.out.println("RECEIVED: " + stock.getName() + " "
                            + stock.getCurrentValue() + " " + stock.getQuantity());
                    Bank.getPortfolio().updateStockValue(stock.getName(), stock.getCurrentValue());
                    System.out.println("-----------------------------------------------");

                } catch (SocketTimeoutException e) {
                    performanceTester.calcPackLoss();
                    System.out.println("Timeout reached, stopping receiver");
                    break; // stop receiving after timeout
                }
            }
        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
        // Performance:
        performanceTester.calcpacketSizeAvg();
    }

}
