typedef i32 int

exception InvalidOperation {
	1: int whatOp,
	2: string why,
}

struct geldBetrag {
1: required double geld,
2: optional int transaktionsnummer,
3: optional string verwendungszweck,
}
service BankRettungsService
{
    double ausleihen(1: geldBetrag geld),
    double ueberweisen(1: geldBetrag geld),
    bool stornieren(1: geldBetrag geld),
}