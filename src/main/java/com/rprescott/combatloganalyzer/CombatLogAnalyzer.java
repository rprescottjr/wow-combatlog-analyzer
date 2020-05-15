package com.rprescott.combatloganalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.rprescott.combatloganalyzer.model.Creature;
import com.rprescott.combatloganalyzer.services.CreatureNameFinder;
import com.rprescott.combatloganalyzer.services.MobDeathTracker;
import com.rprescott.combatloganalyzer.services.SunderTracker;

public class CombatLogAnalyzer {

    private static final String DEFAULT_COMBAT_LOG_LOCATION = "C:\\Program Files (x86)\\World of Warcraft\\_classic_\\Logs\\WoWCombatLog.txt";
    private static final String SPELL_CAST_SUCCESS = "SPELL_CAST_SUCCESS";

    private File combatLog;
    private SunderTracker sunderTracker;
    private MobDeathTracker mobDeathTracker;

    public static void main(String[] args) throws Exception {
        File combatLog = new File(DEFAULT_COMBAT_LOG_LOCATION);
        if (args != null && args.length > 0) {
            combatLog = new File(args[0]);
            if (combatLog.exists()) {
                System.out.println(
                        "File exists at specified location. Overriding default location with user-specified location.");
            }
            else {
                System.err.println("File does not exist at " + args[0] + ". Exiting program.");
                System.exit(1);
            }
        }
        else {
            System.out.println("Using default combat log location: " + DEFAULT_COMBAT_LOG_LOCATION);
            if (!combatLog.exists()) {
                System.err.println("Combat log does not exist. Exiting program.");
                System.exit(1);
            }
        }
        new CombatLogAnalyzer(combatLog).analyze();
    }

    public CombatLogAnalyzer(File combatLog) {
        this.combatLog = combatLog;
        this.sunderTracker = new SunderTracker();
        this.mobDeathTracker = new MobDeathTracker();
    }

    public void analyze() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(combatLog))) {
            String currentLine;
            boolean readingFirstLine = true;
            while ((currentLine = reader.readLine()) != null) {
                if (!readingFirstLine) {
                    String actualData = currentLine.substring(StringUtils.ordinalIndexOf(currentLine, " ", 3) + 1,
                            currentLine.length());
                    String[] lineAsArray = actualData.split(",");
                    CombatLogEventType eventType = determineEventType(lineAsArray);
                    switch (eventType) {
                    case SUCCESSFUL_SUNDER:
                        // Grab player and increment sunder count
                        sunderTracker.insertSunder(lineAsArray);
                        break;
                    case MOB_DEATH:
                        mobDeathTracker.insertMobDeath(lineAsArray);
                        break;
                    case UNKNOWN:
                        break;
                    }
                }
                else {
                    // Do nothing;
                    readingFirstLine = false;
                }
            }
        }

        List<String> bwlTrackedCreatures = CreatureNameFinder.findAllBWLSunderTrackedCreatures();
        Map<String, List<Creature>> bwlSunders = sunderTracker.getSundersByMobNames(bwlTrackedCreatures);
        Map<String, List<Creature>> bwlUnnecessarySunder = sunderTracker.getUnnecessarySunders(bwlTrackedCreatures);
        Integer bwlCreatureDeaths = mobDeathTracker.getDeathsByMobNames(bwlTrackedCreatures);

        System.out.println();
        System.out.println(StringUtils.center("|| BWL Sunder Statistics ||", 96));
        System.out.println(StringUtils.rightPad("Player Name", 13) + "--  "
                + StringUtils.rightPad("Effective Sunder Count", 24) + "--  "
                + StringUtils.rightPad("Effective Sunder Percentage", 29) + "--  "
                + StringUtils.rightPad("Unnecessary Sunder Count", 26) + "--  " + StringUtils.rightPad("Mob Names", 9));
        for (Entry<String, List<Creature>> entry : bwlSunders.entrySet()) {
            double sunderPercentage = entry.getValue().size() / (bwlCreatureDeaths * 1.0) * 100;
            List<Creature> bwlUnnecessarySunderCreatures = bwlUnnecessarySunder.get(entry.getKey());

            System.out.println(StringUtils.rightPad(entry.getKey(), 13) + "--  "
                    + StringUtils.center(String.valueOf(entry.getValue().size()), 24) + "-- "
                    + StringUtils.leftPad(String.format("%.2f", sunderPercentage), 15) + "%              --"
                    + StringUtils.leftPad(String.valueOf(bwlUnnecessarySunderCreatures.size()), 15)
                    + "             --  " + getTopUnnecessarySunderMobs(bwlUnnecessarySunderCreatures));

        }
    }

    // Display Mana Potion / Dark Rune Usage
    // TODO: @RPrescott -- Add some stuff

    private CombatLogEventType determineEventType(String[] lineAsArray) {
        CombatLogEventType eventType = CombatLogEventType.UNKNOWN;
        if (lineIsASuccessfulSunder(lineAsArray)) {
            eventType = CombatLogEventType.SUCCESSFUL_SUNDER;
        }
        else if (lineIsAMobDeathEvent(lineAsArray)) {
            eventType = CombatLogEventType.MOB_DEATH;
        }
        return eventType;
    }

    /**
     * Determines if a line in a combat log is a successful sunder cast. This is
     * determined by first checking if the first element is a SPELL_CAST_SUCCESS,
     * followed by checking that the spellName is Sunder Armor, and finally that the
     * spell caster is a Player.
     * 
     * @param lineAsArray
     * @return
     */
    private boolean lineIsASuccessfulSunder(String[] lineAsArray) {
        return lineAsArray[0].contains(SPELL_CAST_SUCCESS) && lineAsArray[10].contains("Sunder Armor")
                && lineAsArray[1].contains("Player");
    }

    private boolean lineIsAMobDeathEvent(String[] lineAsArray) {
        return lineAsArray[0].contains("UNIT_DIED") && lineAsArray[5].contains("Creature");
    }

    /**
     * Formats a string representation of the top 3 creatures and their associated
     * values.
     * 
     * @param unnecessaryCreatureList
     *            a list of creatures that were sundered unnecessarily
     * @return formatted string representation of top 3 creature names and amount of
     *         times they were sundered
     */
    private String getTopUnnecessarySunderMobs(List<Creature> unnecessaryCreatureList) {
        StringBuilder sBuild = new StringBuilder();
        Map<String, Integer> unnecessaryCreatureMapUnordered = new HashMap<>();
        Map<String, Integer> unnecessaryCreatureMapOrdered = new LinkedHashMap<>();
        String topSunderMobsString = "";

        for (Creature creature : unnecessaryCreatureList) {
            if (unnecessaryCreatureMapUnordered.containsKey(creature.getName())) {
                unnecessaryCreatureMapUnordered.put(creature.getName(),
                        unnecessaryCreatureMapUnordered.get(creature.getName()) + 1);
            }
            else {
                unnecessaryCreatureMapUnordered.put(creature.getName(), 1);
            }
        }

        // Takes an unordered HashMap, sorts it from largest value to smallest, takes
        // the top 3, and puts them into an LinkedHashMap
        unnecessaryCreatureMapUnordered.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(3)
                .forEachOrdered(x -> unnecessaryCreatureMapOrdered.put(x.getKey(), x.getValue()));

        for (Map.Entry<String, Integer> entry : unnecessaryCreatureMapOrdered.entrySet()) {
            topSunderMobsString = sBuild.append(entry.getKey()).append("(").append(entry.getValue()).append("), ")
                    .toString();
        }

        return topSunderMobsString.substring(0, topSunderMobsString.lastIndexOf(','));

    }

}
