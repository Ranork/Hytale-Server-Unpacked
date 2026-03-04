package com.hypixel.hytale.server.core.entity;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.block.BlockSphereUtil;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockGathering;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemTool;
import com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.Selector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat.Knockback;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExplosionUtils {
   private static final boolean DEBUG_SHAPES = false;
   private static final Vector3f DEBUG_POTENTIAL_TARGET_COLOR = new Vector3f(1.0F, 1.0F, 0.0F);
   private static final int DEBUG_POTENTIAL_TARGET_TIME = 5;
   private static final float DEBUG_BLOCK_HIT_SCALE = 1.1F;
   private static final float DEBUG_BLOCK_HIT_TIME = 2.0F;
   private static final float DEBUG_BLOCK_HIT_ALPHA = 0.25F;
   private static final Vector3f DEBUG_BLOCK_RADIUS_COLOR = new Vector3f(1.0F, 0.5F, 0.5F);
   private static final Vector3f DEBUG_ENTITY_RADIUS_COLOR = new Vector3f(0.5F, 1.0F, 0.5F);
   private static final int DEBUG_BLOCK_RADIUS_TIME = 5;
   private static final int DEBUG_ENTITY_RADIUS_TIME = 5;

   public static void performExplosion(
      @Nonnull Damage.Source damageSource,
      @Nonnull Vector3d position,
      @Nonnull ExplosionConfig config,
      @Nullable Ref<EntityStore> ignoreRef,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull ComponentAccessor<ChunkStore> chunkStore
   ) {
      if (config.damageBlocks || config.damageEntities) {
         Set<Ref<EntityStore>> targetRefs = new ObjectOpenHashSet();
         Vector3d blockPosition = new Vector3d(Math.floor(position.x) + 0.5, Math.floor(position.y) + 0.5, Math.floor(position.z) + 0.5);
         processTargetBlocks(blockPosition, config, ignoreRef, targetRefs, commandBuffer, chunkStore);
         if (config.damageEntities) {
            processTargetEntities(config, position, damageSource, ignoreRef, targetRefs, commandBuffer);
         }
      }
   }

   private static void processTargetBlocks(
      @Nonnull Vector3d position,
      @Nonnull ExplosionConfig config,
      @Nullable Ref<EntityStore> ignoreRef,
      @Nonnull Set<Ref<EntityStore>> targetRefs,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull ComponentAccessor<ChunkStore> chunkStore
   ) {
      ThreadLocalRandom random = ThreadLocalRandom.current();
      World world = commandBuffer.getExternalData().getWorld();
      int explosionBlockRadius = config.blockDamageRadius;
      if (config.damageEntities && config.entityDamageRadius > config.blockDamageRadius) {
         explosionBlockRadius = (int)config.entityDamageRadius;
      }

      List<Ref<EntityStore>> potentialTargets = new ObjectArrayList();
      if (config.damageEntities) {
         Selector.selectNearbyEntities(
            commandBuffer, position, (double)config.entityDamageRadius, potentialTargets::add, e -> ignoreRef == null || !e.equals(ignoreRef)
         );
      }

      if (config.damageBlocks || !potentialTargets.isEmpty()) {
         ItemTool itemTool = config.itemTool;
         Set<Vector3i> targetBlocks = new ObjectOpenHashSet();
         int posX = MathUtil.floor(position.x);
         int posY = MathUtil.floor(position.y);
         int posZ = MathUtil.floor(position.z);
         BlockSphereUtil.forEachBlock(posX, posY, posZ, explosionBlockRadius, null, (x, y, z, aVoid) -> {
            targetBlocks.add(new Vector3i(x, y, z));
            return true;
         });
         Set<Vector3i> avoidBlocks = new ObjectOpenHashSet();

         for (Vector3i targetBlock : targetBlocks) {
            Vector3d targetBlockPosition = targetBlock.toVector3d().add(0.5, 0.5, 0.5);
            int setBlockSettings = 1028;
            if (random.nextFloat() > config.blockDropChance) {
               setBlockSettings |= 2048;
            }

            double distance = position.distanceTo(targetBlockPosition);
            if (!(distance <= 0.0) && !Double.isNaN(distance)) {
               Vector3d direction = targetBlockPosition.clone().subtract(position);
               Vector3i targetBlockPos = TargetUtil.getTargetBlock(
                  world,
                  (id, fluidId) -> isValidTargetBlock(id, config.damageBlocks),
                  position.x,
                  position.y,
                  position.z,
                  direction.x,
                  direction.y,
                  direction.z,
                  distance
               );
               if (targetBlockPos == null) {
                  if (config.damageEntities) {
                     Vector3d entityHitPos = position.clone().add(direction);
                     collectPotentialTargets(targetRefs, potentialTargets, entityHitPos, position, commandBuffer);
                  }
               } else if (!avoidBlocks.contains(targetBlockPos)) {
                  Vector3d targetBlockPosD = targetBlockPos.toVector3d().add(0.5, 0.5, 0.5);
                  if (config.damageEntities) {
                     collectPotentialTargets(targetRefs, potentialTargets, targetBlockPosD, position, commandBuffer);
                  }

                  float damageDistance = (float)position.distanceTo(targetBlockPosD);
                  float damageScale = calculateBlockDamageScale(damageDistance, explosionBlockRadius, config.blockDamageFalloff);
                  long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlockPos.x, targetBlockPos.z);
                  Ref<ChunkStore> chunkReference = chunkStore.getExternalData().getChunkReference(chunkIndex);
                  if (chunkReference != null) {
                     boolean canDamageBlock = distance <= config.blockDamageRadius;
                     if (!config.damageBlocks
                        || canDamageBlock
                           && !BlockHarvestUtils.performBlockDamage(
                              targetBlockPos, null, itemTool, damageScale, setBlockSettings, chunkReference, commandBuffer, chunkStore
                           )) {
                        avoidBlocks.add(targetBlockPos);
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean isValidTargetBlock(int blockTypeId, boolean damageBlocks) {
      if (blockTypeId == 0 || blockTypeId == 1) {
         return false;
      } else if (!damageBlocks) {
         BlockType blockType = BlockType.getAssetMap().getAsset(blockTypeId);
         if (blockType == null) {
            return false;
         } else {
            BlockGathering gathering = blockType.getGathering();
            return gathering == null || !gathering.isSoft();
         }
      } else {
         return true;
      }
   }

   private static void collectPotentialTargets(
      @Nonnull Set<Ref<EntityStore>> targetRefs,
      @Nonnull List<Ref<EntityStore>> potentialTargetRefs,
      @Nonnull Vector3d startPosition,
      @Nonnull Vector3d endPosition,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      World world = commandBuffer.getExternalData().getWorld();

      for (Ref<EntityStore> potentialTarget : potentialTargetRefs) {
         if (processPotentialEntity(potentialTarget, startPosition, endPosition, commandBuffer) && targetRefs.add(potentialTarget)) {
         }
      }
   }

   private static boolean processPotentialEntity(
      @Nonnull Ref<EntityStore> ref, @Nonnull Vector3d startPosition, @Nonnull Vector3d endPosition, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      BoundingBox boundingBoxComponent = commandBuffer.getComponent(ref, BoundingBox.getComponentType());
      if (boundingBoxComponent == null) {
         return false;
      } else {
         TransformComponent transformComponent = commandBuffer.getComponent(ref, TransformComponent.getComponentType());
         if (transformComponent == null) {
            return false;
         } else {
            Vector3d entityPosition = transformComponent.getPosition();
            Box boundingBox = boundingBoxComponent.getBoundingBox().clone().offset(entityPosition);
            return boundingBox.intersectsLine(startPosition, endPosition);
         }
      }
   }

   private static float calculateBlockDamageScale(float distance, float radius, float fallOff) {
      if (distance >= radius) {
         return 0.0F;
      } else {
         float normalizedDistance = distance / radius;
         return 1.0F - (float)Math.pow(normalizedDistance, fallOff);
      }
   }

   private static void processTargetEntities(
      @Nonnull ExplosionConfig config,
      @Nonnull Vector3d position,
      @Nonnull Damage.Source damageSource,
      @Nullable Ref<EntityStore> ignoreRef,
      @Nonnull Set<Ref<EntityStore>> targetRefs,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      for (Ref<EntityStore> targetRef : targetRefs) {
         processTargetEntity(config, targetRef, position, damageSource, commandBuffer);
      }
   }

   private static void processTargetEntity(
      @Nonnull ExplosionConfig config,
      @Nonnull Ref<EntityStore> targetRef,
      @Nonnull Vector3d position,
      @Nonnull Damage.Source damageSource,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      float entityDamageRadius = config.entityDamageRadius;
      float explosionDamage = config.entityDamage;
      float explosionFalloff = config.entityDamageFalloff;
      TransformComponent targetTransformComponent = commandBuffer.getComponent(targetRef, TransformComponent.getComponentType());

      assert targetTransformComponent != null;

      Velocity targetVelocityComponent = commandBuffer.getComponent(targetRef, Velocity.getComponentType());

      assert targetVelocityComponent != null;

      Vector3d targetPosition = targetTransformComponent.getPosition();
      Vector3d diff = targetPosition.clone().subtract(position);
      double distance = diff.length();
      float damage = (float)(explosionDamage * Math.pow(1.0 - distance / entityDamageRadius, explosionFalloff));
      if (damage > 0.0F) {
         DamageSystems.executeDamage(targetRef, commandBuffer, new Damage(damageSource, DamageCause.ENVIRONMENT, damage));
      }

      Knockback knockbackConfig = config.knockback;
      if (knockbackConfig != null) {
         ComponentType<EntityStore, KnockbackComponent> knockbackComponentType = KnockbackComponent.getComponentType();
         KnockbackComponent knockbackComponent = commandBuffer.getComponent(targetRef, knockbackComponentType);
         if (knockbackComponent == null) {
            knockbackComponent = new KnockbackComponent();
            commandBuffer.putComponent(targetRef, knockbackComponentType, knockbackComponent);
         }

         Vector3d direction = diff.clone().normalize();
         knockbackComponent.setVelocity(knockbackConfig.calculateVector(position, (float)direction.y, targetPosition));
         knockbackComponent.setVelocityType(knockbackConfig.getVelocityType());
         knockbackComponent.setVelocityConfig(knockbackConfig.getVelocityConfig());
         knockbackComponent.setDuration(knockbackConfig.getDuration());
      }
   }
}
