package vs.lib.MQTT;

import java.util.HashSet;
import java.util.Set;

import org.apache.thrift.TException;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import vs.Bank;
import vs.lib.RPC.RpcController;
import vs.lib.RPC.geldBetrag;

class MqttSubscriber {
    private String id;
    private MqttClient mqttClient;
    private String brokerUrl;
    private MqttConnectOptions options;
    private boolean ackReceived;
    private static String receivedMessage;
    private String[] messageParts;

    private String receivedBankID;
    private String receivedValue;
    private Set<MqttPublisher> publishersList;
    private Set<MqttPublisher> bankruptBanksList;
    private String currentReceivedTopic;
    private Boolean isHelpNeeded;
    private Boolean isVoting;
    private Boolean isCommitted;
    private Boolean areAllPrepared;
    private String response;

    private RpcController rpcHandler;
    private static geldBetrag funds;
    private double fundsToSend;

    public MqttSubscriber(String brURL) throws MqttException {
        System.out.println("MQTT-Subscriber: running ...");
        this.brokerUrl = brURL;
        this.id = Bank.getId() + "SUB";
        this.mqttClient = new MqttClient(brokerUrl, id, new MemoryPersistence());
        this.options = new MqttConnectOptions();
        this.ackReceived = false;
        publishersList = new HashSet<>();
        bankruptBanksList = new HashSet<>();

        this.rpcHandler = new RpcController();
        funds = new geldBetrag(15000);

        this.isHelpNeeded = false;
        this.isVoting = false;
        this.areAllPrepared = false;
        this.isCommitted = false;

    }

    public void connect() throws MqttException {
        System.out.println("MQTT-Subscriber: connecting ...");
        this.settingCallback();
        options.setCleanSession(true);
        mqttClient.connect(options);
    }

    public void disconnect() {
        System.out.println("MQTT-Subscriber: disconnecting ...");
        try {
            mqttClient.disconnect();
        } catch (MqttException e) {
            System.out.println("MQTT-Subscriber: disconnected.");
        }
    }

    public void subscribe(String topic) throws MqttException {
        System.out.println("MQTT-Subscriber: subscribing to topic = " + topic);
        mqttClient.subscribe(topic, 0);
    }

    public boolean isAckReceived() {
        return ackReceived;
    }

    public void settingCallback() {
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                // Handle connection lost scenario
                System.out.println("MQTT-Subscriber: " + cause.getMessage());
                try {
                    mqttClient.reconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                // Handle incoming messages
                setReceivedMessage(new String(message.getPayload()));
                setCurrentReceivedTopic(topic.trim());
                messageParts = receivedMessage.split(":");
                System.out.println(
                        "MQTT-Subscriber: Received message = '" + receivedMessage + "'\ton topic = "
                                + currentReceivedTopic);

                if (topic.equals("balance"))
                    processBalanceMsg();
                else if (topic.equals("transaction")) {
                    if (receivedMessage.equals("Prepare transaction")) {
                        System.out.println("MQTT-Subscriber: Preparing...");
                        // setIsVoting(true);

                        participantVoting();

                    } else if (receivedMessage.equals("Commit transaction")) {
                        System.out.println("MQTT-Subscriber: Committing...");
                        processCommitMsg();

                    } else if (receivedMessage.equals("Abort transaction")) {
                        System.out.println("MQTT-Subscriber: entering Abort-Phase...");

                        // Perform the cancel action
                        rpcHandler.stornieren(funds);
                    }
                } else if (topic.equals("assistance")) {
                    setIsHelpNeeded(true);
                    processAssistanceMsg();
                } else if (topic.equals("vote")) {
                    processVote();
                    areAllPrepared = checkAllPrepared(publishersList);

                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Handle message delivery completion
                System.out.println("MQTT-Subscriber: Message delivered successfully");
            }
        });
    }

    public void processBalanceMsg() throws MqttException {
        // RTT
        MqttBankSimulation.getRttMqtt().setEndTime(System.currentTimeMillis());
        MqttBankSimulation.getRttMqtt().calcRTT();
        // Extract the bank ID from the message
        if (messageParts.length == 2) {
            this.receivedBankID = messageParts[0].trim();
            this.receivedValue = messageParts[1].trim();
            double value = Double.parseDouble(receivedValue);
            // Add the bank ID to the publisher set
            if (value > MqttBankSimulation.getMinimumBalanceThreshold())
                addPublisher(receivedBankID, value);
            else {
                // if broke -> remove from the normal publishers list
                // publishersList.removeIf(publisher ->
                // publisher.getId().equals(receivedBankID));
                addBankruptBank(id, value);
            }
        }
    }

    public void processAssistanceMsg() throws MqttException {
        // Extract the bank ID from the message
        if (messageParts.length == 2) {
            this.receivedBankID = messageParts[0].trim();
            this.receivedValue = messageParts[1].trim();
            // Add the bank ID to the publisher set
            System.out.println("MQTT-Subscriber: " + receivedBankID + " need assistance!");
        }
    }

    public void processCommitMsg() throws TException {
        // Perform the transfer action
        fundsToSend = rpcHandler.ausleihen(funds);
        rpcHandler.ueberweisen(new geldBetrag(fundsToSend));

        this.response = Bank.getId() + ": Helping with " + fundsToSend + "€";
        MqttBankSimulation.mqttPublisher.setTopic("helpfunds");
        MqttBankSimulation.mqttPublisher.setMessage(response);

        MqttBankSimulation.mqttPublisher.publish();

    }

    public void participantVoting() throws MqttException {

        System.out.println("MQTT-Participant: publishing the vote");

        // publishing the vote here if not coordinator
        // check if Bank has more than the help-threshhold
        Boolean vote = MqttBankSimulation.isBankrupt(MqttBankSimulation.getHelpThreshhold()) ? false : true;
        if (vote) {
            System.out.println("MQTT-Subscriber" + ": Prepared to help! ");
            // Construct the response message with the participant's name
            response = Bank.getId() + ": Prepared";

        } else if (!vote) {
            System.out.println("MQTT-Subscriber: Sadly not prepared to help :( ");
            // Construct the response message with the participant's name
            response = Bank.getId() + ": Not Prepared";
        }

        // Publish the response
        System.out.println("MQTT-Simulation: publishing message: " + response + "\t on topic  = " + "vote");
        MqttBankSimulation.mqttPublisher.setTopic("vote");
        MqttBankSimulation.mqttPublisher.setMessage(response);

        MqttBankSimulation.mqttPublisher.publish();
    }

    public void processVote() throws MqttException {
        System.out.println("MQTT-Coordinator: Processing the vote ...");
        // Extract the bank ID from the message
        if (messageParts.length == 2) {
            this.receivedBankID = messageParts[0].trim();
            this.receivedValue = messageParts[1].trim();
            // Add the bank ID to the voter set
            for (MqttPublisher p : publishersList) {
                if (p.getId() == receivedBankID) {
                    System.out.println("MQTT-Subscriber" + p.getId() + ": voted!");
                    if (receivedMessage.equals("prepared")) {
                        p.setIsPrepared(true);
                        System.out.print("\t\t - PREPARED");

                    } else {
                        p.setIsPrepared(false);
                        System.out.print("\t\t - NOT PREPARED");
                    }
                }
            }
        }
    }

    public void addPublisher(String publisherID, Double publisherValue) throws MqttException {
        System.out.println("MQTT-Subscriber: Adding a capable bank -  " + publisherID);
        publishersList.add(new MqttPublisher(publisherID, publisherValue));
    }

    public void addBankruptBank(String ID, Double banksValue) {
        System.out.println("MQTT-Simulation: Adding  " + ID + " to the bankrupt banks list");
        bankruptBanksList.add(new MqttPublisher(ID, banksValue));
    }

    public void printPublishersList() {
        for (MqttPublisher p : publishersList) {
            System.out.println("MQTT-Subscriber: ID = " + p.getId() + " is capable!");
        }
    }

    public void printBankruptBanks() {
        for (MqttPublisher b : bankruptBanksList) {
            System.out.println("MQTT-Subscriber: ID = " + b.getId() + " is bankrupt!");
        }
    }

    public void printNotPrepared() {
        for (MqttPublisher b : publishersList) {
            if (!b.getIsPrepared())
                System.out.println("MQTT-Subscriber: " + b.getId() + " is not prepared!!");
        }

    }

    public boolean checkAllPrepared(Set<MqttPublisher> publishers) {
        for (MqttPublisher publisher : publishers) {
            if (!publisher.getIsPrepared()) {
                return false; // If any publisher is not prepared, return false
            }
        }
        return true; // If all publishers are prepared, return true
    }

    // getters:
    public String getReceivedMessage() {
        return receivedMessage;
    }

    public String getReceivedBankID() {
        return receivedBankID;
    }

    public String getCurrentReceivedTopic() {
        return currentReceivedTopic;
    }

    public Boolean getIsHelpNeeded() {
        return isHelpNeeded;
    }

    public Boolean getIsVoting() {
        return isVoting;
    }

    public Boolean getIsCommitted() {
        return isCommitted;
    }

    public int getPublishersListSize() {
        return publishersList.size();
    }

    public int getBankruptBanksListSize() {
        return bankruptBanksList.size();
    }

    public Set<MqttPublisher> getPublishersList() {
        return publishersList;
    }

    public Set<MqttPublisher> getBankruptBanksList() {
        return bankruptBanksList;
    }

    public Boolean getAreAllPrepared() {
        return areAllPrepared;
    }

    // setters
    public void setReceivedMessage(String receivedMessage) {
        MqttSubscriber.receivedMessage = receivedMessage;
    }

    public void setCurrentReceivedTopic(String currentReceivedTopic) {
        this.currentReceivedTopic = currentReceivedTopic;
    }

    public void setIsHelpNeeded(Boolean isHelpNeeded) {
        this.isHelpNeeded = isHelpNeeded;
    }

    public void setIsVoting(Boolean isVoting) {
        this.isVoting = isVoting;
    }

    public void setIsCommitted(Boolean isCommitted) {
        this.isCommitted = isCommitted;
    }
}