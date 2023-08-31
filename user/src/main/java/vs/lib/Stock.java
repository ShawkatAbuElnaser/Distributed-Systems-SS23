package vs.lib;

public class Stock {
    private String name;
    private double oldValue;
    private double currentValue;
    private int quantity;

    public Stock(String name, double price, int quantity) {
        this.name = name;
        this.currentValue = price;
        this.quantity = quantity;
    }
    
    // getters:
    public String getName() {
        return name;
    }

    public double getOldValue() {
        return oldValue;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public int getQuantity() {
        return quantity;
    }


    // setters:
    public void setName(String name) {
        this.name = name;
    }

    public void setOldValue(double oldValue) {
        this.oldValue = oldValue;
    }

    public void setCurrentValue(double newValue) {
        this.currentValue = newValue;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public double stockValue() {
        return currentValue * quantity;
    }
}
