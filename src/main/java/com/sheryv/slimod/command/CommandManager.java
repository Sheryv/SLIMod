package com.sheryv.slimod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.sheryv.slimod.SLIMod;
import com.sheryv.slimod.config.ConfigHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.stream.Collectors;

public class CommandManager {
  private static final String MOD_LITERAL = "slimod";
  
  public static final EntityTypeTest<Entity, ?> ANY_TYPE = new EntityTypeTest<>() {
    @Override
    public Entity tryCast(Entity entity) {
      return entity;
    }
    
    @Override
    public Class<? extends Entity> getBaseClass() {
      return Entity.class;
    }
  };
  
  private static LiteralArgumentBuilder<CommandSourceStack> registerKill(CommandDispatcher<CommandSourceStack> dispatcher) {
    return Commands.literal("kill")
        .requires(cs -> cs.hasPermission(2))
        .then(Commands.argument("type", StringArgumentType.word())
            .executes(context -> {
              var player = context.getSource().getPlayerOrException();
              String type = context.getArgument("type", String.class);
              if (type == null || type.trim().isEmpty()) {
                player.sendSystemMessage(Component.literal(ChatFormatting.RED + "Use 'all', 'passive', 'hostile' or name of the mob followed by optional dimension id"));
                SLIMod.LOGGER.error("Use 'all', 'passive', 'hostile', 'entity' or name of the mob followed by optional dimension id");
                return 0;
              }
              boolean all = "all".equals(type);
              boolean passive = "passive".equals(type);
              boolean hostile = "hostile".equals(type);
              boolean entity = "entity".equals(type);
              
              var worldServer = player.getCommandSenderWorld().getServer().getLevel(player.getCommandSenderWorld().dimension());
              
              List<? extends Entity> entities = worldServer.getEntities(ANY_TYPE, input -> {
                if (all) {
                  return !(input instanceof Player);
                } else if (passive) {
                  return input instanceof Animal && !(input instanceof Enemy);
                } else if (hostile) {
                  return input instanceof Enemy;
                } else if (entity) {
                  return !(input instanceof Animal) && !(input instanceof Player);
                } else {
                  String id = ForgeRegistries.ENTITY_TYPES.getKey(input.getType()).toString();
                  return type.equals(id);
                }
              });
              for (Entity e : entities) {
                e.setRemoved(Entity.RemovalReason.KILLED);
              }
              player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + "Removed " + entities.size() + " entities!"));
              return 0;
            }));
  }
  
  private static LiteralArgumentBuilder<CommandSourceStack> registerReload(CommandDispatcher<CommandSourceStack> dispatcher) {
    return Commands.literal("reload")
        .requires(cs -> cs.hasPermission(2))
        .executes((context -> {
          var player = context.getSource().getPlayerOrException();
          try {
            SLIMod.reloadConfig();
            player.sendSystemMessage(Component.literal(SLIMod.NAME + " config has been reloaded."));
          } catch (Exception e) {
            SLIMod.LOGGER.error("Cannot reload", e);
            player.sendSystemMessage(Component.literal(ChatFormatting.RED + "Error: " + e.getLocalizedMessage()));
          }
          return 0;
        }));
  }
  
  private static LiteralArgumentBuilder<CommandSourceStack> registerDebug(CommandDispatcher<CommandSourceStack> dispatcher) {
    return Commands.literal("info")
        .requires(cs -> cs.hasPermission(1))
        .executes((context -> {
          try {
            var player = context.getSource().getPlayerOrException();
            var worldServer = context.getSource().getLevel();
            
            var biomeHolder = worldServer.getBiome(player.blockPosition());
            var biome = worldServer.getBiome(player.blockPosition()).get();
            
            print(String.format("Biome at player position %s%s%s \n%s",
                ChatFormatting.AQUA, biomeHolder.unwrapKey().map(k -> k.location().toString()).orElse(""), ChatFormatting.RESET,
                biomeHolder.tags().map(t -> " - " + t.location()).collect(Collectors.joining("\n"))), player);
            
            print(String.format("MobSettings > creature spawn probability: %s%s", biome.getMobSettings().getCreatureProbability(), ChatFormatting.AQUA), player);
            
            String sp = biome.getMobSettings().getSpawnerTypes()
                .stream()
                .map(t -> {
                  String mobSettings = biome.getMobSettings()
                      .getMobs(t)
                      .unwrap()
                      .stream()
                      .map(d -> String.format(" - %s, size: %s%d%s - %s%d%s, weight: %s%d%s",
                          EntityType.getKey(d.type),
                          ChatFormatting.DARK_GREEN, d.minCount, ChatFormatting.RESET,
                          ChatFormatting.GREEN, d.maxCount, ChatFormatting.RESET,
                          ChatFormatting.AQUA, d.getWeight().asInt(), ChatFormatting.RESET))
                      .collect(Collectors.joining("\n"));
                  
                  return String.format("[%s%s%s] current limit in area: %s%d%s\n%s",
                      ChatFormatting.LIGHT_PURPLE,
                      t.getName(),
                      ChatFormatting.RESET,
                      ChatFormatting.AQUA,
                      t.getMaxInstancesPerChunk(),
                      ChatFormatting.RESET,
                      mobSettings);
                })
                .collect(Collectors.joining("\n\n"));
            print(String.format("MobSettings > spawners: \n%s", sp), player);
          } catch (Exception e) {
            SLIMod.LOGGER.error("Debug error", e);
          }
          return 0;
        }));
  }
  
  private static void print(String text, ServerPlayer player) {
    player.sendSystemMessage(Component.literal(text));
    if (ConfigHandler.isLoggingEnabled()) {
      SLIMod.LOGGER.info(ChatFormatting.stripFormatting(text));
    }
  }
  
  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(MOD_LITERAL)
        .then(registerReload(dispatcher))
        .then(registerKill(dispatcher));
    builder.then(registerDebug(dispatcher));
    
    dispatcher.register(builder);
  }
  
  
}
