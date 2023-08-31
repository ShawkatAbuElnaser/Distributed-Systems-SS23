package vs.lib.RPC;

import org.apache.thrift.TException;

import vs.lib.Portfolio;

import java.util.ArrayList;
import java.util.List;

public class RpcController implements BankRettungsService.Iface {

    public static List<geldBetrag> ueberweisungen = new ArrayList<>();

    @Override
    public double ausleihen(geldBetrag geld) throws TException {
        System.out.print("Requested " + geld.getGeld() + " €");
        if (Portfolio.currentTotalValue < 0) {
            System.out.println("Info: Bank can't help - low of funds!");
        } else if (Portfolio.currentTotalValue > 0) {
            double currentTotal = Portfolio.currentTotalValue;
            double diff = currentTotal - geld.geld;
            if (diff < 0) {
                double betrag = currentTotal + diff;
                System.out.println("Info: funds are limited!");
                System.out.println("Bank can partially help with: " + betrag);
                return betrag;
            }
            return geld.geld;
        }
        return 0.0;
    }

    @Override
    public double ueberweisen(geldBetrag geld) throws TException {
        System.out.println("Transferring " + geld.getGeld() + "€");
        ueberweisungen.add(geld);
        Portfolio.currentTotalValue = Portfolio.currentTotalValue - geld.geld;
        return geld.geld;
    }

    @Override
    public boolean stornieren(geldBetrag geld) {
        System.out.print("Cancelling " + geld.getGeld() + " €");
        int id = geld.getTransaktionsnummer();

        if (id >= 0) {
            for (int i = 0; i < ueberweisungen.size(); i++) {
                geldBetrag stornierteUeberweisung = ueberweisungen.get(i);
                if (id == stornierteUeberweisung.getTransaktionsnummer()) {
                    ueberweisungen.remove(i);
                    System.out.println(stornierteUeberweisung.getGeld() + " " +
                            stornierteUeberweisung.getVerwendungszweck() + " " +
                            stornierteUeberweisung.getTransaktionsnummer() +
                            " - This transaction has been cancelled.");
                } else {
                    System.out.println(stornierteUeberweisung.getGeld() + " " +
                            stornierteUeberweisung.getVerwendungszweck() + " " +
                            stornierteUeberweisung.getTransaktionsnummer() + " " +
                            " - This transaction was aborted.");
                }
            }
            return true;
        } else {
            System.out.println("Cancellation or abort operation failed");
            return false;
        }
    }
}