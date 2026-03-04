package com.hypixel.hytale.server.core.modules.entity.teleport;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class PendingTeleport implements Component<EntityStore> {
   public static final double MAX_OFFSET = 0.001;
   @Nonnull
   private final Vector3d position = new Vector3d();
   @Nonnull
   private final List<Teleport> pendingTeleports = new ObjectArrayList();
   private int nextTeleportId = 0;
   private int lastTeleportId = 0;

   public static ComponentType<EntityStore, PendingTeleport> getComponentType() {
      return EntityModule.get().getPendingTeleportComponentType();
   }

   @Nonnull
   public PendingTeleport.Result validate(int teleportId, @Nonnull Position teleportPosition) {
      if (teleportId != this.lastTeleportId) {
         return PendingTeleport.Result.INVALID_ID;
      } else {
         this.position.assign(teleportPosition.x, teleportPosition.y, teleportPosition.z);
         Teleport teleport = this.pendingTeleports.removeFirst();
         this.lastTeleportId++;
         return teleport.getPosition().distanceSquaredTo(this.position) <= 0.001 ? PendingTeleport.Result.OK : PendingTeleport.Result.INVALID_POSITION;
      }
   }

   public boolean isEmpty() {
      return this.pendingTeleports.isEmpty();
   }

   public int queueTeleport(Teleport teleport) {
      this.pendingTeleports.add(teleport);
      return this.nextTeleportId++;
   }

   @Nonnull
   public Vector3d getPosition() {
      return this.position;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new PendingTeleport();
   }

   public static enum Result {
      OK,
      INVALID_ID,
      INVALID_POSITION;
   }
}
