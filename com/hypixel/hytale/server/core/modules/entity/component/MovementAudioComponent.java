package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.function.Predicate;
import javax.annotation.Nonnull;

public class MovementAudioComponent implements Component<EntityStore> {
   public static float NO_REPEAT = -1.0F;
   private final MovementAudioComponent.ShouldHearPredicate shouldHearPredicate = new MovementAudioComponent.ShouldHearPredicate();
   private int lastInsideBlockTypeId = 0;
   private float nextMoveInRepeat = NO_REPEAT;

   public static ComponentType<EntityStore, MovementAudioComponent> getComponentType() {
      return EntityModule.get().getMovementAudioComponentType();
   }

   @Nonnull
   public MovementAudioComponent.ShouldHearPredicate getShouldHearPredicate(Ref<EntityStore> ref) {
      this.shouldHearPredicate.owner = ref;
      return this.shouldHearPredicate;
   }

   public int getLastInsideBlockTypeId() {
      return this.lastInsideBlockTypeId;
   }

   public void setLastInsideBlockTypeId(int lastInsideBlockTypeId) {
      this.lastInsideBlockTypeId = lastInsideBlockTypeId;
   }

   public boolean canMoveInRepeat() {
      return this.nextMoveInRepeat != NO_REPEAT;
   }

   public boolean tickMoveInRepeat(float dt) {
      return (this.nextMoveInRepeat -= dt) <= 0.0F;
   }

   public void setNextMoveInRepeat(float nextMoveInRepeat) {
      this.nextMoveInRepeat = nextMoveInRepeat;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new MovementAudioComponent();
   }

   public static class ShouldHearPredicate implements Predicate<Ref<EntityStore>> {
      protected Ref<EntityStore> owner;

      public boolean test(@Nonnull Ref<EntityStore> targetRef) {
         return !this.owner.equals(targetRef);
      }
   }
}
