package com.hypixel.hytale.server.core.command.commands.world.chunk;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class ChunkCommand extends AbstractCommandCollection {
   public ChunkCommand() {
      super("chunk", "server.commands.chunk.desc");
      this.addAliases("chunks");
      this.addSubCommand(new ChunkFixHeightMapCommand());
      this.addSubCommand(new ChunkForceTickCommand());
      this.addSubCommand(new ChunkInfoCommand());
      this.addSubCommand(new ChunkLightingCommand());
      this.addSubCommand(new ChunkLoadCommand());
      this.addSubCommand(new ChunkLoadedCommand());
      this.addSubCommand(new ChunkMarkSaveCommand());
      this.addSubCommand(new ChunkMaxSendRateCommand());
      this.addSubCommand(new ChunkRegenerateCommand());
      this.addSubCommand(new ChunkResendCommand());
      this.addSubCommand(new ChunkTintCommand());
      this.addSubCommand(new ChunkTrackerCommand());
      this.addSubCommand(new ChunkUnloadCommand());
   }
}
