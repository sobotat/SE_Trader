package com.setrader.se_trader;

public class Api {

    public static long factorial(int n) {
        if (n <= 2) {
            return n;
        }
        return n * factorial(n - 1);
    }
}
