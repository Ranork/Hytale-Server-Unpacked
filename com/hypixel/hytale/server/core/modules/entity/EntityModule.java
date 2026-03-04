package com.hypixel.hytale.server.core.modules.entity;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.codec.DirectDecodeCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.SystemType;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.KDTree;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.player.UpdateMovementSettings;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.damage.DamageDataComponent;
import com.hypixel.hytale.server.core.entity.damage.DamageDataSetupSystem;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.entity.entities.BlockEntity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.entity.entities.player.CameraManager;
import com.hypixel.hytale.server.core.entity.entities.player.HotbarManager;
import com.hypixel.hytale.server.core.entity.entities.player.data.UniqueItemUsagesComponent;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementConfig;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.entity.group.EntityGroup;
import com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesSystems;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.entity.nameplate.NameplateSystems;
import com.hypixel.hytale.server.core.entity.reference.PersistentRefCount;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.collision.TangiableEntitySpatialSystem;
import com.hypixel.hytale.server.core.modules.entity.component.ActiveAnimationComponent;
import com.hypixel.hytale.server.core.modules.entity.component.AudioComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.CollisionResultComponent;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.DynamicLight;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.FromPrefab;
import com.hypixel.hytale.server.core.modules.entity.component.FromWorldGen;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.HiddenFromAdventurePlayers;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.MovementAudioComponent;
import com.hypixel.hytale.server.core.modules.entity.component.NPCMarkerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.NewSpawnComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentDynamicLight;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.PositionDataComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.modules.entity.component.RespondToHit;
import com.hypixel.hytale.server.core.modules.entity.component.RotateObjectComponent;
import com.hypixel.hytale.server.core.modules.entity.component.SnapshotBuffer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.component.WorldGenId;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.dynamiclight.DynamicLightSystems;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollision;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionConfig;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionConfigPacketGenerator;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionSystems;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemMergeSystem;
import com.hypixel.hytale.server.core.modules.entity.item.ItemPhysicsComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemPhysicsSystem;
import com.hypixel.hytale.server.core.modules.entity.item.ItemPrePhysicsSystem;
import com.hypixel.hytale.server.core.modules.entity.item.ItemSystems;
import com.hypixel.hytale.server.core.modules.entity.item.PickupItemComponent;
import com.hypixel.hytale.server.core.modules.entity.item.PickupItemSystem;
import com.hypixel.hytale.server.core.modules.entity.item.PreventItemMerging;
import com.hypixel.hytale.server.core.modules.entity.item.PreventPickup;
import com.hypixel.hytale.server.core.modules.entity.livingentity.LivingEntityEffectClearChangesSystem;
import com.hypixel.hytale.server.core.modules.entity.livingentity.LivingEntityEffectSystem;
import com.hypixel.hytale.server.core.modules.entity.player.ApplyRandomSkinPersistedComponent;
import com.hypixel.hytale.server.core.modules.entity.player.ChunkTracker;
import com.hypixel.hytale.server.core.modules.entity.player.KnockbackPredictionSystems;
import com.hypixel.hytale.server.core.modules.entity.player.KnockbackSimulation;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerCameraAddSystem;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerChunkTrackerSystems;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerHudManagerSystems;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerInput;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerItemEntityPickupSystem;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerMovementManagerSystems;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerProcessMovementSystem;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSavingSystems;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSendInventorySystem;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSystems;
import com.hypixel.hytale.server.core.modules.entity.repulsion.Repulsion;
import com.hypixel.hytale.server.core.modules.entity.repulsion.RepulsionConfig;
import com.hypixel.hytale.server.core.modules.entity.repulsion.RepulsionConfigPacketGenerator;
import com.hypixel.hytale.server.core.modules.entity.repulsion.RepulsionSystems;
import com.hypixel.hytale.server.core.modules.entity.system.AudioSystems;
import com.hypixel.hytale.server.core.modules.entity.system.EntityInteractableSystems;
import com.hypixel.hytale.server.core.modules.entity.system.EntitySpatialSystem;
import com.hypixel.hytale.server.core.modules.entity.system.EntitySystems;
import com.hypixel.hytale.server.core.modules.entity.system.HideEntitySystems;
import com.hypixel.hytale.server.core.modules.entity.system.IntangibleSystems;
import com.hypixel.hytale.server.core.modules.entity.system.InvulnerableSystems;
import com.hypixel.hytale.server.core.modules.entity.system.ItemSpatialSystem;
import com.hypixel.hytale.server.core.modules.entity.system.ModelSystems;
import com.hypixel.hytale.server.core.modules.entity.system.NetworkSendableSpatialSystem;
import com.hypixel.hytale.server.core.modules.entity.system.PlayerCollisionResultAddSystem;
import com.hypixel.hytale.server.core.modules.entity.system.PlayerSpatialSystem;
import com.hypixel.hytale.server.core.modules.entity.system.RespondToHitSystems;
import com.hypixel.hytale.server.core.modules.entity.system.RotateObjectSystem;
import com.hypixel.hytale.server.core.modules.entity.system.SnapshotSystems;
import com.hypixel.hytale.server.core.modules.entity.system.TransformSystems;
import com.hypixel.hytale.server.core.modules.entity.system.UpdateEntitySeedSystem;
import com.hypixel.hytale.server.core.modules.entity.system.UpdateLocationSystems;
import com.hypixel.hytale.server.core.modules.entity.teleport.PendingTeleport;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.entity.teleport.TeleportRecord;
import com.hypixel.hytale.server.core.modules.entity.teleport.TeleportSystems;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.entity.tracker.LegacyEntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.physics.systems.GenericVelocityInstructionSystem;
import com.hypixel.hytale.server.core.modules.physics.systems.IVelocityModifyingSystem;
import com.hypixel.hytale.server.core.modules.physics.systems.PhysicsValuesAddSystem;
import com.hypixel.hytale.server.core.modules.physics.systems.VelocitySystems;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginState;
import com.hypixel.hytale.server.core.prefab.PrefabCopyableComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.system.PlayerVelocityInstructionSystem;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityModule extends JavaPlugin {
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(EntityModule.class).depends(Universe.class).depends(CollisionModule.class).build();
   public static final String[] LEGACY_ENTITY_CLASS_NAMES = new String[]{
      "SpawnSuppressor", "Block", "LegacySpawnBeacon", "PatrolPathMarker", "Player", "SpawnBeacon", "SpawnMarker"
   };
   public static final String MOUNT_MOVEMENT_SETTINGS_ASSET_ID = "Mount";
   private static EntityModule instance;
   private final Map<String, Class<? extends Entity>> idMap = new ConcurrentHashMap<>();
   private final Map<Class<? extends Entity>, String> classIdMap = new ConcurrentHashMap<>();
   private final Map<Class<? extends Entity>, Function<World, ? extends Entity>> classMap = new ConcurrentHashMap<>();
   private final Map<Class<? extends Entity>, DirectDecodeCodec<? extends Entity>> codecMap = new ConcurrentHashMap<>();
   @Deprecated
   private final Map<Class<? extends Entity>, ComponentType<EntityStore, ? extends Entity>> classToComponentType = new ConcurrentHashMap<>();
   private ComponentType<EntityStore, UUIDComponent> uuidComponentType;
   private ComponentType<EntityStore, TransformComponent> transformComponentType;
   private ComponentType<EntityStore, HeadRotation> headRotationComponentType;
   private ComponentType<EntityStore, NetworkId> networkIdComponentType;
   private ComponentType<EntityStore, EntityScaleComponent> entityScaleComponentType;
   private ComponentType<EntityStore, Player> playerComponentType;
   private ComponentType<EntityStore, MovementManager> movementManagerComponentType;
   private ComponentType<EntityStore, CameraManager> cameraManagerComponentType;
   private ComponentType<EntityStore, Frozen> frozenComponentType;
   private ComponentType<EntityStore, CollisionResultComponent> collisionResultComponentType;
   private ComponentType<EntityStore, ChunkTracker> chunkTrackerComponentType;
   private ComponentType<EntityStore, ProjectileComponent> projectileComponentType;
   private ComponentType<EntityStore, BlockEntity> blockEntityComponentType;
   private ComponentType<EntityStore, EffectControllerComponent> effectControllerComponentType;
   private ComponentType<EntityStore, RotateObjectComponent> rotateObjectComponentType;
   private ComponentType<EntityStore, ModelComponent> modelComponentType;
   private ComponentType<EntityStore, PersistentModel> persistentModelComponentType;
   private ComponentType<EntityStore, PropComponent> propComponentType;
   private ComponentType<EntityStore, NPCMarkerComponent> npcMarkerComponentType;
   private ComponentType<EntityStore, BoundingBox> boundingBoxComponentType;
   private ComponentType<EntityStore, PlayerSkinComponent> playerSkinComponentType;
   private ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> playerSpatialResourceType;
   private ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> entitySpatialResourceType;
   private ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> itemSpatialResourceType;
   private ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> networkSendableSpatialResourceType;
   private ComponentType<EntityStore, DisplayNameComponent> displayNameComponentType;
   private ComponentType<EntityStore, EntityGroup> entityGroupComponentType;
   private ComponentType<EntityStore, MovementStatesComponent> movementStatesComponentType;
   private ComponentType<EntityStore, DamageDataComponent> damageDataComponentType;
   private ComponentType<EntityStore, KnockbackComponent> knockbackComponentType;
   private ComponentType<EntityStore, DespawnComponent> despawnComponentComponentType;
   private ComponentType<EntityStore, EntityTrackerSystems.EntityViewer> entityViewerComponentType;
   private ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;
   private ResourceType<EntityStore, SnapshotSystems.SnapshotWorldInfo> snapshotWorldInfoResourceType;
   private ComponentType<EntityStore, SnapshotBuffer> snapshotBufferComponentType;
   private ComponentType<EntityStore, PersistentRefCount> persistentRefCountComponentType;
   private ComponentType<EntityStore, Velocity> velocityComponentType;
   private ComponentType<EntityStore, PhysicsValues> physicsValuesComponentType;
   private ComponentType<EntityStore, FromPrefab> fromPrefabComponentType;
   private ComponentType<EntityStore, FromWorldGen> fromWorldGenComponentType;
   private ComponentType<EntityStore, WorldGenId> worldGenIdComponentType;
   private ComponentType<EntityStore, Interactable> interactableComponentType;
   private ComponentType<EntityStore, Intangible> intangibleComponentType;
   private ComponentType<EntityStore, PreventPickup> preventPickupComponentType;
   private ComponentType<EntityStore, Invulnerable> invulnerableComponentType;
   private ComponentType<EntityStore, RespondToHit> respondToHitComponentType;
   private ResourceType<EntityStore, EntityInteractableSystems.QueueResource> interactableQueueResourceType;
   private ResourceType<EntityStore, IntangibleSystems.QueueResource> intangibleQueueResourceType;
   private ResourceType<EntityStore, InvulnerableSystems.QueueResource> invulnerableQueueResourceType;
   private ResourceType<EntityStore, RespondToHitSystems.QueueResource> respondToHitQueueResourceType;
   private ComponentType<EntityStore, HiddenFromAdventurePlayers> hiddenFromAdventurePlayerComponentType;
   private ComponentType<EntityStore, Nameplate> nameplateComponentType;
   private ComponentType<EntityStore, HitboxCollision> hitboxCollisionComponentType;
   private ComponentType<EntityStore, Repulsion> repulsionComponentType;
   private ComponentType<EntityStore, Teleport> teleportComponentType;
   private ComponentType<EntityStore, PendingTeleport> pendingTeleportComponentType;
   private ComponentType<EntityStore, TeleportRecord> teleportRecordComponentType;
   private ComponentType<EntityStore, ApplyRandomSkinPersistedComponent> applyRandomSkinPersistedComponent;
   private SystemGroup<EntityStore> preClearMarkersGroup;
   private ComponentType<EntityStore, PlayerInput> playerInputComponentType;
   private ComponentType<EntityStore, KnockbackSimulation> knockbackSimulationComponentType;
   private ComponentType<EntityStore, PlayerSettings> playerSettingsComponentType;
   private SystemType<EntityStore, EntityModule.MigrationSystem> migrationSystemType;
   private SystemType<EntityStore, ? extends ISystem<EntityStore>> velocityModifyingSystemType;
   private ComponentType<EntityStore, AudioComponent> audioComponentType;
   private ComponentType<EntityStore, MovementAudioComponent> movementAudioComponentType;
   private ComponentType<EntityStore, PositionDataComponent> positionDataComponentType;
   private ComponentType<EntityStore, ActiveAnimationComponent> activeAnimationComponentType;
   private ComponentType<EntityStore, NewSpawnComponent> newSpawnComponentType;
   private ComponentType<EntityStore, ItemComponent> itemComponentType;
   private ComponentType<EntityStore, PickupItemComponent> pickupItemComponentType;
   private ComponentType<EntityStore, PreventItemMerging> preventItemMergingType;
   private ComponentType<EntityStore, ItemPhysicsComponent> itemPhysicsComponentType;
   private ComponentType<EntityStore, DynamicLight> dynamicLightComponentType;
   private ComponentType<EntityStore, PersistentDynamicLight> persistentDynamicLightComponentType;
   private ComponentType<EntityStore, PrefabCopyableComponent> prefabCopyableComponentType;
   private ComponentType<EntityStore, UniqueItemUsagesComponent> uniqueItemUsagesComponentType;

   public static EntityModule get() {
      return instance;
   }

   public EntityModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      this.getEventRegistry().registerGlobal(LivingEntityInventoryChangeEvent.class, event -> {
         Ref<EntityStore> entityRef = event.getEntity().getReference();
         if (entityRef != null && entityRef.isValid()) {
            Store<EntityStore> store = entityRef.getStore();
            World world = store.getExternalData().getWorld();
            world.execute(() -> {
               if (entityRef.isValid()) {
                  Player playerComponent = store.getComponent(entityRef, Player.getComponentType());
                  if (playerComponent != null) {
                     HotbarManager hotbarManager = playerComponent.getHotbarManager();
                     if (!hotbarManager.getIsCurrentlyLoadingHotbar()) {
                        if (playerComponent.getGameMode().equals(GameMode.Creative)) {
                           if (event.getItemContainer().equals(playerComponent.getInventory().getHotbar())) {
                              hotbarManager.saveHotbar(entityRef, (short)hotbarManager.getCurrentHotbarIndex(), store);
                           }
                        }
                     }
                  }
               }
            });
         }
      });
      ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
      this.physicsValuesComponentType = entityStoreRegistry.registerComponent(PhysicsValues.class, PhysicsValues::new);
      this.velocityComponentType = entityStoreRegistry.registerComponent(Velocity.class, "Velocity", Velocity.CODEC);
      this.migrationSystemType = entityStoreRegistry.registerSystemType(EntityModule.MigrationSystem.class);
      this.velocityModifyingSystemType = entityStoreRegistry.registerSystemType(IVelocityModifyingSystem.class);
      this.boundingBoxComponentType = entityStoreRegistry.registerComponent(BoundingBox.class, BoundingBox::new);
      this.entityScaleComponentType = entityStoreRegistry.registerComponent(EntityScaleComponent.class, "EntityScale", EntityScaleComponent.CODEC);
      this.transformComponentType = entityStoreRegistry.registerComponent(TransformComponent.class, "Transform", TransformComponent.CODEC);
      this.headRotationComponentType = entityStoreRegistry.registerComponent(HeadRotation.class, "HeadRotation", HeadRotation.CODEC);
      this.uuidComponentType = entityStoreRegistry.registerComponent(UUIDComponent.class, "UUID", UUIDComponent.CODEC);
      this.collisionResultComponentType = entityStoreRegistry.registerComponent(CollisionResultComponent.class, CollisionResultComponent::new);
      this.networkIdComponentType = entityStoreRegistry.registerComponent(NetworkId.class, () -> {
         throw new UnsupportedOperationException("Not implemented");
      });
      this.rotateObjectComponentType = entityStoreRegistry.registerComponent(RotateObjectComponent.class, "RotateObject", RotateObjectComponent.CODEC);
      this.effectControllerComponentType = entityStoreRegistry.registerComponent(
         EffectControllerComponent.class, "EffectController", EffectControllerComponent.CODEC
      );
      this.interactableComponentType = entityStoreRegistry.registerComponent(Interactable.class, "Interactable", Interactable.CODEC);
      this.intangibleComponentType = entityStoreRegistry.registerComponent(Intangible.class, "Intangible", Intangible.CODEC);
      this.preventPickupComponentType = entityStoreRegistry.registerComponent(PreventPickup.class, "PreventPickup", PreventPickup.CODEC);
      this.invulnerableComponentType = entityStoreRegistry.registerComponent(Invulnerable.class, "Invulnerable", Invulnerable.CODEC);
      this.respondToHitComponentType = entityStoreRegistry.registerComponent(RespondToHit.class, "RespondToHit", RespondToHit.CODEC);
      this.applyRandomSkinPersistedComponent = entityStoreRegistry.registerComponent(
         ApplyRandomSkinPersistedComponent.class, "ApplyRandomSkinPersisted", ApplyRandomSkinPersistedComponent.CODEC
      );
      this.audioComponentType = entityStoreRegistry.registerComponent(AudioComponent.class, AudioComponent::new);
      this.movementAudioComponentType = entityStoreRegistry.registerComponent(MovementAudioComponent.class, MovementAudioComponent::new);
      this.positionDataComponentType = entityStoreRegistry.registerComponent(PositionDataComponent.class, PositionDataComponent::new);
      this.activeAnimationComponentType = entityStoreRegistry.registerComponent(ActiveAnimationComponent.class, ActiveAnimationComponent::new);
      this.newSpawnComponentType = entityStoreRegistry.registerComponent(NewSpawnComponent.class, () -> {
         throw new UnsupportedOperationException("Not implemented");
      });
      entityStoreRegistry.registerSystem(new EntityStore.NetworkIdSystem());
      entityStoreRegistry.registerSystem(new EntityStore.UUIDSystem());
      entityStoreRegistry.registerSystem(new VelocitySystems.AddSystem(this.velocityComponentType));
      entityStoreRegistry.registerSystem(new TangiableEntitySpatialSystem(CollisionModule.get().getTangibleEntitySpatialResourceType()));
      SystemGroup<EntityStore> _trackerGroup = EntityTrackerSystems.FIND_VISIBLE_ENTITIES_GROUP;
      this.visibleComponentType = entityStoreRegistry.registerComponent(EntityTrackerSystems.Visible.class, EntityTrackerSystems.Visible::new);
      entityStoreRegistry.registerSystem(new TransformSystems.EntityTrackerUpdate());
      this.blockEntityComponentType = entityStoreRegistry.registerComponent(BlockEntity.class, "BlockEntity", BlockEntity.CODEC);
      this.projectileComponentType = entityStoreRegistry.registerComponent(ProjectileComponent.class, "ProjectileComponent", ProjectileComponent.CODEC);
      entityStoreRegistry.registerSystem(new BlockEntitySystems.Ticking(this.blockEntityComponentType));
      entityStoreRegistry.registerSystem(new BlockEntitySystems.BlockEntitySetupSystem(this.blockEntityComponentType));
      entityStoreRegistry.registerSystem(new LegacyProjectileSystems.OnAddRefSystem());
      entityStoreRegistry.registerSystem(new LegacyProjectileSystems.OnAddHolderSystem());
      entityStoreRegistry.registerSystem(
         new LegacyProjectileSystems.TickingSystem(
            this.projectileComponentType, this.transformComponentType, this.velocityComponentType, this.boundingBoxComponentType
         )
      );
      entityStoreRegistry.registerSystem(new RotateObjectSystem(this.transformComponentType, this.rotateObjectComponentType));
      this.snapshotWorldInfoResourceType = entityStoreRegistry.registerResource(SnapshotSystems.SnapshotWorldInfo.class, SnapshotSystems.SnapshotWorldInfo::new);
      this.snapshotBufferComponentType = entityStoreRegistry.registerComponent(SnapshotBuffer.class, SnapshotBuffer::new);
      entityStoreRegistry.registerSystem(new SnapshotSystems.Add());
      entityStoreRegistry.registerSystem(new SnapshotSystems.Resize());
      entityStoreRegistry.registerSystem(new SnapshotSystems.Capture());
      entityStoreRegistry.registerSystem(new UpdateEntitySeedSystem());
      entityStoreRegistry.registerSystem(new EntityModule.LegacyTransformSystem());
      entityStoreRegistry.registerSystem(new EntityModule.LegacyUUIDSystem());
      entityStoreRegistry.registerSystem(new EntityModule.LegacyUUIDUpdateSystem());
      entityStoreRegistry.registerSystem(new EntitySystems.UnloadEntityFromChunk());
      this.teleportComponentType = entityStoreRegistry.registerComponent(Teleport.class, () -> {
         throw new UnsupportedOperationException("Teleport must be created directly");
      });
      this.pendingTeleportComponentType = entityStoreRegistry.registerComponent(PendingTeleport.class, PendingTeleport::new);
      this.teleportRecordComponentType = entityStoreRegistry.registerComponent(TeleportRecord.class, TeleportRecord::new);
      this.playerComponentType = entityStoreRegistry.registerComponent(Player.class, "Player", Player.CODEC);
      this.frozenComponentType = entityStoreRegistry.registerComponent(Frozen.class, "Frozen", Frozen.CODEC);
      entityStoreRegistry.registerSystem(new PlayerCollisionResultAddSystem(this.playerComponentType, this.collisionResultComponentType));
      this.playerSettingsComponentType = entityStoreRegistry.registerComponent(PlayerSettings.class, PlayerSettings::defaults);
      this.movementStatesComponentType = entityStoreRegistry.registerComponent(MovementStatesComponent.class, MovementStatesComponent::new);
      entityStoreRegistry.registerSystem(new MovementStatesSystems.AddSystem(this.movementStatesComponentType));
      entityStoreRegistry.registerSystem(new MovementStatesSystems.PlayerInitSystem(this.playerComponentType, this.movementStatesComponentType));
      entityStoreRegistry.registerSystem(new TeleportSystems.MoveSystem());
      entityStoreRegistry.registerSystem(new TeleportSystems.PlayerMoveSystem());
      entityStoreRegistry.registerSystem(new TeleportSystems.PlayerMoveCompleteSystem());
      this.modelComponentType = entityStoreRegistry.registerComponent(ModelComponent.class, () -> {
         throw new UnsupportedOperationException();
      });
      this.persistentModelComponentType = entityStoreRegistry.registerComponent(PersistentModel.class, "Model", PersistentModel.CODEC);
      this.propComponentType = entityStoreRegistry.registerComponent(PropComponent.class, "Prop", PropComponent.CODEC);
      this.npcMarkerComponentType = entityStoreRegistry.registerComponent(NPCMarkerComponent.class, NPCMarkerComponent::get);
      entityStoreRegistry.registerSystem(new EntityModule.LegacyEntityHolderSystem<>(this.playerComponentType), true);
      entityStoreRegistry.registerSystem(new EntityModule.LegacyEntityRefSystem<>(this.playerComponentType), true);
      this.playerInputComponentType = entityStoreRegistry.registerComponent(PlayerInput.class, PlayerInput::new);
      this.knockbackSimulationComponentType = entityStoreRegistry.registerComponent(KnockbackSimulation.class, () -> {
         throw new UnsupportedOperationException();
      });
      this.movementManagerComponentType = entityStoreRegistry.registerComponent(MovementManager.class, MovementManager::new);
      this.displayNameComponentType = entityStoreRegistry.registerComponent(DisplayNameComponent.class, "DisplayName", DisplayNameComponent.CODEC);
      entityStoreRegistry.registerSystem(new PlayerSystems.PlayerSpawnedSystem());
      entityStoreRegistry.registerSystem(new PlayerSystems.PlayerAddedSystem(this.movementManagerComponentType));
      entityStoreRegistry.registerSystem(new PlayerSystems.EnsurePlayerInput());
      entityStoreRegistry.registerSystem(new PlayerSystems.EnsureEffectControllerSystem());
      entityStoreRegistry.registerSystem(new PlayerSystems.PlayerRemovedSystem());
      entityStoreRegistry.registerSystem(new PlayerSystems.ProcessPlayerInput());
      entityStoreRegistry.registerSystem(new PlayerSystems.UpdatePlayerRef());
      entityStoreRegistry.registerSystem(new PlayerSystems.BlockPausedMovementSystem());
      entityStoreRegistry.registerSystem(new PlayerSystems.KillFeedKillerEventSystem());
      entityStoreRegistry.registerSystem(new PlayerSystems.KillFeedDecedentEventSystem());
      entityStoreRegistry.registerSystem(new KnockbackPredictionSystems.InitKnockback());
      entityStoreRegistry.registerSystem(new KnockbackPredictionSystems.CaptureKnockbackInput());
      entityStoreRegistry.registerSystem(new KnockbackPredictionSystems.SimulateKnockback());
      entityStoreRegistry.registerSystem(new KnockbackPredictionSystems.ClearOnTeleport());
      entityStoreRegistry.registerSystem(new KnockbackPredictionSystems.ClearOnRemove());
      this.preClearMarkersGroup = entityStoreRegistry.registerSystemGroup();
      this.playerSkinComponentType = entityStoreRegistry.registerComponent(PlayerSkinComponent.class, () -> {
         throw new UnsupportedOperationException("Not implemented");
      });
      this.fromPrefabComponentType = entityStoreRegistry.registerComponent(FromPrefab.class, "FromPrefab", FromPrefab.CODEC);
      entityStoreRegistry.registerSystem(new EntitySystems.ClearFromPrefabMarker(this.fromPrefabComponentType, this.preClearMarkersGroup));
      this.hiddenFromAdventurePlayerComponentType = entityStoreRegistry.registerComponent(
         HiddenFromAdventurePlayers.class, "HiddenFromAdventurePlayer", HiddenFromAdventurePlayers.CODEC
      );
      entityStoreRegistry.registerSystem(new PlayerMovementManagerSystems.AssignmentSystem());
      entityStoreRegistry.registerSystem(new PlayerMovementManagerSystems.PostAssignmentSystem());
      this.cameraManagerComponentType = entityStoreRegistry.registerComponent(CameraManager.class, CameraManager::new);
      entityStoreRegistry.registerSystem(new PlayerCameraAddSystem());
      entityStoreRegistry.registerSystem(new PlayerHudManagerSystems.InitializeSystem());
      this.fromWorldGenComponentType = entityStoreRegistry.registerComponent(FromWorldGen.class, "FromWorldGen", FromWorldGen.CODEC);
      entityStoreRegistry.registerSystem(new EntitySystems.ClearFromWorldGenMarker(this.fromWorldGenComponentType, this.preClearMarkersGroup));
      this.worldGenIdComponentType = entityStoreRegistry.registerComponent(WorldGenId.class, "WorldGenId", WorldGenId.CODEC);
      entityStoreRegistry.registerSystem(
         new EntitySystems.OnLoadFromExternal(this.fromPrefabComponentType, this.fromWorldGenComponentType, this.preClearMarkersGroup)
      );
      this.playerSpatialResourceType = entityStoreRegistry.registerSpatialResource(() -> new KDTree<>(Ref::isValid));
      entityStoreRegistry.registerSystem(new PlayerSpatialSystem(this.playerSpatialResourceType));
      this.entitySpatialResourceType = entityStoreRegistry.registerSpatialResource(() -> new KDTree<>(Ref::isValid));
      entityStoreRegistry.registerSystem(new EntitySpatialSystem(this.entitySpatialResourceType));
      this.despawnComponentComponentType = entityStoreRegistry.registerComponent(DespawnComponent.class, "Despawn", DespawnComponent.CODEC);
      this.dynamicLightComponentType = entityStoreRegistry.registerComponent(DynamicLight.class, DynamicLight::new);
      this.persistentDynamicLightComponentType = entityStoreRegistry.registerComponent(
         PersistentDynamicLight.class, "DynamicLight", PersistentDynamicLight.CODEC
      );
      this.preventItemMergingType = entityStoreRegistry.registerComponent(PreventItemMerging.class, "PreventItemMerging", PreventItemMerging.CODEC);
      this.itemComponentType = entityStoreRegistry.registerComponent(ItemComponent.class, "Item", ItemComponent.CODEC);
      this.itemPhysicsComponentType = entityStoreRegistry.registerComponent(ItemPhysicsComponent.class, ItemPhysicsComponent::new);
      entityStoreRegistry.registerSystem(new ItemSystems.EnsureRequiredComponents());
      entityStoreRegistry.registerSystem(new ItemSystems.TrackerSystem(this.visibleComponentType));
      this.prefabCopyableComponentType = entityStoreRegistry.registerComponent(PrefabCopyableComponent.class, "PrefabCopyable", PrefabCopyableComponent.CODEC);
      this.pickupItemComponentType = entityStoreRegistry.registerComponent(PickupItemComponent.class, PickupItemComponent::new);
      entityStoreRegistry.registerSystem(new DespawnSystem(this.despawnComponentComponentType));
      this.itemSpatialResourceType = entityStoreRegistry.registerSpatialResource(() -> new KDTree<>(Ref::isValid));
      entityStoreRegistry.registerSystem(new ItemSpatialSystem(this.itemSpatialResourceType));
      entityStoreRegistry.registerSystem(new ItemMergeSystem(this.itemComponentType, this.interactableComponentType, this.itemSpatialResourceType));
      entityStoreRegistry.registerSystem(new PlayerItemEntityPickupSystem(this.itemComponentType, this.playerComponentType, this.playerSpatialResourceType));
      entityStoreRegistry.registerSystem(
         new ItemPrePhysicsSystem(
            this.itemComponentType, this.boundingBoxComponentType, this.velocityComponentType, this.transformComponentType, this.physicsValuesComponentType
         )
      );
      entityStoreRegistry.registerSystem(new ItemPhysicsSystem(this.itemPhysicsComponentType, this.velocityComponentType, this.boundingBoxComponentType));
      entityStoreRegistry.registerSystem(new PickupItemSystem(this.pickupItemComponentType, this.transformComponentType));
      entityStoreRegistry.registerSystem(new LivingEntityEffectSystem());
      entityStoreRegistry.registerSystem(
         new PlayerProcessMovementSystem(this.playerComponentType, this.velocityComponentType, this.collisionResultComponentType)
      );
      this.chunkTrackerComponentType = entityStoreRegistry.registerComponent(ChunkTracker.class, ChunkTracker::new);
      entityStoreRegistry.registerSystem(new PlayerChunkTrackerSystems.AddSystem());
      entityStoreRegistry.registerSystem(new PlayerChunkTrackerSystems.UpdateSystem());
      entityStoreRegistry.registerSystem(new PlayerSystems.NameplateRefSystem());
      entityStoreRegistry.registerSystem(new PlayerSystems.NameplateRefChangeSystem());
      this.entityViewerComponentType = entityStoreRegistry.registerComponent(EntityTrackerSystems.EntityViewer.class, () -> {
         throw new UnsupportedOperationException("not supported");
      });
      entityStoreRegistry.registerSystem(new EntityTrackerSystems.ClearEntityViewers(this.entityViewerComponentType));
      entityStoreRegistry.registerSystem(new EntityTrackerSystems.ClearPreviouslyVisible(this.visibleComponentType));
      entityStoreRegistry.registerSystem(new EntityTrackerSystems.EnsureVisibleComponent(this.entityViewerComponentType, this.visibleComponentType));
      entityStoreRegistry.registerSystem(new EntityTrackerSystems.AddToVisible(this.entityViewerComponentType, this.visibleComponentType));
      entityStoreRegistry.registerSystem(new EntityTrackerSystems.RemoveEmptyVisibleComponent(this.visibleComponentType));
      entityStoreRegistry.registerSystem(new EntityTrackerSystems.RemoveVisibleComponent(this.visibleComponentType));
      entityStoreRegistry.registerSystem(new EntityTrackerSystems.SendPackets(this.entityViewerComponentType));
      entityStoreRegistry.registerSystem(new MovementStatesSystems.TickingSystem(this.visibleComponentType, this.movementStatesComponentType));
      this.networkSendableSpatialResourceType = entityStoreRegistry.registerSpatialResource(() -> new KDTree<>(Ref::isValid));
      entityStoreRegistry.registerSystem(new NetworkSendableSpatialSystem(this.networkSendableSpatialResourceType));
      entityStoreRegistry.registerSystem(new EntityTrackerSystems.CollectVisible(this.entityViewerComponentType));
      entityStoreRegistry.registerSystem(new LegacyEntityTrackerSystems.LegacyLODCull(this.entityViewerComponentType));
      entityStoreRegistry.registerSystem(new LegacyEntityTrackerSystems.LegacyHideFromEntity(this.entityViewerComponentType));
      entityStoreRegistry.registerSystem(new LegacyEntityTrackerSystems.LegacyEntityModel(this.visibleComponentType));
      entityStoreRegistry.registerSystem(new LegacyEntityTrackerSystems.LegacyEntitySkin(this.visibleComponentType, this.playerSkinComponentType));
      entityStoreRegistry.registerSystem(new BlockEntitySystems.BlockEntityTrackerSystem(this.visibleComponentType, this.blockEntityComponentType));
      entityStoreRegistry.registerSystem(new LegacyEntityTrackerSystems.LegacyEquipment(this.visibleComponentType));
      entityStoreRegistry.registerSystem(new EntityTrackerSystems.EffectControllerSystem(this.visibleComponentType, this.effectControllerComponentType));
      entityStoreRegistry.registerSystem(new EntitySystems.DynamicLightTracker(this.visibleComponentType));
      entityStoreRegistry.registerSystem(new DynamicLightSystems.Setup());
      entityStoreRegistry.registerSystem(new DynamicLightSystems.EntityTrackerRemove(this.visibleComponentType));
      entityStoreRegistry.registerSystem(new LivingEntityEffectClearChangesSystem());
      entityStoreRegistry.registerSystem(new PlayerSendInventorySystem(this.playerComponentType));
      entityStoreRegistry.registerSystem(new PlayerSavingSystems.WorldRemovedSystem(this.playerComponentType));
      entityStoreRegistry.registerSystem(new PlayerSavingSystems.TickingSystem(this.playerComponentType));
      this.entityGroupComponentType = entityStoreRegistry.registerComponent(EntityGroup.class, () -> {
         throw new UnsupportedOperationException("Not implemented");
      });
      this.damageDataComponentType = entityStoreRegistry.registerComponent(DamageDataComponent.class, DamageDataComponent::new);
      entityStoreRegistry.registerSystem(new DamageDataSetupSystem(this.damageDataComponentType));
      this.knockbackComponentType = entityStoreRegistry.registerComponent(KnockbackComponent.class, KnockbackComponent::new);
      entityStoreRegistry.registerSystem(new UpdateLocationSystems.SpawnSystem());
      entityStoreRegistry.registerSystem(new UpdateLocationSystems.TickingSystem());
      this.persistentRefCountComponentType = entityStoreRegistry.registerComponent(PersistentRefCount.class, "RefId", PersistentRefCount.CODEC);
      this.nameplateComponentType = entityStoreRegistry.registerComponent(Nameplate.class, "Nameplate", Nameplate.CODEC);
      entityStoreRegistry.registerSystem(new NameplateSystems.EntityTrackerUpdate(this.visibleComponentType, this.nameplateComponentType));
      entityStoreRegistry.registerSystem(new NameplateSystems.EntityTrackerRemove(this.visibleComponentType, this.nameplateComponentType));
      this.interactableQueueResourceType = entityStoreRegistry.registerResource(
         EntityInteractableSystems.QueueResource.class, EntityInteractableSystems.QueueResource::new
      );
      entityStoreRegistry.registerSystem(new EntityInteractableSystems.EntityTrackerUpdate(this.visibleComponentType));
      entityStoreRegistry.registerSystem(new EntityInteractableSystems.EntityTrackerAddAndRemove(this.visibleComponentType));
      this.intangibleQueueResourceType = entityStoreRegistry.registerResource(IntangibleSystems.QueueResource.class, IntangibleSystems.QueueResource::new);
      entityStoreRegistry.registerSystem(new IntangibleSystems.EntityTrackerUpdate(this.visibleComponentType));
      entityStoreRegistry.registerSystem(new IntangibleSystems.EntityTrackerAddAndRemove(this.visibleComponentType));
      entityStoreRegistry.registerSystem(new EntityModule.TangibleMigrationSystem(ProjectileComponent.getComponentType()), true);
      this.invulnerableQueueResourceType = entityStoreRegistry.registerResource(InvulnerableSystems.QueueResource.class, InvulnerableSystems.QueueResource::new);
      entityStoreRegistry.registerSystem(new InvulnerableSystems.EntityTrackerUpdate(this.visibleComponentType));
      entityStoreRegistry.registerSystem(new InvulnerableSystems.EntityTrackerAddAndRemove(this.visibleComponentType));
      this.respondToHitQueueResourceType = entityStoreRegistry.registerResource(RespondToHitSystems.QueueResource.class, RespondToHitSystems.QueueResource::new);
      entityStoreRegistry.registerSystem(new RespondToHitSystems.EntityTrackerUpdate(this.visibleComponentType));
      entityStoreRegistry.registerSystem(new RespondToHitSystems.EntityTrackerAddAndRemove(this.visibleComponentType));
      entityStoreRegistry.registerSystem(new RespondToHitSystems.OnPlayerSettingsChange());
      entityStoreRegistry.registerSystem(new AudioSystems.EntityTrackerUpdate());
      entityStoreRegistry.registerSystem(new AudioSystems.TickMovementAudio());
      entityStoreRegistry.registerSystem(new ModelSystems.SetRenderedModel());
      entityStoreRegistry.registerSystem(new ModelSystems.AssignNetworkIdToProps());
      entityStoreRegistry.registerSystem(new ModelSystems.EnsurePropsPrefabCopyable());
      entityStoreRegistry.registerSystem(new ModelSystems.ApplyRandomSkin());
      entityStoreRegistry.registerSystem(new ModelSystems.ModelSpawned());
      entityStoreRegistry.registerSystem(new ModelSystems.PlayerConnect());
      entityStoreRegistry.registerSystem(new ModelSystems.ModelChange());
      entityStoreRegistry.registerSystem(new ModelSystems.UpdateBoundingBox());
      entityStoreRegistry.registerSystem(new ModelSystems.UpdateMovementStateBoundingBox());
      entityStoreRegistry.registerSystem(new ModelSystems.PlayerUpdateMovementManager());
      entityStoreRegistry.registerSystem(new ModelSystems.AnimationEntityTrackerUpdate());
      entityStoreRegistry.registerSystem(new EntitySystems.NewSpawnEntityTrackerUpdate());
      entityStoreRegistry.registerSystem(new HideEntitySystems.AdventurePlayerSystem());
      entityStoreRegistry.registerSystem(new TransformSystems.OnRemove());
      entityStoreRegistry.registerSystem(new PhysicsValuesAddSystem(this.physicsValuesComponentType));
      entityStoreRegistry.registerSystem(new GenericVelocityInstructionSystem());
      entityStoreRegistry.registerSystem(new PlayerVelocityInstructionSystem());
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                           HitboxCollisionConfig.class, new IndexedLookupTableAssetMap<>(HitboxCollisionConfig[]::new)
                        )
                        .setPath("Entity/HitboxCollision"))
                     .setCodec(HitboxCollisionConfig.CODEC))
                  .setKeyFunction(HitboxCollisionConfig::getId))
               .setPacketGenerator(new HitboxCollisionConfigPacketGenerator())
               .setReplaceOnRemove(HitboxCollisionConfig::new))
            .build()
      );
      this.hitboxCollisionComponentType = entityStoreRegistry.registerComponent(HitboxCollision.class, "HitboxCollision", HitboxCollision.CODEC);
      entityStoreRegistry.registerSystem(new HitboxCollisionSystems.Setup(this.hitboxCollisionComponentType, this.playerComponentType));
      entityStoreRegistry.registerSystem(new HitboxCollisionSystems.EntityTrackerUpdate(this.visibleComponentType, this.hitboxCollisionComponentType));
      entityStoreRegistry.registerSystem(new HitboxCollisionSystems.EntityTrackerRemove(this.visibleComponentType, this.hitboxCollisionComponentType));
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                           RepulsionConfig.class, new IndexedLookupTableAssetMap<>(RepulsionConfig[]::new)
                        )
                        .setPath("Entity/Repulsion"))
                     .setCodec(RepulsionConfig.CODEC))
                  .setKeyFunction(RepulsionConfig::getId))
               .setPacketGenerator(new RepulsionConfigPacketGenerator())
               .setReplaceOnRemove(RepulsionConfig::new))
            .build()
      );
      this.repulsionComponentType = entityStoreRegistry.registerComponent(Repulsion.class, "Repulsion", Repulsion.CODEC);
      entityStoreRegistry.registerSystem(new RepulsionSystems.PlayerSetup(this.repulsionComponentType, this.playerComponentType));
      entityStoreRegistry.registerSystem(new RepulsionSystems.EntityTrackerUpdate(this.visibleComponentType, this.repulsionComponentType));
      entityStoreRegistry.registerSystem(new RepulsionSystems.EntityTrackerRemove(this.visibleComponentType, this.repulsionComponentType));
      entityStoreRegistry.registerSystem(
         new RepulsionSystems.RepulsionTicker(this.repulsionComponentType, this.transformComponentType, this.entitySpatialResourceType)
      );
      entityStoreRegistry.registerSystem(new EntitySystems.NewSpawnTick());
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                                 MovementConfig.class, new IndexedLookupTableAssetMap<>(MovementConfig[]::new)
                              )
                              .setPath("Entity/MovementConfig"))
                           .setCodec(MovementConfig.CODEC))
                        .setKeyFunction(MovementConfig::getId))
                     .setReplaceOnRemove(MovementConfig::new))
                  .loadsBefore(GameplayConfig.class))
               .preLoadAssets(Collections.singletonList(MovementConfig.DEFAULT_MOVEMENT)))
            .build()
      );
      this.getEventRegistry().register(LoadedAssetsEvent.class, MovementConfig.class, this::onMovementConfigLoadedAssetsEvent);
      this.getEventRegistry().register(LoadedAssetsEvent.class, GameplayConfig.class, this::onGameplayConfigLoadedAssetsEvent);
      this.uniqueItemUsagesComponentType = entityStoreRegistry.registerComponent(
         UniqueItemUsagesComponent.class, "UniqueItemUsages", UniqueItemUsagesComponent.CODEC
      );
      entityStoreRegistry.registerSystem(new PlayerSystems.EnsureUniqueItemUsagesSystem());
   }

   @Override
   protected void start() {
      DamageCause.PHYSICAL = DamageCause.getAssetMap().getAsset("Physical");
      DamageCause.PROJECTILE = DamageCause.getAssetMap().getAsset("Projectile");
      DamageCause.COMMAND = DamageCause.getAssetMap().getAsset("Command");
      DamageCause.DROWNING = DamageCause.getAssetMap().getAsset("Drowning");
      DamageCause.ENVIRONMENT = DamageCause.getAssetMap().getAsset("Environment");
      DamageCause.FALL = DamageCause.getAssetMap().getAsset("Fall");
      DamageCause.OUT_OF_WORLD = DamageCause.getAssetMap().getAsset("OutOfWorld");
      DamageCause.SUFFOCATION = DamageCause.getAssetMap().getAsset("Suffocation");
      if (DamageCause.PHYSICAL == null
         || DamageCause.PROJECTILE == null
         || DamageCause.COMMAND == null
         || DamageCause.DROWNING == null
         || DamageCause.ENVIRONMENT == null
         || DamageCause.FALL == null
         || DamageCause.OUT_OF_WORLD == null
         || DamageCause.SUFFOCATION == null) {
         throw new IllegalStateException("Missing default DamageCause assets");
      }
   }

   public SystemType<EntityStore, EntityModule.MigrationSystem> getMigrationSystemType() {
      return this.migrationSystemType;
   }

   public SystemType<EntityStore, ? extends ISystem<EntityStore>> getVelocityModifyingSystemType() {
      return this.velocityModifyingSystemType;
   }

   public ComponentType<EntityStore, Player> getPlayerComponentType() {
      return this.playerComponentType;
   }

   public ComponentType<EntityStore, Frozen> getFrozenComponentType() {
      return this.frozenComponentType;
   }

   public ComponentType<EntityStore, ChunkTracker> getChunkTrackerComponentType() {
      return this.chunkTrackerComponentType;
   }

   public ComponentType<EntityStore, PlayerSkinComponent> getPlayerSkinComponentType() {
      return this.playerSkinComponentType;
   }

   public ComponentType<EntityStore, DisplayNameComponent> getDisplayNameComponentType() {
      return this.displayNameComponentType;
   }

   public ComponentType<EntityStore, ApplyRandomSkinPersistedComponent> getApplyRandomSkinPersistedComponent() {
      return this.applyRandomSkinPersistedComponent;
   }

   public ComponentType<EntityStore, EntityGroup> getEntityGroupComponentType() {
      return this.entityGroupComponentType;
   }

   public ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> getPlayerSpatialResourceType() {
      return this.playerSpatialResourceType;
   }

   public ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> getItemSpatialResourceType() {
      return this.itemSpatialResourceType;
   }

   public ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> getNetworkSendableSpatialResourceType() {
      return this.networkSendableSpatialResourceType;
   }

   public ComponentType<EntityStore, CollisionResultComponent> getCollisionResultComponentType() {
      return this.collisionResultComponentType;
   }

   public ComponentType<EntityStore, EntityTrackerSystems.EntityViewer> getEntityViewerComponentType() {
      return this.entityViewerComponentType;
   }

   public ComponentType<EntityStore, EntityTrackerSystems.Visible> getVisibleComponentType() {
      return this.visibleComponentType;
   }

   public ComponentType<EntityStore, DamageDataComponent> getDamageDataComponentType() {
      return this.damageDataComponentType;
   }

   public ComponentType<EntityStore, KnockbackComponent> getKnockbackComponentType() {
      return this.knockbackComponentType;
   }

   public ComponentType<EntityStore, DespawnComponent> getDespawnComponentType() {
      return this.despawnComponentComponentType;
   }

   public ResourceType<EntityStore, SnapshotSystems.SnapshotWorldInfo> getSnapshotWorldInfoResourceType() {
      return this.snapshotWorldInfoResourceType;
   }

   public ComponentType<EntityStore, SnapshotBuffer> getSnapshotBufferComponentType() {
      return this.snapshotBufferComponentType;
   }

   public ComponentType<EntityStore, Interactable> getInteractableComponentType() {
      return this.interactableComponentType;
   }

   public ComponentType<EntityStore, Intangible> getIntangibleComponentType() {
      return this.intangibleComponentType;
   }

   public ComponentType<EntityStore, PreventPickup> getPreventPickupComponentType() {
      return this.preventPickupComponentType;
   }

   public ComponentType<EntityStore, Invulnerable> getInvulnerableComponentType() {
      return this.invulnerableComponentType;
   }

   public ComponentType<EntityStore, RespondToHit> getRespondToHitComponentType() {
      return this.respondToHitComponentType;
   }

   public ResourceType<EntityStore, EntityInteractableSystems.QueueResource> getInteractableQueueResourceType() {
      return this.interactableQueueResourceType;
   }

   public ResourceType<EntityStore, IntangibleSystems.QueueResource> getIntangibleQueueResourceType() {
      return this.intangibleQueueResourceType;
   }

   public ResourceType<EntityStore, InvulnerableSystems.QueueResource> getInvulnerableQueueResourceType() {
      return this.invulnerableQueueResourceType;
   }

   public ResourceType<EntityStore, RespondToHitSystems.QueueResource> getRespondToHitQueueResourceType() {
      return this.respondToHitQueueResourceType;
   }

   public ComponentType<EntityStore, HiddenFromAdventurePlayers> getHiddenFromAdventurePlayerComponentType() {
      return this.hiddenFromAdventurePlayerComponentType;
   }

   public ComponentType<EntityStore, FromPrefab> getFromPrefabComponentType() {
      return this.fromPrefabComponentType;
   }

   public ComponentType<EntityStore, FromWorldGen> getFromWorldGenComponentType() {
      return this.fromWorldGenComponentType;
   }

   public ComponentType<EntityStore, WorldGenId> getWorldGenIdComponentType() {
      return this.worldGenIdComponentType;
   }

   public ComponentType<EntityStore, MovementManager> getMovementManagerComponentType() {
      return this.movementManagerComponentType;
   }

   public ComponentType<EntityStore, Nameplate> getNameplateComponentType() {
      return this.nameplateComponentType;
   }

   public SystemGroup<EntityStore> getPreClearMarkersGroup() {
      return this.preClearMarkersGroup;
   }

   public ComponentType<EntityStore, PersistentRefCount> getPersistentRefCountComponentType() {
      return this.persistentRefCountComponentType;
   }

   public ComponentType<EntityStore, TransformComponent> getTransformComponentType() {
      return this.transformComponentType;
   }

   public ComponentType<EntityStore, HeadRotation> getHeadRotationComponentType() {
      return this.headRotationComponentType;
   }

   public ComponentType<EntityStore, NetworkId> getNetworkIdComponentType() {
      return this.networkIdComponentType;
   }

   public ComponentType<EntityStore, EffectControllerComponent> getEffectControllerComponentType() {
      return this.effectControllerComponentType;
   }

   public ComponentType<EntityStore, MovementStatesComponent> getMovementStatesComponentType() {
      return this.movementStatesComponentType;
   }

   public ComponentType<EntityStore, BlockEntity> getBlockEntityComponentType() {
      return this.blockEntityComponentType;
   }

   public ComponentType<EntityStore, EntityScaleComponent> getEntityScaleComponentType() {
      return this.entityScaleComponentType;
   }

   public ComponentType<EntityStore, CameraManager> getCameraManagerComponentType() {
      return this.cameraManagerComponentType;
   }

   public ComponentType<EntityStore, UUIDComponent> getUuidComponentType() {
      return this.uuidComponentType;
   }

   public ComponentType<EntityStore, PlayerInput> getPlayerInputComponentType() {
      return this.playerInputComponentType;
   }

   public ComponentType<EntityStore, KnockbackSimulation> getKnockbackSimulationComponentType() {
      return this.knockbackSimulationComponentType;
   }

   public ComponentType<EntityStore, Teleport> getTeleportComponentType() {
      return this.teleportComponentType;
   }

   public ComponentType<EntityStore, ProjectileComponent> getProjectileComponentType() {
      return this.projectileComponentType;
   }

   public ComponentType<EntityStore, PendingTeleport> getPendingTeleportComponentType() {
      return this.pendingTeleportComponentType;
   }

   public ComponentType<EntityStore, TeleportRecord> getTeleportRecordComponentType() {
      return this.teleportRecordComponentType;
   }

   public ComponentType<EntityStore, ModelComponent> getModelComponentType() {
      return this.modelComponentType;
   }

   public ComponentType<EntityStore, PersistentModel> getPersistentModelComponentType() {
      return this.persistentModelComponentType;
   }

   public ComponentType<EntityStore, PropComponent> getPropComponentType() {
      return this.propComponentType;
   }

   public ComponentType<EntityStore, NPCMarkerComponent> getNPCMarkerComponentType() {
      return this.npcMarkerComponentType;
   }

   public ComponentType<EntityStore, BoundingBox> getBoundingBoxComponentType() {
      return this.boundingBoxComponentType;
   }

   public ComponentType<EntityStore, HitboxCollision> getHitboxCollisionComponentType() {
      return this.hitboxCollisionComponentType;
   }

   public ComponentType<EntityStore, Velocity> getVelocityComponentType() {
      return this.velocityComponentType;
   }

   public ComponentType<EntityStore, PhysicsValues> getPhysicsValuesComponentType() {
      return this.physicsValuesComponentType;
   }

   public ComponentType<EntityStore, Repulsion> getRepulsionComponentType() {
      return this.repulsionComponentType;
   }

   public ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> getEntitySpatialResourceType() {
      return this.entitySpatialResourceType;
   }

   public ComponentType<EntityStore, ItemComponent> getItemComponentType() {
      return this.itemComponentType;
   }

   public ComponentType<EntityStore, PickupItemComponent> getPickupItemComponentType() {
      return this.pickupItemComponentType;
   }

   public ComponentType<EntityStore, PreventItemMerging> getPreventItemMergingType() {
      return this.preventItemMergingType;
   }

   public ComponentType<EntityStore, ItemPhysicsComponent> getItemPhysicsComponentType() {
      return this.itemPhysicsComponentType;
   }

   public ComponentType<EntityStore, DynamicLight> getDynamicLightComponentType() {
      return this.dynamicLightComponentType;
   }

   public ComponentType<EntityStore, PersistentDynamicLight> getPersistentDynamicLightComponentType() {
      return this.persistentDynamicLightComponentType;
   }

   public ComponentType<EntityStore, PrefabCopyableComponent> getPrefabCopyableComponentType() {
      return this.prefabCopyableComponentType;
   }

   public ComponentType<EntityStore, RotateObjectComponent> getRotateObjectComponentType() {
      return this.rotateObjectComponentType;
   }

   public ComponentType<EntityStore, NewSpawnComponent> getNewSpawnComponentType() {
      return this.newSpawnComponentType;
   }

   private void onMovementConfigLoadedAssetsEvent(@Nonnull LoadedAssetsEvent<String, MovementConfig, DefaultAssetMap<String, MovementConfig>> event) {
      Universe.get()
         .getWorlds()
         .forEach(
            (s, world) -> world.execute(
               () -> {
                  Store<EntityStore> store = world.getEntityStore().getStore();
                  store.forEachEntityParallel(
                     MovementManager.getComponentType(),
                     (index, archetypeChunk, commandBuffer) -> {
                        String gameplayMovementConfigId = world.getGameplayConfig().getPlayerConfig().getMovementConfigId();

                        for (MovementConfig movementConfig : event.getLoadedAssets().values()) {
                           Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
                           Player playerComponent = archetypeChunk.getComponent(index, Player.getComponentType());

                           assert playerComponent != null;

                           PlayerRef playerRefComponent = archetypeChunk.getComponent(index, PlayerRef.getComponentType());

                           assert playerRefComponent != null;

                           PacketHandler packetHandler = playerRefComponent.getPacketHandler();
                           if (movementConfig.getId().equals("Mount") && playerComponent.getMountEntityId() > 0) {
                              packetHandler.writeNoCache(new UpdateMovementSettings(movementConfig.toPacket()));
                              return;
                           }

                           if (gameplayMovementConfigId.equals(movementConfig.getId())) {
                              MovementManager movementManagerComponent = archetypeChunk.getComponent(index, MovementManager.getComponentType());

                              assert movementManagerComponent != null;

                              movementManagerComponent.setDefaultSettings(
                                 movementConfig.toPacket(), EntityUtils.getPhysicsValues(ref, store), playerComponent.getGameMode()
                              );
                              movementManagerComponent.applyDefaultSettings();
                              movementManagerComponent.update(packetHandler);
                           }
                        }
                     }
                  );
               }
            )
         );
   }

   private void onGameplayConfigLoadedAssetsEvent(LoadedAssetsEvent<String, GameplayConfig, DefaultAssetMap<String, GameplayConfig>> event) {
      Universe.get()
         .getWorlds()
         .forEach(
            (s, world) -> world.execute(
               () -> {
                  Store<EntityStore> store = world.getEntityStore().getStore();
                  store.forEachEntityParallel(
                     MovementManager.getComponentType(),
                     (index, archetypeChunk, commandBuffer) -> {
                        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
                        int gameplayMovementConfigIndex = world.getGameplayConfig().getPlayerConfig().getMovementConfigIndex();
                        MovementManager movementManagerComponent = archetypeChunk.getComponent(index, MovementManager.getComponentType());

                        assert movementManagerComponent != null;

                        Player playerComponent = archetypeChunk.getComponent(index, Player.getComponentType());

                        assert playerComponent != null;

                        PlayerRef playerRefComponent = archetypeChunk.getComponent(index, PlayerRef.getComponentType());

                        assert playerRefComponent != null;

                        MovementConfig movConfig = (MovementConfig)((IndexedLookupTableAssetMap)MovementConfig.getAssetStore().getAssetMap())
                           .getAsset(gameplayMovementConfigIndex);
                        movementManagerComponent.setDefaultSettings(
                           movConfig.toPacket(), EntityUtils.getPhysicsValues(ref, store), playerComponent.getGameMode()
                        );
                        movementManagerComponent.applyDefaultSettings();
                        PacketHandler packetHandler = playerRefComponent.getPacketHandler();
                        movementManagerComponent.update(packetHandler);
                     }
                  );
               }
            )
         );
   }

   public ComponentType<EntityStore, AudioComponent> getAudioComponentType() {
      return this.audioComponentType;
   }

   public ComponentType<EntityStore, MovementAudioComponent> getMovementAudioComponentType() {
      return this.movementAudioComponentType;
   }

   public ComponentType<EntityStore, PositionDataComponent> getPositionDataComponentType() {
      return this.positionDataComponentType;
   }

   public ComponentType<EntityStore, PlayerSettings> getPlayerSettingsComponentType() {
      return this.playerSettingsComponentType;
   }

   public ComponentType<EntityStore, UniqueItemUsagesComponent> getUniqueItemUsagesComponentType() {
      return this.uniqueItemUsagesComponentType;
   }

   public ComponentType<EntityStore, ActiveAnimationComponent> getActiveAnimationComponentType() {
      return this.activeAnimationComponentType;
   }

   @Nullable
   public <T extends Entity> EntityRegistration registerEntity(
      @Nonnull String id, @Nonnull Class<T> clazz, Function<World, T> entityConstructor, @Nullable DirectDecodeCodec<T> codec
   ) {
      if (this.isDisabled()) {
         return null;
      } else {
         this.idMap.put(id, clazz);
         this.classIdMap.put(clazz, id);
         this.classMap.put(clazz, entityConstructor);
         if (codec != null) {
            this.codecMap.put(clazz, codec);
         }

         ComponentType<EntityStore, T> componentType;
         if (codec != null) {
            componentType = this.getEntityStoreRegistry().registerComponent(clazz, id, (BuilderCodec<T>)codec, true);
         } else {
            componentType = this.getEntityStoreRegistry().registerComponent(clazz, () -> {
               throw new UnsupportedOperationException("Not implemented!");
            });
         }

         this.classToComponentType.put(clazz, componentType);
         this.getEntityStoreRegistry().registerSystem(new EntityModule.LegacyEntityHolderSystem<>(componentType), true);
         this.getEntityStoreRegistry().registerSystem(new EntityModule.LegacyEntityRefSystem<>(componentType), true);
         return new EntityRegistration(clazz, () -> this.getState() == PluginState.ENABLED, () -> this.unregisterEntity(clazz));
      }
   }

   private <T extends Entity> void unregisterEntity(Class<T> clazz) {
      if (!HytaleServer.get().isShuttingDown()) {
         String id = this.classIdMap.remove(clazz);
         this.idMap.remove(id);
         this.classMap.remove(clazz);
         this.codecMap.remove(clazz);
         ComponentType<EntityStore, ? extends Entity> componentType = this.classToComponentType.remove(clazz);
         EntityStore.REGISTRY.unregisterComponent(componentType);
      }
   }

   @Nullable
   public <T extends Entity> Function<World, T> getConstructor(@Nullable Class<T> entityClass) {
      if (this.isDisabled()) {
         return null;
      } else {
         return (Function<World, T>)(entityClass == null ? null : this.classMap.get(entityClass));
      }
   }

   @Nullable
   public <T extends Entity> DirectDecodeCodec<T> getCodec(@Nullable Class<T> entityClass) {
      if (this.isDisabled()) {
         return null;
      } else {
         return (DirectDecodeCodec<T>)(entityClass == null ? null : this.codecMap.get(entityClass));
      }
   }

   @Nullable
   public Class<? extends Entity> getClass(@Nullable String name) {
      if (this.isDisabled()) {
         return null;
      } else {
         return name == null ? null : this.idMap.get(name);
      }
   }

   @Nullable
   public String getIdentifier(@Nullable Class<? extends Entity> entityClass) {
      if (this.isDisabled()) {
         return null;
      } else {
         return entityClass == null ? null : this.classIdMap.get(entityClass);
      }
   }

   @Nullable
   public <T extends Entity> ComponentType<EntityStore, T> getComponentType(@Nullable Class<T> entityClass) {
      if (this.isDisabled()) {
         return null;
      } else if (entityClass == null) {
         return null;
      } else if (entityClass.equals(Player.class)) {
         throw new IllegalArgumentException("Get player component type via #getPlayerComponentType()");
      } else {
         return (ComponentType<EntityStore, T>)this.classToComponentType.get(entityClass);
      }
   }

   public boolean isKnown(@Nullable Entity entity) {
      return entity != null && this.getConstructor(entity.getClass()) != null;
   }

   @Deprecated(forRemoval = true)
   public static class HiddenFromPlayerMigrationSystem extends EntityModule.MigrationSystem {
      private final ComponentType<EntityStore, HiddenFromAdventurePlayers> hiddenFromAdventurePlayersComponentType = HiddenFromAdventurePlayers.getComponentType();
      @Nonnull
      private final Query<EntityStore> query;

      public HiddenFromPlayerMigrationSystem(Query<EntityStore> query) {
         this.query = Query.and(query, Query.not(this.hiddenFromAdventurePlayersComponentType));
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.ensureComponent(this.hiddenFromAdventurePlayersComponentType);
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }
   }

   public static class LegacyEntityHolderSystem<T extends Entity> extends HolderSystem<EntityStore> {
      private final ComponentType<EntityStore, T> componentType;

      public LegacyEntityHolderSystem(ComponentType<EntityStore, T> componentType) {
         this.componentType = componentType;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return RootDependency.firstSet();
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.componentType;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         T entityComponent = holder.getComponent(this.componentType);

         assert entityComponent != null;

         entityComponent.loadIntoWorld(store.getExternalData().getWorld());
         holder.putComponent(NetworkId.getComponentType(), new NetworkId(entityComponent.getNetworkId()));
         if (holder.getComponent(Player.getComponentType()) != null) {
            String displayName = entityComponent.getLegacyDisplayName();
            if (displayName != null && holder.getComponent(DisplayNameComponent.getComponentType()) == null) {
               holder.putComponent(DisplayNameComponent.getComponentType(), new DisplayNameComponent(Message.raw(displayName)));
            }
         }
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
         T entity = holder.getComponent(this.componentType);
         switch (reason) {
            case REMOVE:
               if (!entity.wasRemoved()) {
                  entity.remove();
                  entity.unloadFromWorld();
               }
               break;
            case UNLOAD:
               entity.unloadFromWorld();
               entity.clearReference();
         }
      }

      @Nonnull
      @Override
      public String toString() {
         return "LegacyEntityHolderSystem{componentType=" + this.componentType + "}";
      }
   }

   public static class LegacyEntityRefSystem<T extends Entity> extends RefSystem<EntityStore> {
      private final ComponentType<EntityStore, T> componentType;

      public LegacyEntityRefSystem(ComponentType<EntityStore, T> componentType) {
         this.componentType = componentType;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return RootDependency.firstSet();
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.componentType;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         T entityComponent = store.getComponent(ref, this.componentType);

         assert entityComponent != null;

         entityComponent.setReference(ref);
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      @Nonnull
      @Override
      public String toString() {
         return "LegacyEntityRefSystem{componentType=" + this.componentType + "}";
      }
   }

   public static class LegacyTransformSystem extends EntityModule.MigrationSystem {
      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         TransformComponent transformComponent = holder.getComponent(TransformComponent.getComponentType());
         Objects.requireNonNull(transformComponent);
         Entity entity = EntityUtils.getEntity(holder);
         entity.setTransformComponent(transformComponent);
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return RootDependency.firstSet();
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return AllLegacyEntityTypesQuery.INSTANCE;
      }
   }

   public static class LegacyUUIDSystem extends EntityModule.MigrationSystem {
      private final Set<Dependency<EntityStore>> dependencies = Set.of(
         new SystemDependency<>(Order.BEFORE, EntityStore.UUIDSystem.class), RootDependency.first()
      );

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         UUIDComponent uuid = holder.getComponent(UUIDComponent.getComponentType());
         Entity entity = EntityUtils.getEntity(holder);
         if (uuid == null) {
            UUID legacyUuid = entity.getUuid();
            if (legacyUuid != null) {
               holder.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(legacyUuid));
            }
         } else {
            entity.setLegacyUUID(uuid.getUuid());
         }
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return AllLegacyEntityTypesQuery.INSTANCE;
      }
   }

   public static class LegacyUUIDUpdateSystem extends RefChangeSystem<EntityStore, UUIDComponent> {
      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return AllLegacyEntityTypesQuery.INSTANCE;
      }

      @Nonnull
      @Override
      public ComponentType<EntityStore, UUIDComponent> componentType() {
         return UUIDComponent.getComponentType();
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull UUIDComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         EntityUtils.getEntity(ref, store).setLegacyUUID(component.getUuid());
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         UUIDComponent oldComponent,
         @Nonnull UUIDComponent newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         EntityUtils.getEntity(ref, store).setLegacyUUID(newComponent.getUuid());
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref, @Nonnull UUIDComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         EntityUtils.getEntity(ref, store).setLegacyUUID(null);
      }
   }

   public abstract static class MigrationSystem extends HolderSystem<EntityStore> {
   }

   @Deprecated(forRemoval = true)
   public static class TangibleMigrationSystem extends EntityModule.MigrationSystem {
      private final ComponentType<EntityStore, Intangible> intangibleComponentType = Intangible.getComponentType();
      @Nonnull
      private final Query<EntityStore> query;

      public TangibleMigrationSystem(Query<EntityStore> query) {
         this.query = Query.and(query, Query.not(this.intangibleComponentType));
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.ensureComponent(this.intangibleComponentType);
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }
   }

   public static enum Type {
      PLAYERS,
      ALL;
   }
}
