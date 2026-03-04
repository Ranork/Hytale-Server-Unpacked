package com.hypixel.hytale.server.npc.role.support;

import com.hypixel.hytale.common.collection.BucketItem;
import com.hypixel.hytale.common.collection.BucketItemPool;
import com.hypixel.hytale.common.collection.BucketList;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.function.consumer.DoubleQuadObjectConsumer;
import com.hypixel.hytale.function.consumer.QuadConsumer;
import com.hypixel.hytale.function.consumer.TriConsumer;
import com.hypixel.hytale.function.predicate.QuadObjectDoublePredicate;
import com.hypixel.hytale.function.predicate.QuadPredicate;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.components.SortBufferProviderResource;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityList extends BucketList<Ref<EntityStore>> {
   protected static final int BUCKET_COUNT = 6;
   protected static final int BUCKET_DISTANCE_NEAR = 5;
   protected static final int BUCKET_DISTANCE_NEARER = 3;
   protected static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   @Nonnull
   protected final BiPredicate<Ref<EntityStore>, ComponentAccessor<EntityStore>> validator;
   protected IntArrayList bucketRanges;
   protected int maxDistanceUnsorted;
   protected int maxDistanceSorted;
   protected int maxDistanceAvoidance;
   protected int squaredMaxDistanceSorted;
   protected int squaredMaxDistanceAvoidance;
   protected int squaredMaxDistanceUnsorted;
   protected int searchRadius;

   public EntityList(@Nullable BucketItemPool<Ref<EntityStore>> holderPool, @Nonnull BiPredicate<Ref<EntityStore>, ComponentAccessor<EntityStore>> validator) {
      super(holderPool);
      this.validator = validator;
      this.bucketRanges = new IntArrayList();
   }

   public int getMaxDistanceUnsorted() {
      return this.maxDistanceUnsorted;
   }

   public int getMaxDistanceSorted() {
      return this.maxDistanceSorted;
   }

   public int getMaxDistanceAvoidance() {
      return this.maxDistanceAvoidance;
   }

   public int getSearchRadius() {
      return this.searchRadius;
   }

   public IntArrayList getBucketRanges() {
      return this.bucketRanges;
   }

   @Override
   public void reset() {
      this.maxDistanceUnsorted = 0;
      this.maxDistanceSorted = 0;
      this.maxDistanceAvoidance = 0;
      this.squaredMaxDistanceSorted = 0;
      this.squaredMaxDistanceUnsorted = 0;
      this.squaredMaxDistanceAvoidance = 0;
      this.searchRadius = 0;
      this.squaredMaxDistance = 0;
      super.clear();
      this.bucketRanges.clear();
   }

   public int requireDistanceSorted(int value) {
      value = MathUtil.fastCeil((float)value);
      if (this.maxDistanceSorted < value) {
         this.maxDistanceSorted = value;
      }

      addBucketDistance(this.bucketRanges, 6, value);
      return value;
   }

   public int requireDistanceUnsorted(int value) {
      value = MathUtil.fastCeil((float)value);
      if (this.maxDistanceUnsorted < value) {
         this.maxDistanceUnsorted = value;
      }

      addBucketDistance(this.bucketRanges, 6, value);
      return value;
   }

   public int requireDistanceAvoidance(int value) {
      value = MathUtil.fastCeil((float)value);
      if (this.maxDistanceAvoidance < value) {
         this.maxDistanceAvoidance = value;
      }

      addBucketDistance(this.bucketRanges, 6, value);
      return value;
   }

   public void finalizeConfiguration() {
      this.squaredMaxDistanceSorted = this.maxDistanceSorted * this.maxDistanceSorted;
      this.squaredMaxDistanceUnsorted = this.maxDistanceUnsorted * this.maxDistanceUnsorted;
      this.squaredMaxDistanceAvoidance = this.maxDistanceAvoidance * this.maxDistanceAvoidance;
      this.searchRadius = MathUtil.maxValue(this.maxDistanceAvoidance, this.maxDistanceSorted, this.maxDistanceUnsorted);
      this.squaredMaxDistance = this.searchRadius * this.searchRadius;
      if (this.searchRadius != 0) {
         int keepRange = this.maxDistanceAvoidance > 0 ? this.maxDistanceAvoidance : -1;
         if (keepRange > 0) {
            addBucketDistance(this.bucketRanges, 6, keepRange);
         }

         if (this.maxDistanceSorted > 3) {
            addBucketDistance(this.bucketRanges, 6, 3, keepRange);
            if (this.maxDistanceSorted > 5) {
               addBucketDistance(this.bucketRanges, 6, 5, keepRange);
            }
         }

         super.configureWithPreSortedArray(this.bucketRanges.toIntArray());
      }
   }

   public void add(@Nonnull Ref<EntityStore> ref, @Nonnull Vector3d parentPosition, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      if (this.validator.test(ref, commandBuffer)) {
         TransformComponent transformComponent = commandBuffer.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

         assert transformComponent != null;

         double distance = parentPosition.distanceSquaredTo(transformComponent.getPosition());
         this.add(ref, distance);
      }
   }

   public <T, U, V> void forEachEntity(
      @Nonnull DoubleQuadObjectConsumer<Ref<EntityStore>, T, U, V> consumer, T t, U u, V v, double d, ComponentAccessor<EntityStore> componentAccessor
   ) {
      for (BucketList.Bucket<Ref<EntityStore>> bucket : this.buckets) {
         if (!bucket.isEmpty()) {
            BucketItem<Ref<EntityStore>>[] entityHolders = bucket.getItems();
            int i = 0;

            for (int entityHoldersSize = bucket.size(); i < entityHoldersSize; i++) {
               Ref<EntityStore> ref = this.validateEntityRef(entityHolders[i], componentAccessor);
               if (ref != null) {
                  consumer.accept(d, ref, t, u, v);
               }
            }
         }
      }
   }

   public <T, U, V, R> void forEachEntityUnordered(
      double maxDistance,
      @Nonnull QuadPredicate<Ref<EntityStore>, T, U, ComponentAccessor<EntityStore>> predicate,
      @Nonnull QuadConsumer<Ref<EntityStore>, T, V, R> consumer,
      T t,
      U u,
      V v,
      R r,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      int maxDistanceSquared = (int)(maxDistance * maxDistance);
      int endBucket = this.getLastBucketIndex(maxDistanceSquared);

      for (int i = 0; i <= endBucket; i++) {
         BucketList.Bucket<Ref<EntityStore>> bucket = this.buckets[i];
         if (!bucket.isEmpty()) {
            BucketItem<Ref<EntityStore>>[] entityHolders = bucket.getItems();
            if (bucket.isUnsorted()) {
               int i1 = 0;

               for (int entityHoldersSize = bucket.size(); i1 < entityHoldersSize; i1++) {
                  BucketItem<Ref<EntityStore>> holder = entityHolders[i1];
                  if (holder.squaredDistance < maxDistanceSquared) {
                     Ref<EntityStore> ref = this.validateEntityRef(holder, componentAccessor);
                     if (ref != null && predicate.test(ref, t, u, componentAccessor)) {
                        consumer.accept(ref, t, v, r);
                     }
                  }
               }
            } else {
               int i1 = 0;

               for (int entityHoldersSizex = bucket.size(); i1 < entityHoldersSizex; i1++) {
                  BucketItem<Ref<EntityStore>> holder = entityHolders[i1];
                  if (holder.squaredDistance >= maxDistanceSquared) {
                     break;
                  }

                  Ref<EntityStore> ref = this.validateEntityRef(holder, componentAccessor);
                  if (ref != null && predicate.test(ref, t, u, componentAccessor)) {
                     consumer.accept(ref, t, v, r);
                  }
               }
            }
         }
      }
   }

   public <T> void forEachEntityAvoidance(
      @Nonnull Set<Ref<EntityStore>> ignoredEntitiesForAvoidance,
      @Nonnull TriConsumer<Ref<EntityStore>, T, CommandBuffer<EntityStore>> consumer,
      T t,
      CommandBuffer<EntityStore> commandBuffer
   ) {
      int endBucket = this.getLastBucketIndex(this.squaredMaxDistanceAvoidance);

      for (int i = 0; i <= endBucket; i++) {
         BucketList.Bucket<Ref<EntityStore>> bucket = this.buckets[i];
         if (!bucket.isEmpty()) {
            BucketItem<Ref<EntityStore>>[] entityHolders = bucket.getItems();
            if (bucket.isUnsorted()) {
               int i1 = 0;

               for (int entityHoldersSize = bucket.size(); i1 < entityHoldersSize; i1++) {
                  BucketItem<Ref<EntityStore>> entityHolder = entityHolders[i1];
                  if (entityHolder.squaredDistance <= this.squaredMaxDistanceAvoidance) {
                     Ref<EntityStore> ref = this.validateEntityRef(entityHolder, commandBuffer);
                     if (ref != null && !ignoredEntitiesForAvoidance.contains(ref)) {
                        consumer.accept(ref, t, commandBuffer);
                     }
                  }
               }
            } else {
               int i1 = 0;

               for (int entityHoldersSizex = bucket.size(); i1 < entityHoldersSizex; i1++) {
                  BucketItem<Ref<EntityStore>> entityHolder = entityHolders[i1];
                  if (entityHolder.squaredDistance > this.squaredMaxDistanceAvoidance) {
                     break;
                  }

                  Ref<EntityStore> ref = this.validateEntityRef(entityHolder, commandBuffer);
                  if (ref != null && !ignoredEntitiesForAvoidance.contains(ref)) {
                     consumer.accept(ref, t, commandBuffer);
                  }
               }
            }
         }
      }
   }

   public <T, U> void forEachEntityAvoidance(
      @Nonnull Set<Ref<EntityStore>> ignoredEntitiesForAvoidance,
      @Nonnull QuadConsumer<Ref<EntityStore>, T, U, CommandBuffer<EntityStore>> consumer,
      T t,
      U u,
      CommandBuffer<EntityStore> commandBuffer
   ) {
      int endBucket = this.getLastBucketIndex(this.squaredMaxDistanceAvoidance);

      for (int i = 0; i <= endBucket; i++) {
         BucketList.Bucket<Ref<EntityStore>> bucket = this.buckets[i];
         if (!bucket.isEmpty()) {
            BucketItem<Ref<EntityStore>>[] entityHolders = bucket.getItems();
            if (bucket.isUnsorted()) {
               int i1 = 0;

               for (int entityHoldersSize = bucket.size(); i1 < entityHoldersSize; i1++) {
                  BucketItem<Ref<EntityStore>> entityHolder = entityHolders[i1];
                  if (entityHolder.squaredDistance <= this.squaredMaxDistanceAvoidance) {
                     Ref<EntityStore> ref = this.validateEntityRef(entityHolder, commandBuffer);
                     if (ref != null && !ignoredEntitiesForAvoidance.contains(ref)) {
                        consumer.accept(ref, t, u, commandBuffer);
                     }
                  }
               }
            } else {
               int i1 = 0;

               for (int entityHoldersSizex = bucket.size(); i1 < entityHoldersSizex; i1++) {
                  BucketItem<Ref<EntityStore>> entityHolder = entityHolders[i1];
                  if (entityHolder.squaredDistance > this.squaredMaxDistanceAvoidance) {
                     break;
                  }

                  Ref<EntityStore> ref = this.validateEntityRef(entityHolder, commandBuffer);
                  if (ref != null && !ignoredEntitiesForAvoidance.contains(ref)) {
                     consumer.accept(ref, t, u, commandBuffer);
                  }
               }
            }
         }
      }
   }

   public <S, T> int countEntitiesInRange(
      double minRange,
      double maxRange,
      int maxCount,
      @Nonnull QuadPredicate<S, Ref<EntityStore>, T, ComponentAccessor<EntityStore>> filter,
      S s,
      T t,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      int minRangeSquared = (int)(minRange * minRange);
      int startBucket = this.getFirstBucketIndex(minRangeSquared);
      if (startBucket < 0) {
         return 0;
      } else {
         int maxRangeSquared = (int)(maxRange * maxRange);
         int endBucket = this.getLastBucketIndex(maxRangeSquared);
         int count = 0;

         for (int i = startBucket; i <= endBucket && count < maxCount; i++) {
            BucketList.Bucket<Ref<EntityStore>> bucket = this.buckets[i];
            if (!bucket.isEmpty()) {
               BucketItem<Ref<EntityStore>>[] entityHolders = bucket.getItems();
               if (bucket.isUnsorted()) {
                  int i1 = 0;

                  for (int entityHoldersSize = bucket.size(); i1 < entityHoldersSize; i1++) {
                     BucketItem<Ref<EntityStore>> entityHolder = entityHolders[i1];
                     double squaredDistance = entityHolder.squaredDistance;
                     if (!(squaredDistance < minRangeSquared) && !(squaredDistance >= maxRangeSquared)) {
                        Ref<EntityStore> ref = this.validateEntityRef(entityHolder, componentAccessor);
                        if (ref != null && filter.test(s, ref, t, componentAccessor)) {
                           if (++count >= maxCount) {
                              return count;
                           }
                        }
                     }
                  }
               } else {
                  int i1 = 0;

                  for (int entityHoldersSizex = bucket.size(); i1 < entityHoldersSizex; i1++) {
                     BucketItem<Ref<EntityStore>> entityHolder = entityHolders[i1];
                     double squaredDistance = entityHolder.squaredDistance;
                     if (!(squaredDistance < minRangeSquared)) {
                        if (squaredDistance >= maxRangeSquared) {
                           return count;
                        }

                        Ref<EntityStore> ref = this.validateEntityRef(entityHolder, componentAccessor);
                        if (ref != null && filter.test(s, ref, t, componentAccessor)) {
                           if (++count >= maxCount) {
                              return count;
                           }
                        }
                     }
                  }
               }
            }
         }

         return count;
      }
   }

   @Nullable
   public Ref<EntityStore> getClosestEntityInRange(
      double minRange, double maxRange, @Nonnull Predicate<Ref<EntityStore>> filter, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      int minRangeSquared = (int)(minRange * minRange);
      int startBucket = this.getFirstBucketIndex(minRangeSquared);
      if (startBucket < 0) {
         return null;
      } else {
         int maxRangeSquared = (int)(maxRange * maxRange);
         int endBucket = this.getLastBucketIndex(maxRangeSquared);
         BucketList.SortBufferProvider sortBufferProvider = componentAccessor.getResource(SortBufferProviderResource.getResourceType()).getSortBufferProvider();

         for (int i = startBucket; i <= endBucket; i++) {
            BucketList.Bucket<Ref<EntityStore>> bucket = this.buckets[i];
            if (!bucket.isEmpty()) {
               if (bucket.isUnsorted()) {
                  bucket.sort(sortBufferProvider);
               }

               BucketItem<Ref<EntityStore>>[] entityHolders = bucket.getItems();
               int i1 = 0;

               for (int entityHoldersSize = bucket.size(); i1 < entityHoldersSize; i1++) {
                  BucketItem<Ref<EntityStore>> holder = entityHolders[i1];
                  double squaredDistance = holder.squaredDistance;
                  if (!(squaredDistance < minRangeSquared)) {
                     if (squaredDistance >= maxRangeSquared) {
                        return null;
                     }

                     Ref<EntityStore> ref = this.validateEntityRef(holder, componentAccessor);
                     if (ref != null && filter.test(ref)) {
                        return ref;
                     }
                  }
               }
            }
         }

         return null;
      }
   }

   @Nullable
   public <S, T> Ref<EntityStore> getClosestEntityInRange(
      @Nullable Ref<EntityStore> ignoredEntityReference,
      double minRange,
      double maxRange,
      @Nonnull QuadPredicate<S, Ref<EntityStore>, Role, T> filter,
      Role role,
      S s,
      T t,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      int minRangeSquared = (int)(minRange * minRange);
      int startBucket = this.getFirstBucketIndex(minRangeSquared);
      if (startBucket < 0) {
         return null;
      } else {
         int maxRangeSquared = (int)(maxRange * maxRange);
         int endBucket = this.getLastBucketIndex(maxRangeSquared);
         BucketList.SortBufferProvider sortBufferProvider = componentAccessor.getResource(SortBufferProviderResource.getResourceType()).getSortBufferProvider();
         if (ignoredEntityReference == null) {
            for (int i = startBucket; i <= endBucket; i++) {
               BucketList.Bucket<Ref<EntityStore>> bucket = this.buckets[i];
               if (!bucket.isEmpty()) {
                  if (bucket.isUnsorted()) {
                     bucket.sort(sortBufferProvider);
                  }

                  BucketItem<Ref<EntityStore>>[] entityHolders = bucket.getItems();
                  int i1 = 0;

                  for (int entityHoldersSize = bucket.size(); i1 < entityHoldersSize; i1++) {
                     BucketItem<Ref<EntityStore>> holder = entityHolders[i1];
                     double squaredDistance = holder.squaredDistance;
                     if (!(squaredDistance < minRangeSquared)) {
                        if (squaredDistance >= maxRangeSquared) {
                           return null;
                        }

                        Ref<EntityStore> ref = this.validateEntityRef(holder, componentAccessor);
                        if (ref != null && filter.test(s, ref, role, t)) {
                           return ref;
                        }
                     }
                  }
               }
            }
         } else {
            for (int ix = startBucket; ix <= endBucket; ix++) {
               BucketList.Bucket<Ref<EntityStore>> bucket = this.buckets[ix];
               if (!bucket.isEmpty()) {
                  if (bucket.isUnsorted()) {
                     bucket.sort(sortBufferProvider);
                  }

                  BucketItem<Ref<EntityStore>>[] entityHolders = bucket.getItems();
                  int i1 = 0;

                  for (int entityHoldersSizex = bucket.size(); i1 < entityHoldersSizex; i1++) {
                     BucketItem<Ref<EntityStore>> holder = entityHolders[i1];
                     double squaredDistance = holder.squaredDistance;
                     if (!(squaredDistance < minRangeSquared)) {
                        if (squaredDistance >= maxRangeSquared) {
                           return null;
                        }

                        Ref<EntityStore> ref = this.validateEntityRef(holder, componentAccessor);
                        if (ref != null && !ref.equals(ignoredEntityReference) && filter.test(s, ref, role, t)) {
                           return ref;
                        }
                     }
                  }
               }
            }
         }

         return null;
      }
   }

   @Nullable
   public <S, T> Ref<EntityStore> getClosestEntityInRangeProjected(
      @Nonnull Ref<EntityStore> parentRef,
      @Nullable Ref<EntityStore> ignoredEntityReference,
      @Nonnull MotionController motionController,
      double minRange,
      double maxRange,
      @Nonnull QuadPredicate<S, Ref<EntityStore>, Role, T> filter,
      Role role,
      S s,
      T t,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      int minRangeSquared = (int)(minRange * minRange);
      int startBucket = this.getFirstBucketIndex(minRangeSquared);
      if (startBucket < 0) {
         return null;
      } else {
         int maxRangeSquared = (int)(maxRange * maxRange);
         int endBucket = this.getLastBucketIndex(maxRangeSquared);
         Vector3d position = componentAccessor.getComponent(parentRef, TRANSFORM_COMPONENT_TYPE).getPosition();
         BucketList.SortBufferProvider sortBufferProvider = componentAccessor.getResource(SortBufferProviderResource.getResourceType()).getSortBufferProvider();
         if (ignoredEntityReference == null) {
            for (int i = startBucket; i <= endBucket; i++) {
               BucketList.Bucket<Ref<EntityStore>> bucket = this.buckets[i];
               if (!bucket.isEmpty()) {
                  if (bucket.isUnsorted()) {
                     bucket.sort(sortBufferProvider);
                  }

                  BucketItem<Ref<EntityStore>>[] entityHolders = bucket.getItems();
                  int i1 = 0;

                  for (int entityHoldersSize = bucket.size(); i1 < entityHoldersSize; i1++) {
                     BucketItem<Ref<EntityStore>> holder = entityHolders[i1];
                     Ref<EntityStore> ref = this.validateEntityRef(holder, componentAccessor);
                     if (ref != null) {
                        double squaredDistance = motionController.waypointDistanceSquared(
                           componentAccessor.getComponent(ref, TRANSFORM_COMPONENT_TYPE).getPosition(), position
                        );
                        if (!(squaredDistance < minRangeSquared)) {
                           if (squaredDistance >= maxRangeSquared) {
                              return null;
                           }

                           if (filter.test(s, ref, role, t)) {
                              return ref;
                           }
                        }
                     }
                  }
               }
            }
         } else {
            for (int ix = startBucket; ix <= endBucket; ix++) {
               BucketList.Bucket<Ref<EntityStore>> bucket = this.buckets[ix];
               if (!bucket.isEmpty()) {
                  if (bucket.isUnsorted()) {
                     bucket.sort(sortBufferProvider);
                  }

                  BucketItem<Ref<EntityStore>>[] entityHolders = bucket.getItems();
                  int i1 = 0;

                  for (int entityHoldersSizex = bucket.size(); i1 < entityHoldersSizex; i1++) {
                     BucketItem<Ref<EntityStore>> holder = entityHolders[i1];
                     Ref<EntityStore> ref = this.validateEntityRef(holder, componentAccessor);
                     if (ref != null && !ref.equals(ignoredEntityReference)) {
                        double squaredDistance = motionController.waypointDistanceSquared(
                           componentAccessor.getComponent(ref, TRANSFORM_COMPONENT_TYPE).getPosition(), position
                        );
                        if (!(squaredDistance < minRangeSquared)) {
                           if (squaredDistance >= maxRangeSquared) {
                              return null;
                           }

                           if (filter.test(s, ref, role, t)) {
                              return ref;
                           }
                        }
                     }
                  }
               }
            }
         }

         return null;
      }
   }

   public <S, T> boolean testAnyEntity(
      double maxDistance,
      @Nonnull QuadObjectDoublePredicate<S, Ref<EntityStore>, T, ComponentAccessor<EntityStore>> predicate,
      S s,
      T t,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      return this.testAnyEntityDistanceSquared(maxDistance * maxDistance, predicate, s, t, maxDistance, componentAccessor);
   }

   public <S, T> boolean testAnyEntityDistanceSquared(
      double maxDistanceSquared,
      @Nonnull QuadObjectDoublePredicate<S, Ref<EntityStore>, T, ComponentAccessor<EntityStore>> predicate,
      S s,
      T t,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      return this.testAnyEntityDistanceSquared(maxDistanceSquared, predicate, s, t, maxDistanceSquared, componentAccessor);
   }

   public <S, T> boolean testAnyEntityDistanceSquared(
      double maxDistanceSquared,
      @Nonnull QuadObjectDoublePredicate<S, Ref<EntityStore>, T, ComponentAccessor<EntityStore>> predicate,
      S s,
      T t,
      double d,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      int endBucket = this.getLastBucketIndex((int)maxDistanceSquared);

      for (int i = 0; i <= endBucket; i++) {
         BucketList.Bucket<Ref<EntityStore>> bucket = this.buckets[i];
         if (!bucket.isEmpty()) {
            BucketItem<Ref<EntityStore>>[] entityHolders = bucket.getItems();
            if (bucket.isUnsorted()) {
               int i1 = 0;

               for (int entityHoldersSize = bucket.size(); i1 < entityHoldersSize; i1++) {
                  BucketItem<Ref<EntityStore>> entityHolder = entityHolders[i1];
                  if (entityHolder.squaredDistance < maxDistanceSquared) {
                     Ref<EntityStore> ref = this.validateEntityRef(entityHolder, componentAccessor);
                     if (ref != null && predicate.test(s, ref, t, componentAccessor, d)) {
                        return true;
                     }
                  }
               }

               return false;
            }

            int i1 = 0;

            for (int entityHoldersSizex = bucket.size(); i1 < entityHoldersSizex; i1++) {
               BucketItem<Ref<EntityStore>> entityHolder = entityHolders[i1];
               if (entityHolder.squaredDistance >= maxDistanceSquared) {
                  break;
               }

               Ref<EntityStore> ref = this.validateEntityRef(entityHolder, componentAccessor);
               if (ref != null && predicate.test(s, ref, t, componentAccessor, d)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   @Nullable
   protected Ref<EntityStore> validateEntityRef(@Nonnull BucketItem<Ref<EntityStore>> holder, ComponentAccessor<EntityStore> componentAccessor) {
      Ref<EntityStore> ref = holder.item;
      return ref != null && ref.isValid() && this.validator.test(ref, componentAccessor) ? ref : null;
   }
}
