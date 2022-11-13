package com.sheryv.slimod;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sheryv.slimod.config.ConfigHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.MobSpawnSettingsBuilder;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public record SpawnProbabilityBiomeModifier(float defaultCreatureSpawnProbabilityMultiplier,
                                            List<Value> overwrites) implements BiomeModifier {
  
  public static final String NAME = "set_creature_spawn_probability";
  
  public static final Supplier<Codec<SpawnProbabilityBiomeModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(builder -> builder.group(
      Codec.FLOAT.fieldOf("default_probability_multiplier").forGetter(SpawnProbabilityBiomeModifier::defaultCreatureSpawnProbabilityMultiplier),
      Value.CODEC.listOf().optionalFieldOf("overwrites_per_biome", Collections.emptyList()).forGetter(SpawnProbabilityBiomeModifier::overwrites)
  ).apply(builder, SpawnProbabilityBiomeModifier::new)));
  
  private static final float BASE_PROBABILITY = new DummyBuilder().getDefaultCreatureGenerationProbability();
  
  public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
    if (phase == Phase.BEFORE_EVERYTHING) {
      setSpawnProbability(builder.getMobSpawnSettings(), defaultCreatureSpawnProbabilityMultiplier, biome);
      
      for (Value value : overwrites) {
        if (value.biomes.contains(biome)) {
          setSpawnProbability(builder.getMobSpawnSettings(), value.probabilityMultiplier(), biome);
        }
      }
    }
  }
  
  public Codec<? extends BiomeModifier> codec() {
    return CODEC.get();
  }
  
  record Value(HolderSet<Biome> biomes, float probabilityMultiplier) {
    private static final Codec<Value> CODEC = RecordCodecBuilder.create((c) -> c.group(
        Biome.LIST_CODEC.fieldOf("biomes").forGetter(Value::biomes),
        Codec.FLOAT.fieldOf("probability_multiplier").forGetter(Value::probabilityMultiplier)
    ).apply(c, Value::new));
  }
  
  private static void setSpawnProbability(MobSpawnSettingsBuilder spawner, Float probabilityMultiplier, Holder<Biome> biome) {
    if (Math.abs(probabilityMultiplier) < 0.0001f) {
      return;
    }
    float probability = BASE_PROBABILITY * Math.abs(probabilityMultiplier);
    try {
      if (spawner.getProbability() == probability) {
        return;
      }
      
      spawner.creatureGenerationProbability(probability);
      if (spawner.getProbability() != probability) {
        if (ConfigHandler.isLoggingEnabled())
          SLIMod.LOGGER.warn("Using reflection to change spawn probability");
        ObfuscationReflectionHelper.setPrivateValue(MobSpawnSettingsBuilder.class, spawner, probability, "creatureGenerationProbability");
      }
      if (ConfigHandler.isLoggingEnabled())
        SLIMod.LOGGER.info("Changed vanilla spawn probability to " + spawner.getProbability() + " in " + biome.unwrapKey().map(k -> k.location().toString()).orElse("<none>"));
    } catch (Exception c) {
      SLIMod.LOGGER.error("Failed to set spawn probability to " + probability, c);
    }
  }
  
  private static class DummyBuilder extends MobSpawnSettings.Builder {
    private float getDefaultCreatureGenerationProbability() {
      return creatureGenerationProbability;
    }
  }
}
