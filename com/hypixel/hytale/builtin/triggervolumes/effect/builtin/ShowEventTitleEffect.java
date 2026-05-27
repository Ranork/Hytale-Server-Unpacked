package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShowEventTitleEffect extends TriggerEffect {
   @Nonnull
   public static final BuilderCodec<ShowEventTitleEffect> CODEC = BuilderCodec.builder(ShowEventTitleEffect.class, ShowEventTitleEffect::new, BASE_CODEC)
      .append(new KeyedCodec<>("PrimaryTitle", Codec.STRING), (e, v) -> e.primaryTitle = v, e -> e.primaryTitle)
      .add()
      .append(new KeyedCodec<>("SecondaryTitle", Codec.STRING, false), (e, v) -> e.secondaryTitle = v, e -> e.secondaryTitle)
      .add()
      .append(new KeyedCodec<>("IsMajor", Codec.BOOLEAN, false), (e, v) -> e.isMajor = v, e -> e.isMajor)
      .add()
      .append(new KeyedCodec<>("Icon", Codec.STRING, false), (e, v) -> e.icon = v, e -> e.icon)
      .add()
      .append(new KeyedCodec<>("Duration", Codec.FLOAT, false), (e, v) -> e.duration = v, e -> e.duration)
      .add()
      .append(new KeyedCodec<>("FadeInDuration", Codec.FLOAT, false), (e, v) -> e.fadeInDuration = v, e -> e.fadeInDuration)
      .add()
      .append(new KeyedCodec<>("FadeOutDuration", Codec.FLOAT, false), (e, v) -> e.fadeOutDuration = v, e -> e.fadeOutDuration)
      .add()
      .build();
   @Nullable
   private String primaryTitle;
   @Nullable
   private String secondaryTitle;
   private boolean isMajor = true;
   @Nullable
   private String icon;
   private float duration = 4.0F;
   private float fadeInDuration = 1.5F;
   private float fadeOutDuration = 1.5F;

   @Nonnull
   public static ShowEventTitleEffect create(
      @Nonnull TriggerEventType eventType, @Nonnull String primaryTitle, @Nullable String secondaryTitle, boolean isMajor
   ) {
      ShowEventTitleEffect effect = new ShowEventTitleEffect();
      effect.setEventType(eventType);
      effect.primaryTitle = primaryTitle;
      effect.secondaryTitle = secondaryTitle;
      effect.isMajor = isMajor;
      return effect;
   }

   @Override
   public void execute(@Nonnull TriggerContext context) {
      if (this.primaryTitle != null) {
         PlayerRef playerRef = context.getStore().getComponent(context.getEntityRef(), PlayerRef.getComponentType());
         if (playerRef != null) {
            Message primary = Message.translation(this.primaryTitle);
            Message secondary = this.secondaryTitle != null ? Message.translation(this.secondaryTitle) : Message.raw("");
            EventTitleUtil.showEventTitleToPlayer(
               playerRef, primary, secondary, this.isMajor, this.icon, this.duration, this.fadeInDuration, this.fadeOutDuration
            );
         }
      }
   }
}
