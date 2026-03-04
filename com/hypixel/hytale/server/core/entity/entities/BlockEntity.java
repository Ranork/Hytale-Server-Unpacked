package com.hypixel.hytale.server.core.entity.entities;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.projectile.config.Projectile;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.BlockMigrationExtraInfo;
import com.hypixel.hytale.server.core.modules.entity.DespawnComponent;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.SimplePhysicsProvider;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockEntity implements Component<EntityStore> {
   public static final BuilderCodec<BlockEntity> CODEC = BuilderCodec.builder(BlockEntity.class, BlockEntity::new)
      .append(new KeyedCodec<>("BlockTypeKey", Codec.STRING), (blockEntity, newBlockTypeKey, extraInfo) -> {
         blockEntity.blockTypeKey = newBlockTypeKey;
         if (extraInfo instanceof BlockMigrationExtraInfo) {
            blockEntity.blockTypeKey = ((BlockMigrationExtraInfo)extraInfo).getBlockMigration().apply(newBlockTypeKey);
         }
      }, (blockEntity, extraInfo) -> blockEntity.blockTypeKey)
      .add()
      .build();
   public static final int DEFAULT_DESPAWN_SECONDS = 120;
   @Nonnull
   private transient SimplePhysicsProvider simplePhysicsProvider = new SimplePhysicsProvider();
   protected String blockTypeKey;
   private boolean isBlockIdNetworkOutdated;

   public static ComponentType<EntityStore, BlockEntity> getComponentType() {
      return EntityModule.get().getBlockEntityComponentType();
   }

   protected BlockEntity() {
   }

   public BlockEntity(String blockTypeKey) {
      this.blockTypeKey = blockTypeKey;
   }

   @Nonnull
   public static Holder<EntityStore> assembleDefaultBlockEntity(@Nonnull TimeResource time, String blockTypeKey, @Nonnull Vector3d position) {
      Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
      holder.addComponent(getComponentType(), new BlockEntity(blockTypeKey));
      holder.addComponent(DespawnComponent.getComponentType(), DespawnComponent.despawnInSeconds(time, 120));
      holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(position.clone(), Vector3f.FORWARD));
      holder.ensureComponent(Velocity.getComponentType());
      holder.ensureComponent(UUIDComponent.getComponentType());
      return holder;
   }

   @Nonnull
   public SimplePhysicsProvider initPhysics(@Nonnull BoundingBox boundingBox) {
      this.simplePhysicsProvider.initialize(Projectile.getAssetMap().getAsset("Projectile"), boundingBox);
      this.simplePhysicsProvider.setProvideCharacterCollisions(false);
      this.simplePhysicsProvider.setMoveOutOfSolid(true);
      return this.simplePhysicsProvider;
   }

   @Nonnull
   public BoundingBox updateHitbox(@Nonnull Ref<EntityStore> ref, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      BoundingBox boundingBoxComponent = this.createBoundingBoxComponent();
      commandBuffer.putComponent(ref, BoundingBox.getComponentType(), boundingBoxComponent);
      return boundingBoxComponent;
   }

   @Nullable
   public BoundingBox createBoundingBoxComponent() {
      if (this.blockTypeKey == null) {
         return null;
      } else {
         BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
         if (assetMap == null) {
            return null;
         } else {
            BlockType blockType = assetMap.getAsset(this.blockTypeKey);
            return blockType == null
               ? null
               : new BoundingBox(BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex()).get(0).getBoundingBox());
         }
      }
   }

   public void setBlockTypeKey(String blockTypeKey, @Nonnull Ref<EntityStore> ref, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      this.blockTypeKey = blockTypeKey;
      this.isBlockIdNetworkOutdated = true;
      this.updateHitbox(ref, commandBuffer);
   }

   @Nonnull
   public SimplePhysicsProvider getSimplePhysicsProvider() {
      return this.simplePhysicsProvider;
   }

   public String getBlockTypeKey() {
      return this.blockTypeKey;
   }

   public void addForce(float x, float y, float z) {
      this.simplePhysicsProvider.addVelocity(x, y, z);
   }

   public void addForce(@Nonnull Vector3d force) {
      this.simplePhysicsProvider.addVelocity((float)force.x, (float)force.y, (float)force.z);
   }

   public boolean consumeBlockIdNetworkOutdated() {
      boolean temp = this.isBlockIdNetworkOutdated;
      this.isBlockIdNetworkOutdated = false;
      return temp;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new BlockEntity(this.blockTypeKey);
   }
}
