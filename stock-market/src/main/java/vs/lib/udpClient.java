package vs.lib;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class udpClient {

    private DatagramSocket clientSocket;
    private byte[] sendData = new byte[1024];
    private String sentence;
    private DatagramPacket sendPacket;
    // private DatagramPacket receivePacket;
    // private byte[] receiveData;
    private String[] receiverIPs;
    private int reciverPort;
    private int waitingTime;
    private int numChanges; // Number of changes to simulate
    private int repetition; // how many times to send each change

    // for Performance:
    private int sentPacketSizeSum;
    private int sentPacketSizeAvg;

    public udpClient() throws SocketException, UnknownHostException {
        this.clientSocket = new DatagramSocket();
        this.sendData = new byte[1024];
        // this.receiveData = new byte[1024];
        this.receiverIPs = System.getenv("BANK_IPS").split(",");
        this.reciverPort = Integer.parseInt(System.getenv("BANK_PORT"));
        this.waitingTime = Integer.parseInt(System.getenv("WAITING_TIME"));

        this.numChanges = Integer.parseInt(System.getenv("CHANGES_NUM"));
        this.repetition = Integer.parseInt(System.getenv("REPETITION"));

        System.out.println("udp-client is available:");
    }

    public void sendUdpHello() throws IOException {
        for (int i = 0; i < 1; ++i) {
            this.sentence = "Hello from stock-market #" + (i + 1);
            sendData = sentence.getBytes();
            this.sendPacket = new DatagramPacket(sendData, sendData.length);

            for (String receiver : receiverIPs) {
                InetAddress address = InetAddress.getByName(receiver);
                sendPacket.setSocketAddress(new InetSocketAddress(address, reciverPort));
                clientSocket.send(sendPacket);
            }
            this.clientSocket.setSoTimeout(5000);
            System.out.println("SENT: " + this.sentence);

            clientSocket.close();
        }
    }

    public void sendUdpMsg(byte[] stockData) {
        sendData = stockData;
        this.sendPacket = new DatagramPacket(sendData, sendData.length);

        for (int i = 0; i < repetition; ++i) {
            for (String receiver : receiverIPs) {
                try {
                    InetAddress address = InetAddress.getByName(receiver);
                    sendPacket.setSocketAddress(new InetSocketAddress(address, reciverPort));
                    clientSocket.send(sendPacket);
                } catch (IOException e) {
                    System.out.println("Exception: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(waitingTime);
            } catch (InterruptedException e) {
                System.out.println("Exception: " + e.getMessage());
                e.printStackTrace();
            } // Pause for a certain amount of time: look at docker-compose file
            System.out.println("Stock object sent successfully.");
        }
        // clientSocket.close();
    }

    public DatagramSocket getClientSocket() {
        return clientSocket;
    }

    public DatagramPacket getSendPacket() {
        return sendPacket;
    }

    // for Performance:
    public int getPacketsNum() {
        // number of packets = number of change * reptition
        return numChanges * repetition;
    }

    public int getSentPacketSizeSum() {
        return sentPacketSizeSum;
    }

    public int getSentPacketSizeAvg() {
        return sentPacketSizeAvg;
    }

    public void calcPacketSizeSum(int newPacketSize) {
        this.sentPacketSizeSum += newPacketSize;
    }

    public void calcpacketSizeAvg() {
        this.sentPacketSizeAvg = sentPacketSizeSum / getPacketsNum();
        System.out.println("Sent Packet Size (Avg): " + sentPacketSizeAvg + " Bytes");
    }
}
