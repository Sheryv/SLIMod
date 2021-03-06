package com.sheryv.slimod;

import com.sheryv.slimod.command.CommandManager;
import com.sheryv.slimod.config.*;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.MobSpawnInfoBuilder;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Mod(SLIMod.ID)
public class SLIMod {
  public static final String ID = "sli_mcmod";
  public static final String NAME = "SLI-SpawnLimitIncrease";
  public static final Logger LOGGER = LogManager.getLogger(ID);
  
  public SLIMod() {
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
    MinecraftForge.EVENT_BUS.register(this);
    ConfigProvider.loadServerConfig();
  }
  
  public static void reloadConfig() {
    LimitConfig limits = ConfigHandler.getLimits();
    
    if (limits.getEnableLimitModification()) {
      updateVanillaSpawnCapacity(EntityClassification.CREATURE, limits.getCreature());
      updateVanillaSpawnCapacity(EntityClassification.MONSTER, limits.getMonster());
      updateVanillaSpawnCapacity(EntityClassification.WATER_AMBIENT, limits.getWaterAmbient());
      updateVanillaSpawnCapacity(EntityClassification.WATER_CREATURE, limits.getWaterCreature());
      updateVanillaSpawnCapacity(EntityClassification.AMBIENT, limits.getAmbient());
    }
  }
  
  private static void updateVanillaSpawnCapacity(EntityClassification e, int max) {
    try {
      Optional<String> fieldName = findFieldName(e);
      if (fieldName.isPresent()) {
        ObfuscationReflectionHelper.setPrivateValue(EntityClassification.class, e, max, fieldName.get());
        if (ConfigHandler.isLoggingEnabled()) {
          LOGGER.info("Changed vanilla spawn limit for '" + e + "' to " + e.getMaxInstancesPerChunk());
        }
      } else {
        LOGGER.error("Failed to set spawn limit for '" + e + "' to " + max + ". Cannot find field max.");
      }
    } catch (Exception c) {
      LOGGER.error("Failed to set spawn limit for '" + e + "' to " + max, c);
    }
  }
  
  private static Optional<String> findFieldName(EntityClassification classification) {
    Map<String, Integer> fields = Arrays.stream(EntityClassification.class.getDeclaredFields())
        .filter(f -> !Modifier.isStatic(f.getModifiers()) && (int.class.equals(f.getType()) || Integer.class.equals(f.getType())))
        .collect(Collectors.toMap(
            Field::getName,
            f -> Optional.ofNullable(ObfuscationReflectionHelper.getPrivateValue(EntityClassification.class, classification, f.getName()))
                .map(r -> (Integer) r)
                .orElse(-1)
            )
        );
    
    Optional<String> target = fields.entrySet().stream()
        .filter(e -> classification.getMaxInstancesPerChunk() == e.getValue()).map(Map.Entry::getKey)
        .findFirst();
    
    if (target.isPresent())
      return target;
    
    return fields.entrySet().stream().filter(e -> e.getValue() != 32 && e.getValue() != 128).map(Map.Entry::getKey).findFirst();
  }
  
  
  @SubscribeEvent(priority = EventPriority.LOW)
  public void onBiomeLoadingEvent(BiomeLoadingEvent event) {
    MobSpawnInfoBuilder spawner = event.getSpawns();
    SpawnPerMobConfig perMob = ConfigHandler.getPerMob();
    if (perMob.getEnableSpawnerModification()) {
      for (SpawnPerMobConfig.ConfigEntry entry : perMob.getEntries()) {
        if (isAllowedBiome(entry, event.getName().toString(), event.getCategory())) {
          EntityType<?> entity = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entry.getEntity()));
          if (entity != null) {
            List<MobSpawnInfo.Spawners> spawners = spawner.getSpawner(entity.getCategory());
            MobSpawnInfo.Spawners current = spawners.stream()
                .filter(s -> s.type.getRegistryName().equals(entity.getRegistryName()))
                .findFirst().orElse(null);
            if (current != null) {
              spawners.remove(current);
            }
            if (entry.getAddIfMissing() || current != null) {
              MobSpawnInfo.Spawners changed = new MobSpawnInfo.Spawners(entity, entry.getWeight(), entry.getGroupMinSize(), entry.getGroupMaxSize());
              spawners.add(changed);
              if (ConfigHandler.isLoggingEnabled()) {
                SLIMod.LOGGER.debug(String.format("Changed spawn for %25s in %42s %15s to %s", entry.getEntity(), event.getName(), "[" + event.getCategory() + "]", changed));
              }
            }
          }
        }
      }
    }
    SpawnAttemptConfig probabilityConfig = ConfigHandler.getProbability();
    float base = spawner.getProbability();
    setSpawnProbability(spawner, (float) (base * probabilityConfig.getDefaultCreatureSpawnProbabilityMultiplier()));
    
    for (SpawnAttemptConfig.ConfigEntry entry : probabilityConfig.getEntries()) {
      if (entry.getAffectedBiomes().contains(event.getName().toString())) {
        setSpawnProbability(spawner, (float) (base * entry.getProbability()));
      }
    }
  }
  
  private boolean isAllowedBiome(SpawnPerMobConfig.ConfigEntry entry, String biome, Biome.Category category) {
    boolean affected = entry.getAffectedBiomes().isEmpty() || entry.getAffectedBiomes().contains(biome);
    boolean notForbidden = entry.getForbiddenBiomeCategories().stream()
        .noneMatch(c -> c.toUpperCase().equals(category.toString()) || c.toUpperCase().equals(category.getName()));
    return affected && notForbidden;
  }
  
  private void setSpawnProbability(MobSpawnInfoBuilder spawner, Float probability) {
    if (Math.abs(probability - 0.1f) < 0.0001f) {
      return;
    }
    try {
      spawner.creatureGenerationProbability(probability);
      if (spawner.getProbability() != probability) {
        ObfuscationReflectionHelper.setPrivateValue(MobSpawnInfoBuilder.class, spawner, probability, "creatureGenerationProbability");
        LOGGER.info("Changed vanilla spawn probability to " + spawner.getProbability());
      }
    } catch (Exception c) {
      LOGGER.error("Failed to set spawn probability to " + probability, c);
    }
  }
  
  private void preInit(final FMLCommonSetupEvent event) {
    reloadConfig();
  }
  
  @SubscribeEvent
  public void serverLoad(RegisterCommandsEvent event) {
    CommandManager.register(event.getDispatcher());
  }
}
