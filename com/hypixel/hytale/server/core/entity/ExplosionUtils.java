package com.hypixel.hytale.server.core.entity;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.block.BlockSphereUtil;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockGathering;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemTool;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.Selector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat.Knockback;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

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
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nonnull ComponentAccessor<ChunkStore> chunkStore
   ) {
      if (config.damageBlocks || config.damageEntities) {
         Set<Ref<EntityStore>> targetRefs = new ReferenceOpenHashSet();
         Vector3d blockPosition = new Vector3d(Math.floor(position.x) + 0.5, Math.floor(position.y) + 0.5, Math.floor(position.z) + 0.5);
         processTargetBlocks(blockPosition, config, ignoreRef, targetRefs, componentAccessor, chunkStore);
         if (config.damageEntities) {
            processTargetEntities(config, position, damageSource, ignoreRef, targetRefs, componentAccessor);
         }

         performExplosionEffects(position, new Rotation3f(0.0F, 0.0F, 0.0F), config, componentAccessor);
      }
   }

   private static void performExplosionEffects(
      @Nonnull Vector3d position, @Nonnull Rotation3f rotation, @Nonnull ExplosionConfig config, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = componentAccessor.getResource(EntityModule.get().getPlayerSpatialResourceType());
      List<Ref<EntityStore>> playerRefs = SpatialResource.getThreadLocalReferenceList();
      playerSpatialResource.getSpatialStructure().collect(position, 75.0, playerRefs);
      if (config.particles != null) {
         for (ModelParticle particle : config.particles) {
            Direction particleRotation = new Direction(rotation.yaw(), rotation.pitch(), rotation.roll());
            Direction particleRotationOffset = particle.getRotationOffset();
            if (particleRotationOffset != null) {
               particleRotation.yaw = particleRotation.yaw + (float)Math.toRadians(particleRotationOffset.yaw);
               particleRotation.pitch = particleRotation.pitch + (float)Math.toRadians(particleRotationOffset.pitch);
               particleRotation.roll = particleRotation.roll + (float)Math.toRadians(particleRotationOffset.roll);
            }

            ParticleUtil.spawnParticleEffect(
               particle.getSystemId(),
               position.x,
               position.y,
               position.z,
               particleRotation.yaw,
               particleRotation.pitch,
               particleRotation.roll,
               particle.getScale(),
               particle.getColor(),
               null,
               playerRefs,
               componentAccessor
            );
         }
      }

      SoundUtil.playSoundEvent3d(config.soundEventIndex, SoundCategory.SFX, position.x, position.y, position.z, componentAccessor);
   }

   private static void processTargetBlocks(
      @Nonnull Vector3d position,
      @Nonnull ExplosionConfig config,
      @Nullable Ref<EntityStore> ignoreRef,
      @Nonnull Set<Ref<EntityStore>> targetRefs,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nonnull ComponentAccessor<ChunkStore> chunkStore
   ) {
      ThreadLocalRandom random = ThreadLocalRandom.current();
      World world = componentAccessor.getExternalData().getWorld();
      int explosionBlockRadius = config.blockDamageRadius;
      if (config.damageEntities && config.entityDamageRadius > config.blockDamageRadius) {
         explosionBlockRadius = (int)config.entityDamageRadius;
      }

      List<Ref<EntityStore>> potentialTargets = new ReferenceArrayList();
      if (config.damageEntities) {
         Selector.selectNearbyEntities(
            componentAccessor, position, (double)config.entityDamageRadius, potentialTargets::add, e -> ignoreRef == null || !e.equals(ignoreRef)
         );
      }

      if (config.damageBlocks || !potentialTargets.isEmpty()) {
         ItemTool itemTool = config.itemTool;
         Set<Vector3i> targetBlocks = new ObjectOpenHashSet();
         int posX = MathUtil.floor(position.x);
         int posY = MathUtil.floor(position.y);
         int posZ = MathUtil.floor(position.z);
         BlockSphereUtil.forEachBlock(posX, posY, posZ, explosionBlockRadius, null, (x, y, z, var4x) -> {
            targetBlocks.add(new Vector3i(x, y, z));
            return true;
         });
         Set<Vector3i> avoidBlocks = new ObjectOpenHashSet();

         for (Vector3i targetBlock : targetBlocks) {
            Vector3d targetBlockPosition = Vector3iUtil.toVector3d(targetBlock).add(0.5, 0.5, 0.5);
            int setBlockSettings = 1028;
            if (random.nextFloat() > config.blockDropChance) {
               setBlockSettings |= 2048;
            }

            double distance = position.distance(targetBlockPosition);
            if (!(distance <= 0.0) && !Double.isNaN(distance)) {
               Vector3d direction = new Vector3d(targetBlockPosition).sub(position);
               Vector3i targetBlockPos = TargetUtil.getTargetBlock(
                  world,
                  (id, var2x) -> isValidTargetBlock(id, config.damageBlocks),
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
                     Vector3d entityHitPos = new Vector3d(position).add(direction);
                     collectPotentialTargets(targetRefs, potentialTargets, entityHitPos, position, componentAccessor);
                  }
               } else if (!avoidBlocks.contains(targetBlockPos)) {
                  Vector3d targetBlockPosD = Vector3iUtil.toVector3d(targetBlockPos).add(0.5, 0.5, 0.5);
                  if (config.damageEntities) {
                     collectPotentialTargets(targetRefs, potentialTargets, targetBlockPosD, position, componentAccessor);
                  }

                  float damageDistance = (float)position.distance(targetBlockPosD);
                  float damageScale = calculateBlockDamageScale(damageDistance, explosionBlockRadius, config.blockDamageFalloff);
                  long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlockPos.x, targetBlockPos.z);
                  Ref<ChunkStore> chunkReference = chunkStore.getExternalData().getChunkReference(chunkIndex);
                  if (chunkReference != null && chunkReference.isValid()) {
                     boolean canDamageBlock = distance <= config.blockDamageRadius;
                     if (!config.damageBlocks
                        || canDamageBlock
                           && !BlockHarvestUtils.performBlockDamage(
                              targetBlockPos, null, itemTool, damageScale, setBlockSettings, chunkReference, componentAccessor, chunkStore
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
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      World world = componentAccessor.getExternalData().getWorld();

      for (Ref<EntityStore> potentialTarget : potentialTargetRefs) {
         if (processPotentialEntity(potentialTarget, startPosition, endPosition, componentAccessor) && targetRefs.add(potentialTarget)) {
         }
      }
   }

   private static boolean processPotentialEntity(
      @Nonnull Ref<EntityStore> ref, @Nonnull Vector3d startPosition, @Nonnull Vector3d endPosition, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      BoundingBox boundingBoxComponent = componentAccessor.getComponent(ref, BoundingBox.getComponentType());
      if (boundingBoxComponent == null) {
         return false;
      } else {
         TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());
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
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      for (Ref<EntityStore> targetRef : targetRefs) {
         processTargetEntity(config, targetRef, position, damageSource, componentAccessor);
      }
   }

   private static void processTargetEntity(
      @Nonnull ExplosionConfig config,
      @Nonnull Ref<EntityStore> targetRef,
      @Nonnull Vector3d position,
      @Nonnull Damage.Source damageSource,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      float entityDamageRadius = config.entityDamageRadius;
      float explosionDamage = config.entityDamage;
      float explosionFalloff = config.entityDamageFalloff;
      TransformComponent targetTransformComponent = componentAccessor.getComponent(targetRef, TransformComponent.getComponentType());

      assert targetTransformComponent != null;

      Velocity targetVelocityComponent = componentAccessor.getComponent(targetRef, Velocity.getComponentType());

      assert targetVelocityComponent != null;

      Vector3d targetPosition = targetTransformComponent.getPosition();
      Vector3d diff = new Vector3d(targetPosition).sub(position);
      double distance = diff.length();
      float damage = (float)(explosionDamage * Math.pow(1.0 - distance / entityDamageRadius, explosionFalloff));
      if (damage > 0.0F) {
         DamageSystems.executeDamage(targetRef, componentAccessor, new Damage(damageSource, DamageCause.ENVIRONMENT, damage));
      }

      Knockback knockbackConfig = config.knockback;
      if (knockbackConfig != null) {
         ComponentType<EntityStore, KnockbackComponent> knockbackComponentType = KnockbackComponent.getComponentType();
         KnockbackComponent knockbackComponent = componentAccessor.getComponent(targetRef, knockbackComponentType);
         if (knockbackComponent == null) {
            knockbackComponent = new KnockbackComponent();
            componentAccessor.putComponent(targetRef, knockbackComponentType, knockbackComponent);
         }

         Vector3d direction = new Vector3d(diff).normalize();
         knockbackComponent.setVelocity(knockbackConfig.calculateVector(position, (float)direction.y, targetPosition));
         knockbackComponent.setVelocityType(knockbackConfig.getVelocityType());
         knockbackComponent.setVelocityConfig(knockbackConfig.getVelocityConfig());
         knockbackComponent.setDuration(knockbackConfig.getDuration());
      }
   }
}
