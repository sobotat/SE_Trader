package com.setrader.se_trader;

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static Thread getThreadByName(String threadName) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals(threadName)) return t;
        }
        return null;
    }

    public static boolean isValidHexaCode(String str)    {
        // Regex to check valid hexadecimal color code.
        String regex = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";

        Pattern p = Pattern.compile(regex);

        if (str == null) {
            return false;
        }

        Matcher m = p.matcher(str);
        return m.matches();
    }
}
