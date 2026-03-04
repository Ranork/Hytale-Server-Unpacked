package com.hypixel.hytale.server.npc.corecomponents;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.blackboard.view.resource.ResourceView;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockTarget {
   private final Vector3d position = new Vector3d(Vector3d.MIN);
   private int chunkChangeRevision = -1;
   private int foundBlockType = Integer.MIN_VALUE;
   @Nullable
   private ResourceView reservationHolder;

   @Nonnull
   public Vector3d getPosition() {
      return this.position;
   }

   public int getChunkChangeRevision() {
      return this.chunkChangeRevision;
   }

   public int getFoundBlockType() {
      return this.foundBlockType;
   }

   public void setChunkChangeRevision(int chunkChangeRevision) {
      this.chunkChangeRevision = chunkChangeRevision;
   }

   public void setFoundBlockType(int foundBlockType) {
      this.foundBlockType = foundBlockType;
   }

   public void setReservationHolder(ResourceView resourceView) {
      this.reservationHolder = resourceView;
   }

   public void reset(@Nonnull NPCEntity parent) {
      if (this.reservationHolder != null) {
         this.reservationHolder.clearReservation(parent.getReference());
         Blackboard.LOGGER.at(Level.FINE).log("Entity %s cleared reservation at %s", parent.getRoleName(), this.position);
      }

      this.reservationHolder = null;
      this.position.assign(Vector3d.MIN);
      this.chunkChangeRevision = -1;
      this.foundBlockType = Integer.MIN_VALUE;
   }

   public boolean isActive() {
      return this.foundBlockType >= 0;
   }
}
