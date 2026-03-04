package com.hypixel.hytale.server.core.modules.entityui.asset;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.CombatTextEntityUIComponentAnimationEvent;
import com.hypixel.hytale.protocol.EntityUIType;
import com.hypixel.hytale.protocol.RangeVector2f;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class CombatTextUIComponent extends EntityUIComponent {
   private static final float DEFAULT_FONT_SIZE = 68.0F;
   private static final Color DEFAULT_TEXT_COLOR = new Color((byte)-1, (byte)-1, (byte)-1);
   public static final BuilderCodec<CombatTextUIComponent> CODEC = BuilderCodec.builder(
         CombatTextUIComponent.class, CombatTextUIComponent::new, EntityUIComponent.ABSTRACT_CODEC
      )
      .appendInherited(
         new KeyedCodec<>("RandomPositionOffsetRange", ProtocolCodecs.RANGE_VECTOR2F),
         (component, v) -> component.randomPositionOffsetRange = v,
         component -> component.randomPositionOffsetRange,
         (component, parent) -> component.randomPositionOffsetRange = parent.randomPositionOffsetRange
      )
      .addValidator(Validators.nonNull())
      .documentation(
         "The maximum range for randomly offsetting text instances from their starting position. Values are treated as absolute and apply in both directions of the axis."
      )
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("ViewportMargin", Codec.FLOAT),
         (component, f) -> component.viewportMargin = f,
         component -> component.viewportMargin,
         (component, parent) -> component.viewportMargin = parent.viewportMargin
      )
      .addValidator(Validators.range(0.0F, 200.0F))
      .documentation("The minimum distance (in px) from the edges of the viewport that text instances should clamp to.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("Duration", Codec.FLOAT),
         (component, f) -> component.duration = f,
         component -> component.duration,
         (component, parent) -> component.duration = parent.duration
      )
      .addValidator(Validators.nonNull())
      .addValidator(Validators.range(0.1F, 10.0F))
      .documentation("The duration for which text instances in this component should be visible.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("HitAngleModifierStrength", Codec.FLOAT),
         (component, f) -> component.hitAngleModifierStrength = f,
         component -> component.hitAngleModifierStrength,
         (component, parent) -> component.hitAngleModifierStrength = parent.hitAngleModifierStrength
      )
      .addValidator(Validators.range(0.0F, 10.0F))
      .documentation("Strength of the modifier to apply to the X axis of a position animation (if set) based on the angle of a melee attack.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("FontSize", Codec.FLOAT),
         (component, f) -> component.fontSize = f,
         component -> component.fontSize,
         (component, parent) -> component.fontSize = parent.fontSize
      )
      .documentation("The font size to apply to text instances.")
      .add()
      .<Color>appendInherited(
         new KeyedCodec<>("TextColor", ProtocolCodecs.COLOR),
         (component, c) -> component.textColor = c,
         component -> component.textColor,
         (component, parent) -> component.textColor = parent.textColor
      )
      .documentation("The text color to apply to text instances.")
      .add()
      .<CombatTextUIComponentAnimationEvent[]>appendInherited(
         new KeyedCodec<>("AnimationEvents", new ArrayCodec<>(CombatTextUIComponentAnimationEvent.CODEC, CombatTextUIComponentAnimationEvent[]::new)),
         (component, o) -> component.animationEvents = o,
         component -> component.animationEvents,
         (component, parent) -> component.animationEvents = parent.animationEvents
      )
      .addValidator(Validators.nonNull())
      .documentation("Animation events for controlling animation of scale, position, and opacity.")
      .add()
      .build();
   private RangeVector2f randomPositionOffsetRange;
   private float viewportMargin;
   private float duration;
   private float hitAngleModifierStrength = 1.0F;
   private float fontSize = 68.0F;
   private Color textColor = DEFAULT_TEXT_COLOR;
   private CombatTextUIComponentAnimationEvent[] animationEvents;

   @Nonnull
   @Override
   protected com.hypixel.hytale.protocol.EntityUIComponent generatePacket() {
      com.hypixel.hytale.protocol.EntityUIComponent packet = super.generatePacket();
      packet.type = EntityUIType.CombatText;
      packet.combatTextRandomPositionOffsetRange = this.randomPositionOffsetRange;
      packet.combatTextViewportMargin = this.viewportMargin;
      packet.combatTextDuration = this.duration;
      packet.combatTextHitAngleModifierStrength = this.hitAngleModifierStrength;
      packet.combatTextFontSize = this.fontSize;
      packet.combatTextColor = this.textColor;
      packet.combatTextAnimationEvents = new CombatTextEntityUIComponentAnimationEvent[this.animationEvents.length];

      for (int i = 0; i < this.animationEvents.length; i++) {
         packet.combatTextAnimationEvents[i] = this.animationEvents[i].generatePacket();
      }

      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "CombatTextUIComponent{randomPositionOffsetRange="
         + this.randomPositionOffsetRange
         + ", viewportMargin"
         + this.viewportMargin
         + ", duration="
         + this.duration
         + ", hitAngleModifierStrength="
         + this.hitAngleModifierStrength
         + ", fontSize="
         + this.fontSize
         + ", textColor="
         + this.textColor
         + ", animationEvents="
         + Arrays.toString((Object[])this.animationEvents)
         + "} "
         + super.toString();
   }
}
