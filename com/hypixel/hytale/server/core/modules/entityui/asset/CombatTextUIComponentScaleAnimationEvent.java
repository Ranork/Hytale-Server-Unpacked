package com.hypixel.hytale.server.core.modules.entityui.asset;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.CombatTextEntityUIAnimationEventType;
import com.hypixel.hytale.protocol.CombatTextEntityUIComponentAnimationEvent;
import javax.annotation.Nonnull;

public class CombatTextUIComponentScaleAnimationEvent extends CombatTextUIComponentAnimationEvent {
   public static final BuilderCodec<CombatTextUIComponentScaleAnimationEvent> CODEC = BuilderCodec.builder(
         CombatTextUIComponentScaleAnimationEvent.class, CombatTextUIComponentScaleAnimationEvent::new, CombatTextUIComponentAnimationEvent.ABSTRACT_CODEC
      )
      .appendInherited(
         new KeyedCodec<>("StartScale", Codec.FLOAT),
         (event, f) -> event.startScale = f,
         event -> event.startScale,
         (parent, event) -> event.startScale = parent.startScale
      )
      .documentation("The scale that should be applied to text instances before the animation event begins.")
      .addValidator(Validators.nonNull())
      .addValidator(Validators.range(0.0F, 1.0F))
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("EndScale", Codec.FLOAT),
         (event, f) -> event.endScale = f,
         event -> event.endScale,
         (parent, event) -> event.endScale = parent.endScale
      )
      .documentation("The scale that should be applied to text instances by the end of the animation.")
      .addValidator(Validators.nonNull())
      .addValidator(Validators.range(0.0F, 1.0F))
      .add()
      .build();
   private float startScale;
   private float endScale;

   @Nonnull
   @Override
   public CombatTextEntityUIComponentAnimationEvent generatePacket() {
      CombatTextEntityUIComponentAnimationEvent packet = super.generatePacket();
      packet.type = CombatTextEntityUIAnimationEventType.Scale;
      packet.startScale = this.startScale;
      packet.endScale = this.endScale;
      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "CombatTextUIComponentConfig{startScale=" + this.startScale + ", endScale=" + this.endScale + "} " + super.toString();
   }
}
