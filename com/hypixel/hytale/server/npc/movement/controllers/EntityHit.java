package com.hypixel.hytale.server.npc.movement.controllers;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public final class EntityHit {
   @Nullable
   public Ref<EntityStore> entity;
   public boolean isPlayer;
   @Nonnull
   public final Vector3d sourcePosition = new Vector3d();
   @Nonnull
   public final Vector3d targetPosition = new Vector3d();
   public double t;

   public void assign(@Nonnull Ref<EntityStore> entity, boolean isPlayer, @Nonnull Vector3dc sourcePosition, @Nonnull Vector3dc targetPosition, double t) {
      this.entity = entity;
      this.isPlayer = isPlayer;
      this.sourcePosition.set(sourcePosition);
      this.targetPosition.set(targetPosition);
      this.t = t;
   }

   void clearReferences() {
      this.entity = null;
   }
}
