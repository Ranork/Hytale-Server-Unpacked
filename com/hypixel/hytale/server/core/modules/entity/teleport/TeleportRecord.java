package com.hypixel.hytale.server.core.modules.entity.teleport;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Duration;
import javax.annotation.Nullable;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class TeleportRecord implements Component<EntityStore> {
   private TeleportRecord.Entry lastTeleport;

   public static ComponentType<EntityStore, TeleportRecord> getComponentType() {
      return EntityModule.get().getTeleportRecordComponentType();
   }

   @Nullable
   public TeleportRecord.Entry getLastTeleport() {
      return this.lastTeleport;
   }

   public void setLastTeleport(TeleportRecord.Entry lastTeleport) {
      this.lastTeleport = lastTeleport;
   }

   public boolean hasElapsedSinceLastTeleport(Duration duration) {
      return this.hasElapsedSinceLastTeleport(System.nanoTime(), duration);
   }

   public boolean hasElapsedSinceLastTeleport(long nowNanos, Duration duration) {
      if (this.lastTeleport == null) {
         return true;
      } else {
         long elapsedNanos = nowNanos - this.lastTeleport.timestampNanos();
         return elapsedNanos >= duration.toNanos();
      }
   }

   @NullableDecl
   @Override
   public Component<EntityStore> clone() {
      TeleportRecord clone = new TeleportRecord();
      clone.lastTeleport = this.lastTeleport;
      return clone;
   }

   public record Entry(Location origin, Location destination, long timestampNanos) {
   }
}
