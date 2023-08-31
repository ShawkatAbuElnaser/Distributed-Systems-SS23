package vs.lib;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Portfolio {
    private List<Stock> stockList;
    private Stock targetStock = null;
    public static double currentTotalValue;
    private double oldTotalValue;
    private double valueDifference;
    private String myArrayEnv;
    private List<String> nameList;
    private int quantity;

    public Portfolio() {
        this.stockList = new ArrayList<>();
        this.myArrayEnv = System.getenv("STOCK_NAMES");
        this.nameList = new ArrayList<>(Arrays.asList(myArrayEnv.split(",")));
        this.quantity = Integer.parseInt(System.getenv("STOCKS_QUANTITIY"));

        for (String name : nameList) {
            stockList.add(new Stock(name, 300, quantity));
        }

        Portfolio.currentTotalValue = portfolioValue();
        this.oldTotalValue = Portfolio.currentTotalValue;
        // System.out.println("PORTFOLIO: Current Total value: " +
        // this.currentTotalValue);
        printPortfolio();
    }

    public double portfolioValue() {
        currentTotalValue = 0;
        for (Stock stock : stockList) {
            currentTotalValue += stock.stockValue();
        }
        return currentTotalValue;
    }

    public void updateStockValue(String name, Double newValue) {
        // Iterate over the list and check each object's name:
        for (Stock s : stockList) {
            if (s.getName().equals(name)) {
                System.out.println("Updating stock value of  '" + s.getName() + "': ");
                // saving old stock value
                s.setOldValue(s.getCurrentValue());
                // updating new stock value
                s.setCurrentValue(newValue);

                targetStock = s;

                // saving old Portfolio value
                this.oldTotalValue = currentTotalValue;
                // updating current Portfolio value
                Portfolio.currentTotalValue = portfolioValue();
                break;
            }
        }

        if (targetStock != null) {
            System.out.println("\t" + targetStock.getName() + ": Old stock value: " + targetStock.getOldValue() +
                    " €\n\t" + targetStock.getName() + ": New stock value: " + targetStock.getCurrentValue() + " €\n");
        } else {
            System.out.println("Stock '" + targetStock.getName() + "'' not found");
        }

        // calculating and printing out the difference:
        calDifference();
    }

    public void calDifference() {
        valueDifference = currentTotalValue - oldTotalValue;

        // printing out difference:
        if (valueDifference > 0) {
            printPortfolio();
            System.out.println("PORTFOLIO:\tValue profit:     " + valueFormatter(valueDifference) + " €");

        } else if (valueDifference < 0) {
            valueDifference = -valueDifference; // change the sign of difference to make it positive
            printPortfolio();
            System.out.println("PORTFOLIO:\tValue Loss:       " + valueFormatter(valueDifference) + " €");

        } else {
            System.out.println("PORTFOLIO: Value is unchanged: " + valueFormatter(currentTotalValue) + " €");
        }
    }

    public void printPortfolio() {
        if (oldTotalValue == currentTotalValue)
            System.out.println("PORTFOLIO: Current Total value:     " + valueFormatter(currentTotalValue) + " €");

        else {
            System.out.print("PORTFOLIO: Previous Total value:    " + valueFormatter(oldTotalValue) +
                    " €\nPORTFOLIO: Current Total value:     " + valueFormatter(currentTotalValue) +
                    " €\n                                   -------------\n");
        }
    }

    // getters:
    public double getCurrentTotalValue() {
        return currentTotalValue;
    }

    public double getOldTotalValue() {
        return oldTotalValue;
    }

    public List<Stock> getStockList() {
        return stockList;
    }

    public String valueFormatter(double valueToFormat) {
        // adds "thousands"-comma to the string of the portfolio value
        // looks better in my opinion
        NumberFormat formatter = NumberFormat.getNumberInstance();
        return formatter.format(valueToFormat);
    }
}