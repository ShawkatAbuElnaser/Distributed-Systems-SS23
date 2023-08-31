package vs.lib.MQTT;

import org.eclipse.paho.client.mqttv3.*;

import vs.Bank;

class MqttPublisher {
    private String id;
    private MqttClient mqttClient;
    private String topic;
    private String message;
    private String brokerURL;
    private MqttConnectOptions options;
    private Double currentValue;
    private Boolean isPrepared;
    private Boolean canVote;
    private String vote;
    private Double helpThreshhold; // Minimum balance threshold for assistance

    public MqttPublisher(String brokerUrl) throws MqttException {
        System.out.println("MQTT-Publisher: running ...");
        this.brokerURL = brokerUrl;
        this.topic = System.getenv("TOPIC");
        this.id = Bank.getId() + "PUB";
        this.mqttClient = new MqttClient(brokerUrl, id);
        this.options = new MqttConnectOptions();
        this.helpThreshhold = Double.parseDouble(System.getenv("HELP_THRESHOLD"));
    }

    public MqttPublisher(String id, Double value) {
        // This Constructor is used from the Subscriber side (publishersList)
        setId(id);
        setCurrentValue(value);
        this.helpThreshhold = Double.parseDouble(System.getenv("HELP_THRESHOLD"));

        if (currentValue > MqttBankSimulation.getMinimumBalanceThreshold()) {
            canVote = true;
        } else
            canVote = false;
    }

    public void connect() throws MqttException {
        System.out.println("MQTT-Publisher: Connecting to " + this.brokerURL);
        options.setCleanSession(true);
        mqttClient.connect(options);
    }

    public void disconnect() throws MqttException {
        System.out.println("MQTT-Publisher: disconnecting ...");
        mqttClient.disconnect();
    }

    public void publish() {
        try {
            System.out.println("MQTT-Publisher: publishing ...");
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(0);

            mqttClient.publish(topic, mqttMessage);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // setters
    public void setId(String id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setCurrentValue(Double currentValue) {
        this.currentValue = currentValue;
    }

    public void setCanVote(Boolean canVote) {
        this.canVote = canVote;
    }

    public void setIsPrepared(Boolean isPrepared) {
        this.isPrepared = isPrepared;
    }

    public void setVote(String vote) {
        this.vote = vote;
    }

    // getters
    public String getId() {
        return id;
    }

    public Double getCurrentValue() {
        return currentValue;
    }

    public Boolean getIsPrepared() {
        isPrepared = MqttBankSimulation.isBankrupt(helpThreshhold) ? false : true;
        return isPrepared;
    }

    public Boolean getCanVote() {
        return canVote;
    }

    public String getVote() {
        return vote;
    }
}
