package com.sheryv.slimod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.sheryv.slimod.SLIMod;
import com.sheryv.slimod.config.ConfigProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CommandManager {
  private static final UUID DUMMY_UUID = new UUID(0L, 0L);
  private static final String MOD_LITERAL = "slimod";
  
  private static ArgumentBuilder<CommandSource, ?> registerKill(CommandDispatcher<CommandSource> dispatcher) {
    return Commands.literal("kill")
        .requires(cs -> cs.hasPermission(2))
        .then(Commands.argument("type", StringArgumentType.word())
            .executes(context -> {
              ServerPlayerEntity player = context.getSource().getPlayerOrException();
              String type = context.getArgument("type", String.class);
              if (type == null || type.trim().isEmpty()) {
                player.sendMessage(new StringTextComponent(TextFormatting.RED + "Use 'all', 'passive', 'hostile' or name of the mob followed by optional dimension id"), DUMMY_UUID);
                SLIMod.LOGGER.error("Use 'all', 'passive', 'hostile', 'entity' or name of the mob followed by optional dimension id");
                return 0;
              }
              boolean all = "all".equals(type);
              boolean passive = "passive".equals(type);
              boolean hostile = "hostile".equals(type);
              boolean entity = "entity".equals(type);
              
              ServerWorld worldServer = player.getLevel();
              
              List<Entity> entities = worldServer.getEntities(null, input -> {
                if (all) {
                  return !(input instanceof PlayerEntity);
                } else if (passive) {
                  return input instanceof AnimalEntity && !(input instanceof IMob);
                } else if (hostile) {
                  return input instanceof IMob;
                } else if (entity) {
                  return !(input instanceof AnimalEntity) && !(input instanceof PlayerEntity);
                } else {
                  String id = input.getType().getRegistryName().toString();
                  return type.equals(id);
                }
              });
              for (Entity e : entities) {
                worldServer.removeEntity(e, false);
              }
              player.sendMessage(new StringTextComponent(TextFormatting.YELLOW + "Removed " + entities.size() + " entities!"), DUMMY_UUID);
              return 0;
            }));
  }
  
  private static ArgumentBuilder<CommandSource, ?> registerReload(CommandDispatcher<CommandSource> dispatcher) {
    return Commands.literal("reload")
        .requires(cs -> cs.hasPermission(1))
        .executes((context -> {
          try {
            ConfigProvider.reload();
            SLIMod.reloadConfig();
            context.getSource().sendSuccess(new StringTextComponent(SLIMod.NAME + " config has been reloaded."), true);
          } catch (Exception e) {
            SLIMod.LOGGER.error("Cannot reload", e);
            context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "Error: " + e.getLocalizedMessage()));
          }
          return 0;
        }));
  }
  
  private static ArgumentBuilder<CommandSource, ?> registerDebug(CommandDispatcher<CommandSource> dispatcher) {
    return Commands.literal("debug")
        .requires(cs -> cs.hasPermission(1))
        .executes((context -> {
          try {
            ServerPlayerEntity player = context.getSource().getPlayerOrException();
            ServerWorld worldServer = player.getLevel();
            
            Biome biome = worldServer.getBiome(player.blockPosition());
            SLIMod.LOGGER.debug("Biome at player {} {}", biome.getRegistryName(), biome.getBiomeCategory());
            SLIMod.LOGGER.debug("MobSettings.probability: {}", biome.getMobSettings().getCreatureProbability());
            String sp = biome.getMobSettings().getSpawnerTypes()
                .stream()
                .map(t -> "[" + t.getName() + "] max=" + t.getMaxInstancesPerChunk() + "\n" + biome.getMobSettings().getMobs(t).stream().map(MobSpawnInfo.Spawners::toString).collect(Collectors.joining(",\n\t", "\t", "\n")))
                .collect(Collectors.joining("\n"));
            SLIMod.LOGGER.debug("MobSettings.spawners: \n{}", sp);
            
          } catch (Exception e) {
            SLIMod.LOGGER.error("Debug error", e);
          }
          return 0;
        }));
  }
  
  
  public static void register(CommandDispatcher<CommandSource> dispatcher) {
    LiteralArgumentBuilder<CommandSource> builder = Commands.literal(MOD_LITERAL)
        .then(registerReload(dispatcher))
        .then(registerKill(dispatcher));
    if (SLIMod.LOGGER.isDebugEnabled()) {
      builder.then(registerDebug(dispatcher));
    }
    
    dispatcher.register(builder);
//    dispatcher.register(Commands.literal(MOD_LITERAL).redirect(commands));
  }
  
  
}
