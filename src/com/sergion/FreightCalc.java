package com.sergion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FreightCalc {

    Map<String, BigDecimal> currencyMap = new HashMap<>();
    Map<String, Integer> vatOnlineRates = new HashMap<>();
    Map<String, Integer> vatBookRates = new HashMap<>();

    String[] acceptableParams = new String[] {"vat", "input-currency", "output-currency"};
    String[] acceptableTypes = new String[] {"book", "online"};
    HashMap<String, String> params = null;

    int amount = 0;
    BigDecimal price = null;
    String type = null;
    String currency = "DKK";
    BigDecimal resultPrice = null;


    public void run(String[] args){
        currencyMap.put("DKK", BigDecimal.valueOf(100));
        currencyMap.put("NOK", BigDecimal.valueOf(73.50));
        currencyMap.put("SEK", BigDecimal.valueOf(70.23));
        currencyMap.put("GBP", BigDecimal.valueOf(891.07));
        currencyMap.put("EUR", BigDecimal.valueOf(743.93));

        vatOnlineRates.put("DK", 25);
        vatOnlineRates.put("NO", 25);
        vatOnlineRates.put("SE", 25);
        vatOnlineRates.put("GB", 20);
        vatOnlineRates.put("DE", 19);

        vatBookRates.put("DK", 25);
        vatBookRates.put("NO", 25);
        vatBookRates.put("SE", 25);
        vatBookRates.put("GB", 20);
        vatBookRates.put("DE", 12);

        if (isParametersValid(args)){
           resultPrice = calcBase(amount, price, type);
            if (params != null){
                //params.forEach((k, v) -> System.out.println("Key : " + k + " Value : " + v));
                if (params.containsKey("input-currency")){
                    resultPrice = currencyConverter(params.get("input-currency"), currency, resultPrice);
                }
                if (params.containsKey("output-currency")){
                    resultPrice = currencyConverter(currency, params.get("output-currency"), resultPrice);
                    currency = params.get("output-currency");
                }

                if (params.containsKey("vat")){
                    BigDecimal vat = calcVat(resultPrice, params.get("vat"), type);
                    //System.out.println(vat);
                    resultPrice = resultPrice.add(vat);

                }
            }
            System.out.println(resultPrice + " " + currency);
        }


    }

    public BigDecimal calcVat(BigDecimal price, String countryCode, String type){
        BigDecimal result = null;
        if (type.equals("online")){
            return price.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(vatOnlineRates.get(countryCode)))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        if (type.equals("book")){
            return price.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(vatBookRates.get(countryCode)))
                    .setScale(2, RoundingMode.HALF_UP);

        }
        return result;
    }



    public BigDecimal calcBase(int amount, BigDecimal price, String type){
        BigDecimal result = null;
        if (type.equals("online")){
            result = price.multiply(BigDecimal.valueOf(amount));
        }
        if (type.equals("book")){
            result = price.multiply(BigDecimal.valueOf(amount));
            result = result.add(BigDecimal.valueOf(calcFreight(amount)));
        }
        return result;

    }

    int calcFreight(int amount){
        if (amount > 10)  {
            return ((amount-10)/10*25)+75;
        }
        else return 50;
    }

    public BigDecimal currencyConverter(String fromCode, String toCode, BigDecimal amountIn) {
        return amountIn.divide(currencyMap.get(fromCode), 4, RoundingMode.HALF_UP)
                .multiply(currencyMap.get(toCode))
                .setScale(2, RoundingMode.HALF_UP);
    }


    public boolean isParametersValid(String[] args){
        //check amount
        try {
            amount = Integer.parseInt(args[0]);
            if (amount <= 0) {
                System.out.println("Amount is negative or zero");
                return false;
            }
        }
        catch (Exception ex){
            System.out.println("Amount is incorrect");
            return false;
        }

        //check price
        try {
            price = BigDecimal.valueOf(Double.parseDouble(args[1]));
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                System.out.println("Price is negative");
                return false;
            }
        }
        catch (Exception ex){
            System.out.println("Price is incorrect");
            return false;
        }

        //check type
        try {
            if (Arrays.asList(acceptableTypes).contains(args[2])){
                type = args[2];
            }
            else {
                System.out.println("Type must be \"book\" or \"online\"");
                return false;
            }
        }
        catch (Exception ex){
            System.out.println("Type must be \"book\" or \"online\"");
            return false;
        }

        //check optional params
        if (args.length > 2){
            params = convertToKeyValuePair(args);
            return params != null;
        }

        return true;
    }

    public HashMap<String, String> convertToKeyValuePair(String[] argsIn) {
        HashMap<String, String> params = new HashMap<>();
        for (int i = 3; i < argsIn.length; i++) {
            //check additional parameters format
            if (argsIn[i].contains("=") && argsIn[i].startsWith("--")){
                String[] splitFromEqual = argsIn[i].split("=");
                String key = splitFromEqual[0].substring(2);
                String value = splitFromEqual[1];
                if (Arrays.asList(acceptableParams).contains(key)){
                    //check currency exist
                    if (key.equals("input-currency") || key.equals("output-currency")){
                        if (!currencyMap.containsKey(value)){
                            System.out.println("Incorrect currency code: " + value);
                            return null;
                        }
                    }
                    //check country-code exist
                    if (key.equals("vat")){
                        if (!vatOnlineRates.containsKey(value)){
                            System.out.println("Incorrect country-code: " + value);
                            return null;
                        }
                    }
                    params.put(key, value);
                }
                else {
                    System.out.println("Incorrect parameter: " + key);
                    return null;
                }
            }
            else {
                System.out.println("Incorrect parameters format");
                return null;
            }
        }
        return params;
    }
}
