package com.hypixel.hytale.server.npc.components.messaging;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NPCMessage {
   public static final double AGE_INFINITE = -1.0;
   private boolean enabled = true;
   private boolean activated = false;
   private double age;
   private Ref<EntityStore> target;

   public boolean tickAge(float dt) {
      return (this.age -= dt) <= 0.0;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public boolean isActivated() {
      return this.activated;
   }

   public boolean isInfinite() {
      return this.age == -1.0;
   }

   @Nullable
   public Ref<EntityStore> getTarget() {
      return this.target != null && this.target.isValid() ? this.target : null;
   }

   public void activate(Ref<EntityStore> target, double age) {
      this.age = age;
      this.activated = true;
      this.target = target;
   }

   public void deactivate() {
      this.activated = false;
   }

   @Nonnull
   public NPCMessage clone() {
      NPCMessage message = new NPCMessage();
      message.enabled = this.enabled;
      message.activated = this.activated;
      message.age = this.age;
      message.target = this.target;
      return message;
   }
}
