package com.sheryv.slimod.config;

import net.minecraft.entity.EntityClassification;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber
public class ConfigHandler {
  public static final ForgeConfigSpec SERVER;
  private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
  private static LimitConfig limits;
  private static SpawnPerMobConfig perMob;
  private static SpawnAttemptConfig probability;
  private static ForgeConfigSpec.BooleanValue loggingEnabled;
  
  static {
    prepare(SERVER_BUILDER);
    SERVER = SERVER_BUILDER.build();
  }
  
  private static void prepare(ForgeConfigSpec.Builder builder) {
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
            + EntityClassification.MONSTER.getMaxInstancesPerChunk())
        .defineInRange("monsterLimit", 70, 1, max);
    
    limitConfig.creature = builder
        .comment("Limit for creatures - passive mobs, animals\nVanilla default: "
            + EntityClassification.CREATURE.getMaxInstancesPerChunk())
        .defineInRange("creatureLimit", 10, 1, max);
    
    limitConfig.ambient = builder
        .comment("Limit for ambient - bats...\nVanilla default: "
            + EntityClassification.AMBIENT.getMaxInstancesPerChunk())
        .defineInRange("ambientLimit", 15, 1, max);
    
    limitConfig.waterCreature = builder
        .comment("Limit for water creatures - dolphins, squids...\nVanilla default: "
            + EntityClassification.WATER_CREATURE.getMaxInstancesPerChunk())
        .defineInRange("waterCreatureLimit", 5, 1, max);
    
    limitConfig.waterAmbient = builder
        .comment("Limit for water ambient - fish...\nVanilla default: "
            + EntityClassification.WATER_AMBIENT.getMaxInstancesPerChunk())
        .defineInRange("waterAmbientLimit", 20, 1, max);
    builder.pop();
    limits = limitConfig;
    
    
    probability = new SpawnAttemptConfig();
    builder
        .push("Spawn_Attempts");
    probability.defaultCreatureSpawnProbabilityMultiplier = builder
        .comment("Change probability of spawn of creature for each attempt. \nIt is calculated as <vanilla value> * " +
            "<provided here>, e.g.: 2.0 means two times bigger probability. \nIt only affects passive mobs that are generated with terrain and may " +
            "decrease performance. \nValues bigger than 3.0 are not recommended. Vanilla default: 1.0. \nValue 1.0 also disables this feature. To refresh this value config and world reload is required")
        .defineInRange("defaultCreatureSpawnProbabilityMultiplier", 1.0, 0.01, 5.0);
    
    List<SpawnAttemptConfig.ConfigEntry> defAttemptList = Arrays.asList(new SpawnAttemptConfig.ConfigEntry(1.0, Arrays.asList("minecraft:plains")));
    
    probability.entries = builder
        .comment("Defines list of multipliers that override default value. List is loaded from top to bottom. Example is provided")
        .defineList("entries", defAttemptList, (e) -> true);
    builder.pop();
    
    
    perMob = new SpawnPerMobConfig();
    builder.comment("Allows to change spawner configuration for each biome. You can change weight or add new mob to some" +
        " biomes. \nIt is not intended to block spawning - use InControl mod for advanced controlling of spawning\n\nFields:\n" +
        "\tentity                   <- entity to configure eg. \"minecraft:cow\"\n" +
        "\taddIfMissing             <- if true add mob spawning to configured biome, if false modifies only vanilla spawning " +
        "(for example: if false cows won't spawn in desert but only in biomes where vanilla spawn is configured), default false\n" +
        "\taffectedBiomes           <- biomes list where change should be applied, empty list means all biomes eg. [\"minecraft:plains\"]\n" +
        "\tforbiddenBiomeCategories <- biome category list that should be ignored, it overrides 'affectedBiomes' list eg. [\"ICY\", \"NETHER\", \"THEEND\"]\n" +
        "\tweight                   <- defines proportions/probability of spawning, every mob category has own pool (for zombie it is 100, for cow 8) eg. 10\n" +
        "\tgroupMaxSize             <- max group size for single spawn eg. 4\n" +
        "\tgroupMinSize             <- min group size for single spawn eg. 2\n\n" +
        "Example for cow that increases spawn group size in plains to 10:\n" +
        "\t[[Spawner_Properties.entries]]\n" +
        "\t\tentity = \"minecraft:cow\"\n" +
        "\t\taffectedBiomes = [\"minecraft:plains\"]\n" +
        "\t\tweight = 8\n" +
        "\t\taddIfMissing = false\n" +
        "\t\tforbiddenBiomeCategories = []\n" +
        "\t\tgroupMaxSize = 10\n" +
        "\t\tgroupMinSize = 10" +
        "\n\nTo refresh this value config and world reload is required. More info on: https://github.com/Sheryv/SLIMod")
        .push("Spawner_Properties");
    perMob.enableSpawnerModification = builder.comment("When false spawners are not changed")
        .define("enableSpawnerModification", false);
    
    List<SpawnPerMobConfig.ConfigEntry> defList = Arrays.asList(new SpawnPerMobConfig.ConfigEntry("minecraft:cow", Arrays.asList("minecraft:plains"), 8, 4, 4));
    perMob.entries = builder.defineList("entries", defList, (e) -> true);
  }
  
  public static LimitConfig getLimits() {
    return limits;
  }
  
  public static SpawnPerMobConfig getPerMob() {
    return perMob;
  }
  
  public static SpawnAttemptConfig getProbability() {
    return probability;
  }
  
  public static boolean isLoggingEnabled() {
    return loggingEnabled.get();
  }
  
  public static void validate() {
    getLimits().validate();
    getPerMob().validate();
    getProbability().validate();
  }
}
