package com.setrader.se_trader;

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.io.IOException;

public class Api {

    // Counts Factorial (Used for all Combinations)
    public static long factorial(int n) {
        if (n <= 2) {
            return n;
        }
        return n * factorial(n - 1);
    }

    // Clipboard Read and Write
    public static void writeTextToClipboard(String s) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = new StringSelection(s);
        clipboard.setContents(transferable, null);
    }

    public static String getFromClipboard(){
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            return (String) clipboard.getContents(null).getTransferData(DataFlavor.stringFlavor);
        } catch (Exception e){
            return "";
        }
    }
}
