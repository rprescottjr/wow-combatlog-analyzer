package com.rprescott.combatloganalyzer.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.rprescott.combatloganalyzer.utils.NameNormalizer;

public class MobDeathTracker {

    private Map<String, Integer> MOB_DEATH_COUNTER = new HashMap<>();

    public void insertMobDeath(String[] combatLogLineAsArray) {
        String normalizedMobName = NameNormalizer.normalizeMobName(combatLogLineAsArray[6]);
        Integer deathCount = MOB_DEATH_COUNTER.get(normalizedMobName);
        if (deathCount == null) {
            MOB_DEATH_COUNTER.put(normalizedMobName, 1);
        }
        else {
            MOB_DEATH_COUNTER.put(normalizedMobName, ++deathCount);
        }
    }

    public Integer getDeathsByMobNames(List<String> mobNames) {
        Integer mobDeaths = 0;
        for (Entry<String, Integer> entry : MOB_DEATH_COUNTER.entrySet()) {
            if (mobNames.contains(entry.getKey())) {
                mobDeaths += entry.getValue();
            }
        }
        return mobDeaths;
    }

    public Integer getDeathsByMobName(String mobName) {
        return getDeathsByMobNames(Arrays.asList(mobName));
    }

}
