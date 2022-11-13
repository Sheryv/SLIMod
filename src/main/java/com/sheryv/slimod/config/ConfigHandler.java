package com.sheryv.slimod.config;

import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigHandler {
  private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
  
  public static final ConfigHandler COMMON = new ConfigHandler(BUILDER);
  public static final ForgeConfigSpec COMMON_SPEC = BUILDER.build();
  private final LimitConfig limits;
  private final ForgeConfigSpec.BooleanValue loggingEnabled;
  
  
  private ConfigHandler(ForgeConfigSpec.Builder builder) {
    LimitConfig limitConfig = new LimitConfig();
    
    builder.push("General");
    loggingEnabled = builder.define("enableLogging", false);
    builder.pop();
    
    builder
        .comment("Here you can change vanilla spawn limits. \nLimits are applied per mob category and defines how " +
            "many mobs can be spawned in certain area. \nIt affects attempts of spawn of animals and monsters but it does not " +
            "increase spawn rate. To refresh these values only config reload is required")
        .push("Spawn_Limits");
    int max = 2000;
    limitConfig.enableLimitModification = builder.comment("When false spawn limits are not changed")
        .define("enableLimitModification", false);
    
    limitConfig.monster = builder
        .comment("Limit for monsters - hostile mobs\nVanilla default: "
            + MobCategory.MONSTER.getMaxInstancesPerChunk())
        .defineInRange("monsterLimit", MobCategory.MONSTER.getMaxInstancesPerChunk(), 1, max);
    
    limitConfig.creature = builder
        .comment("Limit for creatures - passive mobs, animals\nVanilla default: "
            + MobCategory.CREATURE.getMaxInstancesPerChunk())
        .defineInRange("creatureLimit", MobCategory.CREATURE.getMaxInstancesPerChunk(), 1, max);
    
    limitConfig.ambient = builder
        .comment("Limit for ambient - bats...\nVanilla default: "
            + MobCategory.AMBIENT.getMaxInstancesPerChunk())
        .defineInRange("ambientLimit", MobCategory.AMBIENT.getMaxInstancesPerChunk(), 1, max);
    
    limitConfig.waterCreature = builder
        .comment("Limit for water creatures - dolphins, squids...\nVanilla default: "
            + MobCategory.WATER_CREATURE.getMaxInstancesPerChunk())
        .defineInRange("waterCreatureLimit", MobCategory.WATER_CREATURE.getMaxInstancesPerChunk(), 1, max);
    
    limitConfig.waterAmbient = builder
        .comment("Limit for water ambient - fish...\nVanilla default: "
            + MobCategory.WATER_AMBIENT.getMaxInstancesPerChunk())
        .defineInRange("waterAmbientLimit", MobCategory.WATER_AMBIENT.getMaxInstancesPerChunk(), 1, max);
    
    limitConfig.axolotls = builder
        .comment("Limit for axolotls\nVanilla default: "
            + MobCategory.AXOLOTLS.getMaxInstancesPerChunk())
        .defineInRange("axolotlsLimit", MobCategory.AXOLOTLS.getMaxInstancesPerChunk(), 1, max);
    
    limitConfig.undergroundWaterCreature = builder
        .comment("Limit for underground water creatures - glow squids\nVanilla default: "
            + MobCategory.UNDERGROUND_WATER_CREATURE.getMaxInstancesPerChunk())
        .defineInRange("undergroundWaterCreatureLimit", MobCategory.UNDERGROUND_WATER_CREATURE.getMaxInstancesPerChunk(), 1, max);
    builder.pop();
    limits = limitConfig;
  }
  
  public static LimitConfig getLimits() {
    return COMMON.limits;
  }
  
  public static boolean isLoggingEnabled() {
    return COMMON.loggingEnabled.get();
  }
  
  public static void validate() {
    getLimits().validate();
  }
}
