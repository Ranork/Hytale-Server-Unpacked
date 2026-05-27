package com.hypixel.hytale.builtin.fallingblocks;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.fallingblocks.FallingBlockSettings;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.BlockEntity;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollision;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionConfig;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class FallingBlock implements Component<EntityStore> {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   public static final BuilderCodec<FallingBlock> CODEC = BuilderCodec.builder(FallingBlock.class, FallingBlock::new)
      .append(new KeyedCodec<>("BreakOnImpact", FallingBlockSettings.CODEC), (o, v) -> o.fallingBlockSettings = v, o -> o.fallingBlockSettings)
      .add()
      .build();
   private FallingBlockSettings fallingBlockSettings;

   public FallingBlock() {
      this(FallingBlockSettings.DEFAULT);
   }

   public FallingBlock(FallingBlockSettings fallingBlockSettings) {
      this.fallingBlockSettings = fallingBlockSettings;
   }

   @Nonnull
   public static ComponentType<EntityStore, FallingBlock> getComponentType() {
      return FallingBlocksPlugin.get().getFallingBlockComponentType();
   }

   public static boolean fallBlock(@Nonnull World world, @Nonnull Store<EntityStore> store, int blockX, int blockY, int blockZ) {
      WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(blockX, blockZ));
      if (chunk == null) {
         return false;
      } else {
         int blockId = chunk.getBlock(blockX, blockY, blockZ);
         BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
         if (blockType != null && blockType.getMaterial() != BlockMaterial.Empty) {
            BlockSection section = chunk.getBlockChunk().getSectionAtBlockY(blockY);
            int rotationIndex = section != null ? section.getRotationIndex(blockX, blockY, blockZ) : 0;
            RotationTuple rotationTuple = RotationTuple.get(rotationIndex);
            boolean broken = world.breakBlock(blockX, blockY, blockZ, 0);
            if (!broken) {
               return false;
            } else {
               BlockBoundingBoxes hitBoxType = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
               if (hitBoxType != null) {
                  FillerBlockUtil.forEachFillerBlock(
                     hitBoxType.get(rotationIndex), (x, y, z) -> world.performBlockUpdate(blockX + x, blockY + y, blockZ + z, false)
                  );
               }

               FallingBlockSettings fallingBlockSettings = blockType.getFallingBlockSettings();
               Vector3d fallingBlockPosition = new Vector3d(blockX + 0.5, blockY, blockZ + 0.5);
               Holder<EntityStore> fallingBlockHolder = generateFallingBlock(blockType, fallingBlockPosition, rotationTuple, fallingBlockSettings);
               if (fallingBlockHolder == null) {
                  return false;
               } else {
                  store.addEntity(fallingBlockHolder, AddReason.SPAWN);
                  return true;
               }
            }
         } else {
            return false;
         }
      }
   }

   @Nullable
   public static Holder<EntityStore> generateFallingBlock(
      @Nullable BlockType blockType, @Nonnull Vector3d position, @Nonnull RotationTuple rotation, @Nullable FallingBlockSettings fallingBlockSettings
   ) {
      if (blockType != null && blockType.getMaterial() != BlockMaterial.Empty) {
         String hitboxCollisionConfigId = fallingBlockSettings != null ? fallingBlockSettings.getHitboxCollisionConfigId() : null;
         Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
         holder.addComponent(getComponentType(), new FallingBlock(fallingBlockSettings));
         BlockEntity blockEntityComponent = new BlockEntity(blockType.getId());
         holder.addComponent(BlockEntity.getComponentType(), blockEntityComponent);
         holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(position, new Rotation3f()));
         holder.addComponent(
            HeadRotation.getComponentType(),
            new HeadRotation(
               new Rotation3f((float)rotation.pitch().getRadians(), (float)rotation.yaw().getRadians() + (float) Math.PI, (float)rotation.roll().getRadians())
            )
         );
         holder.ensureAndGetComponent(Velocity.getComponentType());
         PhysicsValues physicsValues = holder.ensureAndGetComponent(PhysicsValues.getComponentType());
         physicsValues.replaceValues(new PhysicsValues(0.5, 0.05, false));
         holder.ensureComponent(UUIDComponent.getComponentType());
         Box blockBoundingBox = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex()).get(rotation.index()).getBoundingBox();
         Box box = new Box(blockBoundingBox);
         box.offset(-0.5, 0.0, -0.5);
         holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(box));
         if (hitboxCollisionConfigId != null) {
            int hitboxCollisionConfigIndex = HitboxCollisionConfig.getAssetMap().getIndex(hitboxCollisionConfigId);
            if (hitboxCollisionConfigIndex != Integer.MIN_VALUE) {
               holder.addComponent(
                  HitboxCollision.getComponentType(), new HitboxCollision(HitboxCollisionConfig.getAssetMap().getAsset(hitboxCollisionConfigIndex))
               );
            } else {
               LOGGER.at(Level.WARNING)
                  .log(
                     "Attempted to spawn falling block %s at %s with invalid hitbox collision config id %s",
                     blockType.getId(),
                     position,
                     hitboxCollisionConfigId
                  );
            }
         }

         return holder;
      } else {
         LOGGER.at(Level.WARNING).log("Attempted to spawn invalid falling block %s at %s", blockType, position);
         return null;
      }
   }

   public FallingBlockSettings getFallingBlockSettings() {
      return this.fallingBlockSettings;
   }

   @Override
   public Component<EntityStore> clone() {
      return new FallingBlock(this.fallingBlockSettings);
   }
}
