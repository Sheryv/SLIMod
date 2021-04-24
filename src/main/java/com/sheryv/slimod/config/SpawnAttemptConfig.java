package com.sheryv.slimod.config;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SpawnAttemptConfig {
  ForgeConfigSpec.DoubleValue defaultCreatureSpawnProbabilityMultiplier;
  ForgeConfigSpec.ConfigValue<List<? extends ConfigEntry>> entries;
  
  public Double getDefaultCreatureSpawnProbabilityMultiplier() {
    return defaultCreatureSpawnProbabilityMultiplier.get();
  }
  
  public List<? extends ConfigEntry> getEntries() {
    List<? extends AbstractConfig> config = entries.get();
    return config.stream().map(e -> new ConfigEntry(e.valueMap())).collect(Collectors.toList());
  }
  
  public void validate() {
    for (ConfigEntry configEntry : getEntries()) {
      if (configEntry.getAffectedBiomes().isEmpty()) {
        throw new IllegalArgumentException("Field " + entries.getPath() + "->'affectedBiomes' cannot be empty");
      }
    }
  }
  
  @Override
  public String toString() {
    return "SpawnAttemptConfig{" +
        "defaultCreatureSpawnProbabilityMultiplier=" + defaultCreatureSpawnProbabilityMultiplier.get() +
        '}';
  }
  
  public static class ConfigEntry extends AbstractConfig {
    
    public ConfigEntry(double creatureSpawnProbabilityMultiplier, List<String> biomes) {
      super(new LinkedHashMap<>());
      add("creatureSpawnProbabilityMultiplier", creatureSpawnProbabilityMultiplier);
      add("affectedBiomes", biomes);
    }
    
    public ConfigEntry(Map<String, Object> map) {
      super(map);
    }
    
    public List<String> getAffectedBiomes() {
      return get("affectedBiomes");
    }
    
    public double getProbability() {
      return get("creatureSpawnProbabilityMultiplier");
    }
    
    @Override
    public AbstractConfig clone() {
      return new ConfigEntry(valueMap());
    }
    
    @Override
    public ConfigFormat<?> configFormat() {
      return null;
    }
    
    @Override
    public Config createSubConfig() {
      return null;
    }
  }
}
