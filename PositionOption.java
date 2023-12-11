import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class PositionOption {
    public static void main(String[] args) {
        PositionOption positionOption = new PositionOption();
        TradeOption tradeOption =new TradeOption();
        List<Trade> tradeList=tradeOption.readTradeFile("new\\src\\trade.csv");
        tradeList.sort((o1, o2) -> o1.getTradeDateTime().compareTo(o2.getTradeDateTime()));

    }
public   Map<String,Long> calculateHoldingQuantity(List<Trade> tradeList){
    Map<String,Long> holdingQuantityMap = new HashMap<>();
    for (Trade trade:tradeList){
        String code = trade.getCode();
        Long quantity=trade.getQuantity();
        if (trade.getType()==TradeType.buy){
            holdingQuantityMap.put(code, holdingQuantityMap.getOrDefault(code, 0L)+quantity);
        } else if (trade.getType()==TradeType.sell) {
            holdingQuantityMap.put(code, holdingQuantityMap.getOrDefault(code, 0L)-quantity);
        }
    }
    return holdingQuantityMap;
}
public Map<String,BigDecimal> calculateAverageBuyPrice(List<Trade> tradeList){
    Map<String,Long> buyQuantityMap = new HashMap<>();
    Map<String, BigDecimal> buyTotalPriceMap = new HashMap<>();
    Map<String,BigDecimal> averageBuyPriceMap = new HashMap<>();
    for(Trade trade:tradeList){
        String code= trade.getCode();
        Long quantity= trade.getQuantity();
        BigDecimal price=trade.getPrice();
        if (trade.getType()==TradeType.buy){
            buyTotalPriceMap.put(code,buyTotalPriceMap.getOrDefault(code,BigDecimal.ZERO).add(price.multiply(BigDecimal.valueOf(quantity))));
            buyQuantityMap.put(code, buyQuantityMap.getOrDefault(code,0L)+quantity);
            averageBuyPriceMap.put(code,buyTotalPriceMap.get(code).divide(BigDecimal.valueOf(buyQuantityMap.get(code)),2, RoundingMode.HALF_UP));
        }
    }
    return averageBuyPriceMap;
}
public Map<String,BigDecimal> calculateRealizedProfitAndLoss(List<Trade> tradeList){
    Map<String,Long> buyQuantityMap = new HashMap<>();
    Map<String, BigDecimal> buyTotalPriceMap = new HashMap<>();
    Map<String,BigDecimal> averageBuyPriceMap = new HashMap<>();
    Map<String,BigDecimal> realizedProfitAndLossMap =new HashMap<>();
    for (Trade trade:tradeList){
        String code= trade.getCode();
        Long quantity= trade.getQuantity();
        BigDecimal price=trade.getPrice();
        if (trade.getType()==TradeType.buy){
            buyTotalPriceMap.put(code,buyTotalPriceMap.getOrDefault(code,BigDecimal.ZERO).add(price.multiply(BigDecimal.valueOf(quantity))));
            buyQuantityMap.put(code, buyQuantityMap.getOrDefault(code,0L)+quantity);
            averageBuyPriceMap.put(code,buyTotalPriceMap.get(code).divide(BigDecimal.valueOf(buyQuantityMap.get(code)),2, RoundingMode.HALF_UP));
            realizedProfitAndLossMap.put(code,realizedProfitAndLossMap.getOrDefault(code,BigDecimal.ZERO).add(BigDecimal.ZERO));
        }else{
            realizedProfitAndLossMap.put(code,realizedProfitAndLossMap.getOrDefault(code,BigDecimal.ZERO)
                    .add(price.subtract(averageBuyPriceMap.get(code)).multiply(BigDecimal.valueOf(quantity))));
        }
    }return realizedProfitAndLossMap;
}
public void showPositionList(List<Position> positionList){

    for (Position position:positionList){
        System.out.printf("| %-5s | %,-15d | %-15s | %15s | %15s | %15s |%n",
               position.getCode(),position.getHoldingQuantity(),
                position.getAveragePrice().toString(),position.getRealizedProfitAndLoss().toString(),
                position.getValuation().toString(),position.getUnrealizedGainAndLoss().toString());
    }
}
public  List<Position> readPosition(List<Trade> tradeList){
        List<Position> positionList = new ArrayList<>();
        tradeList.sort((o1, o2) -> o1.getTradeDateTime().compareTo(o2.getTradeDateTime()));
        Map<String,Long> holdingQuantity =calculateHoldingQuantity(tradeList);
        Map<String,BigDecimal> averageBuyPrice=calculateAverageBuyPrice(tradeList);
        Map<String,BigDecimal> realizedProfitAndLoss =calculateRealizedProfitAndLoss(tradeList);
        Set<String> codeSet = new HashSet<>();
    for (Trade trade:tradeList){
        codeSet.add(trade.getCode());
    }
    for (String code:codeSet){
        Position position = new Position();
        position.setCode(code);
        position.setHoldingQuantity(holdingQuantity.get(code));
        position.setAveragePrice(averageBuyPrice.get(code));
        position.setRealizedProfitAndLoss(realizedProfitAndLoss.get(code));
        position.setValuation(BigDecimal.ZERO);
        position.setUnrealizedGainAndLoss(BigDecimal.ZERO);
        positionList.add(position);
    }
    return positionList;
}
public List<Position>  markToMarket(List<Position> positionList){
    Scanner scr = new Scanner(System.in);
    //Position position = new Position();
    for (Position position:positionList){
        System.out.println("銘柄名: "+position.getCode()+" の時価を入力してください:");
        while (true) {
            String price = scr.nextLine();
            if (!price.isBlank()) {
                if (price.matches("^[0-9\\.]+$") && !price.matches("^[0\\.]+$")) {
                    try {
                        BigDecimal bigDecimalPrice = new BigDecimal(price);
                        position.setMarketPrice(bigDecimalPrice.setScale(2, RoundingMode.HALF_UP));
                        System.out.println(position.getMarketPrice());
                        break;
                    } catch (NumberFormatException numberFormatException) {
                        System.out.println("正しい価格を半角数字で入力してください。");
                    }
                } else {
                    System.out.println("正しい価格を半角数字で入力してください。");
                }
            } else {
                System.out.println("入力してください。");
            }
        }
        position.setValuation(position.getMarketPrice().multiply(BigDecimal.valueOf(position.getHoldingQuantity())));
        BigDecimal acquisitionCost =position.getAveragePrice().multiply(BigDecimal.valueOf(position.getHoldingQuantity()));
        position.setUnrealizedGainAndLoss(position.getValuation().subtract(acquisitionCost));
    }
    return positionList;
}
}
