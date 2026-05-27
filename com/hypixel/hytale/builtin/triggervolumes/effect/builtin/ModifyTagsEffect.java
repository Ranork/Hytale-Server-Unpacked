package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModifyTagsEffect extends TriggerEffect {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   public static final BuilderCodec<ModifyTagsEffect> CODEC = BuilderCodec.builder(ModifyTagsEffect.class, ModifyTagsEffect::new, BASE_CODEC)
      .append(
         new KeyedCodec<>("Operation", new EnumCodec<>(ModifyTagsEffect.Operation.class), false),
         (effect, operation) -> effect.operation = operation,
         effect -> effect.operation
      )
      .add()
      .append(new KeyedCodec<>("TagKey", Codec.STRING), (effect, tagKey) -> effect.tagKey = tagKey, effect -> effect.tagKey)
      .add()
      .append(new KeyedCodec<>("TagValue", Codec.STRING, false), (effect, tagValue) -> effect.tagValue = tagValue, effect -> effect.tagValue)
      .add()
      .append(new KeyedCodec<>("MatchKey", Codec.STRING, false), (effect, matchKey) -> effect.matchKey = matchKey, effect -> effect.matchKey)
      .add()
      .append(new KeyedCodec<>("MatchValue", Codec.STRING, false), (effect, matchValue) -> effect.matchValue = matchValue, effect -> effect.matchValue)
      .add()
      .append(new KeyedCodec<>("Radius", Codec.DOUBLE, false), (effect, radius) -> effect.radius = radius, effect -> effect.radius)
      .add()
      .append(
         new KeyedCodec<>("Center", new EnumCodec<>(TaggedVolumeEffectUtil.Center.class), false),
         (effect, center) -> effect.center = center,
         effect -> effect.center
      )
      .add()
      .build();
   @Nonnull
   private ModifyTagsEffect.Operation operation = ModifyTagsEffect.Operation.SET;
   @Nonnull
   private String tagKey = "";
   @Nullable
   private String tagValue;
   @Nullable
   private String matchKey;
   @Nullable
   private String matchValue;
   private double radius = 50.0;
   @Nonnull
   private TaggedVolumeEffectUtil.Center center = TaggedVolumeEffectUtil.Center.VOLUME;

   @Nonnull
   public static ModifyTagsEffect set(@Nonnull TriggerEventType eventType, @Nonnull String tagKey, @Nullable String tagValue) {
      return create(eventType, ModifyTagsEffect.Operation.SET, tagKey, tagValue);
   }

   @Nonnull
   public static ModifyTagsEffect remove(@Nonnull TriggerEventType eventType, @Nonnull String tagKey, @Nullable String tagValue) {
      return create(eventType, ModifyTagsEffect.Operation.REMOVE, tagKey, tagValue);
   }

   @Nonnull
   public static ModifyTagsEffect increment(@Nonnull TriggerEventType eventType, @Nonnull String tagKey, @Nullable String tagValue) {
      return create(eventType, ModifyTagsEffect.Operation.INCREMENT, tagKey, tagValue);
   }

   @Nonnull
   public static ModifyTagsEffect toggle(@Nonnull TriggerEventType eventType, @Nonnull String tagKey) {
      return create(eventType, ModifyTagsEffect.Operation.TOGGLE, tagKey, null);
   }

   @Nonnull
   public static ModifyTagsEffect replace(@Nonnull TriggerEventType eventType, @Nonnull String tagKey, @Nullable String tagValue) {
      return create(eventType, ModifyTagsEffect.Operation.REPLACE, tagKey, tagValue);
   }

   @Nonnull
   public static ModifyTagsEffect append(@Nonnull TriggerEventType eventType, @Nonnull String tagKey, @Nullable String tagValue) {
      return create(eventType, ModifyTagsEffect.Operation.APPEND, tagKey, tagValue);
   }

   @Nonnull
   private static ModifyTagsEffect create(
      @Nonnull TriggerEventType eventType, @Nonnull ModifyTagsEffect.Operation operation, @Nonnull String tagKey, @Nullable String tagValue
   ) {
      ModifyTagsEffect effect = new ModifyTagsEffect();
      effect.setEventType(eventType);
      effect.operation = operation;
      effect.tagKey = tagKey;
      effect.tagValue = tagValue;
      return effect;
   }

   @Nonnull
   public ModifyTagsEffect withMatchTag(@Nullable String matchKey, @Nullable String matchValue) {
      this.matchKey = matchKey;
      this.matchValue = matchValue;
      return this;
   }

   @Override
   public void execute(@Nonnull TriggerContext context) {
      TriggerVolumeManager manager = TaggedVolumeEffectUtil.manager(context);
      if (manager != null && !this.tagKey.isBlank()) {
         UUIDComponent uuidComponent = context.getStore().getComponent(context.getEntityRef(), UUIDComponent.getComponentType());
         if (uuidComponent != null) {
            String tagFilter = TaggedVolumeEffectUtil.composeTagFilter(this.matchKey, this.matchValue);

            for (VolumeEntry volume : TaggedVolumeEffectUtil.collectTargets(context, tagFilter, this.radius, this.center)) {
               this.applyToVolume(manager, volume, context.getEntityRef(), uuidComponent.getUuid());
            }
         }
      }
   }

   void applyToVolume(@Nonnull TriggerVolumeManager manager, @Nonnull VolumeEntry volume, @Nullable Ref<EntityStore> actorRef, @Nonnull UUID actorUuid) {
      if (!this.tagKey.isBlank()) {
         switch (this.operation) {
            case SET:
            case REPLACE:
               manager.setTag(volume.getId(), this.tagKey, this.tagValue, actorRef, actorUuid);
               break;
            case REMOVE:
               manager.removeTag(volume.getId(), this.tagKey, this.tagValue, actorRef, actorUuid);
               break;
            case INCREMENT:
               this.applyIncrement(manager, volume, actorRef, actorUuid);
               break;
            case TOGGLE:
               this.applyToggle(manager, volume, actorRef, actorUuid);
               break;
            case APPEND:
               this.applyAppend(manager, volume, actorRef, actorUuid);
         }
      }
   }

   private void applyAppend(@Nonnull TriggerVolumeManager manager, @Nonnull VolumeEntry volume, @Nullable Ref<EntityStore> actorRef, @Nonnull UUID actorUuid) {
      String currentValue = volume.getRawTags().get(this.tagKey);
      String appendValue = this.tagValue != null ? this.tagValue : "";
      manager.setTag(volume.getId(), this.tagKey, (currentValue != null ? currentValue : "") + appendValue, actorRef, actorUuid);
   }

   private void applyIncrement(@Nonnull TriggerVolumeManager manager, @Nonnull VolumeEntry volume, @Nullable Ref<EntityStore> actorRef, @Nonnull UUID actorUuid) {
      String currentValue = volume.getRawTags().get(this.tagKey);
      BigDecimal currentAmount = currentValue != null ? parseDecimal(currentValue) : BigDecimal.ZERO;
      if (currentAmount == null) {
         this.logInvalidNumber(volume, currentValue);
      } else {
         BigDecimal incrementAmount = parseDecimal(this.tagValue);
         if (incrementAmount == null) {
            this.logInvalidNumber(volume, this.tagValue);
         } else {
            manager.setTag(volume.getId(), this.tagKey, currentAmount.add(incrementAmount).stripTrailingZeros().toPlainString(), actorRef, actorUuid);
         }
      }
   }

   private void applyToggle(@Nonnull TriggerVolumeManager manager, @Nonnull VolumeEntry volume, @Nullable Ref<EntityStore> actorRef, @Nonnull UUID actorUuid) {
      String currentValue = volume.getRawTags().get(this.tagKey);
      if (currentValue == null) {
         manager.setTag(volume.getId(), this.tagKey, "true", actorRef, actorUuid);
      } else {
         String normalized = currentValue.toLowerCase(Locale.ROOT);
         if ("true".equals(normalized)) {
            manager.setTag(volume.getId(), this.tagKey, "false", actorRef, actorUuid);
         } else if ("false".equals(normalized)) {
            manager.setTag(volume.getId(), this.tagKey, "true", actorRef, actorUuid);
         } else {
            LOGGER.at(Level.WARNING)
               .log("Cannot toggle trigger volume tag '%s' on volume '%s': current value '%s' is not boolean", this.tagKey, volume.getId(), currentValue);
         }
      }
   }

   private void logInvalidNumber(@Nonnull VolumeEntry volume, @Nullable String value) {
      LOGGER.at(Level.WARNING).log("Cannot increment trigger volume tag '%s' on volume '%s': value '%s' is not numeric", this.tagKey, volume.getId(), value);
   }

   @Nullable
   private static BigDecimal parseDecimal(@Nullable String value) {
      if (value != null && !value.isBlank()) {
         try {
            return new BigDecimal(value.trim());
         } catch (NumberFormatException var2) {
            return null;
         }
      } else {
         return null;
      }
   }

   public double getRadius() {
      return this.radius;
   }

   public static enum Operation {
      SET,
      REMOVE,
      INCREMENT,
      TOGGLE,
      REPLACE,
      APPEND;
   }
}
