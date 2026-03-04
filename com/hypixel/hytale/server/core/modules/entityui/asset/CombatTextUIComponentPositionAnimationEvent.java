package com.hypixel.hytale.server.core.modules.entityui.asset;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.CombatTextEntityUIAnimationEventType;
import com.hypixel.hytale.protocol.CombatTextEntityUIComponentAnimationEvent;
import com.hypixel.hytale.protocol.Vector2f;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import javax.annotation.Nonnull;

public class CombatTextUIComponentPositionAnimationEvent extends CombatTextUIComponentAnimationEvent {
   public static final BuilderCodec<CombatTextUIComponentPositionAnimationEvent> CODEC = BuilderCodec.builder(
         CombatTextUIComponentPositionAnimationEvent.class,
         CombatTextUIComponentPositionAnimationEvent::new,
         CombatTextUIComponentAnimationEvent.ABSTRACT_CODEC
      )
      .appendInherited(
         new KeyedCodec<>("PositionOffset", ProtocolCodecs.VECTOR2F),
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
