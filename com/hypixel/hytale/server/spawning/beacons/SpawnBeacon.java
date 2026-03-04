package com.hypixel.hytale.server.spawning.beacons;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockPlugin;
import com.hypixel.hytale.server.flock.config.FlockAsset;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.systems.NewSpawnStartTickingSystem;
import com.hypixel.hytale.server.spawning.ISpawnableWithModel;
import com.hypixel.hytale.server.spawning.SpawningContext;
import com.hypixel.hytale.server.spawning.assets.spawns.config.BeaconNPCSpawn;
import com.hypixel.hytale.server.spawning.assets.spawns.config.RoleSpawnParameters;
import com.hypixel.hytale.server.spawning.util.FloodFillPositionSelector;
import com.hypixel.hytale.server.spawning.wrappers.BeaconSpawnWrapper;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpawnBeacon extends Entity {
   public static final BuilderCodec<SpawnBeacon> CODEC = BuilderCodec.builder(SpawnBeacon.class, SpawnBeacon::new, Entity.CODEC)
      .append(new KeyedCodec<>("SpawnConfiguration", Codec.STRING), (spawnBeacon, s) -> spawnBeacon.spawnConfigId = s, spawnBeacon -> spawnBeacon.spawnConfigId)
      .add()
      .build();
   private BeaconSpawnWrapper spawnWrapper;
   private String spawnConfigId;
   private final IntSet unspawnableRoles = new IntOpenHashSet();
   private final SpawningContext spawningContext = new SpawningContext();

   @Nullable
   public static ComponentType<EntityStore, SpawnBeacon> getComponentType() {
      return EntityModule.get().getComponentType(SpawnBeacon.class);
   }

   public SpawnBeacon() {
   }

   public SpawnBeacon(World world) {
      super(world);
   }

   public BeaconSpawnWrapper getSpawnWrapper() {
      return this.spawnWrapper;
   }

   public void setSpawnWrapper(@Nonnull BeaconSpawnWrapper spawnWrapper) {
      this.spawnWrapper = spawnWrapper;
      this.spawnConfigId = spawnWrapper.getSpawn().getId();
   }

   public String getSpawnConfigId() {
      return this.spawnConfigId;
   }

   @Override
   public boolean isHiddenFromLivingEntity(
      @Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Player targetPlayerComponent = componentAccessor.getComponent(targetRef, Player.getComponentType());
      return targetPlayerComponent == null || targetPlayerComponent.getGameMode() != GameMode.Creative;
   }

   @Override
   public boolean isCollidable() {
      return false;
   }

   @Override
   public void moveTo(@Nonnull Ref<EntityStore> ref, double locX, double locY, double locZ, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      super.moveTo(ref, locX, locY, locZ, componentAccessor);
      FloodFillPositionSelector positionSelectorComponent = componentAccessor.getComponent(ref, FloodFillPositionSelector.getComponentType());

      assert positionSelectorComponent != null;

      positionSelectorComponent.setCalculatePositionsAfter(SpawnBeaconSystems.POSITION_CALCULATION_DELAY_RANGE[1]);
      positionSelectorComponent.forceRebuildCache();
      this.unspawnableRoles.clear();
   }

   public boolean manualTrigger(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull FloodFillPositionSelector positionSelector,
      @Nonnull Ref<EntityStore> targetRef,
      @Nonnull Store<EntityStore> store
   ) {
      int concurrentSpawns = RandomExtra.randomRange(this.spawnWrapper.getSpawn().getConcurrentSpawnsRange());
      int spawnedCount = 0;

      for (int i = 0; i < concurrentSpawns; i++) {
         RoleSpawnParameters roleSpawnParameters = this.spawnWrapper.pickRole(ThreadLocalRandom.current());
         if (roleSpawnParameters != null) {
            int roleIndex = NPCPlugin.get().getIndex(roleSpawnParameters.getId());
            if (!this.unspawnableRoles.contains(roleIndex)) {
               ISpawnableWithModel spawnable = (ISpawnableWithModel)NPCPlugin.get().tryGetCachedValidRole(roleIndex);
               this.spawningContext.setSpawnable(spawnable);
               if (!positionSelector.hasPositionsForRole(roleIndex)) {
                  this.markUnspawnable(ref, roleIndex, store);
                  this.spawningContext.releaseFull();
               } else {
                  Vector3d targetPos = targetRef.getStore().getComponent(targetRef, TransformComponent.getComponentType()).getPosition();
                  if (!positionSelector.prepareSpawnContext(targetPos, concurrentSpawns, roleIndex, this.spawningContext, this.spawnWrapper)) {
                     this.spawningContext.releaseFull();
                  } else {
                     Vector3d position = this.spawningContext.newPosition();
                     Vector3f rotation = this.spawningContext.newRotation();
                     FlockAsset flockDefinition = roleSpawnParameters.getFlockDefinition();
                     int flockSize = flockDefinition != null ? flockDefinition.pickFlockSize() : 1;

                     try {
                        Pair<Ref<EntityStore>, NPCEntity> npcPair = NPCPlugin.get()
                           .spawnEntity(
                              store,
                              roleIndex,
                              position,
                              rotation,
                              this.spawningContext.getModel(),
                              (_npc, _ref, _store) -> postSpawn(_npc, _ref, this.spawnWrapper.getSpawn(), targetRef, _store)
                           );
                        Ref<EntityStore> npcRef = (Ref<EntityStore>)npcPair.first();
                        NPCEntity npcComponent = (NPCEntity)npcPair.second();
                        FlockPlugin.trySpawnFlock(
                           npcRef,
                           npcComponent,
                           roleIndex,
                           position,
                           rotation,
                           flockSize,
                           flockDefinition,
                           null,
                           (_npc, _ref, _store) -> postSpawn(_npc, _ref, this.spawnWrapper.getSpawn(), targetRef, _store),
                           store
                        );
                        spawnedCount++;
                     } catch (RuntimeException var22) {
                        LOGGER.at(Level.WARNING).log("Failed to create %s: %s", NPCPlugin.get().getName(roleIndex), var22.getMessage());
                        this.markUnspawnable(ref, roleIndex, store);
                     } finally {
                        this.spawningContext.releaseFull();
                     }
                  }
               }
            }
         }
      }

      return spawnedCount != 0;
   }

   protected void markUnspawnable(Ref<EntityStore> ref, int index, ComponentAccessor<EntityStore> componentAccessor) {
      this.unspawnableRoles.add(index);
      if (this.unspawnableRoles.size() >= this.spawnWrapper.getRoles().size()) {
         UUIDComponent uuidComponent = componentAccessor.getComponent(ref, UUIDComponent.getComponentType());

         assert uuidComponent != null;

         LOGGER.at(Level.WARNING).log("Removed spawn beacon %s due to being unable to spawn any NPC types", uuidComponent.getUuid());
         this.remove();
      }
   }

   protected static void postSpawn(
      @Nonnull NPCEntity npc,
      @Nonnull Ref<EntityStore> selfRef,
      @Nonnull BeaconNPCSpawn spawn,
      Ref<EntityStore> targetRef,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      Role role = npc.getRole();
      role.getMarkedEntitySupport().setMarkedEntity(spawn.getTargetSlot(), targetRef);
      String spawnState = spawn.getNpcSpawnState();
      if (spawnState != null) {
         role.getStateSupport().setState(selfRef, spawnState, spawn.getNpcSpawnSubState(), componentAccessor);
      }

      NewSpawnStartTickingSystem.queueNewSpawn(selfRef, selfRef.getStore());
   }

   @Nonnull
   @Override
   public String toString() {
      return "SpawnBeacon{spawnConfigId='" + this.spawnConfigId + "'} " + super.toString();
   }
}
