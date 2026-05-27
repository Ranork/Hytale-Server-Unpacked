package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Rotation3fc;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class SetVelocityEffect extends TriggerEffect {
   @Nonnull
   public static final BuilderCodec<SetVelocityEffect> CODEC = BuilderCodec.builder(SetVelocityEffect.class, SetVelocityEffect::new, BASE_CODEC)
      .append(new KeyedCodec<>("Velocity", Vector3dUtil.CODEC), (e, v) -> e.velocity = v, e -> e.velocity)
      .add()
      .append(new KeyedCodec<>("Additive", Codec.BOOLEAN, false), (e, v) -> e.additive = v, e -> e.additive)
      .add()
      .append(new KeyedCodec<>("RelativeMode", new EnumCodec<>(SetVelocityEffect.RelativeMode.class), false), (e, v) -> e.relativeMode = v, e -> e.relativeMode)
      .add()
      .build();
   private Vector3d velocity = new Vector3d(0.0, 0.0, 0.0);
   private boolean additive = false;
   @Nonnull
   private SetVelocityEffect.RelativeMode relativeMode = SetVelocityEffect.RelativeMode.ABSOLUTE;

   @Nonnull
   public static SetVelocityEffect create(@Nonnull TriggerEventType eventType, @Nonnull Vector3d velocity, boolean additive) {
      SetVelocityEffect effect = new SetVelocityEffect();
      effect.setEventType(eventType);
      effect.velocity = velocity;
      effect.additive = additive;
      return effect;
   }

   @Override
   public void execute(@Nonnull TriggerContext context) {
      if (this.velocity != null) {
         Ref<EntityStore> entityRef = context.getEntityRef();
         Store<EntityStore> store = context.getStore();
         Velocity velocityComponent = store.getComponent(entityRef, Velocity.getComponentType());
         if (velocityComponent != null) {
            Vector3d resolvedVelocity = this.resolveVelocity(context);
            if (resolvedVelocity != null) {
               if (this.additive) {
                  velocityComponent.addInstruction(resolvedVelocity, null, ChangeVelocityType.Add);
               } else {
                  velocityComponent.addInstruction(resolvedVelocity, null, ChangeVelocityType.Set);
               }
            }
         }
      }
   }

   @Nullable
   private Vector3d resolveVelocity(@Nonnull TriggerContext context) {
      SetVelocityEffect.RelativeMode mode = this.relativeMode != null ? this.relativeMode : SetVelocityEffect.RelativeMode.ABSOLUTE;
      if (mode == SetVelocityEffect.RelativeMode.ABSOLUTE) {
         return new Vector3d(this.velocity);
      } else {
         Rotation3fc rotation = getLookRotation(context);
         if (rotation == null) {
            return null;
         } else if (mode == SetVelocityEffect.RelativeMode.HORIZONTAL_FACING) {
            Vector3d horizontal = new Rotation3f(0.0F, rotation.yaw(), 0.0F).transform(new Vector3d(this.velocity.x, 0.0, this.velocity.z));
            horizontal.y = this.velocity.y;
            return horizontal;
         } else {
            return rotation.transform(new Vector3d(this.velocity));
         }
      }
   }

   @Nullable
   private static Rotation3fc getLookRotation(@Nonnull TriggerContext context) {
      Store<EntityStore> store = context.getStore();
      Ref<EntityStore> entityRef = context.getEntityRef();
      HeadRotation headRotation = store.getComponent(entityRef, HeadRotation.getComponentType());
      if (headRotation != null) {
         return headRotation.getRotation();
      } else {
         TransformComponent transform = store.getComponent(entityRef, TransformComponent.getComponentType());
         return transform != null ? transform.getRotation() : null;
      }
   }

   public static enum RelativeMode {
      ABSOLUTE,
      HORIZONTAL_FACING,
      FULL_LOOK;
   }
}
