package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.OriginSource;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferUtil;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.PrefabUtil;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import org.joml.Vector3i;

public class SpawnPrefabInteraction extends SimpleInstantInteraction {
   public static final BuilderCodec<SpawnPrefabInteraction> CODEC = BuilderCodec.builder(
         SpawnPrefabInteraction.class, SpawnPrefabInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Spawns a prefab at the current location.")
      .appendInherited(new KeyedCodec<>("PrefabPath", Codec.STRING), (o, i) -> o.prefabPath = i, o -> o.prefabPath, (o, p) -> o.prefabPath = p.prefabPath)
      .add()
      .<Vector3i>appendInherited(new KeyedCodec<>("Offset", Vector3iUtil.CODEC), (o, i) -> o.offset = i, o -> o.offset, (o, p) -> o.offset = p.offset)
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
      .appendInherited(new KeyedCodec<>("Force", Codec.BOOLEAN), (o, i) -> o.force = i, o -> o.force, (o, p) -> o.force = p.force)
      .add()
      .build();
   private String prefabPath;
   private Vector3i offset = new Vector3i();
   private Rotation rotationYaw = Rotation.None;
   @Nonnull
   private OriginSource originSource = OriginSource.ENTITY;
   private boolean force;

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      Path resolvedPath = PrefabStore.get().findAssetPrefabPath(this.prefabPath);
      if (resolvedPath != null) {
         IPrefabBuffer prefab = PrefabBufferUtil.getCached(resolvedPath);
         CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

         assert commandBuffer != null;

         World world = commandBuffer.getExternalData().getWorld();
         Rotation yaw = this.rotationYaw;
         Vector3i target;
         switch (this.originSource) {
            case ENTITY:
               Ref<EntityStore> ref = context.getEntity();
               TransformComponent transformComponent = commandBuffer.getComponent(ref, TransformComponent.getComponentType());

               assert transformComponent != null;

               target = Vector3dUtil.toVector3i(transformComponent.getPosition());
               target.add(this.offset);
               break;
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
               target = new Vector3i();
               blockYaw.rotateYaw(this.offset, target);
               yaw = yaw.add(blockYaw);
               target.add(targetBlock.x, targetBlock.y, targetBlock.z);
               break;
            default:
               throw new IllegalArgumentException("Unhandled origin source");
         }

         PrefabUtil.paste(prefab, world, target, yaw, this.force, new FastRandom(), commandBuffer);
      }
   }
}
