package com.hypixel.hytale.server.core.modules.entityui.asset;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector2fUtil;
import com.hypixel.hytale.protocol.CombatTextEntityUIAnimationEventType;
import com.hypixel.hytale.protocol.CombatTextEntityUIComponentAnimationEvent;
import javax.annotation.Nonnull;
import org.joml.Vector2f;

public class CombatTextUIComponentPositionAnimationEvent extends CombatTextUIComponentAnimationEvent {
   public static final BuilderCodec<CombatTextUIComponentPositionAnimationEvent> CODEC = BuilderCodec.builder(
         CombatTextUIComponentPositionAnimationEvent.class,
         CombatTextUIComponentPositionAnimationEvent::new,
         CombatTextUIComponentAnimationEvent.ABSTRACT_CODEC
      )
      .appendInherited(
         new KeyedCodec<>("PositionOffset", Vector2fUtil.CODEC),
         (event, f) -> event.positionOffset = f,
         event -> event.positionOffset,
         (parent, event) -> event.positionOffset = parent.positionOffset
      )
      .documentation("The offset from the starting position that the text instance should animate to.")
      .addValidator(Validators.nonNull())
      .add()
      .build();
   private Vector2f positionOffset;

   @Nonnull
   @Override
   public CombatTextEntityUIComponentAnimationEvent generatePacket() {
      CombatTextEntityUIComponentAnimationEvent packet = super.generatePacket();
      packet.type = CombatTextEntityUIAnimationEventType.Position;
      packet.positionOffset = this.positionOffset;
      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "CombatTextUIComponentPositionAnimationEvent{positionOffset=" + this.positionOffset + "} " + super.toString();
   }
}
