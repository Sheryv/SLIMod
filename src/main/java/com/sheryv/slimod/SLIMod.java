package com.sheryv.slimod;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.MobSpawnInfoBuilder;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mod(SLIMod.ID)
public class SLIMod {
  public static final String ID = "sli_mcmod";
  public static final Logger LOGGER = LogManager.getLogger();
  
  public SLIMod() {
    // Register the setup method for modloading
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    // Register the enqueueIMC method for modloading
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
    // Register the processIMC method for modloading
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
    // Register the doClientStuff method for modloading
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
    
    // Register ourselves for server and other game events we are interested in
    MinecraftForge.EVENT_BUS.register(this);
  }
  
  @SubscribeEvent(priority = EventPriority.HIGH)
  public void onBiomeLoadingEvent(BiomeLoadingEvent event) {
    
    List<MobSpawnInfo.Spawners> spawns = event.getSpawns().getSpawner(EntityClassification.MONSTER);
    spawns.removeIf(e -> e.type == EntityType.ENDERMAN);
    
    float n = 0.3f;
    
    List<EntityType<? extends AnimalEntity>> types = Arrays.asList(EntityType.PIG, EntityType.COW, EntityType.CHICKEN, EntityType.SHEEP, EntityType.HORSE);
    
    List<MobSpawnInfo.Spawners> animals =
        event.getSpawns().getSpawner(EntityClassification.CREATURE);
    List<MobSpawnInfo.Spawners> old = animals.stream().filter(s -> types.contains(s.type)).collect(Collectors.toList());
    
    for (MobSpawnInfo.Spawners spawners : old) {
      animals.remove(spawners);
      animals.add(new MobSpawnInfo.Spawners(spawners.type, spawners.weight, (int) Math.ceil(spawners.minCount * (spawners.minCount < spawners.maxCount ? 2 : 1.25f)), spawners.maxCount * 2));
    }
    
    try {
      event.getSpawns().creatureGenerationProbability(n);
      LOGGER.info("Changed vanilla spawn probability for '" + event.getName() + "' to " + event.getSpawns().getProbability());
      if (event.getSpawns().getProbability() <= 0.1f) {
        ObfuscationReflectionHelper.setPrivateValue(MobSpawnInfoBuilder.class, event.getSpawns(), n, "creatureGenerationProbability");
        LOGGER.info("Changed2 vanilla spawn probability for '" + event.getName() + "' to " + event.getSpawns().getProbability());
      }
    } catch (Exception c) {
      LOGGER.error("Failed to set spawn probability for '" + event.getName() + "' to " + n, c);
    }
    spawns.add(new MobSpawnInfo.Spawners(EntityType.ENDERMAN, 100, 2, 4));
    LOGGER.info("Adding spawns to {}", event.getName());
  }
  
  
  void updateVanillaSpawnCapacity(EntityClassification e, int max) {
    try {
      ObfuscationReflectionHelper.setPrivateValue(EntityClassification.class, e, max, "max");
      LOGGER.info("Changed vanilla spawn max for '" + e + "' to " + e.getMaxInstancesPerChunk());
    } catch (Exception c) {
      LOGGER.error("Failed to set spawn capacity for '" + e + "' to " + max, c);
    }
  }
  
  private void setup(final FMLCommonSetupEvent event) {
    // some preinit code
    LOGGER.info("HELLO FROM PREINIT");
    LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    updateVanillaSpawnCapacity(EntityClassification.CREATURE, 300);
    updateVanillaSpawnCapacity(EntityClassification.MONSTER, 250);
    updateVanillaSpawnCapacity(EntityClassification.WATER_AMBIENT, 20);
  }
  
  private void doClientStuff(final FMLClientSetupEvent event) {
    // do something that can only be done on the client
    LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().options);
  }
  
  private void enqueueIMC(final InterModEnqueueEvent event) {
    // some example code to dispatch IMC to another mod
    InterModComms.sendTo(ID, "helloworld", () -> {
      LOGGER.info("Hello world from the MDK");
      return "Hello world";
    });
  }
  
  private void processIMC(final InterModProcessEvent event) {
    // some example code to receive and process InterModComms from other mods
    LOGGER.info("Got IMC {}", event.getIMCStream().
        map(m -> m.getMessageSupplier().get()).
        collect(Collectors.toList()));
  }
  
  // You can use SubscribeEvent and let the Event Bus discover methods to call
  @SubscribeEvent
  public void onServerStarting(FMLServerStartingEvent event) {
    // do something when the server starts
    LOGGER.info("HELLO from server starting");
  }
  
  // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
  // Event bus for receiving Registry Events)
  @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
  public static class RegistryEvents {
    @SubscribeEvent
    public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
      // register a new block here
      LOGGER.info("HELLO from Register Block");
    }
  }
}
