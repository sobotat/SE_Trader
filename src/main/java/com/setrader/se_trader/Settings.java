package com.setrader.se_trader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import com.google.gson.*;

public class Settings {
    private static final Logger logger = LogManager.getLogger(Settings.class.getName());

    protected static double winWidthDefault = 900;
    protected static double winHeightDefault = 500;
    protected static double winWidth = winWidthDefault;
    protected static double winHeight = winHeightDefault;
    protected static boolean winCanResize = true;
    protected static boolean winIsMaximize = false;

    public static void loadSettings(){
        logger.info("Load Settings Started");
        String read = Files.readString("\\App\\settings.txt");

        if (!read.equals("")) {
            try {
                JSONObject settingsObj = new JSONObject(read);
                RouteCalculator.homeIndex = settingsObj.getInt("homeIndex");

                RouteCalculator.backHome = settingsObj.getBoolean("backHome");

                JSONObject windowObj = settingsObj.getJSONObject("window");
                winWidth = windowObj.getDouble("winWidth");
                winHeight = windowObj.getDouble("winHeight");
                winCanResize = windowObj.getBoolean("winCanResize");
                winIsMaximize = windowObj.getBoolean("winIsMaximize");

                logger.info("Load Settings Done");
            }catch (org.json.JSONException e){
                saveSettings();
                logger.error("Wrong Settings File -> Creating New");
            }
        }
    }
    public static void saveSettings(){
        logger.info("Save Settings Started");
        JSONObject settingsObj = new JSONObject();

        settingsObj.put("homeIndex", RouteCalculator.homeIndex);
        settingsObj.put("backHome", RouteCalculator.backHome);

        JSONObject windowObj = new JSONObject();
        windowObj.put("winWidth", winWidth);
        windowObj.put("winHeight", winHeight);
        windowObj.put("winWidthDefault", winWidthDefault);
        windowObj.put("winHeightDefault", winHeightDefault);
        windowObj.put("winCanResize", winCanResize);
        windowObj.put("winIsMaximize", winIsMaximize);

        settingsObj.put("window", windowObj);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement je = JsonParser.parseString( settingsObj.toString());
        String enhanceJson = gson.toJson(je);

        Files.writeString("\\App\\settings.txt", enhanceJson);
        logger.info("Save Settings Done");
    }
}
