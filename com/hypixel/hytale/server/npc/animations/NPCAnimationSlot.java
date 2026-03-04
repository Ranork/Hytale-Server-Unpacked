package com.hypixel.hytale.server.npc.animations;

import com.hypixel.hytale.protocol.AnimationSlot;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

@Deprecated
public enum NPCAnimationSlot implements Supplier<String> {
   Status(AnimationSlot.Status),
   Action(AnimationSlot.Action),
   Face(AnimationSlot.Face);

   public static final NPCAnimationSlot[] VALUES = values();
   private final AnimationSlot mappedSlot;

   private NPCAnimationSlot(AnimationSlot mappedSlot) {
      this.mappedSlot = mappedSlot;
   }

   @Nonnull
   public String get() {
      return this.name();
   }

   public AnimationSlot getMappedSlot() {
      return this.mappedSlot;
   }
}
