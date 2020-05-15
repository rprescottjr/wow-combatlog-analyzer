package com.rprescott.combatloganalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
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

        // Display BWL Sunder Count.
        // TODO -- @Johnny Napoline -- Track unnecessary sunders and add to the table
        // that is formatted below.
        List<String> bwlTrackedCreatures = CreatureNameFinder.findAllBWLSunderTrackedCreatures();
        Map<String, List<Creature>> bwlSunders = sunderTracker.getSundersByMobNames(bwlTrackedCreatures);
        Map<String, List<Creature>> bwlUnnecessarySunder = sunderTracker.getUnnecessarySunders(bwlTrackedCreatures);
        Integer bwlCreatureDeaths = mobDeathTracker.getDeathsByMobNames(bwlTrackedCreatures);

        System.out.println();
        System.out.println(StringUtils.center("|| BWL Sunder Statistics ||", 96));
        System.out.println(StringUtils.rightPad("Player Name", 14) + "-- "
                + StringUtils.rightPad("Effective Sunder Count", 14) + "  -- "
                + StringUtils.rightPad("Effective Sunder Percentage", 14) + "    -- " + "Unnecessary Sunder Count");
        for (Entry<String, List<Creature>> entry : bwlSunders.entrySet()) {
            double sunderPercentage = entry.getValue().size() / (bwlCreatureDeaths * 1.0) * 100;
            Map<String, Integer> bwlUnnecessarySunderMobCounts = new HashMap<>();
            List<Creature> bwlUnnecessarySunderCreatures = bwlUnnecessarySunder.get(entry.getKey());
            System.out.println(StringUtils.rightPad(entry.getKey(), 14) + "-- "
                    + StringUtils.center(String.valueOf(entry.getValue().size()), 24) + "-- "
                    + StringUtils.leftPad(String.format("%.2f", sunderPercentage), 15) + "%               --"
                    + StringUtils.leftPad(String.valueOf(bwlUnnecessarySunderCreatures.size()), 12));

            for (int i = 0; i < bwlUnnecessarySunderCreatures.size(); i++) {
                Creature beep = bwlUnnecessarySunderCreatures.get(i);

                if (bwlUnnecessarySunderMobCounts.containsKey(beep.getName())) {
                    bwlUnnecessarySunderMobCounts.put(beep.getName(),
                            bwlUnnecessarySunderMobCounts.get(beep.getName() + 1));
                }
                else {

                    bwlUnnecessarySunderMobCounts.put(beep.getName(), 1);
                }
            }

            bwlUnnecessarySunderMobCounts.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).forEachOrdered(System.out::println);

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

}
