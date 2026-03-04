package com.hypixel.hytale.server.core.modules.collision;

import com.hypixel.hytale.math.iterator.BoxBlockIterator;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.universe.world.World;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockCollisionProvider implements BoxBlockIterator.BoxIterationConsumer {
   protected final BoxBlockIntersectionEvaluator boxBlockIntersectionEvaluator = new BoxBlockIntersectionEvaluator();
   protected final MovingBoxBoxCollisionEvaluator movingBoxBoxCollisionEvaluator = new MovingBoxBoxCollisionEvaluator();
   protected final BlockDataProvider blockData = new BlockDataProvider();
   protected final Box fluidBox = new Box(Box.UNIT);
   protected final CollisionTracker damageTracker = new CollisionTracker();
   protected final CollisionTracker triggerTracker = new CollisionTracker();
   protected final BlockTracker collisionTracker = new BlockTracker();
   protected int requestedCollisionMaterials = 4;
   protected boolean reportOverlaps;
   @Nullable
   protected IBlockCollisionConsumer collisionConsumer;
   @Nullable
   protected IBlockTracker activeTriggers;
   @Nullable
   protected Vector3d motion;
   protected double relativeStopDistance;
   protected IBlockCollisionConsumer.Result collisionState;

   public void setRequestedCollisionMaterials(int requestedCollisionMaterials) {
      this.requestedCollisionMaterials = requestedCollisionMaterials;
   }

   public void setReportOverlaps(boolean reportOverlaps) {
      this.reportOverlaps = reportOverlaps;
      this.movingBoxBoxCollisionEvaluator.setComputeOverlaps(reportOverlaps);
   }

   @Override
   public boolean next() {
      return this.onSliceFinished();
   }

   @Override
   public boolean accept(long x, long y, long z) {
      return this.processBlockDynamic((int)x, (int)y, (int)z);
   }

   public void cast(
      @Nonnull World world,
      @Nonnull Box collider,
      @Nonnull Vector3d pos,
      @Nonnull Vector3d v,
      @Nonnull IBlockCollisionConsumer collisionConsumer,
      @Nonnull IBlockTracker activeTriggers,
      double collisionStop
   ) {
      if (!CollisionModule.get().isDisabled()) {
         this.collisionConsumer = collisionConsumer;
         this.activeTriggers = activeTriggers;
         this.motion = v;
         this.blockData.initialize(world);
         boolean isFarDistance = !CollisionModule.isBelowMovementThreshold(v);
         if (isFarDistance) {
            this.castIterative(collider, pos, v, collisionStop);
         } else {
            this.castShortDistance(collider, pos, v);
         }

         collisionConsumer.onCollisionFinished();
         this.blockData.cleanup();
         this.triggerTracker.reset();
         this.damageTracker.reset();
         this.collisionConsumer = null;
         this.activeTriggers = null;
         this.motion = null;
      }
   }

   protected void castShortDistance(@Nonnull Box collider, @Nonnull Vector3d pos, @Nonnull Vector3d v) {
      this.boxBlockIntersectionEvaluator.setBox(collider, pos).offsetPosition(v);
      collider.forEachBlock(pos.x + v.x, pos.y + v.y, pos.z + v.z, 1.0E-5, this, (x, y, z, _this) -> _this.processBlockStatic(x, y, z));
      this.generateTriggerExit();
   }

   protected boolean processBlockStatic(int x, int y, int z) {
      this.blockData.read(x, y, z);
      BlockBoundingBoxes boundingBoxes = this.blockData.getBlockBoundingBoxes();
      int blockX = this.blockData.originX(x);
      int blockY = this.blockData.originY(y);
      int blockZ = this.blockData.originZ(z);
      boolean trigger = this.blockData.isTrigger() && !this.triggerTracker.isTracked(blockX, blockY, blockZ);
      int damage = this.blockData.getBlockDamage();
      boolean canCollide = this.canCollide();
      Box[] boxes = boundingBoxes.get(this.blockData.rotation).getDetailBoxes();
      this.boxBlockIntersectionEvaluator.setDamageAndSubmerged(damage, false);
      if (this.blockData.getBlockType().getMaterial() != BlockMaterial.Empty
         || this.blockData.getBlockType().getMaterial() == BlockMaterial.Empty && this.blockData.getFluidId() == 0) {
         if (damage != 0 && boundingBoxes.protrudesUnitBox()) {
            if (this.damageTracker.isTracked(blockX, blockY, blockZ)) {
               damage = 0;
            } else {
               this.damageTracker.trackNew(blockX, blockY, blockZ);
            }
         }

         if (canCollide && boundingBoxes.protrudesUnitBox()) {
            if (this.collisionTracker.isTracked(blockX, blockY, blockZ)) {
               canCollide = false;
            } else {
               this.collisionTracker.trackNew(blockX, blockY, blockZ);
            }
         }

         for (int i = 0; (canCollide || trigger || damage > 0) && i < boxes.length; i++) {
            Box box = boxes[i];
            if (!CollisionMath.isDisjoint(this.boxBlockIntersectionEvaluator.intersectBoxComputeTouch(box, blockX, blockY, blockZ))) {
               if (canCollide || this.boxBlockIntersectionEvaluator.isOverlapping() && this.reportOverlaps) {
                  this.collisionConsumer.onCollision(blockX, blockY, blockZ, this.motion, this.boxBlockIntersectionEvaluator, this.blockData, box);
                  canCollide = false;
               }

               if (trigger) {
                  if (!this.activeTriggers.isTracked(blockX, blockY, blockZ)) {
                     this.activeTriggers.trackNew(blockX, blockY, blockZ);
                  }

                  this.triggerTracker.trackNew(blockX, blockY, blockZ);
                  trigger = false;
               }

               if (damage != 0) {
                  this.collisionConsumer.onCollisionDamage(blockX, blockY, blockZ, this.motion, this.boxBlockIntersectionEvaluator, this.blockData);
                  damage = 0;
               }
            }
         }

         Fluid fluid = this.blockData.getFluid();
         if (fluid != null && this.blockData.getFluidId() != 0) {
            this.processBlockStaticFluid(x, y, z, fluid, true);
         }

         return true;
      } else {
         if (trigger) {
            this.boxBlockIntersectionEvaluator.setDamageAndSubmerged(damage, false);

            for (Box box : boxes) {
               if (!CollisionMath.isDisjoint(this.boxBlockIntersectionEvaluator.intersectBoxComputeTouch(box, blockX, blockY, blockZ))) {
                  this.triggerTracker.trackNew(blockX, blockY, blockZ);
                  break;
               }
            }
         }

         this.processBlockStaticFluid(x, y, z, this.blockData.getFluid(), false);
         return true;
      }
   }

   protected void processBlockStaticFluid(int x, int y, int z, @Nonnull Fluid fluid, boolean submergeFluid) {
      boolean processDamage = fluid.getDamageToEntities() != 0;
      boolean processCollision = this.canCollide(2);
      if (processDamage || processCollision) {
         this.fluidBox.max.y = this.blockData.getFillHeight();
         if (!CollisionMath.isDisjoint(this.boxBlockIntersectionEvaluator.intersectBoxComputeTouch(this.fluidBox, x, y, z))) {
            this.boxBlockIntersectionEvaluator.setDamageAndSubmerged(fluid.getDamageToEntities(), submergeFluid);
            if (processCollision) {
               this.collisionConsumer.onCollision(x, y, z, this.motion, this.boxBlockIntersectionEvaluator, this.blockData, this.fluidBox);
            }

            if (processDamage) {
               this.collisionConsumer.onCollisionDamage(x, y, z, this.motion, this.boxBlockIntersectionEvaluator, this.blockData);
            }
         }
      }
   }

   protected boolean canCollide() {
      return this.canCollide(this.blockData.getCollisionMaterials());
   }

   protected boolean canCollide(int collisionMaterials) {
      return (collisionMaterials & this.requestedCollisionMaterials) != 0;
   }

   protected void castIterative(@Nonnull Box collider, @Nonnull Vector3d pos, @Nonnull Vector3d v, double collisionStop) {
      this.relativeStopDistance = MathUtil.clamp(collisionStop, 0.0, 1.0);
      this.collisionState = IBlockCollisionConsumer.Result.CONTINUE;
      this.movingBoxBoxCollisionEvaluator.setCollider(collider).setMove(pos, v);
      collider.forEachBlock(pos, 1.0E-5, this, (xx, yx, zx, _this) -> _this.processBlockDynamic(xx, yx, zx));
      BoxBlockIterator.iterate(collider, pos, v, v.length(), this);
      int count = this.damageTracker.getCount();

      for (int i = 0; i < count; i++) {
         BlockContactData collision = this.damageTracker.getContactData(i);
         if (collision.getCollisionStart() <= this.relativeStopDistance) {
            Vector3i position = this.damageTracker.getPosition(i);
            this.collisionConsumer.onCollisionDamage(position.x, position.y, position.z, this.motion, collision, this.damageTracker.getBlockData(i));
         }
      }

      this.generateTriggerExit();
      count = this.triggerTracker.getCount();

      for (int ix = 0; ix < count; ix++) {
         BlockContactData collision = this.triggerTracker.getContactData(ix);
         if (collision.getCollisionStart() <= this.relativeStopDistance) {
            Vector3i position = this.triggerTracker.getPosition(ix);
            int x = position.x;
            int y = position.y;
            int z = position.z;
            if (!this.activeTriggers.isTracked(x, y, z)) {
               this.activeTriggers.trackNew(x, y, z);
            }
         }
      }
   }

   protected boolean onSliceFinished() {
      IBlockCollisionConsumer.Result result = this.collisionConsumer.onCollisionSliceFinished();
      if (result != null && this.collisionState.ordinal() < result.ordinal()) {
         this.collisionState = result;
      }

      return this.collisionState == IBlockCollisionConsumer.Result.CONTINUE;
   }

   protected boolean processBlockDynamic(int x, int y, int z) {
      this.blockData.read(x, y, z);
      int blockX = this.blockData.originX(x);
      int blockY = this.blockData.originY(y);
      int blockZ = this.blockData.originZ(z);
      BlockBoundingBoxes boundingBoxes = this.blockData.getBlockBoundingBoxes();
      Box[] boxes = boundingBoxes.get(this.blockData.rotation).getDetailBoxes();
      boolean canCollide = this.canCollide();
      int damage = this.blockData.getBlockDamage();
      boolean trigger = this.blockData.isTrigger();
      this.movingBoxBoxCollisionEvaluator.setDamageAndSubmerged(damage, false);
      BlockContactData triggerCollisionData = null;
      BlockContactData damageCollisionData = null;
      if (trigger) {
         triggerCollisionData = this.triggerTracker.getContactData(blockX, blockY, blockZ);
         if (triggerCollisionData != null) {
            trigger = false;
         }
      }

      if (damage != 0 && boundingBoxes.protrudesUnitBox()) {
         damageCollisionData = this.damageTracker.getContactData(blockX, blockY, blockZ);
         if (damageCollisionData != null) {
            damage = 0;
         }
      }

      if (this.blockData.getBlockType().getMaterial() != BlockMaterial.Empty
         || this.blockData.getBlockType().getMaterial() == BlockMaterial.Empty && this.blockData.getFluidId() == 0) {
         for (Box box : boxes) {
            if (this.movingBoxBoxCollisionEvaluator.isBoundingBoxColliding(box, blockX, blockY, blockZ)) {
               if (this.movingBoxBoxCollisionEvaluator.getCollisionStart() > this.relativeStopDistance) {
                  if (this.movingBoxBoxCollisionEvaluator.isOverlapping() && this.reportOverlaps) {
                     IBlockCollisionConsumer.Result result = this.collisionConsumer
                        .onCollision(blockX, blockY, blockZ, this.motion, this.movingBoxBoxCollisionEvaluator, this.blockData, box);
                     this.updateStopDistance(result);
                  }
               } else {
                  if (canCollide || this.movingBoxBoxCollisionEvaluator.isOverlapping() && this.reportOverlaps) {
                     IBlockCollisionConsumer.Result result = this.collisionConsumer
                        .onCollision(blockX, blockY, blockZ, this.motion, this.movingBoxBoxCollisionEvaluator, this.blockData, box);
                     this.updateStopDistance(result);
                  }

                  if (trigger) {
                     triggerCollisionData = this.processTriggerDynamic(blockX, blockY, blockZ, triggerCollisionData);
                  }

                  if (damage != 0) {
                     damageCollisionData = this.processDamageDynamic(blockX, blockY, blockZ, damageCollisionData);
                  }
               }
            }
         }

         Fluid fluid = this.blockData.getFluid();
         if (fluid != null && this.blockData.getFluidId() != 0) {
            this.processBlockDynamicFluid(x, y, z, fluid, damageCollisionData, true);
         }

         return this.collisionState != IBlockCollisionConsumer.Result.STOP_NOW;
      } else {
         if (trigger) {
            for (Box boxx : boxes) {
               if (this.movingBoxBoxCollisionEvaluator.isBoundingBoxColliding(boxx, blockX, blockY, blockZ)
                  && this.movingBoxBoxCollisionEvaluator.getCollisionStart() <= this.relativeStopDistance) {
                  triggerCollisionData = this.processTriggerDynamic(blockX, blockY, blockZ, triggerCollisionData);
               }
            }
         }

         this.processBlockDynamicFluid(x, y, z, this.blockData.getFluid(), damageCollisionData, false);
         return this.collisionState != IBlockCollisionConsumer.Result.STOP_NOW;
      }
   }

   protected void processBlockDynamicFluid(int x, int y, int z, @Nonnull Fluid fluid, BlockContactData damageCollisionData, boolean isSubmergeFluid) {
      boolean processDamage = fluid.getDamageToEntities() != 0;
      boolean processCollision = this.canCollide(2);
      if (processDamage || processCollision) {
         this.fluidBox.max.y = this.blockData.getFillHeight();
         if (this.movingBoxBoxCollisionEvaluator.isBoundingBoxColliding(this.fluidBox, x, y, z)
            && this.movingBoxBoxCollisionEvaluator.getCollisionStart() <= this.relativeStopDistance) {
            this.movingBoxBoxCollisionEvaluator.setDamageAndSubmerged(fluid.getDamageToEntities(), isSubmergeFluid);
            if (processCollision) {
               IBlockCollisionConsumer.Result result = this.collisionConsumer
                  .onCollision(x, y, z, this.motion, this.movingBoxBoxCollisionEvaluator, this.blockData, this.fluidBox);
               this.updateStopDistance(result);
            }

            if (processDamage) {
               this.processDamageDynamic(x, y, z, damageCollisionData);
            }
         }
      }
   }

   @Nonnull
   protected BlockContactData processTriggerDynamic(int blockX, int blockY, int blockZ, @Nullable BlockContactData collisionData) {
      if (collisionData == null) {
         return this.triggerTracker.trackNew(blockX, blockY, blockZ, this.movingBoxBoxCollisionEvaluator, this.blockData);
      } else {
         double collisionEnd = Math.max(collisionData.collisionEnd, this.movingBoxBoxCollisionEvaluator.getCollisionEnd());
         if (this.movingBoxBoxCollisionEvaluator.getCollisionStart() < collisionData.collisionStart) {
            collisionData.assign(this.movingBoxBoxCollisionEvaluator);
         }

         collisionData.collisionEnd = collisionEnd;
         return collisionData;
      }
   }

   @Nonnull
   protected BlockContactData processDamageDynamic(int blockX, int blockY, int blockZ, @Nullable BlockContactData collisionData) {
      IBlockCollisionConsumer.Result result = this.collisionConsumer
         .probeCollisionDamage(blockX, blockY, blockZ, this.motion, this.movingBoxBoxCollisionEvaluator, this.blockData);
      this.updateStopDistance(result);
      if (collisionData == null) {
         return this.damageTracker.trackNew(blockX, blockY, blockZ, this.movingBoxBoxCollisionEvaluator, this.blockData);
      } else {
         if (this.movingBoxBoxCollisionEvaluator.getCollisionStart() < collisionData.collisionStart) {
            collisionData.assign(this.movingBoxBoxCollisionEvaluator);
         }

         return collisionData;
      }
   }

   protected void updateStopDistance(@Nullable IBlockCollisionConsumer.Result result) {
      if (result != null && result != IBlockCollisionConsumer.Result.CONTINUE) {
         if (this.movingBoxBoxCollisionEvaluator.collisionStart < this.relativeStopDistance) {
            this.relativeStopDistance = this.movingBoxBoxCollisionEvaluator.collisionStart;
         }

         if (result.ordinal() > this.collisionState.ordinal()) {
            this.collisionState = result;
         }
      }
   }

   protected void generateTriggerExit() {
      for (int i = this.activeTriggers.getCount() - 1; i >= 0; i--) {
         Vector3i p = this.activeTriggers.getPosition(i);
         if (!this.triggerTracker.isTracked(p.x, p.y, p.z)) {
            this.activeTriggers.untrack(p.x, p.y, p.z);
         }
      }
   }
}
