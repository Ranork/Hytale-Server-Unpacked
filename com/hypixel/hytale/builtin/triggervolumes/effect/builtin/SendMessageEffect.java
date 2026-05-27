package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import javax.annotation.Nonnull;

public class SendMessageEffect extends TriggerEffect {
   @Nonnull
   public static final BuilderCodec<SendMessageEffect> CODEC = BuilderCodec.builder(SendMessageEffect.class, SendMessageEffect::new, BASE_CODEC)
      .append(new KeyedCodec<>("Message", Codec.STRING), (e, v) -> e.message = v, e -> e.message)
      .add()
      .build();
   private String message;

   @Nonnull
   public static SendMessageEffect create(@Nonnull TriggerEventType eventType, @Nonnull String message) {
      SendMessageEffect effect = new SendMessageEffect();
      effect.setEventType(eventType);
      effect.message = message;
      return effect;
   }

   @Override
   public void execute(@Nonnull TriggerContext context) {
      if (this.message != null) {
         PlayerRef playerRef = context.getStore().getComponent(context.getEntityRef(), PlayerRef.getComponentType());
         if (playerRef != null) {
            playerRef.sendMessage(Message.translation(this.message));
         }
      }
   }
}
