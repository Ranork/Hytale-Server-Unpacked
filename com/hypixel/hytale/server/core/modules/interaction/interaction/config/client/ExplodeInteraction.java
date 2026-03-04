package com.hypixel.hytale.server.core.modules.interaction.interaction.config.client;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector4d;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.ExplosionConfig;
import com.hypixel.hytale.server.core.entity.ExplosionUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.meta.DynamicMetaStore;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.InteractionTypeUtils;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.projectile.component.Projectile;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExplodeInteraction extends SimpleInstantInteraction {
   @Nonnull
   public static final BuilderCodec<ExplodeInteraction> CODEC = BuilderCodec.builder(
         ExplodeInteraction.class, ExplodeInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Performs an explosion using the provided config.")
      .<ExplosionConfig>appendInherited(
         new KeyedCodec<>("Config", ExplosionConfig.CODEC),
         (interaction, s) -> interaction.config = s,
         interaction -> interaction.config,
         (interaction, parent) -> interaction.config = parent.config
      )
      .addValidator(Validators.nonNull())
      .documentation("The explosion config associated with this projectile.")
      .add()
      .build();
   @Nonnull
   public static final Damage.EnvironmentSource DAMAGE_SOURCE_EXPLOSION = new Damage.EnvironmentSource("explosion");
   @Nullable
   private ExplosionConfig config;

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      assert this.config != null;

      DynamicMetaStore<InteractionContext> metaStore = context.getMetaStore();
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      Ref<EntityStore> ref = context.getEntity();
      Ref<EntityStore> ownerRef = context.getOwningEntity();
      World world = commandBuffer.getExternalData().getWorld();
      Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
      BlockPosition blockPosition = metaStore.getIfPresentMetaObject(Interaction.TARGET_BLOCK);
      Vector4d hitLocation = metaStore.getIfPresentMetaObject(Interaction.HIT_LOCATION);
      Vector3d position;
      if (hitLocation != null) {
         position = new Vector3d(hitLocation.x, hitLocation.y, hitLocation.z);
      } else if (InteractionTypeUtils.isCollisionType(type) && blockPosition != null) {
         long chunkIndex = ChunkUtil.indexChunkFromBlock(blockPosition.x, blockPosition.z);
         Ref<ChunkStore> chunkReference = chunkStore.getExternalData().getChunkReference(chunkIndex);
         if (chunkReference == null || !chunkReference.isValid()) {
            return;
         }

         WorldChunk worldChunkComponent = chunkStore.getComponent(chunkReference, WorldChunk.getComponentType());

         assert worldChunkComponent != null;

         BlockChunk blockChunkComponent = chunkStore.getComponent(chunkReference, BlockChunk.getComponentType());

         assert blockChunkComponent != null;

         BlockType blockType = worldChunkComponent.getBlockType(blockPosition.x, blockPosition.y, blockPosition.z);
         if (blockType == null) {
            return;
         }

         BlockSection blockSection = blockChunkComponent.getSectionAtBlockY(blockPosition.y);
         int rotationIndex = blockSection.getRotationIndex(blockPosition.x, blockPosition.y, blockPosition.z);
         position = new Vector3d();
         blockType.getBlockCenter(rotationIndex, position);
         position.add(blockPosition.x, blockPosition.y, blockPosition.z);
      } else {
         TransformComponent transformComponent = commandBuffer.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         position = transformComponent.getPosition();
      }

      Archetype<EntityStore> archetype = commandBuffer.getArchetype(ref);
      boolean isProjectile = archetype.contains(Projectile.getComponentType()) || archetype.contains(ProjectileComponent.getComponentType());
      Damage.Source damageSource = (Damage.Source)(isProjectile ? new Damage.ProjectileSource(ownerRef, ref) : DAMAGE_SOURCE_EXPLOSION);
      ExplosionUtils.performExplosion(damageSource, position, this.config, isProjectile ? ref : null, commandBuffer, chunkStore);
   }
}
