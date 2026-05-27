package com.hypixel.hytale.builtin.deployables.interaction;

import com.hypixel.hytale.builtin.deployables.DeployablesUtils;
import com.hypixel.hytale.builtin.deployables.config.DeployableConfig;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.OriginSource;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RelativeRotationMode;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class SpawnDeployableAtLocationInteraction extends SimpleInstantInteraction {
   @Nonnull
   public static final BuilderCodec<SpawnDeployableAtLocationInteraction> CODEC = BuilderCodec.builder(
         SpawnDeployableAtLocationInteraction.class, SpawnDeployableAtLocationInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Spawns a deployable at a specific location.")
      .<DeployableConfig>appendInherited(
         new KeyedCodec<>("Config", DeployableConfig.CODEC), (o, i) -> o.config = i, o -> o.config, (o, p) -> o.config = p.config
      )
      .addValidator(Validators.nonNull())
      .add()
      .<Vector3d>appendInherited(new KeyedCodec<>("Offset", Vector3dUtil.CODEC), (o, i) -> o.offset = i, o -> o.offset, (o, p) -> o.offset = p.offset)
      .addValidator(Validators.nonNull())
      .add()
      .<Rotation>appendInherited(
         new KeyedCodec<>("RotationYaw", Rotation.CODEC), (o, i) -> o.rotationYaw = i, o -> o.rotationYaw, (o, p) -> o.rotationYaw = p.rotationYaw
      )
      .addValidator(Validators.nonNull())
      .add()
      .<OriginSource>appendInherited(
         new KeyedCodec<>("OriginSource", OriginSource.CODEC), (o, i) -> o.originSource = i, o -> o.originSource, (o, p) -> o.originSource = p.originSource
      )
      .addValidator(Validators.nonNull())
      .add()
      .<RelativeRotationMode>appendInherited(
         new KeyedCodec<>("OffsetRotationMode", RelativeRotationMode.CODEC),
         (o, i) -> o.offsetRotationMode = i,
         o -> o.offsetRotationMode,
         (o, p) -> o.offsetRotationMode = p.offsetRotationMode
      )
      .addValidator(Validators.nonNull())
      .add()
      .<RelativeRotationMode>appendInherited(
         new KeyedCodec<>("DeployableRotationMode", RelativeRotationMode.CODEC),
         (o, i) -> o.deployableRotationMode = i,
         o -> o.deployableRotationMode,
         (o, p) -> o.deployableRotationMode = p.deployableRotationMode
      )
      .addValidator(Validators.nonNull())
      .add()
      .build();
   private DeployableConfig config;
   @Nonnull
   private Vector3d offset = new Vector3d();
   @Nonnull
   private Rotation rotationYaw = Rotation.None;
   @Nonnull
   private OriginSource originSource = OriginSource.ENTITY;
   @Nonnull
   private RelativeRotationMode offsetRotationMode = RelativeRotationMode.NONE;
   @Nonnull
   private RelativeRotationMode deployableRotationMode = RelativeRotationMode.NONE;

   @Override
   public boolean needsRemoteSync() {
      return false;
   }

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      Rotation3f spawnRotation;
      Vector3d target;
      World world = commandBuffer.getExternalData().getWorld();
      spawnRotation = new Rotation3f(0.0F, (float)this.rotationYaw.getRadians(), 0.0F);
      label60:
      switch (this.originSource) {
         case ENTITY:
            Ref<EntityStore> ref = context.getEntity();
            TransformComponent transformComponent = commandBuffer.getComponent(ref, TransformComponent.getComponentType());

            assert transformComponent != null;

            HeadRotation headRotationComponent = commandBuffer.getComponent(ref, HeadRotation.getComponentType());
            Rotation3f entityRotation = headRotationComponent != null ? headRotationComponent.getRotation() : transformComponent.getRotation();
            Vector3d rotatedOffset = new Vector3d(this.offset);
            switch (this.offsetRotationMode) {
               case NONE:
               default:
                  break;
               case YAW:
                  rotatedOffset.rotateY(entityRotation.yaw());
                  break;
               case FULL:
                  entityRotation.transform(rotatedOffset);
            }

            target = new Vector3d(transformComponent.getPosition()).add(rotatedOffset);
            switch (this.deployableRotationMode) {
               case NONE:
               default:
                  break label60;
               case YAW:
                  spawnRotation.addYaw(entityRotation.yaw());
                  break label60;
               case FULL:
                  spawnRotation.add(entityRotation);
                  break label60;
            }
         case BLOCK:
            BlockPosition targetBlock = context.getTargetBlock();
            if (targetBlock == null) {
               return;
            }

            ChunkStore chunkStore = world.getChunkStore();
            long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z);
            Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
            if (chunkRef == null || !chunkRef.isValid()) {
               return;
            }

            Store<ChunkStore> chunkComponentStore = chunkStore.getStore();
            BlockChunk blockChunkComponent = chunkComponentStore.getComponent(chunkRef, BlockChunk.getComponentType());
            if (blockChunkComponent == null) {
               return;
            }

            Rotation blockYaw = blockChunkComponent.getSectionAtBlockY(targetBlock.y).getRotation(targetBlock.x, targetBlock.y, targetBlock.z).yaw();
            target = new Vector3d();
            blockYaw.rotateY(this.offset, target);
            spawnRotation.addYaw((float)blockYaw.getRadians());
            target.add(targetBlock.x, targetBlock.y, targetBlock.z);
            break;
         default:
            throw new IllegalArgumentException("Unhandled origin source");
      }

      Ref<EntityStore> owningEntityRef = context.getOwningEntity();
      Ref<EntityStore> deployerRef = owningEntityRef != null ? owningEntityRef : context.getEntity();
      DeployablesUtils.spawnDeployable(commandBuffer, commandBuffer.getStore(), this.config, deployerRef, target, spawnRotation, "UP");
   }
}
