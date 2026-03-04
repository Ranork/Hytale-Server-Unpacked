package com.hypixel.hytale.builtin.instances.interactions;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.builtin.instances.InstanceValidator;
import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.builtin.instances.blocks.InstanceBlock;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.PendingTeleport;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TeleportInstanceInteraction extends SimpleInstantInteraction {
   @Nonnull
   public static final BuilderCodec<TeleportInstanceInteraction> CODEC = BuilderCodec.builder(
         TeleportInstanceInteraction.class, TeleportInstanceInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Teleports the **Player** to the named instance, creating it if required.")
      .<String>appendInherited(
         new KeyedCodec<>("InstanceName", Codec.STRING), (o, i) -> o.instanceName = i, o -> o.instanceName, (o, p) -> o.instanceName = p.instanceName
      )
      .documentation("The name of the **instance** to teleport to.")
      .addValidator(Validators.nonNull())
      .addValidator(InstanceValidator.INSTANCE)
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("InstanceKey", Codec.STRING), (o, i) -> o.instanceKey = i, o -> o.instanceKey, (o, p) -> o.instanceKey = p.instanceKey
      )
      .documentation("The key to name the world. Random if not provided")
      .add()
      .<Vector3d>appendInherited(
         new KeyedCodec<>("PositionOffset", Vector3d.CODEC),
         (o, i) -> o.positionOffset = i,
         o -> o.positionOffset,
         (o, p) -> o.positionOffset = p.positionOffset
      )
      .documentation("The offset to apply to the return point.\n\nUsed to prevent repeated interactions when returning from the instance.")
      .add()
      .<Vector3f>appendInherited(new KeyedCodec<>("Rotation", Vector3f.ROTATION), (o, i) -> o.rotation = i, o -> o.rotation, (o, p) -> o.rotation = p.rotation)
      .documentation("The rotation to set the player to when returning from an instance.")
      .add()
      .<TeleportInstanceInteraction.OriginSource>appendInherited(
         new KeyedCodec<>("OriginSource", TeleportInstanceInteraction.OriginSource.CODEC),
         (o, i) -> o.originSource = i,
         o -> o.originSource,
         (o, p) -> o.originSource = p.originSource
      )
      .documentation("The source to use for the return position.\n\nDefaults to the player's position.")
      .addValidator(Validators.nonNull())
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("PersonalReturnPoint", Codec.BOOLEAN),
         (o, i) -> o.personalReturnPoint = i,
         o -> o.personalReturnPoint,
         (o, p) -> o.personalReturnPoint = p.personalReturnPoint
      )
      .documentation(
         "Whether the player entering the instance will have their own return point\nset to the current location. Overriding the world's return point."
      )
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("CloseOnBlockRemove", Codec.BOOLEAN),
         (o, i) -> o.closeOnBlockRemove = i,
         o -> o.closeOnBlockRemove,
         (o, p) -> o.closeOnBlockRemove = p.closeOnBlockRemove
      )
      .documentation("Whether to delete the instance when the portal block is removed.")
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("RemoveBlockAfter", Codec.DOUBLE),
         (o, i) -> o.removeBlockAfter = i,
         o -> o.removeBlockAfter,
         (o, p) -> o.removeBlockAfter = p.removeBlockAfter
      )
      .documentation(
         "The number of seconds to wait before removing the block that triggered\nthe interaction. A negative value disables this.\n\nThis is needed instead of using another interaction due to all interactions\nbeing stopped once teleporting to another world."
      )
      .add()
      .afterDecode(i -> {
         if (i.rotation != null) {
            i.rotation.scale((float) (Math.PI / 180.0));
         }
      })
      .build();
   private static final int SET_BLOCK_SETTINGS = 256;
   private String instanceName;
   private String instanceKey;
   private Vector3d positionOffset;
   private Vector3f rotation;
   @Nonnull
   private TeleportInstanceInteraction.OriginSource originSource = TeleportInstanceInteraction.OriginSource.PLAYER;
   private boolean personalReturnPoint = false;
   private boolean closeOnBlockRemove = true;
   private double removeBlockAfter = -1.0;

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Server;
   }

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
      Ref<EntityStore> ref = context.getEntity();
      Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
      if (playerComponent != null && !playerComponent.isWaitingForClientReady()) {
         Archetype<EntityStore> archetype = commandBuffer.getArchetype(ref);
         if (!archetype.contains(Teleport.getComponentType()) && !archetype.contains(PendingTeleport.getComponentType())) {
            World world = commandBuffer.getExternalData().getWorld();
            InstancesPlugin module = InstancesPlugin.get();
            Universe universe = Universe.get();
            CompletableFuture<World> targetWorldFuture = null;
            Transform returnPoint = null;
            World targetWorld;
            if (this.instanceKey != null) {
               targetWorld = universe.getWorld(this.instanceKey);
               if (targetWorld == null) {
                  returnPoint = this.makeReturnPoint(ref, context, commandBuffer);
                  targetWorldFuture = module.spawnInstance(this.instanceName, this.instanceKey, world, returnPoint);
               }
            } else {
               BlockPosition targetBlock = context.getTargetBlock();
               if (targetBlock == null) {
                  return;
               }

               ChunkStore chunkStore = world.getChunkStore();
               Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z));
               if (chunkRef == null || !chunkRef.isValid()) {
                  return;
               }

               BlockComponentChunk blockComponentChunk = chunkStore.getStore().getComponent(chunkRef, BlockComponentChunk.getComponentType());

               assert blockComponentChunk != null;

               int index = ChunkUtil.indexBlockInColumn(targetBlock.x, targetBlock.y, targetBlock.z);
               Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(index);
               InstanceBlock instanceState;
               if (blockRef == null) {
                  Holder<ChunkStore> holder = ChunkStore.REGISTRY.newHolder();
                  instanceState = holder.ensureAndGetComponent(InstanceBlock.getComponentType());
                  holder.addComponent(BlockModule.BlockStateInfo.getComponentType(), new BlockModule.BlockStateInfo(index, chunkRef));
                  blockRef = chunkStore.getStore().addEntity(holder, AddReason.SPAWN);
                  instanceState.setCloseOnRemove(this.closeOnBlockRemove);
               } else {
                  instanceState = chunkStore.getStore().getComponent(chunkRef, InstanceBlock.getComponentType());
               }

               if (blockRef == null) {
                  return;
               }

               if (instanceState == null) {
                  instanceState = chunkStore.getStore().ensureAndGetComponent(blockRef, InstanceBlock.getComponentType());
                  instanceState.setCloseOnRemove(this.closeOnBlockRemove);
               }

               UUID worldName = instanceState.getWorldUUID();
               targetWorldFuture = instanceState.getWorldFuture();
               targetWorld = worldName != null ? universe.getWorld(worldName) : null;
               if (targetWorld == null && targetWorldFuture == null) {
                  returnPoint = this.makeReturnPoint(ref, context, commandBuffer);
                  targetWorldFuture = module.spawnInstance(this.instanceName, world, returnPoint);
                  instanceState.setWorldFuture(targetWorldFuture);
                  Ref<ChunkStore> finalBlockRef = blockRef;
                  InstanceBlock finalInstanceState = instanceState;
                  targetWorldFuture.thenAccept(instanceWorld -> {
                     if (finalBlockRef.isValid()) {
                        finalInstanceState.setWorldFuture(null);
                        finalInstanceState.setWorldUUID(instanceWorld.getWorldConfig().getUuid());
                        blockComponentChunk.markNeedsSaving();
                     }
                  });
               }
            }

            if (targetWorldFuture != null) {
               Transform personalReturnPoint = this.getPersonalReturnPoint(ref, context, returnPoint, commandBuffer);
               InstancesPlugin.teleportPlayerToLoadingInstance(ref, commandBuffer, targetWorldFuture, personalReturnPoint);
            } else if (targetWorld != null) {
               Transform personalReturnPoint = this.getPersonalReturnPoint(ref, context, returnPoint, commandBuffer);
               InstancesPlugin.teleportPlayerToInstance(ref, commandBuffer, targetWorld, personalReturnPoint);
            }

            if (this.removeBlockAfter >= 0.0) {
               BlockPosition targetBlockx = context.getTargetBlock();
               if (targetBlockx != null) {
                  if (this.removeBlockAfter == 0.0) {
                     world.getChunk(ChunkUtil.indexChunkFromBlock(targetBlockx.x, targetBlockx.z))
                        .setBlock(targetBlockx.x, targetBlockx.y, targetBlockx.z, 0, 256);
                  } else {
                     int block = world.getBlock(targetBlockx.x, targetBlockx.y, targetBlockx.z);
                     new CompletableFuture()
                        .completeOnTimeout(null, (long)(this.removeBlockAfter * 1.0E9), TimeUnit.NANOSECONDS)
                        .thenRunAsync(
                           () -> {
                              if (world.getBlock(targetBlock.x, targetBlock.y, targetBlock.z) == block) {
                                 world.getChunk(ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z))
                                    .setBlock(targetBlock.x, targetBlock.y, targetBlock.z, 0, 256);
                              }
                           },
                           world
                        );
                  }
               }
            }
         }
      }
   }

   @Nullable
   private Transform getPersonalReturnPoint(
      @Nonnull Ref<EntityStore> playerRef,
      @Nonnull InteractionContext context,
      @Nullable Transform returnPoint,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (!this.personalReturnPoint) {
         return null;
      } else {
         return returnPoint == null ? this.makeReturnPoint(playerRef, context, componentAccessor) : returnPoint;
      }
   }

   @Nonnull
   private Transform makeReturnPoint(
      @Nonnull Ref<EntityStore> playerRef, @Nonnull InteractionContext context, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Transform transform = null;
      switch (this.originSource) {
         case PLAYER:
            TransformComponent transformComponent = componentAccessor.getComponent(playerRef, TransformComponent.getComponentType());

            assert transformComponent != null;

            transform = transformComponent.getTransform().clone();
            transform.getPosition().add(this.positionOffset);
            transform.setRotation(this.rotation != null ? this.rotation : Vector3f.NaN);
            break;
         case BLOCK:
            BlockPosition targetBlock = context.getTargetBlock();
            if (targetBlock == null) {
               throw new IllegalArgumentException("Can't use OriginSource.BLOCK without a target block");
            }

            World world = componentAccessor.getExternalData().getWorld();
            WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z));
            if (chunk == null) {
               throw new IllegalArgumentException("Missing chunk");
            }

            BlockType blockType = chunk.getBlockType(targetBlock.x, targetBlock.y, targetBlock.z);
            int rotationIndex = chunk.getRotationIndex(targetBlock.x, targetBlock.y, targetBlock.z);
            RotationTuple rotationTuple = RotationTuple.get(rotationIndex);
            IndexedLookupTableAssetMap<String, BlockBoundingBoxes> hitboxAssetMap = BlockBoundingBoxes.getAssetMap();
            Box hitbox = hitboxAssetMap.getAsset(blockType.getHitboxTypeIndex()).get(rotationIndex).getBoundingBox();
            Vector3d position = this.positionOffset != null ? rotationTuple.rotate(this.positionOffset) : new Vector3d();
            position.x = position.x + (hitbox.middleX() + targetBlock.x);
            position.y = position.y + (hitbox.middleY() + targetBlock.y);
            position.z = position.z + (hitbox.middleZ() + targetBlock.z);
            Vector3f rotation = Vector3f.NaN;
            if (this.rotation != null) {
               rotation = this.rotation.clone();
               rotation.addRotationOnAxis(Axis.Y, rotationTuple.yaw().getDegrees());
               rotation.addRotationOnAxis(Axis.X, rotationTuple.pitch().getDegrees());
            }

            transform = new Transform(position, rotation);
      }

      return transform;
   }

   private static enum OriginSource {
      PLAYER,
      BLOCK;

      @Nonnull
      public static EnumCodec<TeleportInstanceInteraction.OriginSource> CODEC = new EnumCodec<>(TeleportInstanceInteraction.OriginSource.class)
         .documentKey(PLAYER, "The origin of operations will be based on the player's current position.")
         .documentKey(BLOCK, "The origin of operations will be based on the middle of the block's hitbox.");
   }
}
