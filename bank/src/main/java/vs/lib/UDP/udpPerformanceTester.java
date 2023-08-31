package vs.lib.UDP;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class udpPerformanceTester {
    // DatagramSocket socket = new DatagramSocket(1234);
    private int numPacketsToReceive;
    private int receivedPacketSizeSum;
    private int receivedPacketSizeAvg;

    private int packetsReceived;
    private int packetsLost;

    private double packetLossRate;

    // file info
    private String fileName;

    public udpPerformanceTester() {
        this.numPacketsToReceive = Integer.parseInt(System.getenv("PACKETS_NUM"));
        this.packetsReceived = 0;
        this.packetsLost = 0;
    }

    // accumalate number of recieved packets:
    public int accPacketsReceived() {
        return packetsReceived++;
    }

    public void calcPacketLossRate() {
        this.packetLossRate = (double) packetsLost / numPacketsToReceive * 100.0;
    }

    public void printResult() throws IOException {
        System.out.println("saving results ...");
        // Write packet loss rate to file
        this.fileName = "/udpPerformance.txt";

        try (FileWriter fileWriter = new FileWriter(fileName);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write("Number of received packages:     " + packetsReceived + " Packets");
            bufferedWriter.newLine();
            bufferedWriter.write("Number of lost packages:          " + packetsLost + " Packets");
            bufferedWriter.newLine();
            bufferedWriter.write("Packet Loss Rate:                " + packetLossRate + "%");
            bufferedWriter.newLine();
            bufferedWriter.write("Received Packet Size (Avg):      " + receivedPacketSizeAvg + " Bytes / Packet");
            bufferedWriter.newLine();
            System.out.println("-----------------------------------------------");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readResults() {
        System.out.println("outputting results ...");
        try (FileReader fileReader = new FileReader(fileName);
                BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void calcPackLoss() {
        packetsLost = numPacketsToReceive - packetsReceived;
        packetLossRate = Math.round(packetLossRate * 100.0) / 100.0;

    }

    public int getNumPacketsToReceive() {
        return numPacketsToReceive;
    }

    public int getPacketsReceived() {
        return packetsReceived;
    }

    public int getReceivedPacketSizeSum() {
        return receivedPacketSizeSum;
    }

    public void calcPacketSizeSum(int newPacketSize) {
        this.receivedPacketSizeSum += newPacketSize;
    }

    public void calcpacketSizeAvg() {
        if (packetsReceived != 0) {
            this.receivedPacketSizeAvg = receivedPacketSizeSum / packetsReceived;
        } else {
            try {
                this.receivedPacketSizeAvg = 0; // Set a default value

            } catch (Exception e) {
                System.out.println("Exception: " + e.getMessage());

            }
            // Handle the scenario when packetsReceived is zero
            // Or throw an exception:
            // throw new ArithmeticException("Division by zero: packetsReceived is zero");
        }
    }
}