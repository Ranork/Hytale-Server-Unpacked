package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class TeleportEffect extends TriggerEffect {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   public static final BuilderCodec<TeleportEffect> CODEC = BuilderCodec.builder(TeleportEffect.class, TeleportEffect::new, BASE_CODEC)
      .append(new KeyedCodec<>("Position", Vector3dUtil.CODEC), (e, v) -> e.position = v, e -> e.position)
      .add()
      .append(new KeyedCodec<>("World", Codec.STRING, false), (e, v) -> e.world = v, e -> e.world)
      .add()
      .append(new KeyedCodec<>("ResetVelocity", Codec.BOOLEAN, false), (e, v) -> e.resetVelocity = v, e -> e.resetVelocity)
      .add()
      .append(new KeyedCodec<>("RelativeToEntity", Codec.BOOLEAN, false), (e, v) -> e.relativeToEntity = v, e -> e.relativeToEntity)
      .add()
      .append(new KeyedCodec<>("RelativeToVolume", Codec.BOOLEAN, false), (e, v) -> e.relativeToVolume = v, e -> e.relativeToVolume)
      .add()
      .append(new KeyedCodec<>("UseRotation", Codec.BOOLEAN, false), (e, v) -> e.useRotation = v, e -> e.useRotation)
      .add()
      .append(new KeyedCodec<>("Rotation", Vector3dUtil.CODEC, false), (e, v) -> e.rotation = v, e -> e.rotation)
      .add()
      .build();
   private Vector3d position;
   @Nullable
   private String world;
   private boolean resetVelocity = true;
   private boolean relativeToEntity;
   private boolean relativeToVolume;
   private boolean useRotation;
   @Nonnull
   private Vector3d rotation = new Vector3d();

   @Override
   public void execute(@Nonnull TriggerContext context) {
      if (this.position != null) {
         Ref<EntityStore> entityRef = context.getEntityRef();
         Store<EntityStore> store = context.getStore();
         TransformComponent transformComponent = store.getComponent(entityRef, TransformComponent.getComponentType());
         if (transformComponent != null) {
            Vector3d destination = new Vector3d(this.position);
            if (this.relativeToEntity) {
               if (this.relativeToVolume) {
                  ((HytaleLogger.Api)LOGGER.atWarning()).log("TeleportEffect has both RelativeToEntity and RelativeToVolume set; using RelativeToEntity");
               }

               destination.add(transformComponent.getPosition());
            } else if (this.relativeToVolume) {
               destination.add(context.getVolume().getPosition());
            }

            Rotation3f teleportRotation = this.useRotation ? toRotation(this.rotation) : transformComponent.getRotation();
            Teleport teleport = Teleport.createForPlayer(destination, teleportRotation);
            if (!this.resetVelocity) {
               teleport = teleport.withoutVelocityReset();
            }

            if (this.world != null) {
               ((HytaleLogger.Api)LOGGER.atWarning())
                  .log("TeleportEffect has 'World' set to '%s' but cross-world teleport is not yet supported; teleporting within current world", this.world);
            }

            store.addComponent(entityRef, Teleport.getComponentType(), teleport);
         }
      }
   }

   @Nonnull
   private static Rotation3f toRotation(@Nonnull Vector3d value) {
      return new Rotation3f((float)Math.toRadians(value.y), (float)Math.toRadians(value.x), (float)Math.toRadians(value.z));
   }
}
