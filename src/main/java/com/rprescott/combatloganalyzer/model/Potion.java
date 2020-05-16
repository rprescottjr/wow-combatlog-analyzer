package com.rprescott.combatloganalyzer.model;

public class Potion {

    private String name;
    private String effect;
    private Integer effectId;

    public Potion(String name, String effect, Integer effectId) {
        this.name = name;
        this.effect = effect;
        this.effectId = effectId;
    }

    public String getName() {
        return name;
    }

    public String getEffect() {
        return effect;
    }

    public Integer getEffectId() {
        return effectId;
    }
}
