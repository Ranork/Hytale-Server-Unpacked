package com.hypixel.hytale.server.core.asset.modifiers;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class MovementEffects implements NetworkSerializable<com.hypixel.hytale.protocol.MovementEffects> {
   @Nonnull
   public static final BuilderCodec<MovementEffects> CODEC = BuilderCodec.builder(MovementEffects.class, MovementEffects::new)
      .appendInherited(
         new KeyedCodec<>("DisableAll", Codec.BOOLEAN),
         (entityEffect, s) -> entityEffect.disableAll = s,
         entityEffect -> entityEffect.disableAll,
         (entityEffect, parent) -> entityEffect.disableAll = parent.disableAll
      )
      .documentation("Determines whether all movement input is disabled")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("DisableForward", Codec.BOOLEAN),
         (entityEffect, s) -> entityEffect.disableForward = s,
         entityEffect -> entityEffect.disableForward,
         (entityEffect, parent) -> entityEffect.disableForward = parent.disableForward
      )
      .documentation("Determines whether forwards movement input is disabled")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("DisableBackward", Codec.BOOLEAN),
         (entityEffect, s) -> entityEffect.disableBackward = s,
         entityEffect -> entityEffect.disableBackward,
         (entityEffect, parent) -> entityEffect.disableBackward = parent.disableBackward
      )
      .documentation("Determines whether backwards movement input is disabled")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("DisableLeft", Codec.BOOLEAN),
         (entityEffect, s) -> entityEffect.disableLeft = s,
         entityEffect -> entityEffect.disableLeft,
         (entityEffect, parent) -> entityEffect.disableLeft = parent.disableLeft
      )
      .documentation("Determines whether left-strafe movement input is disabled")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("DisableRight", Codec.BOOLEAN),
         (entityEffect, s) -> entityEffect.disableRight = s,
         entityEffect -> entityEffect.disableRight,
         (entityEffect, parent) -> entityEffect.disableRight = parent.disableRight
      )
      .documentation("Determines whether right-strafe movement input is disabled")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("DisableSprint", Codec.BOOLEAN),
         (entityEffect, s) -> entityEffect.disableSprint = s,
         entityEffect -> entityEffect.disableSprint,
         (entityEffect, parent) -> entityEffect.disableSprint = parent.disableSprint
      )
      .documentation("Determines whether sprint input is disabled")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("DisableJump", Codec.BOOLEAN),
         (entityEffect, s) -> entityEffect.disableJump = s,
         entityEffect -> entityEffect.disableJump,
         (entityEffect, parent) -> entityEffect.disableJump = parent.disableJump
      )
      .documentation("Determines whether jump input is disabled")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("DisableCrouch", Codec.BOOLEAN),
         (entityEffect, s) -> entityEffect.disableCrouch = s,
         entityEffect -> entityEffect.disableCrouch,
         (entityEffect, parent) -> entityEffect.disableCrouch = parent.disableCrouch
      )
      .documentation("Determines whether crouch input is disabled")
      .add()
      .afterDecode((movementEffects, extraInfo) -> {
         if (movementEffects.disableAll) {
            movementEffects.disableForward = true;
            movementEffects.disableBackward = true;
            movementEffects.disableLeft = true;
            movementEffects.disableRight = true;
            movementEffects.disableSprint = true;
            movementEffects.disableJump = true;
            movementEffects.disableCrouch = true;
         }
      })
      .build();
   protected boolean disableAll = false;
   protected boolean disableForward = false;
   protected boolean disableBackward = false;
   protected boolean disableLeft = false;
   protected boolean disableRight = false;
   protected boolean disableSprint = false;
   protected boolean disableJump = false;
   protected boolean disableCrouch = false;

   protected MovementEffects() {
   }

   public boolean isDisableAll() {
      return this.disableAll;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.MovementEffects toPacket() {
      com.hypixel.hytale.protocol.MovementEffects packet = new com.hypixel.hytale.protocol.MovementEffects();
      packet.disableForward = this.disableForward;
      packet.disableBackward = this.disableBackward;
      packet.disableLeft = this.disableLeft;
      packet.disableRight = this.disableRight;
      packet.disableSprint = this.disableSprint;
      packet.disableJump = this.disableJump;
      packet.disableCrouch = this.disableCrouch;
      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "MovementEffects{, disableAll="
         + this.disableAll
         + ", disableForward="
         + this.disableForward
         + ", disableBackward="
         + this.disableBackward
         + ", disableLeft="
         + this.disableLeft
         + ", disableRight="
         + this.disableRight
         + ", disableSprint="
         + this.disableSprint
         + ", disableJump="
         + this.disableJump
         + ", disableCrouch="
         + this.disableCrouch
         + "}";
   }
}
