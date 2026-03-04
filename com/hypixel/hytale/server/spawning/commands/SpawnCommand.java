package com.hypixel.hytale.server.spawning.commands;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class SpawnCommand extends AbstractCommandCollection {
   public SpawnCommand() {
      super("spawning", "server.commands.spawning.desc");
      this.addAliases("sp");
      this.addSubCommand(new SpawnCommand.EnableCommand());
      this.addSubCommand(new SpawnCommand.DisableCommand());
      this.addSubCommand(new SpawnBeaconsCommand());
      this.addSubCommand(new SpawnMarkersCommand());
      this.addSubCommand(new SpawnPopulateCommand());
      this.addSubCommand(new SpawnStatsCommand());
      this.addSubCommand(new SpawnSuppressionCommand());
   }

   private static class DisableCommand extends AbstractWorldCommand {
      public DisableCommand() {
         super("disable", "server.commands.spawning.disable.desc");
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         WorldConfig worldConfig = world.getWorldConfig();
         worldConfig.setSpawningNPC(false);
         worldConfig.markChanged();
         context.sendMessage(Message.translation("server.commands.spawning.disabled").param("worldName", world.getName()));
      }
   }

   private static class EnableCommand extends AbstractWorldCommand {
      public EnableCommand() {
         super("enable", "server.commands.spawning.enable.desc");
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         WorldConfig worldConfig = world.getWorldConfig();
         worldConfig.setSpawningNPC(true);
         worldConfig.markChanged();
         context.sendMessage(Message.translation("server.commands.spawning.enabled").param("worldName", world.getName()));
      }
   }
}
