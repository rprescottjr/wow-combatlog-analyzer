package com.rprescott.combatloganalyzer.utils;

public class NameNormalizer {

    private NameNormalizer() {
        // Do nothing
    }

    /**
     * Given a quote-surrounded name (Ex: "Zizek-Stalagg"), returns the name of the player without the
     * server information or quotes.
     * 
     * @param playerName
     * @return
     */
    public static String normalizePlayerName(String playerName) {
        return playerName.substring(1, playerName.indexOf('-'));
    }

    /**
     * Given a quote-surrounded mob name (Ex: "Molten Giant"), returns the name of the mob without the
     * quotes.
     * 
     * @param mobName
     * @return
     */
    public static String normalizeMobName(String mobName) {
        return mobName.replaceAll("\"", "");
    }

}
