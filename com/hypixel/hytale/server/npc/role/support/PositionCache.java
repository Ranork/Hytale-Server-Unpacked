package com.hypixel.hytale.server.npc.role.support;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.function.consumer.DoubleQuadObjectConsumer;
import com.hypixel.hytale.function.consumer.QuadConsumer;
import com.hypixel.hytale.function.consumer.TriConsumer;
import com.hypixel.hytale.function.predicate.QuadPredicate;
import com.hypixel.hytale.math.iterator.BlockIterator;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.Opacity;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.blockset.BlockSetModule;
import com.hypixel.hytale.server.core.modules.collision.CollisionMath;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PositionCache {
   public static final BiPredicate<Ref<EntityStore>, ComponentAccessor<EntityStore>> IS_VALID_PLAYER = (ref, componentAccessor) -> {
      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());
      if (playerComponent != null && !playerComponent.isWaitingForClientReady()) {
         if (playerComponent.getGameMode() == GameMode.Adventure) {
            return true;
         } else if (playerComponent.getGameMode() != GameMode.Creative) {
            return false;
         } else {
            PlayerSettings playerSettingsComponent = componentAccessor.getComponent(ref, PlayerSettings.getComponentType());
            return playerSettingsComponent != null && playerSettingsComponent.creativeSettings().allowNPCDetection();
         }
      } else {
         return false;
      }
   };
   public static final BiPredicate<Ref<EntityStore>, ComponentAccessor<EntityStore>> IS_VALID_NPC = (ref, accessor) -> accessor.getArchetype(ref)
      .contains(NPCEntity.getComponentType());
   public static final double MIN_LOS_BLOCKING_DISTANCE_SQUARED = 1.0E-6;
   public static final String FUNCTION_CAN_BE_ONLY_CALLED_WHILE_CONFIGURING_POSITION_CACHE = "function can be only called while configuring PositionCache";
   private static final float LOS_CACHE_TTL_MIN_SECONDS = 0.09F;
   private static final float LOS_CACHE_TTL_MAX_SECONDS = 0.11F;
   private static final float POSITION_CACHE_TTL_SECONDS = 0.2F;
   private static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   private static final ComponentType<EntityStore, ItemComponent> ITEM_COMPONENT_TYPE = ItemComponent.getComponentType();
   private static final ComponentType<EntityStore, ModelComponent> MODEL_COMPONENT_TYPE = ModelComponent.getComponentType();
   protected static final ComponentType<EntityStore, BoundingBox> BOUNDING_BOX_COMPONENT_TYPE = BoundingBox.getComponentType();
   private double maxDroppedItemDistance;
   private double maxSpawnMarkerDistance;
   private int maxSpawnBeaconDistance;
   @Nonnull
   private final Role role;
   private int opaqueBlockSet;
   protected EntityList players;
   protected EntityList npcs;
   protected final List<Consumer<Role>> externalRegistrations = new ObjectArrayList();
   private final List<Ref<EntityStore>> droppedItems = new ObjectArrayList();
   private final List<Ref<EntityStore>> spawnMarkers = new ObjectArrayList();
   private final List<Ref<EntityStore>> spawnBeacons = new ObjectArrayList();
   private final Object2ByteMap<Ref<EntityStore>> lineOfSightCache = new Object2ByteOpenHashMap();
   private final Object2ByteMap<Ref<EntityStore>> inverseLineOfSightCache = new Object2ByteOpenHashMap();
   private final Object2ByteMap<Ref<EntityStore>> friendlyFireCache = new Object2ByteOpenHashMap();
   protected final PositionCache.LineOfSightBuffer lineOfSightComputeBuffer = new PositionCache.LineOfSightBuffer();
   protected final PositionCache.LineOfSightEntityBuffer lineOfSightEntityComputeBuffer = new PositionCache.LineOfSightEntityBuffer();
   private float cacheTTL = 0.09F;
   private float positionCacheNextUpdate;
   private boolean isBenchmarking;
   private boolean isConfiguring;
   private boolean couldBreathe = true;

   public PositionCache(@Nonnull Role role) {
      this.role = role;
      this.players = new EntityList(null, IS_VALID_PLAYER);
      this.npcs = new EntityList(null, IS_VALID_NPC);
   }

   public boolean isBenchmarking() {
      return this.isBenchmarking;
   }

   public void setBenchmarking(boolean benchmarking) {
      this.isBenchmarking = benchmarking;
   }

   public void setCouldBreathe(boolean couldBreathe) {
      this.couldBreathe = couldBreathe;
   }

   public EntityList getPlayers() {
      return this.players;
   }

   public EntityList getNpcs() {
      return this.npcs;
   }

   public boolean tickPositionCacheNextUpdate(float dt) {
      return (this.positionCacheNextUpdate -= dt) <= 0.0F;
   }

   public void resetPositionCacheNextUpdate() {
      this.positionCacheNextUpdate = 0.2F;
   }

   public double getMaxDroppedItemDistance() {
      return this.maxDroppedItemDistance;
   }

   public double getMaxSpawnMarkerDistance() {
      return this.maxSpawnMarkerDistance;
   }

   public int getMaxSpawnBeaconDistance() {
      return this.maxSpawnBeaconDistance;
   }

   public void addExternalPositionCacheRegistration(Consumer<Role> registration) {
      this.externalRegistrations.add(registration);
   }

   @Nonnull
   public List<Consumer<Role>> getExternalRegistrations() {
      return this.externalRegistrations;
   }

   public void reset(boolean isConfiguring) {
      this.players.reset();
      this.npcs.reset();
      this.maxDroppedItemDistance = 0.0;
      this.droppedItems.clear();
      this.spawnMarkers.clear();
      this.spawnBeacons.clear();
      this.positionCacheNextUpdate = RandomExtra.randomRange(0.0F, 0.2F);
      this.clearLineOfSightCache();
      this.isConfiguring = isConfiguring;
   }

   public void finalizeConfiguration() {
      this.isConfiguring = false;
      this.npcs.finalizeConfiguration();
      this.players.finalizeConfiguration();
      RoleStats roleStats = this.role.getRoleStats();
      if (roleStats != null) {
         roleStats.trackBuckets(false, this.npcs.getBucketRanges());
         roleStats.trackBuckets(true, this.players.getBucketRanges());
      }
   }

   public void clear(double tickTime) {
      this.clearLineOfSightCache(tickTime);
      if (this.isBenchmarking) {
         NPCPlugin.get().collectSensorSupportTickDone(this.role.getRoleIndex());
      }

      this.isBenchmarking = false;
   }

   public boolean couldBreatheCached() {
      return this.couldBreathe;
   }

   public <T, U, V> void forEachPlayer(
      @Nonnull DoubleQuadObjectConsumer<Ref<EntityStore>, T, U, V> consumer, T t, U u, V v, double d, ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.players.forEachEntity(consumer, t, u, v, d, componentAccessor);
   }

   @Nullable
   public Ref<EntityStore> getClosestPlayerInRange(double minRange, double maxRange, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return this.getClosestPlayerInRange(minRange, maxRange, p -> true, componentAccessor);
   }

   @Nullable
   public Ref<EntityStore> getClosestPlayerInRange(
      double minRange, double maxRange, @Nonnull Predicate<Ref<EntityStore>> filter, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return this.players.getClosestEntityInRange(minRange, maxRange, filter, componentAccessor);
   }

   @Nullable
   public Ref<EntityStore> getClosestNPCInRange(
      double minRange, double maxRange, @Nonnull Predicate<Ref<EntityStore>> filter, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return this.npcs.getClosestEntityInRange(minRange, maxRange, filter, componentAccessor);
   }

   public <S, T> void processNPCsInRange(
      @Nonnull Ref<EntityStore> ref,
      double minRange,
      double maxRange,
      boolean useProjectedDistance,
      Ref<EntityStore> ignoredEntityReference,
      @Nonnull Role role,
      @Nonnull QuadPredicate<S, Ref<EntityStore>, Role, T> filter,
      S s,
      T t,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.processEntitiesInRange(ref, this.npcs, minRange, maxRange, useProjectedDistance, ignoredEntityReference, role, filter, s, t, componentAccessor);
   }

   public <S, T> void processPlayersInRange(
      @Nonnull Ref<EntityStore> ref,
      double minRange,
      double maxRange,
      boolean useProjectedDistance,
      Ref<EntityStore> ignoredEntityReference,
      @Nonnull Role role,
      @Nonnull QuadPredicate<S, Ref<EntityStore>, Role, T> filter,
      S s,
      T t,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.processEntitiesInRange(ref, this.players, minRange, maxRange, useProjectedDistance, ignoredEntityReference, role, filter, s, t, componentAccessor);
   }

   public <S, T> void processEntitiesInRange(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull EntityList entities,
      double minRange,
      double maxRange,
      boolean useProjectedDistance,
      Ref<EntityStore> ignoredEntityReference,
      @Nonnull Role role,
      @Nonnull QuadPredicate<S, Ref<EntityStore>, Role, T> filter,
      S s,
      T t,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (useProjectedDistance) {
         entities.getClosestEntityInRangeProjected(
            ref, ignoredEntityReference, role.getActiveMotionController(), minRange, maxRange, filter, role, s, t, componentAccessor
         );
      } else {
         entities.getClosestEntityInRange(ignoredEntityReference, minRange, maxRange, filter, role, s, t, componentAccessor);
      }
   }

   @Nullable
   public <S> Ref<EntityStore> getClosestDroppedItemInRange(
      @Nonnull Ref<EntityStore> ref,
      double minRange,
      double maxRange,
      @Nonnull QuadPredicate<S, Ref<EntityStore>, Role, ComponentAccessor<EntityStore>> filter,
      Role role,
      S s,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      int droppedItemsSize = this.droppedItems.size();
      if (droppedItemsSize == 0) {
         return null;
      } else {
         TransformComponent transformComponent = componentAccessor.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

         assert transformComponent != null;

         Vector3d position = transformComponent.getPosition();
         minRange *= minRange;
         maxRange *= maxRange;

         for (int index = 0; index < droppedItemsSize; index++) {
            Ref<EntityStore> itemEntityRef = this.droppedItems.get(index);
            if (itemEntityRef.isValid()) {
               ItemComponent itemComponent = componentAccessor.getComponent(itemEntityRef, ITEM_COMPONENT_TYPE);
               if (itemComponent != null) {
                  TransformComponent itemEntityTransformComponent = componentAccessor.getComponent(itemEntityRef, TRANSFORM_COMPONENT_TYPE);

                  assert itemEntityTransformComponent != null;

                  double squaredDistance = itemEntityTransformComponent.getPosition().distanceSquaredTo(position);
                  if (!(squaredDistance < minRange)) {
                     if (squaredDistance >= maxRange) {
                        break;
                     }

                     if (filter.test(s, itemEntityRef, role, componentAccessor)) {
                        return itemEntityRef;
                     }
                  }
               }
            }
         }

         return null;
      }
   }

   public <S> boolean isEntityCountInRange(
      double minRange,
      double maxRange,
      int minCount,
      int maxCount,
      boolean findPlayers,
      Role role,
      @Nonnull QuadPredicate<S, Ref<EntityStore>, Role, ComponentAccessor<EntityStore>> filter,
      S s,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      int count = 0;
      if (findPlayers) {
         count = this.players.countEntitiesInRange(minRange, maxRange, maxCount + 1, filter, s, role, componentAccessor);
         if (count > maxCount) {
            return false;
         }
      }

      count += this.npcs.countEntitiesInRange(minRange, maxRange, maxCount - count + 1, filter, s, role, componentAccessor);
      return count >= minCount && count <= maxCount;
   }

   public <S, T> int countEntitiesInRange(
      double minRange,
      double maxRange,
      boolean findPlayers,
      @Nonnull QuadPredicate<S, Ref<EntityStore>, T, ComponentAccessor<EntityStore>> filter,
      S s,
      T t,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      int count = 0;
      if (findPlayers) {
         count = this.players.countEntitiesInRange(minRange, maxRange, Integer.MAX_VALUE, filter, s, t, componentAccessor);
      }

      return count + this.npcs.countEntitiesInRange(minRange, maxRange, Integer.MAX_VALUE, filter, s, t, componentAccessor);
   }

   public void requirePlayerDistanceSorted(double v) {
      int value = MathUtil.ceil(v);
      if (!this.isConfiguring) {
         throw new IllegalStateException("function can be only called while configuring PositionCache");
      } else {
         this.players.requireDistanceSorted(value);
         RoleStats roleStats = this.role.getRoleStats();
         if (roleStats != null) {
            roleStats.trackRange(true, RoleStats.RangeType.SORTED, value);
         }
      }
   }

   public void requirePlayerDistanceUnsorted(double v) {
      int value = MathUtil.ceil(v);
      if (!this.isConfiguring) {
         throw new IllegalStateException("function can be only called while configuring PositionCache");
      } else {
         this.players.requireDistanceUnsorted(value);
         RoleStats roleStats = this.role.getRoleStats();
         if (roleStats != null) {
            roleStats.trackRange(true, RoleStats.RangeType.UNSORTED, value);
         }
      }
   }

   public void requirePlayerDistanceAvoidance(double v) {
      int value = MathUtil.ceil(v);
      if (!this.isConfiguring) {
         throw new IllegalStateException("function can be only called while configuring PositionCache");
      } else {
         this.players.requireDistanceAvoidance(value);
         RoleStats roleStats = this.role.getRoleStats();
         if (roleStats != null) {
            roleStats.trackRange(true, RoleStats.RangeType.AVOIDANCE, value);
         }
      }
   }

   public void requireEntityDistanceSorted(double v) {
      int value = MathUtil.ceil(v);
      if (!this.isConfiguring) {
         throw new IllegalStateException("function can be only called while configuring PositionCache");
      } else {
         this.npcs.requireDistanceSorted(value);
         RoleStats roleStats = this.role.getRoleStats();
         if (roleStats != null) {
            roleStats.trackRange(false, RoleStats.RangeType.SORTED, value);
         }
      }
   }

   public void requireEntityDistanceUnsorted(double v) {
      int value = MathUtil.ceil(v);
      if (!this.isConfiguring) {
         throw new IllegalStateException("function can be only called while configuring PositionCache");
      } else {
         this.npcs.requireDistanceUnsorted(value);
         RoleStats roleStats = this.role.getRoleStats();
         if (roleStats != null) {
            roleStats.trackRange(false, RoleStats.RangeType.UNSORTED, value);
         }
      }
   }

   public void requireEntityDistanceAvoidance(double v) {
      int value = MathUtil.ceil(v);
      if (!this.isConfiguring) {
         throw new IllegalStateException("function can be only called while configuring PositionCache");
      } else {
         value = this.npcs.requireDistanceAvoidance(value);
         RoleStats roleStats = this.role.getRoleStats();
         if (roleStats != null) {
            roleStats.trackRange(false, RoleStats.RangeType.AVOIDANCE, value);
         }
      }
   }

   public void requireDroppedItemDistance(double value) {
      if (this.maxDroppedItemDistance < value) {
         this.maxDroppedItemDistance = value;
      }
   }

   public void requireSpawnMarkerDistance(double value) {
      if (this.maxSpawnMarkerDistance < value) {
         this.maxSpawnMarkerDistance = value;
      }
   }

   public void requireSpawnBeaconDistance(int value) {
      if (this.maxSpawnBeaconDistance < value) {
         this.maxSpawnBeaconDistance = value;
      }
   }

   @Nonnull
   public Role getRole() {
      return this.role;
   }

   public <T, U, V, R> void forEachNPCUnordered(
      double maxDistance,
      @Nonnull QuadPredicate<Ref<EntityStore>, T, U, ComponentAccessor<EntityStore>> predicate,
      @Nonnull QuadConsumer<Ref<EntityStore>, T, V, R> consumer,
      T t,
      U u,
      V v,
      R r,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.npcs.forEachEntityUnordered(maxDistance, predicate, consumer, t, u, v, r, componentAccessor);
   }

   public <T> void forEachEntityInAvoidanceRange(
      @Nonnull Set<Ref<EntityStore>> ignoredEntitiesForAvoidance,
      @Nonnull TriConsumer<Ref<EntityStore>, T, CommandBuffer<EntityStore>> consumer,
      T t,
      CommandBuffer<EntityStore> commandBuffer
   ) {
      this.npcs.forEachEntityAvoidance(ignoredEntitiesForAvoidance, consumer, t, commandBuffer);
      this.players.forEachEntityAvoidance(ignoredEntitiesForAvoidance, consumer, t, commandBuffer);
   }

   public <T, U> void forEachEntityInAvoidanceRange(
      @Nonnull Set<Ref<EntityStore>> ignoredEntitiesForAvoidance,
      @Nonnull QuadConsumer<Ref<EntityStore>, T, U, CommandBuffer<EntityStore>> consumer,
      T t,
      U u,
      CommandBuffer<EntityStore> commandBuffer
   ) {
      this.npcs.forEachEntityAvoidance(ignoredEntitiesForAvoidance, consumer, t, u, commandBuffer);
      this.players.forEachEntityAvoidance(ignoredEntitiesForAvoidance, consumer, t, u, commandBuffer);
   }

   public void setOpaqueBlockSet(int blockSet) {
      this.opaqueBlockSet = blockSet;
   }

   private static <T> boolean testLineOfSightRays(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Ref<EntityStore> targetRef,
      @Nonnull PositionCache.RayPredicate<T> predicate,
      @Nonnull T t,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      ModelComponent modelComponent = componentAccessor.getComponent(ref, MODEL_COMPONENT_TYPE);
      float eyeHeight = modelComponent != null ? modelComponent.getModel().getEyeHeight() : 0.0F;
      double sx = position.getX();
      double sy = position.getY() + eyeHeight;
      double sz = position.getZ();
      TransformComponent targetTransformComponent = componentAccessor.getComponent(targetRef, TRANSFORM_COMPONENT_TYPE);

      assert targetTransformComponent != null;

      Vector3d targetPosition = targetTransformComponent.getPosition();
      double tx = targetPosition.getX();
      double ty = targetPosition.getY();
      double tz = targetPosition.getZ();
      ModelComponent targetModelComponent = componentAccessor.getComponent(targetRef, MODEL_COMPONENT_TYPE);
      if (targetModelComponent != null) {
         return predicate.test(sx, sy, sz, tx, ty + targetModelComponent.getModel().getEyeHeight(), tz, t, componentAccessor);
      } else {
         double ox = 0.0;
         double oy = 0.0;
         double oz = 0.0;
         BoundingBox boundingBoxComponent = componentAccessor.getComponent(targetRef, BOUNDING_BOX_COMPONENT_TYPE);
         if (boundingBoxComponent != null) {
            Box boundingBox = boundingBoxComponent.getBoundingBox();
            ox = (boundingBox.getMax().getX() + boundingBox.getMin().getX()) / 2.0;
            oy = (boundingBox.getMax().getY() + boundingBox.getMin().getY()) / 2.0;
            oz = (boundingBox.getMax().getZ() + boundingBox.getMin().getZ()) / 2.0;
         }

         return predicate.test(sx, sy, sz, tx + ox, ty + oy, tz + oz, t, componentAccessor);
      }
   }

   private boolean hasLineOfSightInternal(
      @Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (ref.equals(targetRef)) {
         return false;
      } else {
         TransformComponent transformComponent = componentAccessor.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

         assert transformComponent != null;

         TransformComponent targetTransformComponent = componentAccessor.getComponent(targetRef, TRANSFORM_COMPONENT_TYPE);

         assert targetTransformComponent != null;

         if (transformComponent.getPosition().distanceSquaredTo(targetTransformComponent.getPosition()) <= 1.0E-12) {
            return true;
         } else {
            World world = componentAccessor.getExternalData().getWorld();
            Objects.requireNonNull(world, "World can't be null in isLOS");
            Int2ObjectMap<IntSet> blockSets = BlockSetModule.getInstance().getBlockSets();
            IntSet opaqueSet = this.opaqueBlockSet >= 0 && blockSets != null ? (IntSet)blockSets.get(this.opaqueBlockSet) : null;

            boolean var9;
            try {
               this.lineOfSightComputeBuffer.result = true;
               this.lineOfSightComputeBuffer.assetMap = BlockType.getAssetMap();
               this.lineOfSightComputeBuffer.opaqueSet = opaqueSet;
               this.lineOfSightComputeBuffer.world = world;
               var9 = testLineOfSightRays(
                  ref,
                  targetRef,
                  (sx, sy, sz, tx, ty, tz, buffer, accessor) -> {
                     buffer.chunk = buffer.world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(sx, sz));
                     if (buffer.chunk == null) {
                        return false;
                     } else {
                        BlockIterator.iterateFromTo(
                           sx,
                           sy,
                           sz,
                           tx,
                           ty,
                           tz,
                           (x, y, z, px, py, pz, qx, qy, qz, iBuffer) -> {
                              if (!ChunkUtil.isInsideChunk(iBuffer.chunk.getX(), iBuffer.chunk.getZ(), x, z)) {
                                 iBuffer.chunk = iBuffer.world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(x, z));
                                 if (iBuffer.chunk == null) {
                                    iBuffer.result = false;
                                    return false;
                                 }
                              }

                              int blockId = iBuffer.chunk.getBlock(x, y, z);
                              if (blockId == 0) {
                                 return true;
                              } else {
                                 BlockType blockType = iBuffer.assetMap.getAsset(blockId);
                                 if (blockType != BlockType.UNKNOWN
                                    && blockType.getOpacity() != null
                                    && blockType.getOpacity() == Opacity.Transparent
                                    && (iBuffer.opaqueSet == null || !iBuffer.opaqueSet.contains(blockId))) {
                                    return true;
                                 } else {
                                    iBuffer.result = false;
                                    return false;
                                 }
                              }
                           },
                           buffer
                        );
                        return buffer.result;
                     }
                  },
                  this.lineOfSightComputeBuffer,
                  componentAccessor
               );
            } finally {
               this.lineOfSightComputeBuffer.clearRefs();
            }

            return var9;
         }
      }
   }

   public boolean hasLineOfSight(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      boolean cached = this.lineOfSightCache.containsKey(targetRef);
      if (cached) {
         if (this.isBenchmarking) {
            NPCPlugin.get().collectSensorSupportLosTest(this.role.getRoleIndex(), true, 0L);
         }

         return this.lineOfSightCache.getByte(targetRef) != 0;
      } else {
         boolean hasLineOfSight;
         if (this.isBenchmarking) {
            long start = System.nanoTime();
            hasLineOfSight = this.hasLineOfSightInternal(ref, targetRef, componentAccessor);
            NPCPlugin.get().collectSensorSupportLosTest(this.role.getRoleIndex(), false, System.nanoTime() - start);
         } else {
            hasLineOfSight = this.hasLineOfSightInternal(ref, targetRef, componentAccessor);
         }

         this.lineOfSightCache.put(targetRef, (byte)(hasLineOfSight ? 1 : 0));
         return hasLineOfSight;
      }
   }

   public boolean hasInverseLineOfSight(
      @Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      boolean cached = this.inverseLineOfSightCache.containsKey(targetRef);
      if (this.isBenchmarking) {
         NPCPlugin.get().collectSensorSupportInverseLosTest(this.role.getRoleIndex(), cached);
      }

      if (cached) {
         return this.inverseLineOfSightCache.getByte(targetRef) != 0;
      } else {
         boolean hasLineOfSight = this.hasLineOfSightInternal(targetRef, ref, componentAccessor);
         this.inverseLineOfSightCache.put(targetRef, (byte)(hasLineOfSight ? 1 : 0));
         return hasLineOfSight;
      }
   }

   public boolean isFriendlyBlockingLineOfSight(
      @Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      boolean cached = this.friendlyFireCache.containsKey(targetRef);
      if (this.isBenchmarking) {
         NPCPlugin.get().collectSensorSupportFriendlyBlockingTest(this.role.getRoleIndex(), cached);
      }

      if (cached) {
         return this.friendlyFireCache.getByte(targetRef) != 0;
      } else {
         boolean blocking = testLineOfSightRays(
            ref,
            targetRef,
            (sx, sy, sz, tx, ty, tz, _this, accessor) -> {
               PositionCache.LineOfSightEntityBuffer buffer = _this.lineOfSightEntityComputeBuffer;
               buffer.pos.assign(sx, sy, sz);
               buffer.dir.assign(tx - sx, ty - sy, tz - sz);
               double squaredLength = buffer.dir.squaredLength();
               return squaredLength < 1.0E-6
                  ? false
                  : _this.players
                        .testAnyEntityDistanceSquared(
                           squaredLength,
                           (positionCache, targetRef1, buffer1, componentAccessor1, length2) -> positionCache.testLineOfSightEntity(
                              ref, targetRef1, buffer1, componentAccessor1, length2
                           ),
                           _this,
                           buffer,
                           accessor
                        )
                     || _this.npcs
                        .testAnyEntityDistanceSquared(
                           squaredLength,
                           (positionCache1, targetRef2, buffer2, componentAccessor2, length3) -> positionCache1.testLineOfSightEntity(
                              ref, targetRef2, buffer2, componentAccessor2, length3
                           ),
                           _this,
                           buffer,
                           accessor
                        );
            },
            this,
            componentAccessor
         );
         this.friendlyFireCache.put(targetRef, (byte)(blocking ? 1 : 0));
         return blocking;
      }
   }

   private boolean testLineOfSightEntity(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Ref<EntityStore> targetRef,
      @Nonnull PositionCache.LineOfSightEntityBuffer buffer,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      double length2
   ) {
      return !targetRef.equals(ref)
         && this.role.isFriendly(targetRef, componentAccessor)
         && rayIsIntersectingEntity(targetRef, buffer.pos, buffer.dir, buffer.minMax, length2, componentAccessor);
   }

   private void clearLineOfSightCache(double tickTime) {
      this.cacheTTL = (float)(this.cacheTTL - tickTime);
      if (this.cacheTTL <= 0.0F) {
         this.clearLineOfSightCache();
      }
   }

   private void clearLineOfSightCache() {
      this.cacheTTL = RandomExtra.randomRange(0.09F, 0.11F);
      this.lineOfSightCache.clear();
      this.inverseLineOfSightCache.clear();
      this.friendlyFireCache.clear();
   }

   protected static boolean rayIsIntersectingEntity(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Vector3d pos,
      @Nonnull Vector3d dir,
      @Nonnull Vector2d minMax,
      double length2,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      BoundingBox boundingBoxComponent = componentAccessor.getComponent(ref, BOUNDING_BOX_COMPONENT_TYPE);
      if (boundingBoxComponent == null) {
         return false;
      } else {
         TransformComponent transformComponent = componentAccessor.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

         assert transformComponent != null;

         Vector3d position = transformComponent.getPosition();
         double px = position.getX();
         double py = position.getY();
         double pz = position.getZ();
         double dx = px - pos.x;
         double dy = py - pos.y;
         double dz = pz - pos.z;
         double dotProduct = NPCPhysicsMath.dotProduct(dir.x, dir.y, dir.z, dx, dy, dz);
         if (dotProduct <= 0.0) {
            return false;
         } else {
            double dist2 = NPCPhysicsMath.dotProduct(dx, dy, dz);
            return dotProduct * dotProduct >= dist2 * length2
               ? false
               : CollisionMath.intersectRayAABB(pos, dir, px, py, pz, boundingBoxComponent.getBoundingBox(), minMax);
         }
      }
   }

   @Nonnull
   public List<Ref<EntityStore>> getDroppedItemList() {
      return this.droppedItems;
   }

   @Nonnull
   public List<Ref<EntityStore>> getSpawnMarkerList() {
      return this.spawnMarkers;
   }

   @Nonnull
   public List<Ref<EntityStore>> getSpawnBeaconList() {
      return this.spawnBeacons;
   }

   private static class LineOfSightBuffer {
      @Nullable
      public World world;
      @Nullable
      public WorldChunk chunk;
      @Nullable
      public IntSet opaqueSet;
      @Nullable
      public BlockTypeAssetMap<String, BlockType> assetMap;
      public boolean result;

      public void clearRefs() {
         this.world = null;
         this.chunk = null;
         this.opaqueSet = null;
         this.assetMap = null;
      }
   }

   private static class LineOfSightEntityBuffer {
      public final Vector3d pos = new Vector3d();
      public final Vector3d dir = new Vector3d();
      public final Vector2d minMax = new Vector2d();
   }

   @FunctionalInterface
   public interface RayPredicate<T> {
      boolean test(double var1, double var3, double var5, double var7, double var9, double var11, T var13, @Nonnull ComponentAccessor<EntityStore> var14);
   }
}
