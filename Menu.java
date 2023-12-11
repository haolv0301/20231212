import java.util.List;
import java.util.Scanner;

public class Menu {
    public static void main(String[] args) {
        StockOption stockOption = new StockOption();
        TradeOption tradeOption = new TradeOption();
        PositionOption positionOption = new PositionOption();
        Scanner scr = new Scanner(System.in);
        List<Position> positionList=positionOption.readPosition(tradeOption.readTradeFile("new\\src\\trade.csv"));
        while(true){

            System.out.println("plz choose 1.showlist 2.addstock 3.addtrade 4.showtrade 5.showposition 6.値洗い 9.exist");
            switch (scr.nextLine()) {
                case "1" -> stockOption.showStockList(stockOption.readStockList("new\\src\\stock.csv"));
                case "2" -> stockOption.addStock();
                case "3" -> tradeOption.addNewTrade();
                case "4" -> tradeOption.displayTradeList(tradeOption.readTradeFile("new\\src\\trade.csv") );
                case "5" -> positionOption.showPositionList(positionList);
                case "6" -> positionList=positionOption.markToMarket(positionList);
                case "9" -> {
                    System.out.println("end");
                    System.exit(0);
                }
                default -> {
                    System.out.println("err enter");
                }
            }
        }
    }
}
