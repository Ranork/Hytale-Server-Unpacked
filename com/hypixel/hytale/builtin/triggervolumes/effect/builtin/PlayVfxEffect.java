package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class PlayVfxEffect extends TriggerEffect {
   @Nonnull
   public static final BuilderCodec<PlayVfxEffect> CODEC = BuilderCodec.builder(PlayVfxEffect.class, PlayVfxEffect::new, BASE_CODEC)
      .append(new KeyedCodec<>("ParticleSystem", Codec.STRING), (e, v) -> e.particleSystem = v, e -> e.particleSystem)
      .add()
      .append(new KeyedCodec<>("Offset", Vector3dUtil.CODEC, false), (e, v) -> e.offset = v, e -> e.offset)
      .add()
      .append(new KeyedCodec<>("AtEntity", Codec.BOOLEAN, false), (e, v) -> e.atEntity = v, e -> e.atEntity)
      .add()
      .append(new KeyedCodec<>("Scale", Codec.FLOAT, false), (e, v) -> e.scale = v, e -> e.scale)
      .add()
      .append(new KeyedCodec<>("Rotation", Vector3dUtil.CODEC, false), (e, v) -> e.rotation = v, e -> e.rotation)
      .add()
      .append(new KeyedCodec<>("Duration", Codec.FLOAT, false), (e, v) -> e.duration = v, e -> e.duration)
      .add()
      .build();
   @Nullable
   private String particleSystem;
   @Nonnull
   private Vector3d offset = new Vector3d();
   private boolean atEntity;
   private float scale = 1.0F;
   @Nonnull
   private Vector3d rotation = new Vector3d();
   private float duration;

   @Nonnull
   public static PlayVfxEffect create(@Nonnull TriggerEventType eventType, @Nonnull String particleSystem, boolean atEntity) {
      PlayVfxEffect effect = new PlayVfxEffect();
      effect.setEventType(eventType);
      effect.particleSystem = particleSystem;
      effect.atEntity = atEntity;
      return effect;
   }

   @Override
   public void execute(@Nonnull TriggerContext context) {
      if (this.particleSystem != null) {
         Store<EntityStore> store = context.getStore();
         Vector3d position;
         if (this.atEntity) {
            TransformComponent transform = store.getComponent(context.getEntityRef(), TransformComponent.getComponentType());
            if (transform == null) {
               return;
            }

            position = new Vector3d(transform.getPosition()).add(this.offset);
         } else {
            position = new Vector3d(context.getVolume().getPosition()).add(this.offset);
         }

         ParticleUtil.spawnParticleEffect(
            this.particleSystem,
            position,
            (float)Math.toRadians(this.rotation.x),
            (float)Math.toRadians(this.rotation.y),
            (float)Math.toRadians(this.rotation.z),
            this.scale,
            this.duration,
            store
         );
      }
   }
}
