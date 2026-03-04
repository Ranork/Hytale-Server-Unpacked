package com.hypixel.hytale.server.core.command.commands.utility.lighting;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;

public class LightingSendCommand extends AbstractCommandCollection {
   public LightingSendCommand() {
      super("send", "server.commands.sendlighting.desc");
      this.addSubCommand(new LightingSendCommand.LightingSendLocalCommand());
      this.addSubCommand(new LightingSendCommand.LightingSendGlobalCommand());
   }

   private static class LightingSendGlobalCommand extends LightingSendToggleCommand {
      public LightingSendGlobalCommand() {
         super(
            "global",
            "server.commands.sendlighting.global.desc",
            "server.commands.sendlighting.global.enabled.desc",
            "server.commands.sendlighting.globalLightingStatus",
            () -> BlockChunk.SEND_GLOBAL_LIGHTING_DATA,
            value -> BlockChunk.SEND_GLOBAL_LIGHTING_DATA = value
         );
      }
   }

   private static class LightingSendLocalCommand extends LightingSendToggleCommand {
      public LightingSendLocalCommand() {
         super(
            "local",
            "server.commands.sendlighting.local.desc",
            "server.commands.sendlighting.local.enabled.desc",
            "server.commands.sendlighting.localLightingStatus",
            () -> BlockChunk.SEND_LOCAL_LIGHTING_DATA,
            value -> BlockChunk.SEND_LOCAL_LIGHTING_DATA = value
         );
      }
   }
}
