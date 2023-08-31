package vs.lib.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import vs.Bank;
import vs.lib.Portfolio;
import vs.lib.Stock;

public class HttpServer {
    private int port;
    private Portfolio portfolio2;
    private String title;
    private String method;
    private String path;
    private String protocol;
    private Map<String, Double> accountBalances = new HashMap<>();

    public HttpServer(int port) {
        this.port = port;
        this.portfolio2 = new Portfolio();
        this.title = "HTTP-Server: Welcome to " + Bank.getName();
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("HTTP-Server: listening on port " + port);
            serverSocket.setSoTimeout(Bank.getTimeout()); // set timeout to 5 seconds

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("HTTP-Server: New client connected: " + clientSocket.getRemoteSocketAddress());
                    receiveRequest(clientSocket);

                } catch (SocketTimeoutException e) {
                    System.out.println("Timeout reached, stopping receiver");
                    break; // stop receiving after timeout
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveRequest(Socket clientSocket) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream outputStream = clientSocket.getOutputStream()) {

            String requestLine = reader.readLine();
            if (requestLine == null) {
                return;
            }
            String[] requestParts = requestLine.split("\\s");
            if (requestParts.length != 3) {
                return;
            }
            this.method = requestParts[0];
            this.path = requestParts[1];
            this.protocol = requestParts[2];

            Map<String, String> headers = new HashMap<>();
            String headerLine;
            while (!(headerLine = reader.readLine()).isEmpty()) {
                String[] headerParts = headerLine.split(":\\s", 2);
                headers.put(headerParts[0], headerParts[1]);
            }

            int contentLength = 0;
            if (headers.containsKey("Content-Length")) {
                contentLength = Integer.parseInt(headers.get("Content-Length"));
            }

            StringBuilder requestBodyBuilder = new StringBuilder();
            int c;
            for (int i = 0; i < contentLength && (c = reader.read()) != -1; i++) {
                requestBodyBuilder.append((char) c);
            }

            String responseBody = handleRequest(method, path, headers, requestBodyBuilder.toString());
            // StringBuilder responseBuilder = new StringBuilder();
            String response = String.format("%s %d %s\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: %d\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    "%s",
                    protocol, 200, "OK", responseBody.getBytes(StandardCharsets.UTF_8).length, responseBody);

            outputStream.write(response.getBytes(StandardCharsets.US_ASCII));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            clientSocket.close();
        }
    }

    private String handleRequest(String method, String path, Map<String, String> headers, String requestBody)
            throws IOException {
        switch (method) {
            case "GET":
                if (path.equals("/balance")) {
                    return getStocks();
                } else if (path.equals("/")) {
                    return getHomePage();
                } else {
                    return "HTTP-Server: Unsupported path: " + path;
                }
            case "POST":
                if (path.equals("/deposit")) {
                    return depositMoney(requestBody);
                } else if (path.equals("/withdraw")) {
                    return withdrawMoney(requestBody);
                } else {
                    return "HTTP-Server: Unsupported path: " + path;
                }
            default:
                return "Unsupported method: " + method;
        }
    }

    private String getHomePage() {
        StringBuilder responseBodyBuilder = new StringBuilder();
        responseBodyBuilder.append("<html>\n");
        responseBodyBuilder.append("<head>\n");
        responseBodyBuilder.append("<title>Bank Options</title>\n");
        responseBodyBuilder.append("<style>\n");
        responseBodyBuilder.append("body {\n");
        responseBodyBuilder.append("    font-family: Arial, sans-serif;\n");
        responseBodyBuilder.append("    background-color: #f2f2f2;\n");
        responseBodyBuilder.append("    margin: 0;\n");
        responseBodyBuilder.append("    padding: 20px;\n");
        responseBodyBuilder.append("}\n");
        responseBodyBuilder.append("h1 {\n");
        responseBodyBuilder.append("    color: #333333;\n");
        responseBodyBuilder.append("}\n");
        responseBodyBuilder.append("ul {\n");
        responseBodyBuilder.append("    list-style-type: none;\n");
        responseBodyBuilder.append("    padding: 0;\n");
        responseBodyBuilder.append("}\n");
        responseBodyBuilder.append("label {\n");
        responseBodyBuilder.append("    display: block;\n");
        responseBodyBuilder.append("    margin-bottom: 10px;\n");
        responseBodyBuilder.append("}\n");
        responseBodyBuilder.append("input[type=\"text\"] {\n");
        responseBodyBuilder.append("    padding: 5px;\n");
        responseBodyBuilder.append("    width: 200px;\n");
        responseBodyBuilder.append("}\n");
        responseBodyBuilder.append("input[type=\"submit\"] {\n");
        responseBodyBuilder.append("    padding: 10px;\n");
        responseBodyBuilder.append("    background-color: #4CAF50;\n");
        responseBodyBuilder.append("    color: white;\n");
        responseBodyBuilder.append("    border: none;\n");
        responseBodyBuilder.append("}\n");
        responseBodyBuilder.append("</style>\n");
        responseBodyBuilder.append("</head>\n");
        responseBodyBuilder.append("<body>\n");
        responseBodyBuilder.append("<h1>").append(title).append("</h1>");
        responseBodyBuilder.append("<ul>\n");
        String currentSocket = getStocks(); // Hier wird die Methode `getAccountBalance()` aufgerufen, um den
        String currentBalance = getAccountBalance(); // aktuellen Kontostand zu erhalten
        responseBodyBuilder.append("Current Balance: ").append(currentBalance).append("</p>\n");
        responseBodyBuilder.append(currentSocket).append("</p>\n");
        responseBodyBuilder.append("</ul>\n");
        responseBodyBuilder.append("<form method=\"POST\" action=\"/deposit\">\n");
        responseBodyBuilder.append("<label for=\"amount\">Deposit money:</label>\n");
        responseBodyBuilder.append("<input type=\"text\" id=\"amount\" name=\"amount\">\n");
        responseBodyBuilder.append("<input type=\"submit\" value=\"Deposit\">\n");
        responseBodyBuilder.append("</form>\n");
        responseBodyBuilder.append("<form method=\"POST\" action=\"/withdraw\">\n");
        responseBodyBuilder.append("<label for=\"amount\">Withdraw money:</label>\n");
        responseBodyBuilder.append("<input type=\"text\" id=\"amount\" name=\"amount\">\n");
        responseBodyBuilder.append("<input type=\"submit\" value=\"Withdraw\">\n");
        responseBodyBuilder.append("</form>\n");
        responseBodyBuilder.append("</body>\n");
        responseBodyBuilder.append("</html>");

        return responseBodyBuilder.toString();
    }

    private String getAccountBalance() {
        StringBuilder responseBodyBuilder = new StringBuilder();
        for (Map.Entry<String, Double> entry : accountBalances.entrySet()) {
            responseBodyBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return responseBodyBuilder.toString();
    }

    private String getStocks() {
        StringBuilder responseBodyBuilder = new StringBuilder();
        responseBodyBuilder.append("<p>Current Total Value").append(": ").append(portfolio2.getCurrentTotalValue())
                .append("</p>");
        for (Stock stock : portfolio2.getStockList()) {
            responseBodyBuilder.append("<p>").append(stock.getName()).append(": ").append(stock.getCurrentValue())
                    .append("</p>");
        }
        return responseBodyBuilder.toString();
    }

    private String depositMoney(String requestBody) {
        String[] parts = requestBody.split("=");
        if (parts.length == 2) {
            String account = parts[0];
            String amountStr = parts[1];
            double amount = Double.parseDouble(amountStr);

            if (accountBalances.containsKey(account)) {
                double currentBalance = accountBalances.get(account);
                double newBalance = currentBalance + amount;
                accountBalances.put(account, newBalance);
                return "Amount deposited successfully. New balance for account " + account + ": " + newBalance;
            } else {
                accountBalances.put(account, amount);
                return "Amount deposited successfully. Initial balance for account " + account + ": " + amount;
            }
        } else {
            return "Invalid deposit request";
        }
    }

    private String withdrawMoney(String requestBody) {
        String[] parts = requestBody.split("=");
        if (parts.length == 2) {
            String account = parts[0];
            String amountStr = parts[1];
            double amount = Double.parseDouble(amountStr);

            if (accountBalances.containsKey(account)) {
                double currentBalance = accountBalances.get(account);
                if (currentBalance >= amount) {
                    double newBalance = currentBalance - amount;
                    accountBalances.put(account, newBalance);
                    return "Amount withdrawn successfully. New balance for account " + account + ": " + newBalance;
                } else {
                    return "Insufficient balance for account " + account;
                }
            } else {
                return "Account " + account + " does not exist";
            }
        } else {
            return "Invalid withdraw request";
        }
    }

    public void setPortfolio(Portfolio portfolio2) {
        this.portfolio2 = portfolio2;
    }
}