package com.rprescott.combatloganalyzer.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.rprescott.combatloganalyzer.model.Creature;
import com.rprescott.combatloganalyzer.utils.NameNormalizer;

public class SunderTracker {

    private Map<String, List<Creature>> SUNDER_COUNT_BY_PLAYER = new HashMap<>();

    public void insertSunder(String[] combatLogLineAsArray) {
        String player = NameNormalizer.normalizePlayerName(combatLogLineAsArray[2]);
        List<Creature> mapEntry = SUNDER_COUNT_BY_PLAYER.get(player);
        // If this is the first sunder detected by a player, give them an empty list.
        if (mapEntry == null) {
            // When inserting a sunder count, it should default to one sunder cast.
            mapEntry = new ArrayList<>();
            SUNDER_COUNT_BY_PLAYER.put(player, mapEntry);
        }
        mapEntry.add(new Creature(NameNormalizer.normalizeMobName(combatLogLineAsArray[6]), combatLogLineAsArray[5]));
    }

    public Map<String, List<Creature>> getSundersByMobNames(List<String> mobNames) {
        LinkedHashMap<String, List<Creature>> sortedMap = new LinkedHashMap<>();

        Map<String, List<Creature>> intermediateMap = new HashMap<>();

        for (Entry<String, List<Creature>> entry : SUNDER_COUNT_BY_PLAYER.entrySet()) {
            List<Creature> filteredCreatures = new ArrayList<>();
            for (Creature creature : entry.getValue()) {
                if (mobNames.contains(creature.getName())) {
                    filteredCreatures.add(creature);
                }
            }
            intermediateMap.put(entry.getKey(), filteredCreatures);
        }

        intermediateMap.entrySet().stream().sorted(Entry.comparingByValue(Comparator.comparing(List<Creature>::size).reversed()))
            .forEachOrdered(entry -> sortedMap.put(entry.getKey(), entry.getValue()));

        return sortedMap;
    }

    /**
     * Compares all creatures sundered by each player to the list (mobNames) of tracked creatures. If a
     * sundered creature is not on the tracked list it is added to the unnecessaryCreatures List. That
     * list is then associated back with the original player and sorted.
     * 
     * @param mobNames
     *            List of String literal creature names that are tracked
     * @return Sorted map of Player names and their associated list of Creatures that were sundered
     *         unnecessarily
     */
    public Map<String, List<Creature>> getUnnecessarySunders(List<String> mobNames) {
        LinkedHashMap<String, List<Creature>> unnecessarySortedMap = new LinkedHashMap<>();
        Map<String, List<Creature>> unnecessaryIntermediateMap = new HashMap<>();

        for (Entry<String, List<Creature>> entry : SUNDER_COUNT_BY_PLAYER.entrySet()) {
            List<Creature> unnecessaryCreatures = new ArrayList<>();
            for (Creature creature : entry.getValue()) {
                if (!mobNames.contains(creature.getName())) {
                    unnecessaryCreatures.add(creature);
                }
            }
            unnecessaryIntermediateMap.put(entry.getKey(), unnecessaryCreatures);
        }
        unnecessaryIntermediateMap.entrySet().stream().sorted(Entry.comparingByValue(Comparator.comparing(List<Creature>::size).reversed()))
            .forEachOrdered(entry -> unnecessarySortedMap.put(entry.getKey(), entry.getValue()));

        return unnecessarySortedMap;

    }

    public Map<String, List<Creature>> getSundersByMobName(String mobName) {
        return getSundersByMobNames(Arrays.asList(mobName));
    }

    public void displaySunderCount() {
        System.out.println("Sunder Count by Player Results:");

        LinkedHashMap<String, List<Creature>> sortedMap = new LinkedHashMap<>();
        SUNDER_COUNT_BY_PLAYER.entrySet().stream().sorted(Entry.comparingByValue(Comparator.comparing(List<Creature>::size).reversed()))
            .forEachOrdered(entry -> sortedMap.put(entry.getKey(), entry.getValue()));

        for (Entry<String, List<Creature>> entry : sortedMap.entrySet()) {
            System.out.println(entry.getKey() + " -- " + entry.getValue().size());
        }
    }

}
