package com.rprescott.combatloganalyzer.services;

import java.util.HashMap;
import java.util.Map;

import com.rprescott.combatloganalyzer.utils.NameNormalizer;

/**
 * Class used to track Dark Rune and Demonic Rune usage by player.
 */
public class RuneUsageTracker {

    private Map<String, Integer> RUNES_USED_BY_PLAYER = new HashMap<>();

    /**
     * Given a raw line from a WoW Combat Log, inserts a Dark/Demonic Rune usage event and associates
     * the event to the user of the item.
     * 
     * @param lineAsArrayString
     *            The SPELL_CAST_SUCCESS event from a WoW Combat Log entry.
     */
    public void insertRuneUsage(String[] lineAsArrayString) {
        String player = NameNormalizer.normalizePlayerName(lineAsArrayString[2]);
        Integer runeUsage = RUNES_USED_BY_PLAYER.get(player);
        if (runeUsage == null) {
            RUNES_USED_BY_PLAYER.put(player, 1);
        }
        else {
            RUNES_USED_BY_PLAYER.put(player, ++runeUsage);
        }
    }

    /**
     * Given a normalized player name, returns the number of Dark Runes + Demonic Runes used by this
     * player. If the provided player name did not use any runes, then 0 is returned.
     * 
     * @param playerName
     *            The normalized player name of interest.
     * @return The number of Dark Runes + Demonic Runes used by this player, or 0 if the person did not
     *         use any.
     */
    public Integer getRunesUsedByName(String playerName) {
        Integer ret = RUNES_USED_BY_PLAYER.get(playerName);
        return ret == null ? 0 : ret;
    }
}
