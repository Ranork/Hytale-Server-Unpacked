package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Rotation3fc;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.ModelTransform;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.PositionUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class TransformComponent implements Component<EntityStore> {
   @Nonnull
   public static final BuilderCodec<TransformComponent> CODEC = BuilderCodec.builder(TransformComponent.class, TransformComponent::new)
      .append(new KeyedCodec<>("Position", Vector3dUtil.CODEC), (o, i) -> o.position.set(i), o -> o.position)
      .add()
      .append(new KeyedCodec<>("Rotation", Rotation3f.CODEC), (o, i) -> o.rotation.set(i), o -> o.rotation)
      .add()
      .build();
   @Nonnull
   private final Vector3d position = new Vector3d();
   @Nonnull
   private final Rotation3f rotation = new Rotation3f();
   @Nonnull
   private final ModelTransform sentTransform = new ModelTransform(new Position(), new Direction(), new Direction());
   @Nullable
   @Deprecated(forRemoval = true)
   private WorldChunk chunk;
   @Nullable
   private Ref<ChunkStore> chunkRef;

   public static ComponentType<EntityStore, TransformComponent> getComponentType() {
      return EntityModule.get().getTransformComponentType();
   }

   public TransformComponent() {
   }

   public TransformComponent(@Nonnull Vector3dc position, @Nonnull Rotation3fc rotation) {
      this.position.set(position);
      this.rotation.set(rotation);
   }

   @Nonnull
   public Vector3d getPosition() {
      return this.position;
   }

   public void setPosition(@Nonnull Vector3dc position) {
      this.position.set(position);
   }

   public void teleportPosition(@Nonnull Vector3dc position) {
      double x = position.x();
      if (!Double.isNaN(x)) {
         this.position.x = x;
      }

      double y = position.y();
      if (!Double.isNaN(y)) {
         this.position.y = y;
      }

      double z = position.z();
      if (!Double.isNaN(z)) {
         this.position.z = z;
      }
   }

   @Nonnull
   public Rotation3f getRotation() {
      return this.rotation;
   }

   public void setRotation(@Nonnull Rotation3f rotation) {
      this.rotation.set(rotation);
   }

   @Nonnull
   public Transform getTransform() {
      return new Transform(this.position, this.rotation);
   }

   public void teleportRotation(@Nonnull Rotation3f rotation) {
      float yaw = rotation.yaw();
      if (!Float.isNaN(yaw)) {
         this.rotation.setYaw(yaw);
      }

      float pitch = rotation.pitch();
      if (!Float.isNaN(pitch)) {
         this.rotation.setPitch(pitch);
      }

      float roll = rotation.roll();
      if (!Float.isNaN(roll)) {
         this.rotation.setRoll(roll);
      }
   }

   @Nonnull
   public ModelTransform getSentTransform() {
      return this.sentTransform;
   }

   @Nullable
   @Deprecated(forRemoval = true)
   public WorldChunk getChunk() {
      return this.chunk;
   }

   @Nullable
   public Ref<ChunkStore> getChunkRef() {
      return this.chunkRef;
   }

   public void markChunkDirty(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.chunkRef != null && this.chunkRef.isValid()) {
         World world = componentAccessor.getExternalData().getWorld();
         Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
         WorldChunk worldChunkComponent = chunkStore.getComponent(this.chunkRef, WorldChunk.getComponentType());

         assert worldChunkComponent != null;

         worldChunkComponent.markNeedsSaving();
      }
   }

   public void setChunkLocation(@Nullable Ref<ChunkStore> chunkRef, @Nullable WorldChunk chunk) {
      this.chunkRef = chunkRef;
      this.chunk = chunk;
   }

   @Nonnull
   public TransformComponent clone() {
      TransformComponent transformComponent = new TransformComponent(this.position, this.rotation);
      ModelTransform transform = transformComponent.sentTransform;
      PositionUtil.assign(transform.position, this.sentTransform.position);
      PositionUtil.assign(transform.bodyOrientation, this.sentTransform.bodyOrientation);
      PositionUtil.assign(transform.lookOrientation, this.sentTransform.lookOrientation);
      return transformComponent;
   }
}
