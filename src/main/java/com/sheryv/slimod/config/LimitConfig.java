package com.sheryv.slimod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class LimitConfig {
  ForgeConfigSpec.BooleanValue enableLimitModification;
  ForgeConfigSpec.IntValue monster;
  ForgeConfigSpec.IntValue creature;
  ForgeConfigSpec.IntValue ambient;
  ForgeConfigSpec.IntValue waterCreature;
  ForgeConfigSpec.IntValue waterAmbient;
  ForgeConfigSpec.IntValue undergroundWaterCreature;
  ForgeConfigSpec.IntValue axolotls;
  
  public Boolean getEnableLimitModification() {
    return enableLimitModification.get();
  }
  
  public Integer getMonster() {
    return monster.get();
  }
  
  public Integer getCreature() {
    return creature.get();
  }
  
  public Integer getAmbient() {
    return ambient.get();
  }
  
  public Integer getWaterCreature() {
    return waterCreature.get();
  }
  
  public Integer getWaterAmbient() {
    return waterAmbient.get();
  }
  
  public Integer getUndergroundWaterCreature() {
    return undergroundWaterCreature.get();
  }
  
  public Integer getAxolotls() {
    return axolotls.get();
  }
  
  public void validate() {
  
  }
  
  @Override
  public String toString() {
    return "LimitConfig{" +
        "enableLimitModification=" + enableLimitModification.get() +
        ", monster=" + monster.get() +
        ", creature=" + creature.get() +
        ", ambient=" + ambient.get() +
        ", waterCreature=" + waterCreature.get() +
        ", waterAmbient=" + waterAmbient.get() +
        ", undergroundWaterCreature=" + undergroundWaterCreature.get() +
        ", axolotls=" + axolotls.get() +
        '}';
  }
}
