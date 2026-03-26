package com.hypixel.hytale.server.core.command.commands.utility.worldmap;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.world.worldmap.provider.chunk.ImageBuilder;
import javax.annotation.Nonnull;

public class WorldMapCommand extends AbstractCommandCollection {
   public WorldMapCommand() {
      super("worldmap", "server.commands.worldmap.desc");
      this.addAliases("map");
      this.addSubCommand(new WorldMapReloadCommand());
      this.addSubCommand(new WorldMapDiscoverCommand());
      this.addSubCommand(new WorldMapUndiscoverCommand());
      this.addSubCommand(new WorldMapClearMarkersCommand());
      this.addSubCommand(new WorldMapViewRadiusSubCommand());
      this.addSubCommand(new WorldMapCommand.QuantizeCommand());
   }

   private static class QuantizeCommand extends CommandBase {
      public QuantizeCommand() {
         super("quantize", "server.commands.worldmap.quantize.desc");
         this.addAliases("quant", "q");
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         boolean enabled = ImageBuilder.toggleQuantization();
         String key = enabled ? "server.commands.worldmap.quantize.enabled" : "server.commands.worldmap.quantize.disabled";
         context.sendMessage(Message.translation(key));
      }
   }
}
