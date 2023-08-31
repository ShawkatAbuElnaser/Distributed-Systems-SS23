package vs.lib.MQTT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.thrift.TException;
import org.eclipse.paho.client.mqttv3.MqttException;

import vs.Bank;
import vs.lib.Portfolio;
import vs.lib.RPC.RTT;

public class MqttBankSimulation implements Runnable {

    private static String brokerAddress;
    private String brokerPort;
    private static String brokerUrl;
    private String topics;
    private String topicToSend;
    public static MqttPublisher mqttPublisher;
    private MqttSubscriber mqttSubscriber;
    private List<String> topicList;

    private String mqttSwitch;
    private int mqttWaitingTime;
    private static RTT rttMqtt;
    private String currentMessage;

    private static Double minimumBalanceThreshold; // Minimum balance threshold
    private static Double helpThreshhold; // Minimum balance threshold for assistance

    private boolean isCoordinator;
    private boolean allPrepared;

    public MqttBankSimulation() {
        // MQTT Configuration: Address + other variables (from docker-compose)
        MqttBankSimulation.minimumBalanceThreshold = Double.parseDouble(System.getenv("MINIMUM_THRESHOLD"));
        MqttBankSimulation.helpThreshhold = Double.parseDouble(System.getenv("HELP_THRESHOLD"));

        brokerAddress = System.getenv("BROKER_ADDRESS");
        this.brokerPort = System.getenv("BROKER_PORT");
        brokerUrl = "tcp://" + brokerAddress + ":" + brokerPort;

        mqttWaitingTime = Integer.parseInt(System.getenv("MQTT_WAIT"));
        mqttSwitch = System.getenv("MQTT_MODE");

        // Get the list of topics from the docker system variables
        this.topics = System.getenv("TOPICS");
        this.topicList = new ArrayList<>(Arrays.asList(topics.split(",")));

        System.out.println("MQTT-Bank-Simulation:: address - " + brokerUrl);

        rttMqtt = new RTT();
    }

    private void checkIfCoordinator() throws MqttException {
        // Checks if the bank is the MQTT-Connection Coordinator based on the docker
        // variables
        if (mqttSwitch.equals("CO")) {
            this.isCoordinator = true;
            System.out.println("MQTT-Simulation: This Bank is a MQTT Coordinator");
        } else {
            this.isCoordinator = false;
            System.out.println("MQTT-Simulation: This Bank is a MQTT Participant");
        }
    }

    public void intiatingThePhases() throws MqttException, TException, InterruptedException {
        try {
            // Coordinator processing votes + starting the phases
            if (isCoordinator) {
                System.out.println("MQTT-Coordinator: Initiating the phases...");

                // Perform the prepare phase
                this.allPrepared = preparePhase();
                if (mqttSubscriber.getCurrentReceivedTopic().equals("vote")) {
                    if (allPrepared) {
                        // Perform the commit phase
                        commitPhase();
                    } else {
                        // Abort the transaction
                        abortPhase();
                    }
                }
            }
        } catch (MqttException e) {
            handleException(e);
        }
    }

    private boolean preparePhase() throws MqttException {
        System.out.println("MQTT-Coordinator: Entering Prepare-Phase (1)");
        publishMessage("transaction", "Prepare transaction");

        System.out.println("MQTT-Coordinator: Not all participants are prepared YET ..");

        while (!mqttSubscriber.getAreAllPrepared()) {
            if (mqttSubscriber.getAreAllPrepared()) {
                System.out.println("MQTT-Coordinator: All participants finished voting and are prepared!");
                break;
            }
        }
        return true;
    }

    private void commitPhase() throws MqttException {
        System.out.println("MQTT-Coordinator: Entering Commit-Phase (2)");
        // Send commit requests to all participants
        publishMessage("transaction", "Commit transaction");

    }

    private void abortPhase() throws MqttException {
        System.out.println("MQTT-Coordinator: Abortion-Phase (3)");
        // Send abort requests to all participants
        publishMessage("transaction", "MQTT-Simulation: Abort transaction");

    }

    private void publishMessage(String topic, String message) throws MqttException {
        System.out.println("MQTT-Simulation: publishing message: " + message + "\t on topic  = " + topic);
        mqttPublisher.setTopic(topic);
        mqttPublisher.setMessage(message);
        mqttPublisher.publish();
    }

    private void publishBalance() throws MqttException, InterruptedException {
        // Publish the current balance and bank ID to the coordinator
        topicToSend = "balance";
        String message = Bank.getId() + ": " + Double.toString(Portfolio.currentTotalValue);
        // RTT
        rttMqtt.setStartTime(System.currentTimeMillis());
        publishMessage(topicToSend, message);
    }

    public void checkBalanceForAssistance() throws MqttException {
        System.out.println("Checking if assistance is needed ...");
        // Check the current balance and make a decision if assistance is needed

        if (isBankrupt(minimumBalanceThreshold)) {
            // Publish a request for assistance to the coordinator
            System.out.println("WARNING: Bank needs assistance!");
            mqttSubscriber.subscribe("helpfunds");
            topicToSend = "assistance";
            this.currentMessage = Bank.getId() + ": Bank needs assistance!";
            publishMessage(topicToSend, currentMessage);
        }
    }

    public void intiatingAssistance() throws MqttException {
        // this method is only for Coordinator
        if (isCoordinator) {
            if (mqttSubscriber.getIsHelpNeeded()) {
                System.out.println("MQTT-Coordinator: Must assist!");
            } else {
                System.out.println("MQTT-Coordinator: All banks are doing fine!");
            }
        }
    }

    private void handleException(Exception e) {
        System.out.println("MQTT-Simulation: Exception - " + e.getMessage());
        e.printStackTrace();
    }

    @Override
    public void run() {
        try {
            // wait for the broker to run first
            Thread.sleep(mqttWaitingTime);

            System.out.println("MQTT-Simulation: Running MQTT Bank Simulation...");
            checkIfCoordinator();

            // intialize the connected elements for the Bank
            mqttPublisher = new MqttPublisher(brokerUrl);
            mqttSubscriber = new MqttSubscriber(brokerUrl);

            // connecting
            mqttSubscriber.connect();
            // only participate in the transaction if you are
            if (!isBankrupt(minimumBalanceThreshold)) {
                topicList.add("transaction");
            }

            // Subscribing to all topics
            for (String t : topicList) {
                mqttSubscriber.subscribe(t);
            }

            mqttPublisher.connect();

            // Publish initial balance
            publishBalance();
            Thread.sleep(mqttWaitingTime);

            // Check balance for assistance and vote
            checkBalanceForAssistance();
            Thread.sleep(mqttWaitingTime);

            // if Coordinator:
            intiatingAssistance();
            Thread.sleep(mqttWaitingTime);

            // start voting: both Coordinator and Participants..
            // happens in MessageArrive() in the subscriber

            // if Coordinator:
            intiatingThePhases();
            Thread.sleep(mqttWaitingTime);

            if (isCoordinator) {
                mqttSubscriber.printPublishersList();
                mqttSubscriber.printNotPrepared();
                mqttSubscriber.printBankruptBanks();
            }
            // RTT Output
            Thread.sleep(Bank.getWaitingBeforeResult());
            System.out.println("MQTT-RTT = ");
            MqttBankSimulation.getRttMqtt().printCurrRTT();

        } catch (MqttException | TException | InterruptedException e) {
            handleException(e);
        } finally {
            mqttSubscriber.disconnect();
        }
    }

    // getters and setters...
    public static RTT getRttMqtt() {
        return rttMqtt;
    }

    public static Double getMinimumBalanceThreshold() {
        return minimumBalanceThreshold;
    }

    public static Double getHelpThreshhold() {
        return helpThreshhold;
    }

    public MqttPublisher getMqttPublisher() {
        return mqttPublisher;
    }

    public static boolean isBankrupt(Double minimunAmount) {
        if (Portfolio.currentTotalValue < minimunAmount)
            return true;
        else
            return false;
    }
}