package com.hypixel.hytale.server.core.universe.world.events;

import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.event.IProcessedEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class ChunkPreLoadProcessEvent extends ChunkEvent implements IProcessedEvent {
   private final boolean newlyGenerated;
   private long lastDispatchNanos;
   private boolean didLog;
   @Nonnull
   private final Holder<ChunkStore> holder;

   public ChunkPreLoadProcessEvent(@Nonnull Holder<ChunkStore> holder, @Nonnull WorldChunk chunk, boolean newlyGenerated, long lastDispatchNanos) {
      super(chunk);
      this.newlyGenerated = newlyGenerated;
      this.lastDispatchNanos = lastDispatchNanos;
      this.holder = holder;
   }

   public boolean isNewlyGenerated() {
      return this.newlyGenerated;
   }

   public Holder<ChunkStore> getHolder() {
      return this.holder;
   }

   @Override
   public void processEvent(@Nonnull String hookName) {
      long end = System.nanoTime();
      long diff = end - this.lastDispatchNanos;
      this.lastDispatchNanos = end;
      if (diff > this.getChunk().getWorld().getTickStepNanos()) {
         World world = this.getChunk().getWorld();
         if (world.consumeGCHasRun()) {
            world.getLogger()
               .at(Level.SEVERE)
               .log(
                  String.format(
                     "Took too long to run pre-load process hook for chunk: %s > TICK_STEP, Has GC Run: true, %%s, Hook: %%s", FormatUtil.nanosToString(diff)
                  ),
                  this.getChunk(),
                  hookName
               );
         } else {
            world.getLogger()
               .at(Level.SEVERE)
               .log(
                  String.format("Took too long to run pre-load process hook for chunk: %s > TICK_STEP, %%s, Hook: %%s", FormatUtil.nanosToString(diff)),
                  this.getChunk(),
                  hookName
               );
         }

         this.didLog = true;
      }
   }

   public boolean didLog() {
      return this.didLog;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ChunkPreLoadProcessEvent{newlyGenerated="
         + this.newlyGenerated
         + ", lastDispatchNanos="
         + this.lastDispatchNanos
         + ", didLog="
         + this.didLog
         + "} "
         + super.toString();
   }
}
