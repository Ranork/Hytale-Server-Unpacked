package com.hypixel.hytale.server.flock;

import com.hypixel.fastutil.ints.Int2ObjectConcurrentHashMap;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.function.consumer.TriConsumer;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.group.EntityGroup;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.prefab.event.PrefabPasteEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.config.FlockAsset;
import com.hypixel.hytale.server.flock.config.RangeSizeFlockAsset;
import com.hypixel.hytale.server.flock.corecomponents.builders.BuilderActionFlockBeacon;
import com.hypixel.hytale.server.flock.corecomponents.builders.BuilderActionFlockJoin;
import com.hypixel.hytale.server.flock.corecomponents.builders.BuilderActionFlockLeave;
import com.hypixel.hytale.server.flock.corecomponents.builders.BuilderActionFlockSetTarget;
import com.hypixel.hytale.server.flock.corecomponents.builders.BuilderActionFlockState;
import com.hypixel.hytale.server.flock.corecomponents.builders.BuilderBodyMotionFlock;
import com.hypixel.hytale.server.flock.corecomponents.builders.BuilderEntityFilterFlock;
import com.hypixel.hytale.server.flock.corecomponents.builders.BuilderSensorFlockCombatDamage;
import com.hypixel.hytale.server.flock.corecomponents.builders.BuilderSensorFlockLeader;
import com.hypixel.hytale.server.flock.corecomponents.builders.BuilderSensorInflictedDamage;
import com.hypixel.hytale.server.flock.decisionmaker.conditions.FlockSizeCondition;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base.Condition;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.systems.PositionCacheSystems;
import it.unimi.dsi.fastutil.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FlockPlugin extends JavaPlugin {
   private static FlockPlugin instance;
   private final Int2ObjectConcurrentHashMap<Map<UUID, UUID>> prefabFlockRemappings = new Int2ObjectConcurrentHashMap<>();
   private ComponentType<EntityStore, Flock> flockComponentType;
   private ComponentType<EntityStore, FlockMembership> flockMembershipComponentType;
   private ComponentType<EntityStore, PersistentFlockData> persistentFlockDataComponentType;

   public static FlockPlugin get() {
      return instance;
   }

   public FlockPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   public void setup() {
      instance = this;
      ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                           FlockAsset.class, new IndexedLookupTableAssetMap<>(FlockAsset[]::new)
                        )
                        .setPath("NPC/Flocks"))
                     .setCodec(FlockAsset.CODEC))
                  .setKeyFunction(FlockAsset::getId))
               .setReplaceOnRemove(RangeSizeFlockAsset::getUnknownFor))
            .build()
      );
      this.getEntityStoreRegistry().registerSystem(new FlockPlugin.PrefabPasteEventSystem(this));
      NPCPlugin.get()
         .registerCoreComponentType("Flock", BuilderBodyMotionFlock::new)
         .registerCoreComponentType("JoinFlock", BuilderActionFlockJoin::new)
         .registerCoreComponentType("LeaveFlock", BuilderActionFlockLeave::new)
         .registerCoreComponentType("FlockState", BuilderActionFlockState::new)
         .registerCoreComponentType("FlockTarget", BuilderActionFlockSetTarget::new)
         .registerCoreComponentType("FlockBeacon", BuilderActionFlockBeacon::new)
         .registerCoreComponentType("Flock", BuilderEntityFilterFlock::new)
         .registerCoreComponentType("FlockCombatDamage", BuilderSensorFlockCombatDamage::new)
         .registerCoreComponentType("InflictedDamage", BuilderSensorInflictedDamage::new)
         .registerCoreComponentType("FlockLeader", BuilderSensorFlockLeader::new);
      Condition.CODEC.register("FlockSize", FlockSizeCondition.class, FlockSizeCondition.CODEC);
      this.flockComponentType = entityStoreRegistry.registerComponent(Flock.class, () -> {
         throw new UnsupportedOperationException("Not implemented");
      });
      entityStoreRegistry.registerSystem(new FlockSystems.EntityRemoved(this.flockComponentType));
      entityStoreRegistry.registerSystem(new FlockSystems.Ticking(this.flockComponentType));
      entityStoreRegistry.registerSystem(new FlockSystems.FlockDebugSystem(this.flockComponentType));
      entityStoreRegistry.registerSystem(new FlockSystems.PlayerChangeGameModeEventSystem());
      this.flockMembershipComponentType = entityStoreRegistry.registerComponent(FlockMembership.class, "FlockMembership", FlockMembership.CODEC);
      this.persistentFlockDataComponentType = entityStoreRegistry.registerComponent(PersistentFlockData.class, "FlockData", PersistentFlockData.CODEC);
      entityStoreRegistry.registerSystem(new FlockMembershipSystems.EntityRef(this.flockMembershipComponentType));
      entityStoreRegistry.registerSystem(new FlockMembershipSystems.RefChange(this.flockMembershipComponentType));
      entityStoreRegistry.registerSystem(new PositionCacheSystems.OnFlockJoinSystem(NPCEntity.getComponentType(), this.flockMembershipComponentType));
      entityStoreRegistry.registerSystem(new FlockDeathSystems.EntityDeath());
      entityStoreRegistry.registerSystem(new FlockDeathSystems.PlayerDeath());
      entityStoreRegistry.registerSystem(new FlockMembershipSystems.FilterPlayerFlockDamageSystem());
      entityStoreRegistry.registerSystem(new FlockMembershipSystems.OnDamageReceived());
      entityStoreRegistry.registerSystem(new FlockMembershipSystems.OnDamageDealt());
      entityStoreRegistry.registerSystem(new FlockMembershipSystems.NPCAddedFromWorldGen());
   }

   @Override
   public void start() {
   }

   @Override
   public void shutdown() {
   }

   public ComponentType<EntityStore, Flock> getFlockComponentType() {
      return this.flockComponentType;
   }

   public ComponentType<EntityStore, FlockMembership> getFlockMembershipComponentType() {
      return this.flockMembershipComponentType;
   }

   public ComponentType<EntityStore, PersistentFlockData> getPersistentFlockDataComponentType() {
      return this.persistentFlockDataComponentType;
   }

   @Nonnull
   public UUID getPrefabRemappedFlockReference(int prefabId, UUID oldId) {
      return this.prefabFlockRemappings.get(prefabId).computeIfAbsent(oldId, s -> UUID.randomUUID());
   }

   @Nullable
   public static Ref<EntityStore> trySpawnFlock(
      @Nonnull Ref<EntityStore> npcRef,
      @Nonnull NPCEntity npc,
      @Nonnull Store<EntityStore> store,
      int roleIndex,
      @Nonnull Vector3d position,
      Vector3f rotation,
      @Nullable FlockAsset flockDefinition,
      TriConsumer<NPCEntity, Ref<EntityStore>, Store<EntityStore>> postSpawn
   ) {
      int flockSize = flockDefinition != null ? flockDefinition.pickFlockSize() : 1;
      return trySpawnFlock(npcRef, npc, roleIndex, position, rotation, flockSize, flockDefinition, null, postSpawn, store);
   }

   @Nullable
   public static Ref<EntityStore> trySpawnFlock(
      @Nonnull Ref<EntityStore> npcRef,
      @Nonnull NPCEntity npc,
      @Nonnull Store<EntityStore> store,
      int roleIndex,
      @Nonnull Vector3d position,
      Vector3f rotation,
      int flockSize,
      TriConsumer<NPCEntity, Ref<EntityStore>, Store<EntityStore>> postSpawn
   ) {
      return trySpawnFlock(npcRef, npc, roleIndex, position, rotation, flockSize, null, null, postSpawn, store);
   }

   @Nullable
   public static Ref<EntityStore> trySpawnFlock(
      @Nonnull Ref<EntityStore> npcRef,
      @Nonnull NPCEntity npc,
      int roleIndex,
      @Nonnull Vector3d position,
      Vector3f rotation,
      int flockSize,
      FlockAsset flockDefinition,
      TriConsumer<NPCEntity, Holder<EntityStore>, Store<EntityStore>> preAddToWorld,
      @Nullable TriConsumer<NPCEntity, Ref<EntityStore>, Store<EntityStore>> postSpawn,
      @Nonnull Store<EntityStore> store
   ) {
      if (flockSize > 1 && npcRef.isValid()) {
         Role role = npc.getRole();

         assert role != null;

         FlockMembership membershipComponent = store.getComponent(npcRef, FlockMembership.getComponentType());
         Ref<EntityStore> flockReference = membershipComponent != null
            ? membershipComponent.getFlockRef()
            : createFlock(store, flockDefinition, role.getFlockAllowedRoles());
         if (membershipComponent == null) {
            FlockMembershipSystems.join(npcRef, flockReference, store);
         }

         BoundingBox boundingBoxComponent = store.getComponent(npcRef, BoundingBox.getComponentType());

         assert boundingBoxComponent != null;

         TransformComponent transformComponent = store.getComponent(npcRef, TransformComponent.getComponentType());

         assert transformComponent != null;

         Box boundingBox = boundingBoxComponent.getBoundingBox();
         Vector3f bodyRotation = transformComponent.getRotation();
         double x = position.getX();
         int y = MathUtil.floor(position.getY() + boundingBox.min.y + 1.0E-6);
         double z = position.getZ();
         double yaw = bodyRotation.getYaw();
         boolean randomSpawn = role.isFlockSpawnTypesRandom();
         int[] roles = role.getFlockSpawnTypes();
         int rolesSize = roles == null ? 0 : roles.length;
         int index = 0;
         int memberRoleIndex = roleIndex;

         for (int i = 1; i < flockSize; i++) {
            if (rolesSize > 0) {
               if (randomSpawn) {
                  memberRoleIndex = roles[RandomExtra.randomRange(rolesSize)];
               } else {
                  memberRoleIndex = roles[index];
                  index = (index + 1) % rolesSize;
               }
            }

            Pair<Ref<EntityStore>, NPCEntity> memberPair = NPCPlugin.get()
               .spawnEntity(store, memberRoleIndex, position, rotation, null, preAddToWorld, postSpawn);
            Ref<EntityStore> memberRef = (Ref<EntityStore>)memberPair.first();
            if (memberRef != null && memberRef.isValid()) {
               BoundingBox memberBoundingBoxComponent = store.getComponent(memberRef, BoundingBox.getComponentType());

               assert memberBoundingBoxComponent != null;

               TransformComponent memberTransformComponent = store.getComponent(memberRef, TransformComponent.getComponentType());

               assert memberTransformComponent != null;

               HeadRotation memberHeadRotationComponent = store.getComponent(memberRef, HeadRotation.getComponentType());

               assert memberHeadRotationComponent != null;

               double offsetY = y - memberBoundingBoxComponent.getBoundingBox().min.y;
               memberTransformComponent.getRotation().setYaw((float)(yaw + RandomExtra.randomRange((float) (Math.PI / 4), (float) (Math.PI / 4))));
               memberHeadRotationComponent.getRotation().setPitch(0.0F);
               memberTransformComponent.getPosition().assign(x + RandomExtra.randomRange(-0.5, 0.5), offsetY, z + RandomExtra.randomRange(-0.5, 0.5));
               FlockMembershipSystems.join(memberRef, flockReference, store);
            }
         }

         return flockReference;
      } else {
         return null;
      }
   }

   @Nullable
   @Deprecated
   public static Flock getFlock(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> reference) {
      FlockMembership flockMembershipComponent = componentAccessor.getComponent(reference, FlockMembership.getComponentType());
      if (flockMembershipComponent == null) {
         return null;
      } else {
         Ref<EntityStore> membershipRef = flockMembershipComponent.getFlockRef();
         return membershipRef != null && membershipRef.isValid() ? componentAccessor.getComponent(membershipRef, Flock.getComponentType()) : null;
      }
   }

   @Nonnull
   public static Ref<EntityStore> createFlock(@Nonnull Store<EntityStore> store, @Nonnull Role role) {
      return createFlock(store, null, role.getFlockAllowedRoles());
   }

   @Nonnull
   public static Ref<EntityStore> createFlock(@Nonnull Store<EntityStore> store, @Nullable FlockAsset flockDefinition, @Nonnull String[] allowedRoles) {
      Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
      holder.addComponent(UUIDComponent.getComponentType(), UUIDComponent.randomUUID());
      holder.addComponent(EntityGroup.getComponentType(), new EntityGroup());
      holder.addComponent(Flock.getComponentType(), new Flock(flockDefinition, allowedRoles));
      Ref<EntityStore> ref = store.addEntity(holder, AddReason.SPAWN);
      if (ref == null) {
         throw new UnsupportedOperationException("Unable to handle non-spawned flock!");
      } else {
         return ref;
      }
   }

   @Nullable
   public static Ref<EntityStore> getFlockReference(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      FlockMembership flockMembershipComponent = componentAccessor.getComponent(ref, FlockMembership.getComponentType());
      return flockMembershipComponent == null ? null : flockMembershipComponent.getFlockRef();
   }

   public static boolean isFlockMember(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      FlockMembership flockMembershipComponent = store.getComponent(ref, FlockMembership.getComponentType());
      return flockMembershipComponent != null;
   }

   private static final class PrefabPasteEventSystem extends WorldEventSystem<EntityStore, PrefabPasteEvent> {
      private final FlockPlugin plugin;

      PrefabPasteEventSystem(@Nonnull FlockPlugin plugin) {
         super(PrefabPasteEvent.class);
         this.plugin = plugin;
      }

      public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull PrefabPasteEvent event) {
         if (event.isPasteStart()) {
            this.plugin.prefabFlockRemappings.put(event.getPrefabId(), new HashMap<>());
         } else {
            this.plugin.prefabFlockRemappings.remove(event.getPrefabId());
         }
      }
   }
}
