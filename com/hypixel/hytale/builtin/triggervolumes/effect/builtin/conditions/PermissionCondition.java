package com.hypixel.hytale.builtin.triggervolumes.effect.builtin.conditions;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import javax.annotation.Nonnull;

public class PermissionCondition extends TriggerCondition {
   @Nonnull
   public static final BuilderCodec<PermissionCondition> CODEC = BuilderCodec.builder(PermissionCondition.class, PermissionCondition::new, BASE_CODEC)
      .append(new KeyedCodec<>("Permission", Codec.STRING, false), (c, v) -> c.permission = v, c -> c.permission)
      .add()
      .build();
   private String permission = "";

   @Nonnull
   public static PermissionCondition create(@Nonnull TriggerEventType eventType, @Nonnull String permission) {
      PermissionCondition condition = new PermissionCondition();
      condition.setEventType(eventType);
      condition.permission = permission;
      return condition;
   }

   @Override
   public boolean test(@Nonnull TriggerContext context) {
      if (this.permission != null && !this.permission.isBlank()) {
         PlayerRef playerRef = context.getStore().getComponent(context.getEntityRef(), PlayerRef.getComponentType());
         return playerRef != null && playerRef.hasPermission(this.permission);
      } else {
         return true;
      }
   }
}
