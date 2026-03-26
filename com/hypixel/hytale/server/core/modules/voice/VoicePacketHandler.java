package com.hypixel.hytale.server.core.modules.voice;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.voice.VoiceData;
import com.hypixel.hytale.server.core.io.handlers.IPacketHandler;
import com.hypixel.hytale.server.core.io.handlers.SubPacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class VoicePacketHandler implements SubPacketHandler {
   private final HytaleLogger logger;
   private final IPacketHandler parent;
   private boolean loggedGameStreamRejection = false;

   public VoicePacketHandler(@Nonnull IPacketHandler parent) {
      this.parent = parent;
      this.logger = VoiceModule.get().getLogger();
   }

   @Override
   public void registerHandlers() {
      this.parent.registerHandler(450, p -> this.handleVoiceData((VoiceData)p));
   }

   private void handleVoiceData(@Nonnull VoiceData packet) {
      if (!this.loggedGameStreamRejection) {
         this.loggedGameStreamRejection = true;
         PlayerRef playerRef = this.parent.getPlayerRef();
         this.logger
            .at(Level.WARNING)
            .log(
               "[VoicePacket] REJECTED: Voice data received on game stream from %s - client should use dedicated voice stream",
               playerRef != null ? playerRef.getUsername() : "unknown"
            );
      }
   }
}
