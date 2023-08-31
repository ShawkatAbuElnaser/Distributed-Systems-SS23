package vs.lib;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class stockSimulator {
    private String myArrayEnv;
    private List<String> stockNames;
    private int currentStockIndex = 0;

    
    private int minStockQuantity; // Minimum stock quantity
    private int maxStockQuantity; // Maximum stock quantity
    
    private double initialStockValue;

    private double minChange;   // Minimum percentage change
    private double maxChange;   // Maximum percentage change
    
    private Random rand;
    private double stockValue;
    private Stock currentStock;
    private String stockName;
    private double stockPrice;
    private int stockQuantity;

    public stockSimulator() {
        this.myArrayEnv = System.getenv("STOCK_NAMES");
        this.stockNames = new ArrayList<>(Arrays.asList(myArrayEnv.split(",")));
        
        this.minStockQuantity = 50;  // Minimum stock quantity
        this.maxStockQuantity = 500; // Maximum stock quantity
        
        this.initialStockValue = 100.0;
        this.minChange = -0.05;   // Minimum percentage change
        this.maxChange = 0.05;    // Maximum percentage change
        
        this.rand = new Random();
        this.stockValue = initialStockValue;

        this.stockName = "LSFT";
        this.stockPrice = 280.00;
        this.stockQuantity = 50;
        currentStock = new Stock(stockName, stockPrice, stockQuantity);

    }

    public void generateStockName() {
        // Randomly select a new stock name
        int newStockIndex = rand.nextInt(stockNames.size());
        if (newStockIndex != currentStockIndex) {
            this.currentStock.setName(stockNames.get(newStockIndex));
        }
    }

    public void generateStockValue() {
        // Generate a random value within the specified range
        double percentChange = minChange + (maxChange - minChange) * rand.nextDouble();
        
        // Apply the percentage change to the stock value
        stockValue *= 1 + percentChange;

        // to keep the stock value with 3 digits only after the comma
        stockValue = Math.round(stockValue * 1000.0) / 1000.0;
        this.currentStock.setCurrentValue(stockValue);
    }

    public void generateStockQuantity() {
        // Randomly select a new stock quantity
        int newStockQuantity = minStockQuantity + rand.nextInt(maxStockQuantity - minStockQuantity + 1);
        if (newStockQuantity != stockQuantity) {
            this.currentStock.setQuantity(newStockQuantity);
        }
    }

    public Stock getCurrentStock() {
        return currentStock;
    }

    public void printStockInfos() {
        System.out.println("UPDATE: " + this.currentStock.getQuantity() + " '" + 
                                    this.currentStock.getName() + "' stocks are bought for: " +
                                    this.currentStock.getCurrentValue() + "€ each.");
    }
}
