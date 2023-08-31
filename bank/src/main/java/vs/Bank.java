package vs;

import java.io.IOException;
import vs.lib.Portfolio;
import vs.lib.HTTP.HttpServerRunner;
import vs.lib.MQTT.MqttBankSimulation;
import vs.lib.RPC.*;
import vs.lib.UDP.udpServerRunner;

public class Bank {
    private static String id;
    private static String name;
    private static Portfolio portfolio;
    private static int Timeout;
    private static int waitingBeforeResult;
    private udpServerRunner udpServerRunner;
    private HttpServerRunner httpServerRunner;
    private int tcpPort;
    private String rpcSwitch;
    private RpcServerRunner rpcServerRunner;
    private RpcClientRunner rpcClientRunner;
    private int rpcClientWaitingTime;
    private MqttBankSimulation mqttBankSimulation;

    Bank() throws IOException {
        // Bank Configuration Info
        System.out.println("Bank app is running...");
        Bank.id = System.getenv("ID");
        name = System.getenv("BANK_NAME");
        Bank.portfolio = new Portfolio();
        Bank.Timeout = Integer.parseInt(System.getenv("TIMEOUT"));
        Bank.waitingBeforeResult = Integer.parseInt(System.getenv("WAIT_BEFORE_RESULT"));

        // UDP
        this.udpServerRunner = new udpServerRunner();

        // TCP
        this.tcpPort = Integer.parseInt(System.getenv("TCP_PORT"));
        this.httpServerRunner = new HttpServerRunner(tcpPort);
        this.rpcServerRunner = new RpcServerRunner();
        this.rpcClientRunner = new RpcClientRunner();
        this.rpcSwitch = System.getenv("RPC_MODE");
        this.rpcClientWaitingTime = Integer.parseInt(System.getenv("RPC_WAIT_TIME"));

        // MQTT
        this.mqttBankSimulation = new MqttBankSimulation();
    }

    public void run() throws Exception {
        // Praktikum 4:
        // Creating the MQTT Runner Thread
        // Thread mqttRunnerThread = new Thread(mqttRunner);
        Thread mqttRunnerThread = new Thread(mqttBankSimulation);
        mqttRunnerThread.start();

        // Praktikum 3:
        // Create the RPC thread: Either Server or Client
        System.out.println("This bank is an rpc " + rpcSwitch);
        if (rpcSwitch.equals("server")) {
            Thread rpcServerThread = new Thread(rpcServerRunner);
            rpcServerThread.start();

        } else {
            // wait till server starts running:
            Thread.sleep(rpcClientWaitingTime);
            // Create a new thread for the RPC client.
            Thread rpcClientThread = new Thread(rpcClientRunner);
            rpcClientThread.start();

        }

        // Praktikum 2
        // Create the HTTP server thread
        this.httpServerRunner.getHttpServer().setPortfolio(portfolio);
        Thread httpThread = new Thread(httpServerRunner);
        httpThread.start();

        // Prakitkum 1
        this.udpServerRunner.run();

        // Praktikum 3: Performance
        if (rpcSwitch.equals("client")) {
            System.out.print("RPC=> ");
            System.out.println(RTT.getData());
        }
    }

    // getters
    public static String getId() {
        return id;
    }

    public static String getName() {
        return name;
    }

    public static int getWaitingBeforeResult() {
        return waitingBeforeResult;
    }

    public static int getTimeout() {
        return Timeout;
    }

    public static Portfolio getPortfolio() {
        return portfolio;
    }

    public void runRPC() {

    }
}
