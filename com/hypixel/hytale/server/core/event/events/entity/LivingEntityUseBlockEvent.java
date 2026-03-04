package com.hypixel.hytale.server.core.event.events.entity;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

@Deprecated(forRemoval = true)
public class LivingEntityUseBlockEvent implements IEvent<String> {
   private Ref<EntityStore> ref;
   private String blockType;

   public LivingEntityUseBlockEvent(Ref<EntityStore> ref, String blockType) {
      this.ref = ref;
      this.blockType = blockType;
   }

   public String getBlockType() {
      return this.blockType;
   }

   public Ref<EntityStore> getRef() {
      return this.ref;
   }

   @Nonnull
   @Override
   public String toString() {
      return "LivingEntityUseBlockEvent{blockType=" + this.blockType + "} " + super.toString();
   }
}
