package vs.lib.RPC;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

public class RpcServerRunner implements Runnable {
    // todo:RPC -> Server
    public static BankRettungsService.Processor processor;
    public static RpcController handler;
    private static int rpcPort;
    private static TServer server;

    public RpcServerRunner() {
        RpcServerRunner.rpcPort = Integer.parseInt(System.getenv("RPC_PORT"));
        handler = new RpcController();
    }

    @Override
    public void run() {
        System.out.println("RPC-Server is running on port " + rpcPort);
        startRpcServer();
    }

    private static void startRpcServer() {
        try {
            System.out.println("RPC-Server: Starting the server...");
            processor = new BankRettungsService.Processor(handler);

            TServerTransport serverTransport = new TServerSocket(rpcPort);
            server = new TSimpleServer(new TServer.Args(serverTransport).processor(processor));

            server.serve();

            stopServer();

        } catch (TTransportException e) {
            if (e.getCause() instanceof java.net.SocketException) {
                // Handle the case where the socket is closed by the peer
                System.out.println("Client disconnected: " + e.getMessage());
                System.out.println("Closing Server ...");
                // Perform any necessary cleanup or handling
                server.stop();

            } else {
                // Handle other TTransportException cases
                System.out.println("Transport Exception: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("Transport Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void stopServer() {
        // Stop the server
        if (server != null) {
            server.stop();
        }
    }
}