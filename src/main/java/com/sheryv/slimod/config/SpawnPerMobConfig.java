package com.sheryv.slimod.config;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SpawnPerMobConfig {
  ForgeConfigSpec.ConfigValue<List<? extends ConfigEntry>> entries;
  ForgeConfigSpec.BooleanValue enableSpawnerModification;
  
  public List<? extends ConfigEntry> getEntries() {
    List<? extends AbstractConfig> config = entries.get();
    return config.stream().map(e -> new ConfigEntry(e.valueMap())).collect(Collectors.toList());
  }
  
  public Boolean getEnableSpawnerModification() {
    return enableSpawnerModification.get();
  }
  
  public void validate() {
  
  }
  
  @Override
  public String toString() {
    return "SpawnPerMobConfig{" +
        "enableSpawnerModification=" + enableSpawnerModification +
        '}';
  }
  
  public static class ConfigEntry extends AbstractConfig {
    public ConfigEntry(String entity, List<String> biomes, int weight, int groupMaxSize, int groupMinSize) {
      super(new LinkedHashMap<>());
      add("entity", entity);
      add("affectedBiomes", biomes);
      add("weight", weight);
      add("groupMaxSize", groupMaxSize);
      add("groupMinSize", groupMinSize);
      add("addIfMissing", false);
      add("forbiddenBiomeCategories", new ArrayList<String>());
    }
    
    public ConfigEntry(Map<String, Object> map) {
      super(map);
    }
    
    public String getEntity() {
      return get("entity");
    }
    
    public List<String> getAffectedBiomes() {
      return get("affectedBiomes");
    }
    
    public int getWeight() {
      return get("weight");
    }
    
    public int getGroupMaxSize() {
      return get("groupMaxSize");
    }
    
    public int getGroupMinSize() {
      return get("groupMinSize");
    }
    
    public boolean getAddIfMissing() {
      return get("addIfMissing");
    }
    
    public List<String> getForbiddenBiomeCategories() {
      return get("forbiddenBiomeCategories");
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
