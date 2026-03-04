package com.hypixel.hytale.server.spawning.wrappers;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.blockset.BlockSetModule;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.spawning.SpawningContext;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.assets.spawns.LightType;
import com.hypixel.hytale.server.spawning.assets.spawns.config.NPCSpawn;
import com.hypixel.hytale.server.spawning.assets.spawns.config.RoleSpawnParameters;
import com.hypixel.hytale.server.spawning.util.LightRangePredicate;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class SpawnWrapper<T extends NPCSpawn> {
   protected final int spawnIndex;
   @Nonnull
   protected final T spawn;
   protected Int2ObjectMap<RoleSpawnParameters> roles;
   protected final LightRangePredicate lightRangePredicate = new LightRangePredicate();
   protected final Set<String> invalidNPCs = new HashSet<>();

   public SpawnWrapper(int spawnIndex, @Nonnull T spawn) {
      this.spawnIndex = spawnIndex;
      this.spawn = spawn;

      for (LightType lightType : LightType.VALUES) {
         this.lightRangePredicate.setLightRange(lightType, spawn.getLightRange(lightType));
      }

      this.addRoles();
   }

   @Nonnull
   public T getSpawn() {
      return this.spawn;
   }

   public Int2ObjectMap<RoleSpawnParameters> getRoles() {
      return this.roles;
   }

   @Nullable
   public IntSet getSpawnBlockSet(int roleIndex) {
      int spawnBlockSet = ((RoleSpawnParameters)this.roles.get(roleIndex)).getSpawnBlockSetIndex();
      return spawnBlockSet >= 0 ? (IntSet)BlockSetModule.getInstance().getBlockSets().get(spawnBlockSet) : null;
   }

   public int getSpawnFluidTag(int roleIndex) {
      return ((RoleSpawnParameters)this.roles.get(roleIndex)).getSpawnFluidTagIndex();
   }

   public int getSpawnIndex() {
      return this.spawnIndex;
   }

   @Nonnull
   public LightRangePredicate getLightRangePredicate() {
      return this.lightRangePredicate;
   }

   public boolean hasInvalidNPC(String name) {
      return this.invalidNPCs.contains(name);
   }

   public boolean spawnParametersMatch(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      World world = componentAccessor.getExternalData().getWorld();
      WorldTimeResource worldTimeResource = componentAccessor.getResource(WorldTimeResource.getResourceType());
      double[] dayTimeRange = this.spawn.getDayTimeRange();
      int[] moonPhaseRange = this.spawn.getMoonPhaseRange();
      boolean withinTimeRange = this.spawn.isScaleDayTimeRange()
         ? worldTimeResource.isScaledDayTimeWithinRange(dayTimeRange[0], dayTimeRange[1])
         : worldTimeResource.isDayTimeWithinRange(dayTimeRange[0], dayTimeRange[1]);
      return withinTimeRange && worldTimeResource.isMoonPhaseWithinRange(world, moonPhaseRange[0], moonPhaseRange[1]);
   }

   public boolean shouldDespawn(@Nonnull World world, @Nonnull WorldTimeResource timeManager) {
      NPCSpawn.DespawnParameters despawnParams = this.spawn.getDespawnParameters();
      if (despawnParams == null) {
         return false;
      } else {
         double[] dayTimeRange = despawnParams.getDayTimeRange();
         int[] moonPhaseRange = despawnParams.getMoonPhaseRange();
         boolean withinTimeRange = this.spawn.isScaleDayTimeRange()
            ? timeManager.isScaledDayTimeWithinRange(dayTimeRange[0], dayTimeRange[1])
            : timeManager.isDayTimeWithinRange(dayTimeRange[0], dayTimeRange[1]);
         return world.getWorldConfig().isSpawningNPC() && withinTimeRange && timeManager.isMoonPhaseWithinRange(world, moonPhaseRange[0], moonPhaseRange[1]);
      }
   }

   public boolean withinLightRange(@Nonnull SpawningContext spawningContext) {
      BlockChunk blockChunk = spawningContext.worldChunk.getBlockChunk();
      Store<EntityStore> store = spawningContext.world.getEntityStore().getStore();
      WorldTimeResource worldTimeResource = store.getResource(WorldTimeResource.getResourceType());
      return this.lightRangePredicate
         .test(blockChunk, spawningContext.xBlock, spawningContext.yBlock, spawningContext.zBlock, worldTimeResource.getSunlightFactor());
   }

   private void addRoles() {
      NPCPlugin npcModule = NPCPlugin.get();
      SpawningPlugin spawningModule = SpawningPlugin.get();
      Int2ObjectOpenHashMap<RoleSpawnParameters> roles = new Int2ObjectOpenHashMap();

      for (RoleSpawnParameters roleEntry : this.spawn.getNPCs()) {
         String name = roleEntry.getId();
         int roleIndex = npcModule.getIndex(name);
         if (roleIndex < 0) {
            this.invalidNPCs.add(name);
            spawningModule.getLogger().at(Level.WARNING).log("NPCSpawn %s references unknown NPC %s", this.spawn.getId(), name);
         } else {
            roles.put(roleIndex, roleEntry);
         }
      }

      roles.trim();
      this.roles = Int2ObjectMaps.unmodifiable(roles);
   }
}
