package com.rprescott.combatloganalyzer.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CreatureNameFinder {

    private static final List<String> MOLTEN_CORE_TRASH = Arrays.asList("Molten Giant", "Firelord", "Molten Destroyer", "Core Hound", "Lava Reaver", "Lava Surger",
        "Lava Annihilator", "Firewalker", "Flamewaker", "Flameguard");
    private static final List<String> MOLTEN_CORE_BOSSES = Arrays.asList("Lucifron", "Magmadar", "Gehennas", "Garr", "Baron Geddon", "Shazzrah", "Sulfuron Harbinger",
        "Golemagg the Incinerator", "Majordomo Executus", "Ragnaros");
    private static final List<String> BWL_TRASH = Arrays.asList("Grethok the Controller", "Blackwing Guardsman", "Death Talon Dragonspawn", "Death Talon Seether",
        "Death Talon Flamescale", "Death Talon Wyrmkin", "Death Talon Captain", "Death Talon Hatcher", "Death Talon Taskmaster", "Blackwing Warlock", "Blackwing Spellbinder",
        "Death Talon Overseer", "Death Talon Wyrmguard", "Chromatic Drakonid");
    private static final List<String> BWL_BOSSES = Arrays.asList("Razorgore the Untamed", "Vaelastrasz the Corrupt", "Broodlord Lashlayer", "Firemaw", "Ebonroc", "Flamegor",
        "Chromaggus", "Nefarian");

    private CreatureNameFinder() {
        // Do nothing.
    }

    public static List<String> findAllMoltenCoreTrash() {
        return MOLTEN_CORE_TRASH;
    }

    public static List<String> findAllMoltenCoreBosses() {
        return MOLTEN_CORE_BOSSES;
    }

    public static List<String> findAllBWLTrash() {
        return BWL_TRASH;
    }

    public static List<String> findAllBWLBosses() {
        return BWL_BOSSES;
    }

    public static List<String> findAllMCSunderTrackedCreatures() {
        return Stream.concat(MOLTEN_CORE_TRASH.stream(), MOLTEN_CORE_BOSSES.stream()).collect(Collectors.toList());
    }

    public static List<String> findAllBWLSunderTrackedCreatures() {
        return Stream.concat(BWL_TRASH.stream(), BWL_BOSSES.stream()).collect(Collectors.toList());
    }

}
