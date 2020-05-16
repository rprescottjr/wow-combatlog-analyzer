package com.rprescott.combatloganalyzer.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.rprescott.combatloganalyzer.model.Potion;
import com.rprescott.combatloganalyzer.utils.NameNormalizer;

/**
 * Class used to track all potion usage by player.
 */
public class PotionTracker {

    private static final Map<Integer, String> POTION_NAMES_BY_EFFECT_ID = new HashMap<>();
    private Map<String, List<Potion>> POTIONS_USED_BY_PLAYER = new HashMap<>();

    static {
        POTION_NAMES_BY_EFFECT_ID.put(6615, "Free Action Potion");
        POTION_NAMES_BY_EFFECT_ID.put(17531, "Major Mana Potion");
        POTION_NAMES_BY_EFFECT_ID.put(3169, "Limited Invulnerability Potion");
    }

    /**
     * Given a raw line from a WoW Combat Log, inserts a potion usage event and associates the event to
     * the imbibier of the potion.
     * 
     * @param lineAsArrayString
     *            The SPELL_CAST_SUCCESS event from a WoW Combat Log entry.
     */
    public void insertPotionUsage(String[] lineAsArrayString) {
        String potionName = POTION_NAMES_BY_EFFECT_ID.get(Integer.valueOf(lineAsArrayString[9]));
        String player = NameNormalizer.normalizePlayerName(lineAsArrayString[2]);
        List<Potion> mapEntry = POTIONS_USED_BY_PLAYER.get(player);

        // If this is the first potion detected by a player, give them an empty list.
        if (mapEntry == null) {
            mapEntry = new ArrayList<>();
            POTIONS_USED_BY_PLAYER.put(player, mapEntry);
        }
        mapEntry.add(new Potion(potionName == null ? "Unknown" : potionName, lineAsArrayString[10], Integer.valueOf(lineAsArrayString[9])));
    }

    /**
     * Given a list of potion names, returns the mapping of each player and the number of times the
     * specified potions were used by each player. If a player did not use any of the specified potion
     * names, their entry will not be included in the resultset.
     * 
     * <br/>
     * <br/>
     * 
     * To get the total number of times the potions were used, invokers may do
     * <b>entry.getValue().size();</b>
     * 
     * @param potionName
     *            The name of the potion to query. This should be the in-game display name of the potion
     *            (I.e. Major Mana Potion)
     */
    public Map<String, List<Potion>> getPotionUsageByPotionNames(List<String> potionNames) {
        LinkedHashMap<String, List<Potion>> sortedMap = new LinkedHashMap<>();

        Map<String, List<Potion>> intermediateMap = new HashMap<>();
        for (Entry<String, List<Potion>> entry : POTIONS_USED_BY_PLAYER.entrySet()) {
            List<Potion> filteredPotions = new ArrayList<>();
            for (Potion potion : entry.getValue()) {
                if (potionNames.contains(potion.getName())) {
                    filteredPotions.add(potion);
                }
            }
            if (filteredPotions.size() > 0) {
                intermediateMap.put(entry.getKey(), filteredPotions);
            }
        }

        intermediateMap.entrySet().stream().sorted(Entry.comparingByValue(Comparator.comparing(List<Potion>::size).reversed()))
            .forEachOrdered(entry -> sortedMap.put(entry.getKey(), entry.getValue()));

        return sortedMap;
    }

    /**
     * Given a potion name, returns the mapping of each player and the instances each potion was used by
     * each player. If a player did not use the specified potion name, their entry will not be included
     * in the resultset.
     * 
     * <br/>
     * <br/>
     * 
     * To get the total number of times a potion was used, invokers may do
     * <b>entry.getValue().size();</b>
     * 
     * @param potionName
     *            The name of the potion to query. This should be the in-game display name of the potion
     *            (I.e. Major Mana Potion)
     */
    public Map<String, List<Potion>> getPotionUsageByPotionName(String potionName) {
        return getPotionUsageByPotionNames(Arrays.asList(potionName));
    }
}
