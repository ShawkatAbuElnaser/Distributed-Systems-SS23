package vs.lib.UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class udpServer {
    private DatagramSocket serverSocket;
    private byte[] receiveData;
    DatagramPacket receivePacket;
    String stockStr;

    public udpServer() throws SocketException, UnknownHostException {
        this.serverSocket = new DatagramSocket(6543);
        this.receiveData = new byte[1024];
        this.receivePacket = new DatagramPacket(receiveData, receiveData.length);
        System.out.println("udp-server is available:");
    }

    public void receiveUdpMsg() throws IOException, InterruptedException {
        serverSocket.receive(receivePacket);
        this.stockStr = new String(receivePacket.getData(), 0, receivePacket.getLength());
    }

    public void receiveUdpHello() throws IOException, InterruptedException {
        while (true) {
            serverSocket.receive(receivePacket);
            String sentence = new String(receivePacket.getData());
            sentence = sentence.trim();
            System.out.println("RECEIVED: " + sentence + ". CONNECTION ESTABLISHED");
        }
    }

    public String getStockStr() {
        return stockStr;
    }

    public DatagramSocket getServerSocket() {
        return serverSocket;
    }

    public DatagramPacket getReceivePacket() {
        return receivePacket;
    }
}
