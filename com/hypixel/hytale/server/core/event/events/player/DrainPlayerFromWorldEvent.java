package com.hypixel.hytale.server.core.event.events.player;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DrainPlayerFromWorldEvent implements IEvent<String> {
   @Nonnull
   private final Holder<EntityStore> holder;
   @Nonnull
   private World world;
   @Nullable
   private Transform transform;

   public DrainPlayerFromWorldEvent(@Nonnull Holder<EntityStore> holder, @Nonnull World world, @Nullable Transform transform) {
      this.holder = holder;
      this.world = world;
      this.transform = transform;
   }

   @Nonnull
   public Holder<EntityStore> getHolder() {
      return this.holder;
   }

   @Nonnull
   public World getWorld() {
      return this.world;
   }

   public void setWorld(@Nonnull World world) {
      this.world = world;
   }

   @Nullable
   public Transform getTransform() {
      return this.transform;
   }

   public void setTransform(@Nullable Transform transform) {
      this.transform = transform;
   }

   @Nonnull
   @Override
   public String toString() {
      return "DrainPlayerFromWorldEvent{world=" + this.world + ", transform=" + this.transform + "} " + super.toString();
   }
}
