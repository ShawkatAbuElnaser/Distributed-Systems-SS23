package vs.lib.RPC;

public class RTT {
    private long startTime;
    private long endTime;
    private double currRTT;
    private long totalRTT;
    private long avgRTT;
    private static String data;

    public RTT() {
        this.startTime = 0;
        this.endTime = 0;
        this.totalRTT = 0;
    }

    public void calcRTT() {
        currRTT = getRTT();
    }

    public double measureAverageRTT(int numRequests) {
        this.avgRTT = totalRTT / numRequests;
        return avgRTT;
    }

    // getters:
    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public double getRTT() {
        this.currRTT = (endTime - startTime) / 2;
        return currRTT;
    }

    public long getTotalRTT() {
        return totalRTT;
    }

    public static String getData() {
        return data;
    }

    public double getCurrRTT() {
        return currRTT;
    }

    // setters:
    public void setCurrRTT(long currRTT) {
        this.currRTT = getRTT();
    }

    public void setTotalRTT(long totalRTT) {
        this.totalRTT = totalRTT;
    }

    public void printAvg() {
        System.out.println("Average RTT: " + avgRTT + "ms");
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    void setData(Double rpcRuckgabWert) {
        data = String.format("%-10s | %-20s | %-20s | %-20s",
                currRTT + " ms",
                startTime,
                endTime,
                rpcRuckgabWert + " €");
    }

    void printRTT(Double rpcRuckgabWert) {
        setData(rpcRuckgabWert);
        System.out.println(data);
    }

    public void printCurrRTT() {
        data = String.format("%-10s | %-20s | %-20s",
                currRTT + " ms",
                startTime,
                endTime);
        System.out.println(data);

    }
}
