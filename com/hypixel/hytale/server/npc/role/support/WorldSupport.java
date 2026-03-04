package com.hypixel.hytale.server.npc.role.support;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.builtin.tagset.TagSetPlugin;
import com.hypixel.hytale.builtin.tagset.config.NPCGroup;
import com.hypixel.hytale.builtin.weather.resources.WeatherResource;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderManager;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.blackboard.view.attitude.AttitudeView;
import com.hypixel.hytale.server.npc.blackboard.view.attitude.ItemAttitudeMap;
import com.hypixel.hytale.server.npc.corecomponents.BlockTarget;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.builders.BuilderRole;
import com.hypixel.hytale.server.npc.util.AttitudeMemoryEntry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldSupport {
   public static final double ATTITUDE_CACHE_CLEAR_FREQUENCY = 0.1;
   protected static final ResourceType<EntityStore, Blackboard> BLACKBOARD_RESOURCE_TYPE = Blackboard.getResourceType();
   protected final NPCEntity parent;
   protected Int2ObjectMap<BlockTarget> blockSensorCachedTargets;
   @Nullable
   protected Vector3d[] searchRayCachedPositions;
   protected String blockToPlace;
   protected final Attitude defaultPlayerAttitude;
   protected final Attitude defaultNPCAttitude;
   protected final int attitudeGroup;
   protected final int itemAttitudeGroup;
   protected AttitudeView attitudeView;
   protected Int2ObjectMap<Attitude> attitudeCache;
   protected Int2ObjectMap<AttitudeMemoryEntry> attitudeOverrideMemory;
   protected double nextAttitudeCacheClear = 0.1;
   protected boolean newPathRequested;
   protected int changeCount;
   protected int environmentIdChangeCount;
   protected int cachedEnvironmentId = Integer.MIN_VALUE;
   protected int weatherChangeCount;
   protected int cachedWeatherIndex;

   public WorldSupport(NPCEntity parent, @Nonnull BuilderRole builder, @Nonnull BuilderSupport support) {
      this.parent = parent;
      this.defaultPlayerAttitude = builder.getDefaultPlayerAttitude(support);
      this.defaultNPCAttitude = builder.getDefaultNPCAttitude(support);
      this.attitudeGroup = builder.getAttitudeGroup(support);
      this.itemAttitudeGroup = builder.getItemAttitudeGroup(support);
   }

   public void tick(float dt) {
      if (this.attitudeOverrideMemory != null && !this.attitudeOverrideMemory.isEmpty()) {
         ObjectIterator<AttitudeMemoryEntry> iterator = this.attitudeOverrideMemory.values().iterator();

         while (iterator.hasNext()) {
            AttitudeMemoryEntry entry = (AttitudeMemoryEntry)iterator.next();
            entry.tick(dt);
            if (entry.isExpired()) {
               iterator.remove();
            }
         }
      }

      if (this.attitudeCache != null && (this.nextAttitudeCacheClear -= dt) <= 0.0) {
         this.attitudeCache.clear();
         this.nextAttitudeCacheClear = 0.1;
      }

      this.changeCount++;
   }

   public void postRoleBuilt(@Nonnull BuilderSupport support) {
      if (support.requiresBlockTypeBlackboard()) {
         IntList blackboardBlockSets = support.getBlockTypeBlackboardBlockSets();
         Int2ObjectOpenHashMap<BlockTarget> cachedTargets = new Int2ObjectOpenHashMap(blackboardBlockSets.size());

         for (int i = 0; i < blackboardBlockSets.size(); i++) {
            cachedTargets.put(blackboardBlockSets.getInt(i), new BlockTarget());
         }

         cachedTargets.trim();
         this.blockSensorCachedTargets = Int2ObjectMaps.unmodifiable(cachedTargets);
         this.parent.addBlackboardBlockTypeSets(blackboardBlockSets);
      }

      if (support.requiresAttitudeOverrideMemory()) {
         this.attitudeOverrideMemory = new Int2ObjectOpenHashMap();
      }

      this.searchRayCachedPositions = support.allocateSearchRayPositionSlots();
   }

   public BlockTarget getCachedBlockTarget(int blockSet) {
      return (BlockTarget)this.blockSensorCachedTargets.get(blockSet);
   }

   public void resetBlockSensorFoundBlock(int blockSet) {
      ((BlockTarget)this.blockSensorCachedTargets.get(blockSet)).reset(this.parent);
   }

   public void resetAllBlockSensors() {
      if (this.blockSensorCachedTargets != null) {
         ObjectIterator<Entry<BlockTarget>> it = Int2ObjectMaps.fastIterator(this.blockSensorCachedTargets);

         while (it.hasNext()) {
            Entry<BlockTarget> next = (Entry<BlockTarget>)it.next();
            ((BlockTarget)next.getValue()).reset(this.parent);
         }
      }
   }

   public Vector3d getCachedSearchRayPosition(int id) {
      return this.searchRayCachedPositions[id];
   }

   public void resetCachedSearchRayPosition(int id) {
      this.searchRayCachedPositions[id].assign(Vector3d.MIN);
   }

   public void resetAllCachedSearchRayPositions() {
      for (Vector3d cachedPosition : this.searchRayCachedPositions) {
         cachedPosition.assign(Vector3d.MIN);
      }
   }

   public void setBlockToPlace(String block) {
      this.blockToPlace = block;
   }

   public String getBlockToPlace() {
      return this.blockToPlace;
   }

   public Attitude getDefaultPlayerAttitude() {
      return this.defaultPlayerAttitude;
   }

   public Attitude getDefaultNPCAttitude() {
      return this.defaultNPCAttitude;
   }

   public int getAttitudeGroup() {
      return this.attitudeGroup;
   }

   public int getItemAttitudeGroup() {
      return this.itemAttitudeGroup;
   }

   @Nonnull
   public Attitude getAttitude(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Attitude attitude = (Attitude)this.attitudeCache.getOrDefault(targetRef.getIndex(), null);
      if (attitude != null) {
         return attitude;
      } else {
         if (this.attitudeView == null) {
            this.attitudeView = componentAccessor.getResource(BLACKBOARD_RESOURCE_TYPE)
               .getView(AttitudeView.class, this.parent.getReference(), componentAccessor);
         } else {
            this.attitudeView = this.attitudeView.getUpdatedView(this.parent.getReference(), componentAccessor);
         }

         attitude = this.attitudeView.getAttitude(ref, this.parent.getRole(), targetRef, componentAccessor);
         this.attitudeCache.put(targetRef.getIndex(), attitude);
         return attitude;
      }
   }

   @Nullable
   public Attitude getItemAttitude(@Nullable ItemStack item) {
      ItemAttitudeMap attitudeMap = NPCPlugin.get().getItemAttitudeMap();
      return attitudeMap.getAttitude(this.parent, item);
   }

   public void overrideAttitude(Ref<EntityStore> target, Attitude attitude, double duration) {
      this.attitudeOverrideMemory.put(target.getIndex(), new AttitudeMemoryEntry(attitude, duration));
      if (this.attitudeCache != null) {
         this.attitudeCache.remove(target.getIndex());
      }
   }

   @Nullable
   public Attitude getOverriddenAttitude(Ref<EntityStore> target) {
      if (this.attitudeOverrideMemory == null) {
         return null;
      } else {
         AttitudeMemoryEntry entry = (AttitudeMemoryEntry)this.attitudeOverrideMemory.get(target.getIndex());
         return entry == null ? null : entry.getAttitudeOverride();
      }
   }

   public void requireAttitudeCache() {
      if (this.attitudeCache == null) {
         this.attitudeCache = new Int2ObjectOpenHashMap();
      }
   }

   public void requestNewPath() {
      this.newPathRequested = true;
   }

   public boolean hasRequestedNewPath() {
      return this.newPathRequested;
   }

   public boolean consumeNewPathRequested() {
      boolean requested = this.newPathRequested;
      this.newPathRequested = false;
      return requested;
   }

   public int getEnvironmentId(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.environmentIdChangeCount != this.changeCount) {
         this.environmentIdChangeCount = this.changeCount;
         TransformComponent transformComponent = componentAccessor.getComponent(this.parent.getReference(), TransformComponent.getComponentType());

         assert transformComponent != null;

         Ref<ChunkStore> chunkRef = transformComponent.getChunkRef();
         if (chunkRef == null || !chunkRef.isValid()) {
            return Integer.MIN_VALUE;
         }

         World world = componentAccessor.getExternalData().getWorld();
         Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
         BlockChunk blockChunkComponent = chunkStore.getComponent(chunkRef, BlockChunk.getComponentType());

         assert blockChunkComponent != null;

         this.cachedEnvironmentId = blockChunkComponent.getEnvironment(transformComponent.getPosition());
      }

      return this.cachedEnvironmentId;
   }

   public int getCurrentWeatherIndex(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.weatherChangeCount != this.changeCount) {
         this.weatherChangeCount = this.changeCount;
         WeatherResource weatherResource = componentAccessor.getResource(WeatherResource.getResourceType());
         this.cachedWeatherIndex = weatherResource.getForcedWeatherIndex();
         if (this.cachedWeatherIndex != 0) {
            return this.cachedWeatherIndex;
         }

         int environmentId = this.getEnvironmentId(componentAccessor);
         if (environmentId == Integer.MIN_VALUE) {
            return this.cachedWeatherIndex = 0;
         }

         this.cachedWeatherIndex = weatherResource.getWeatherIndexForEnvironment(environmentId);
      }

      return this.cachedWeatherIndex;
   }

   public static boolean hasTagInGroup(int group, int tag) {
      return TagSetPlugin.get(NPCGroup.class).tagInSet(group, tag);
   }

   public static boolean isGroupMember(
      int parentRoleIndex, @Nonnull Ref<EntityStore> ref, @Nullable int[] groups, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (groups == null) {
         return false;
      } else {
         for (int group : groups) {
            if (isGroupMember(parentRoleIndex, ref, group, componentAccessor)) {
               return true;
            }
         }

         return false;
      }
   }

   public static boolean isGroupMember(
      int parentRoleIndex, @Nullable Ref<EntityStore> ref, int group, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (ref != null && ref.isValid()) {
         NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());
         int targetId;
         if (npcComponent != null) {
            targetId = npcComponent.getRoleIndex();
         } else {
            if (!componentAccessor.getArchetype(ref).contains(Player.getComponentType())) {
               return false;
            }

            targetId = BuilderManager.getPlayerGroupID();
         }

         return targetId == parentRoleIndex && hasTagInGroup(group, BuilderManager.getSelfGroupID()) ? true : hasTagInGroup(group, targetId);
      } else {
         return false;
      }
   }

   public static int[] createTagSetIndexArray(@Nullable String[] tagSets) {
      if (tagSets == null) {
         return null;
      } else {
         int[] groups = new int[tagSets.length];
         IndexedLookupTableAssetMap<String, NPCGroup> npcGroups = NPCGroup.getAssetMap();

         for (int i = 0; i < tagSets.length; i++) {
            String tagSet = tagSets[i];
            int index = npcGroups.getIndex(tagSet);
            if (index == Integer.MIN_VALUE) {
               throw new IllegalArgumentException("Unknown npc group! " + tagSet);
            }

            groups[i] = index;
         }

         return groups;
      }
   }

   public void unloaded() {
      this.resetAllBlockSensors();
      if (this.searchRayCachedPositions != null) {
         for (int i = 0; i < this.searchRayCachedPositions.length; i++) {
            this.resetCachedSearchRayPosition(i);
         }
      }

      if (this.attitudeOverrideMemory != null) {
         this.attitudeOverrideMemory.clear();
      }
   }
}
