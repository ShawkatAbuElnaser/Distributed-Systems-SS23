package vs.lib.RPC;

import java.util.Random;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import vs.Bank;
import vs.lib.Portfolio;

public class RpcClientRunner implements Runnable {
    private static volatile TTransport transport;
    private static volatile RpcController rpcController;
    public static Double rpcReturnValue;
    private static int rpcPort;
    private static String serverAddress;
    private RTT rttRpc;

    public RpcClientRunner() {
        RpcClientRunner.rpcPort = Integer.parseInt(System.getenv("RPC_PORT"));
        RpcClientRunner.serverAddress = System.getenv("SERVER_ADDRESS");
        rpcController = new RpcController();
        // RTT
        rttRpc = new RTT();
    }

    @Override
    public void run() {
        System.out.println("RPC-Client is running...");
        try {
            // RTT start time
            rttRpc.setStartTime(System.currentTimeMillis());
            connectToRpcServer();
            processRpcData();
            closeRpcConnection();

            // RTT end time
            rttRpc.setEndTime(System.currentTimeMillis());
            rttRpc.calcRTT();
            Thread.sleep(Bank.getWaitingBeforeResult());

            rttRpc.setData(rpcReturnValue);
            printTransactionList();
        } catch (TTransportException e) {
            System.out.println("Exception: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (TException e) {
            System.out.println("Exception: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void connectToRpcServer() {
        System.out.println("RPC-Client: Connecting to server on port " + rpcPort);
        try {
            transport = new TSocket(serverAddress, rpcPort);
            transport.open();
        } catch (TTransportException e) {
            // e.printStackTrace();
            System.out.println("Exception: RPC Connection is over " /* + e.getMessage() */);
        }
    }

    private static void closeRpcConnection() {
        if (transport != null && transport.isOpen()) {
            transport.close();
        }
    }

    private static void printTransactionList() {
        System.out.println("RPC: The list of transactions:");
        for (geldBetrag g : RpcController.ueberweisungen) {
            System.out.println(g);
        }
    }

    private static void processRpcData() throws TException {
        geldBetrag g1 = new geldBetrag();
        g1.setGeld(500.0);
        g1.setVerwendungszweck(Bank.getName()); // für docker Compose
        g1.setTransaktionsnummer(1);

        double randomValue = 100.0 + (1500.0 - 100.0) * new Random().nextDouble();
        double randomV = Math.round(randomValue * 100.0) / 100.0;
        geldBetrag g2 = new geldBetrag();
        g2.setGeld(randomV);
        g2.setVerwendungszweck(Bank.getName());// für docker Compose
        g2.setTransaktionsnummer(2);

        System.out.println("RPC-Client:");
        double amountSent1 = rpcController.ueberweisen(g2);

        rpcReturnValue = amountSent1;

        System.out.println("RPC-Client:");
        rpcController.stornieren(g1);

        if (Portfolio.currentTotalValue < 0) {
            geldBetrag g3 = new geldBetrag();
            g2.setGeld(Portfolio.currentTotalValue * -1.0);
            g2.setVerwendungszweck(Bank.getName());// für docker Compose
            g2.setTransaktionsnummer(3);

            System.out.println("RPC-Client: Requesting " + g3.getGeld() + " €");
            System.out.println("RPC-Client: " + rpcController.ausleihen(g3) + " €");
        }
    }
}
