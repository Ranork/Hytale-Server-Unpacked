package com.hypixel.hytale.server.core.modules.collision;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.block.BlockUtil;
import com.hypixel.hytale.math.iterator.BoxBlockIterator;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CollisionResult implements BoxBlockIterator.BoxIterationConsumer {
   public static final Comparator<BlockCollisionData> BLOCK_COLLISION_DATA_COMPARATOR = Comparator.<BlockCollisionData>comparingDouble(a -> a.collisionStart)
      .thenComparingDouble(a -> a.collisionEnd);
   @Nonnull
   private final CollisionConfig collisionConfig;
   @Nonnull
   private final CollisionDataArray<BlockCollisionData> blockCollisions;
   @Nonnull
   private final CollisionDataArray<BlockCollisionData> blockSlides;
   @Nonnull
   private final CollisionDataArray<BlockCollisionData> blockTriggers;
   @Nonnull
   private final CollisionDataArray<CharacterCollisionData> characterCollisions;
   @Nonnull
   private final MovingBoxBoxCollisionEvaluator movingBoxBoxCollision;
   @Nonnull
   private final BoxBlockIntersectionEvaluator boxBlockIntersection;
   public List<Entity> collisionEntities;
   private boolean continueAfterCollision = true;
   private boolean haveNoCollision = true;
   private HytaleLogger logger;
   public double slideStart;
   public double slideEnd;
   public boolean isSliding;
   public int validate;
   private boolean checkForCharacterCollisions;
   private int walkableMaterialMask;
   public Predicate<CollisionConfig> isNonWalkable;
   private LongSet lastTriggers = new LongOpenHashSet();
   private LongSet newTriggers = new LongOpenHashSet();

   public CollisionResult() {
      this(true, false);
   }

   public CollisionResult(boolean enableSlides, boolean enableCharacters) {
      ObjectArrayList<BlockCollisionData> blockCollisionDataFreePool = new ObjectArrayList();
      ObjectArrayList<CharacterCollisionData> characterCollisionDataFreePool = new ObjectArrayList();
      this.blockCollisions = new CollisionDataArray<>(BlockCollisionData::new, BlockCollisionData::clear, blockCollisionDataFreePool);
      this.blockSlides = new CollisionDataArray<>(BlockCollisionData::new, BlockCollisionData::clear, blockCollisionDataFreePool);
      this.blockTriggers = new CollisionDataArray<>(BlockCollisionData::new, BlockCollisionData::clear, blockCollisionDataFreePool);
      this.characterCollisions = new CollisionDataArray<>(CharacterCollisionData::new, null, characterCollisionDataFreePool);
      this.collisionConfig = new CollisionConfig();
      this.collisionConfig.setDefaultCollisionBehaviour();
      this.movingBoxBoxCollision = new MovingBoxBoxCollisionEvaluator();
      this.movingBoxBoxCollision.setCheckForOnGround(enableSlides);
      this.boxBlockIntersection = new BoxBlockIntersectionEvaluator();
      this.checkForCharacterCollisions = enableCharacters;
      this.setDefaultWalkableBehaviour();
   }

   @Nonnull
   public CollisionConfig getConfig() {
      return this.collisionConfig;
   }

   public List<Entity> getCollisionEntities() {
      return this.collisionEntities;
   }

   public void setCollisionEntities(List<Entity> collisionEntities) {
      this.collisionEntities = collisionEntities;
   }

   @Nonnull
   public BoxBlockIntersectionEvaluator getBoxBlockIntersection() {
      return this.boxBlockIntersection;
   }

   @Nonnull
   public MovingBoxBoxCollisionEvaluator getMovingBoxBoxCollision() {
      return this.movingBoxBoxCollision;
   }

   public CharacterCollisionData allocCharacterCollision() {
      return this.characterCollisions.alloc();
   }

   public void addCollision(@Nonnull IBlockCollisionEvaluator blockCollisionEvaluator, int index) {
      if (!(blockCollisionEvaluator.getCollisionStart() > 1.0)) {
         blockCollisionEvaluator.setCollisionData(this.newCollision(), this.collisionConfig, index);
      }
   }

   public BlockCollisionData newCollision() {
      return this.blockCollisions.alloc();
   }

   public void addSlide(@Nonnull IBlockCollisionEvaluator blockCollisionEvaluator, int index) {
      if (!(blockCollisionEvaluator.getCollisionStart() > 1.0)) {
         blockCollisionEvaluator.setCollisionData(this.newSlide(), this.collisionConfig, index);
      }
   }

   public BlockCollisionData newSlide() {
      return this.blockSlides.alloc();
   }

   public void addTrigger(@Nonnull IBlockCollisionEvaluator blockCollisionEvaluator, int index) {
      if (!(blockCollisionEvaluator.getCollisionStart() > 1.0)) {
         blockCollisionEvaluator.setCollisionData(this.newTrigger(), this.collisionConfig, index);
      }
   }

   public BlockCollisionData newTrigger() {
      return this.blockTriggers.alloc();
   }

   public void reset() {
      this.blockCollisions.reset();
      this.blockSlides.reset();
      this.blockTriggers.reset();
      this.characterCollisions.reset();
   }

   public void process() {
      this.blockCollisions.sort(BasicCollisionData.COLLISION_START_COMPARATOR);
      this.blockTriggers.sort(BasicCollisionData.COLLISION_START_COMPARATOR);
      this.characterCollisions.sort(BasicCollisionData.COLLISION_START_COMPARATOR);
      if (this.blockSlides.getCount() > 0) {
         this.blockSlides.sort(BLOCK_COLLISION_DATA_COMPARATOR);
         BlockCollisionData slide = this.blockSlides.get(0);
         this.slideStart = slide.collisionStart;
         this.slideEnd = slide.collisionEnd;

         for (int i = 1; i < this.blockSlides.getCount(); i++) {
            slide = this.blockSlides.get(i);
            if (slide.collisionStart <= this.slideEnd && slide.collisionEnd > this.slideEnd) {
               this.slideEnd = slide.collisionEnd;
            }
         }

         this.isSliding = this.slideStart <= 0.0;
         if (this.slideEnd > 1.0) {
            this.slideEnd = 1.0;
         }
      } else {
         this.isSliding = false;
      }
   }

   public int getBlockCollisionCount() {
      return this.blockCollisions.getCount();
   }

   public BlockCollisionData getBlockCollision(int i) {
      return this.blockCollisions.get(i);
   }

   @Nullable
   public BlockCollisionData getFirstBlockCollision() {
      return this.blockCollisions.getFirst();
   }

   @Nullable
   public BlockCollisionData forgetFirstBlockCollision() {
      return this.blockCollisions.forgetFirst();
   }

   public int getCharacterCollisionCount() {
      return this.characterCollisions.getCount();
   }

   @Nullable
   public CharacterCollisionData getFirstCharacterCollision() {
      return this.characterCollisions.getFirst();
   }

   @Nullable
   public CharacterCollisionData forgetFirstCharacterCollision() {
      return this.characterCollisions.forgetFirst();
   }

   public void pruneTriggerBlocks(double distance) {
      for (int l = this.blockTriggers.size() - 1; l >= 0; l--) {
         BlockCollisionData blockCollisionData = this.blockTriggers.get(l);
         if (blockCollisionData.collisionStart <= distance) {
            break;
         }

         this.blockTriggers.remove(l);
      }
   }

   @Nonnull
   public CollisionDataArray<BlockCollisionData> getTriggerBlocks() {
      return this.blockTriggers;
   }

   public int defaultTriggerBlocksProcessing(
      @Nonnull InteractionManager manager,
      @Nonnull Entity entity,
      @Nonnull Ref<EntityStore> ref,
      boolean executeTriggers,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      World world = componentAccessor.getExternalData().getWorld();
      LongSet temp = this.lastTriggers;
      this.lastTriggers = this.newTriggers;
      this.newTriggers = temp;
      this.newTriggers.clear();
      int damageToEntity = 0;
      CollisionDataArray<BlockCollisionData> triggerBlocks = this.getTriggerBlocks();
      int i = 0;

      for (int size = triggerBlocks.size(); i < size; i++) {
         BlockCollisionData triggerCollision = triggerBlocks.get(i);
         if (triggerCollision.blockType != null) {
            int damageToEntities = Math.max(triggerCollision.blockType.getDamageToEntities(), triggerCollision.fluid.getDamageToEntities());
            if (damageToEntities > damageToEntity) {
               damageToEntity = damageToEntities;
            }
         }

         if (executeTriggers && entity instanceof LivingEntity) {
            WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(triggerCollision.x, triggerCollision.z));
            if (chunk != null) {
               BlockType blockType = chunk.getBlockType(triggerCollision.x, triggerCollision.y, triggerCollision.z);
               Fluid fluidType = Fluid.getAssetMap().getAsset(chunk.getFluidId(triggerCollision.x, triggerCollision.y, triggerCollision.z));
               String interactionsEnter = blockType.getInteractions().get(InteractionType.CollisionEnter);
               if (interactionsEnter == null) {
                  interactionsEnter = fluidType.getInteractions().get(InteractionType.CollisionEnter);
               }

               String interactions = blockType.getInteractions().get(InteractionType.Collision);
               if (interactions == null) {
                  interactions = fluidType.getInteractions().get(InteractionType.Collision);
               }

               if (interactionsEnter != null || interactions != null) {
                  int filler = chunk.getFiller(triggerCollision.x, triggerCollision.y, triggerCollision.z);
                  int x = triggerCollision.x;
                  int y = triggerCollision.y;
                  int z = triggerCollision.z;
                  if (filler != 0) {
                     x -= FillerBlockUtil.unpackX(filler);
                     y -= FillerBlockUtil.unpackY(filler);
                     z -= FillerBlockUtil.unpackZ(filler);
                  }

                  long index = BlockUtil.packUnchecked(x, y, z);
                  if (this.newTriggers.add(index)) {
                     BlockPosition pos = new BlockPosition(x, y, z);
                     if (!this.lastTriggers.remove(index) && interactionsEnter != null) {
                        this.doCollisionInteraction(manager, InteractionType.CollisionEnter, ref, interactionsEnter, pos, componentAccessor);
                     }

                     if (interactions != null) {
                        this.doCollisionInteraction(manager, InteractionType.Collision, ref, interactions, pos, componentAccessor);
                     }
                  }
               }
            }
         }
      }

      if (executeTriggers && entity instanceof LivingEntity && !this.lastTriggers.isEmpty()) {
         LongIterator var25 = this.lastTriggers.iterator();

         while (var25.hasNext()) {
            Long old = (Long)var25.next();
            int xx = BlockUtil.unpackX(old);
            int yx = BlockUtil.unpackY(old);
            int zx = BlockUtil.unpackZ(old);
            WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(xx, zx));
            if (chunk != null) {
               BlockType blockTypex = chunk.getBlockType(xx, yx, zx);
               Fluid fluidTypex = Fluid.getAssetMap().getAsset(chunk.getFluidId(xx, yx, zx));
               String interactionsx = blockTypex.getInteractions().get(InteractionType.CollisionLeave);
               if (interactionsx == null) {
                  interactionsx = fluidTypex.getInteractions().get(InteractionType.CollisionLeave);
               }

               if (interactionsx != null) {
                  this.doCollisionInteraction(manager, InteractionType.CollisionLeave, ref, interactionsx, new BlockPosition(xx, yx, zx), componentAccessor);
               }
            }
         }
      }

      return damageToEntity;
   }

   private void doCollisionInteraction(
      @Nonnull InteractionManager manager,
      @Nonnull InteractionType type,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull String interactions,
      @Nonnull BlockPosition pos,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      RootInteraction root = RootInteraction.getRootInteractionOrUnknown(interactions);
      World world = componentAccessor.getExternalData().getWorld();
      InteractionContext context = InteractionContext.forInteraction(manager, ref, type, componentAccessor);
      context.getMetaStore().putMetaObject(Interaction.TARGET_BLOCK_RAW, pos);
      context.getMetaStore().putMetaObject(Interaction.TARGET_BLOCK, world.getBaseBlock(pos));
      InteractionChain chain = manager.initChain(type, context, root, -1, pos, false);
      manager.queueExecuteChain(chain);
   }

   @Override
   public boolean next() {
      return this.continueAfterCollision || this.haveNoCollision;
   }

   @Override
   public boolean accept(long x, long y, long z) {
      if (this.collisionConfig.canCollide((int)x, (int)y, (int)z)) {
         x += this.collisionConfig.getBoundingBoxOffsetX();
         y += this.collisionConfig.getBoundingBoxOffsetY();
         z += this.collisionConfig.getBoundingBoxOffsetZ();
         int numDetails = this.collisionConfig.getDetailCount();
         boolean haveCollision = this.movingBoxBoxCollision.isBoundingBoxColliding(this.collisionConfig.getBoundingBox(), x, y, z);
         if (this.logger != null) {
            Object arg7 = this.collisionConfig.blockType != null ? this.collisionConfig.blockType.getId() : "null";
            this.logger
               .at(Level.INFO)
               .log(
                  "?? Block Test at %s/%s/%s numDet=%d haveColl=%s overlap=%s blockType=%s",
                  x,
                  y,
                  z,
                  numDetails,
                  haveCollision,
                  this.movingBoxBoxCollision.isOverlapping(),
                  arg7
               );
         }

         if (numDetails <= 1) {
            this.processCollisionResult(haveCollision, 0);
         } else if (haveCollision || this.movingBoxBoxCollision.isOverlapping() || this.movingBoxBoxCollision.isTouching()) {
            for (int i = 0; i < numDetails; i++) {
               haveCollision = this.movingBoxBoxCollision.isBoundingBoxColliding(this.collisionConfig.getBoundingBox(i), x, y, z);
               this.processCollisionResult(haveCollision, i);
            }
         }
      } else if (this.logger != null) {
         Object arg4 = this.collisionConfig.blockType != null ? this.collisionConfig.blockType.getId() : "null";
         this.logger.at(Level.INFO).log("-- Ignoring block at %s/%s/%s blockType=%s", x, y, z, arg4);
      }

      return true;
   }

   private void processCollisionResult(boolean haveCollision, int hitboxIndex) {
      if (this.logger != null) {
         this.logger
            .at(Level.INFO)
            .log(
               "?? Further testing block haveCol=%s hitBoxIndex=%s onGround=%s touching=%s canCollide=%s canTrigger=%s",
               haveCollision,
               hitboxIndex,
               this.movingBoxBoxCollision.isOnGround(),
               this.movingBoxBoxCollision.isTouching(),
               this.collisionConfig.blockCanCollide,
               this.collisionConfig.blockCanTrigger
            );
      }

      if (this.collisionConfig.blockCanCollide) {
         boolean isNoSlideCollision = true;
         if (this.movingBoxBoxCollision.onGround) {
            haveCollision = this.collisionConfig.blockType == null || this.isNonWalkable.test(this.collisionConfig);
            if (!haveCollision) {
               this.addSlide(this.movingBoxBoxCollision, hitboxIndex);
               if (this.collisionConfig.blockCanTrigger) {
                  this.addTrigger(this.movingBoxBoxCollision, hitboxIndex);
               }

               if (this.logger != null) {
                  this.logger
                     .at(Level.INFO)
                     .log(
                        "++ Sliding block start=%s end=%s normal=%s",
                        this.movingBoxBoxCollision.getCollisionStart(),
                        this.movingBoxBoxCollision.getCollisionEnd(),
                        Vector3d.formatShortString(this.movingBoxBoxCollision.getCollisionNormal())
                     );
               }

               return;
            }

            isNoSlideCollision = false;
            if (this.logger != null) {
               this.logger
                  .at(Level.INFO)
                  .log(
                     "?? Sliding block is unwalkable start=%s end=%s normal=%s",
                     this.movingBoxBoxCollision.getCollisionStart(),
                     this.movingBoxBoxCollision.getCollisionEnd(),
                     Vector3d.formatShortString(this.movingBoxBoxCollision.getCollisionNormal())
                  );
            }
         }

         if (haveCollision) {
            this.addCollision(this.movingBoxBoxCollision, hitboxIndex);
            if (isNoSlideCollision) {
               this.haveNoCollision = false;
            }

            if (this.logger != null) {
               this.logger
                  .at(Level.INFO)
                  .log(
                     "++ Collision with block start=%s end=%s normal=%s",
                     this.movingBoxBoxCollision.collisionStart,
                     this.movingBoxBoxCollision.collisionEnd,
                     Vector3d.formatShortString(this.movingBoxBoxCollision.collisionNormal)
                  );
            }
         }
      }

      if (this.collisionConfig.blockCanTrigger && (haveCollision || this.movingBoxBoxCollision.isTouching())) {
         if (this.logger != null) {
            this.logger
               .at(Level.INFO)
               .log(
                  "++ Trigger block start=%s end=%s normal=%s",
                  this.movingBoxBoxCollision.getCollisionStart(),
                  this.movingBoxBoxCollision.getCollisionEnd(),
                  Vector3d.formatShortString(this.movingBoxBoxCollision.getCollisionNormal())
               );
         }

         this.addTrigger(this.movingBoxBoxCollision, hitboxIndex);
      }
   }

   public void iterateBlocks(@Nonnull Box collider, @Nonnull Vector3d pos, @Nonnull Vector3d direction, double length, boolean stopOnCollisionFound) {
      this.continueAfterCollision = !stopOnCollisionFound;
      BoxBlockIterator.iterate(collider, pos, direction, length, this);
   }

   public void acquireCollisionModule() {
      this.haveNoCollision = true;
   }

   public void disableSlides() {
      this.movingBoxBoxCollision.setCheckForOnGround(false);
   }

   public void enableSlides() {
      this.movingBoxBoxCollision.setCheckForOnGround(true);
   }

   public void disableCharacterCollisions() {
      this.checkForCharacterCollisions = false;
   }

   public void enableCharacterCollsions() {
      this.checkForCharacterCollisions = true;
   }

   public boolean isCheckingForCharacterCollisions() {
      return this.checkForCharacterCollisions;
   }

   public void enableTriggerBlocks() {
      this.collisionConfig.setCheckTriggerBlocks(true);
   }

   public void disableTriggerBlocks() {
      this.collisionConfig.setCheckTriggerBlocks(false);
   }

   public boolean isCheckingTriggerBlocks() {
      return this.collisionConfig.isCheckTriggerBlocks();
   }

   public void enableDamageBlocks() {
      this.collisionConfig.setCheckDamageBlocks(true);
   }

   public void disableDamageBlocks() {
      this.collisionConfig.setCheckDamageBlocks(false);
   }

   public boolean isCheckingDamageBlocks() {
      return this.collisionConfig.isCheckDamageBlocks();
   }

   public boolean setDamageBlocking(boolean blocking) {
      boolean oldState = this.collisionConfig.setCollideWithDamageBlocks(blocking);
      this.updateDamageWalkableFlag();
      return oldState;
   }

   public boolean isDamageBlocking() {
      return this.collisionConfig.isCollidingWithDamageBlocks();
   }

   public void setCollisionByMaterial(int collidingMaterials) {
      this.collisionConfig.setCollisionByMaterial(collidingMaterials);
   }

   public void setCollisionByMaterial(int collidingMaterials, int walkableMaterials) {
      this.collisionConfig.setCollisionByMaterial(collidingMaterials);
      this.setWalkableByMaterial(walkableMaterials);
   }

   public int getCollisionByMaterial() {
      return this.collisionConfig.getCollisionByMaterial();
   }

   public void setDefaultCollisionBehaviour() {
      this.collisionConfig.setDefaultCollisionBehaviour();
   }

   public void setDefaultBlockCollisionPredicate() {
      this.collisionConfig.setDefaultBlockCollisionPredicate();
   }

   public void setDefaultNonWalkablePredicate() {
      this.isNonWalkable = collisionConfig -> {
         int matches = collisionConfig.blockMaterialMask & this.walkableMaterialMask;
         return matches == 0 || (matches & 16) != 0;
      };
   }

   public void setNonWalkablePredicate(Predicate<CollisionConfig> classifier) {
      this.isNonWalkable = classifier;
   }

   public void setWalkableByMaterial(int walkableMaterial) {
      this.walkableMaterialMask = 15 & walkableMaterial;
      this.updateDamageWalkableFlag();
   }

   protected void updateDamageWalkableFlag() {
      if (this.collisionConfig.isCollidingWithDamageBlocks()) {
         this.walkableMaterialMask |= 16;
      } else {
         this.walkableMaterialMask &= -17;
      }
   }

   public void setDefaultWalkableBehaviour() {
      this.setDefaultNonWalkablePredicate();
      this.setWalkableByMaterial(5);
   }

   public void setDefaultPlayerSettings() {
      this.enableSlides();
      this.disableCharacterCollisions();
      this.setDefaultNonWalkablePredicate();
      this.setDefaultBlockCollisionPredicate();
      this.setCollisionByMaterial(4);
      this.setWalkableByMaterial(15);
   }

   public boolean isComputeOverlaps() {
      return this.movingBoxBoxCollision.isComputeOverlaps();
   }

   public void setComputeOverlaps(boolean computeOverlaps) {
      this.movingBoxBoxCollision.setComputeOverlaps(computeOverlaps);
   }

   public HytaleLogger getLogger() {
      return this.logger;
   }

   public boolean shouldLog() {
      return this.logger != null;
   }

   public void setLogger(HytaleLogger logger) {
      this.logger = logger;
   }
}
