package com.hypixel.hytale.builtin.triggervolumes.effect;

import com.hypixel.hytale.codec.EmptyExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;

public abstract class TriggerCondition {
   @Nonnull
   public static final CodecMapCodec<TriggerCondition> CODEC = new CodecMapCodec<>("Type");
   @Nonnull
   public static final BuilderCodec<TriggerCondition> BASE_CODEC = BuilderCodec.abstractBuilder(TriggerCondition.class)
      .append(
         new KeyedCodec<>("Event", new EnumCodec<>(TriggerEventType.class, EnumCodec.EnumStyle.LEGACY)),
         TriggerCondition::setEventType,
         TriggerCondition::getEventType
      )
      .add()
      .build();
   private TriggerEventType eventType;

   @Nonnull
   public static TriggerCondition deepCopy(@Nonnull TriggerCondition condition) {
      TriggerCondition copy = CODEC.decode(CODEC.encode(condition, EmptyExtraInfo.EMPTY), EmptyExtraInfo.EMPTY);
      if (copy == null) {
         throw new IllegalStateException("Trigger condition codec returned null for " + condition.getClass().getName());
      } else {
         return copy;
      }
   }

   @Nonnull
   public static List<TriggerCondition> deepCopyList(@Nonnull List<TriggerCondition> conditions) {
      ArrayList<TriggerCondition> copy = new ArrayList<>(conditions.size());

      for (TriggerCondition condition : conditions) {
         copy.add(deepCopy(condition));
      }

      return copy;
   }

   @Nonnull
   public TriggerEventType getEventType() {
      return this.eventType;
   }

   public void setEventType(@Nonnull TriggerEventType eventType) {
      this.eventType = eventType;
   }

   public abstract boolean test(@Nonnull TriggerContext var1);

   public void applyOnAccept(@Nonnull TriggerContext context) {
   }

   public void onEntityExit(@Nonnull UUID entityUuid) {
   }
}
