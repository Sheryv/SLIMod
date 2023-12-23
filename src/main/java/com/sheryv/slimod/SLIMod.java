package com.sheryv.slimod;

import com.mojang.serialization.Codec;
import com.sheryv.slimod.command.CommandManager;
import com.sheryv.slimod.config.ConfigHandler;
import com.sheryv.slimod.config.LimitConfig;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mod(SLIMod.ID)
public class SLIMod {
  public static final String ID = "sli_mcmod";
  public static final String NAME = "SLI-SpawnLimitIncrease";
  public static final Logger LOGGER = LogManager.getLogger(ID);
  
  public SLIMod() {
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    bus.addListener(this::onCommonSetup);
    
    ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC, NAME + ".toml");
    
    MinecraftForge.EVENT_BUS.register(this);
    
    BIOME_MODIFIER_SERIALIZERS.register(bus);
  }
  
  public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, ID);
  
  private static final RegistryObject<Codec<SpawnProbabilityBiomeModifier>> SPAWN_PROBABILITY_CODEC =
      BIOME_MODIFIER_SERIALIZERS.register(SpawnProbabilityBiomeModifier.NAME, SpawnProbabilityBiomeModifier.CODEC);
  private static final RegistryObject<Codec<SpawnRateBiomeModifier>> SPAWN_RATE_CODEC =
      BIOME_MODIFIER_SERIALIZERS.register(SpawnRateBiomeModifier.NAME, SpawnRateBiomeModifier.CODEC);
  
  public static void reloadConfig() {
    LimitConfig limits = ConfigHandler.getLimits();
    
    if (limits.getEnableLimitModification()) {
      updateVanillaSpawnCapacity(MobCategory.CREATURE, limits.getCreature());
      updateVanillaSpawnCapacity(MobCategory.MONSTER, limits.getMonster());
      updateVanillaSpawnCapacity(MobCategory.WATER_AMBIENT, limits.getWaterAmbient());
      updateVanillaSpawnCapacity(MobCategory.WATER_CREATURE, limits.getWaterCreature());
      updateVanillaSpawnCapacity(MobCategory.AMBIENT, limits.getAmbient());
      updateVanillaSpawnCapacity(MobCategory.UNDERGROUND_WATER_CREATURE, limits.getUndergroundWaterCreature());
      updateVanillaSpawnCapacity(MobCategory.AXOLOTLS, limits.getAxolotls());
    }
  }
  
  private static void updateVanillaSpawnCapacity(MobCategory e, int max) {
    try {
      Optional<Field> field = findField(e);
      if (field.isPresent()) {
        Object current = ObfuscationReflectionHelper.getPrivateValue(MobCategory.class, e, field.get().getName());
        if (!Integer.valueOf(max).equals(current)) {
          ObfuscationReflectionHelper.setPrivateValue(MobCategory.class, e, max, field.get().getName());
          if (ConfigHandler.isLoggingEnabled()) {
            LOGGER.info("Changed vanilla spawn limit for '" + e + "' from " + current + " to " + e.getMaxInstancesPerChunk());
          }
        }
      } else {
        LOGGER.error("Failed to set spawn limit for '" + e + "' to " + max + ". Cannot find field max.");
      }
    } catch (Exception c) {
      LOGGER.error("Failed to set spawn limit for '" + e + "' to " + max, c);
    }
  }
  
  private static Optional<Field> findField(MobCategory classification) {
    List<Pair<Field, Integer>> fields = Arrays.stream(MobCategory.class.getDeclaredFields())
        .filter(f -> !Modifier.isStatic(f.getModifiers()) && (int.class.equals(f.getType()) || Integer.class.equals(f.getType())))
        .map(f -> Pair.of(f,
            Optional.ofNullable(ObfuscationReflectionHelper.getPrivateValue(MobCategory.class, classification, f.getName()))
                .map(r -> (Integer) r)
                .orElse(-1)
        ))
        .collect(Collectors.toList());
    
    Optional<Field> target = fields.stream()
        .filter(e -> "max".equals(e.getKey().getName()) || classification.getMaxInstancesPerChunk() == e.getValue()).map(Pair::getKey)
        .findFirst();
    
    if (target.isPresent())
      return target;
    
    return fields.stream().filter(e -> e.getValue() != 64 && e.getValue() != 128).map(Pair::getKey).findFirst();
  }
  
  private void onCommonSetup(FMLCommonSetupEvent event) {
    ConfigHandler.COMMON_SPEC.getValues();
    reloadConfig();
  }
  
  @SubscribeEvent
  public void serverLoad(RegisterCommandsEvent event) {
    CommandManager.register(event.getDispatcher());
  }
}
