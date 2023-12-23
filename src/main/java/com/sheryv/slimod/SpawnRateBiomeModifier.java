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
import net.minecraftforge.common.world.ModifiableBiomeInfo;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public record SpawnRateBiomeModifier(List<BiomeSpawners> spawners) implements BiomeModifier {
  
  public static final String NAME = "overwrite_spawns";
  
  public static final Supplier<Codec<SpawnRateBiomeModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(builder -> builder.group(
      BiomeSpawners.CODEC.listOf().fieldOf("per_biomes").forGetter(SpawnRateBiomeModifier::spawners)
  ).apply(builder, SpawnRateBiomeModifier::new)));
  
  
  public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
    
    if (phase == Phase.BEFORE_EVERYTHING) {
      
      var settings = builder.getMobSpawnSettings();
      for (BiomeSpawners spawnersForBiome : spawners) {
        if (spawnersForBiome.biomesAnyMatch().stream().anyMatch(b -> b.contains(biome))
            || (!spawnersForBiome.biomesAllMatch().isEmpty() && spawnersForBiome.biomesAllMatch().stream().allMatch(b -> b.contains(biome)))) {
          
          for (SpawnerDef spawnerDef : spawnersForBiome.spawners()) {
            var newData = spawnerDef.spawner();
            var spawner = settings.getSpawner(newData.type.getCategory());
            var current = spawner.stream().filter(s -> s.type.equals(newData.type)).findFirst();
            
            current.ifPresent(spawner::remove);
            if (current.isPresent() || spawnerDef.addIfMissing) {
              spawner.add(newData);
              
              if (ConfigHandler.isLoggingEnabled()) {
                SLIMod.LOGGER.debug(String.format("Changed spawn for %25s in %42s %15s to %s", newData.type, biome.unwrapKey().map(k -> k.location().toString()).orElse("<none>"), "[" + newData.type.getCategory() + "]", newData));
              }
            }
          }
        }
      }
    }
  }
  
  public Codec<? extends BiomeModifier> codec() {
    return CODEC.get();
  }
  
  private record BiomeSpawners(List<HolderSet<Biome>> biomesAnyMatch, List<HolderSet<Biome>> biomesAllMatch,
                               List<SpawnerDef> spawners) {
    private static final Codec<BiomeSpawners> CODEC = RecordCodecBuilder.create((c) -> c.group(
        Biome.LIST_CODEC.listOf().optionalFieldOf("biomes_any_match", Collections.emptyList()).forGetter(BiomeSpawners::biomesAnyMatch),
        Biome.LIST_CODEC.listOf().optionalFieldOf("biomes_all_match", Collections.emptyList()).forGetter(BiomeSpawners::biomesAllMatch),
        SpawnerDef.CODEC.listOf().fieldOf("spawners").forGetter(BiomeSpawners::spawners)
    ).apply(c, BiomeSpawners::new));
  }
  
  private record SpawnerDef(boolean addIfMissing, MobSpawnSettings.SpawnerData spawner) {
    private static final Codec<SpawnerDef> CODEC = RecordCodecBuilder.create((c) -> c.group(
        Codec.BOOL.optionalFieldOf("add_if_missing", false).forGetter(SpawnerDef::addIfMissing),
        MobSpawnSettings.SpawnerData.CODEC.fieldOf("spawner").forGetter(SpawnerDef::spawner)
    ).apply(c, SpawnerDef::new));
  }
}
