package com.hypixel.hytale.server.core.modules.interaction;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.event.RemovedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.MouseButtonType;
import com.hypixel.hytale.protocol.WorldInteraction;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.player.MouseInteraction;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.itemanimation.config.ItemPlayerAnimations;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.trail.config.Trail;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.CameraManager;
import com.hypixel.hytale.server.core.event.events.player.PlayerMouseButtonEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerMouseMotionEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionConfig;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.interaction.blocktrack.BlockCounter;
import com.hypixel.hytale.server.core.modules.interaction.blocktrack.TrackedPlacement;
import com.hypixel.hytale.server.core.modules.interaction.commands.InteractionCommand;
import com.hypixel.hytale.server.core.modules.interaction.components.PlacedByInteractionComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.InteractionPacketGenerator;
import com.hypixel.hytale.server.core.modules.interaction.interaction.RootInteractionPacketGenerator;
import com.hypixel.hytale.server.core.modules.interaction.interaction.UnarmedInteractions;
import com.hypixel.hytale.server.core.modules.interaction.interaction.UnarmedInteractionsPacketGenerator;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.AddItemInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ApplyForceInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.BlockConditionInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.BreakBlockInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ChainingInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ChangeBlockInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ChangeStateInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ChargingInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.CooldownConditionInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.CycleBlockGroupInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.DestroyBlockInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ExplodeInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.FirstClickInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.IncrementCooldownInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.MovementConditionInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.PickBlockInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.PlaceBlockInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.PlaceFluidInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ResetCooldownInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ToggleGliderInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.TriggerCooldownInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.UseBlockInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.UseEntityInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.WieldingInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.BuilderToolInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.CameraInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.CancelChainInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.ChainFlagInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.ChangeActiveSlotInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.ConditionInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.EffectConditionInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.ParallelInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.RepeatInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.ReplaceInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.RunRootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.SelectInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.SerialInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.StatsConditionInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.StatsConditionWithModifierInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.simple.ApplyEffectInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.simple.RemoveEntityInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.simple.SendMessageInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.AOECircleSelector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.AOECylinderSelector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.HorizontalSelector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.PlayerMatcher;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.RaycastSelector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.SelectorType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.StabSelector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.VulnerableMatcher;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.ChangeStatInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.ChangeStatWithModifierInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.CheckUniqueItemUsageInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.ClearEntityEffectInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.DamageEntityInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.DestroyConditionInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.DoorInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.EquipItemInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.IncreaseBackpackCapacityInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.InterruptInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.LaunchPadInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.LaunchProjectileInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.ModifyInventoryInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenContainerInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenItemStackContainerInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenPageInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.PlacementCountConditionInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.RefillContainerInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.RunOnBlockTypesInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.SpawnPrefabInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat.DirectionalKnockback;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat.ForceKnockback;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat.Knockback;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat.PointKnockback;
import com.hypixel.hytale.server.core.modules.interaction.suppliers.ItemRepairPageSupplier;
import com.hypixel.hytale.server.core.modules.interaction.system.InteractionSystems;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.meta.state.LaunchPad;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class InteractionModule extends JavaPlugin {
   @Nonnull
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(InteractionModule.class).depends(EntityModule.class).build();
   @Nonnull
   public static final EnumCodec<InteractionType> INTERACTION_TYPE_CODEC = new EnumCodec<>(InteractionType.class);
   @Nonnull
   public static final SetCodec<InteractionType, EnumSet<InteractionType>> INTERACTION_TYPE_SET_CODEC = new SetCodec<>(
      INTERACTION_TYPE_CODEC, () -> EnumSet.noneOf(InteractionType.class), true
   );
   private static InteractionModule instance;
   private ComponentType<EntityStore, InteractionManager> interactionManagerComponent;
   private ComponentType<EntityStore, ChainingInteraction.Data> chainingDataComponent;
   private ComponentType<EntityStore, Interactions> interactionsComponentType;
   private ComponentType<ChunkStore, PlacedByInteractionComponent> placedByComponentType;
   private ResourceType<ChunkStore, BlockCounter> blockCounterResourceType;
   private ComponentType<ChunkStore, TrackedPlacement> trackedPlacementComponentType;

   @Nonnull
   public static InteractionModule get() {
      return instance;
   }

   public InteractionModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      EventRegistry eventRegistry = this.getEventRegistry();
      ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
      this.getCommandRegistry().registerCommand(new InteractionCommand());
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                           UnarmedInteractions.class, new DefaultAssetMap()
                        )
                        .setPath("Item/Unarmed/Interactions"))
                     .setCodec(UnarmedInteractions.CODEC))
                  .setKeyFunction(UnarmedInteractions::getId))
               .loadsAfter(RootInteraction.class))
            .setPacketGenerator(new UnarmedInteractionsPacketGenerator())
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                                    Interaction.class, new IndexedLookupTableAssetMap<>(Interaction[]::new)
                                 )
                                 .setPath("Item/Interactions"))
                              .setCodec(Interaction.CODEC))
                           .setKeyFunction(Interaction::getId))
                        .setReplaceOnRemove(id -> new SendMessageInteraction(id, "Missing interaction: " + id)))
                     .setIsUnknown(Interaction::isUnknown))
                  .setPacketGenerator(new InteractionPacketGenerator())
                  .loadsAfter(
                     EntityStatType.class,
                     EntityEffect.class,
                     Trail.class,
                     ItemPlayerAnimations.class,
                     SoundEvent.class,
                     ParticleSystem.class,
                     ModelAsset.class,
                     HitboxCollisionConfig.class
                  ))
               .preLoadAssets(List.of(ChangeActiveSlotInteraction.DEFAULT_INTERACTION)))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                                    RootInteraction.class, new IndexedLookupTableAssetMap<>(RootInteraction[]::new)
                                 )
                                 .setPath("Item/RootInteractions"))
                              .setCodec(RootInteraction.CODEC))
                           .setKeyFunction(RootInteraction::getId))
                        .setReplaceOnRemove(x$0 -> new RootInteraction(x$0)))
                     .setPacketGenerator(new RootInteractionPacketGenerator())
                     .loadsAfter(Interaction.class))
                  .loadsBefore(BlockType.class, Item.class))
               .preLoadAssets(List.of(ChangeActiveSlotInteraction.DEFAULT_ROOT)))
            .build()
      );
      this.interactionManagerComponent = this.getEntityStoreRegistry().registerComponent(InteractionManager.class, () -> {
         throw new UnsupportedOperationException();
      });
      this.interactionsComponentType = this.getEntityStoreRegistry().registerComponent(Interactions.class, "Interactions", Interactions.CODEC);
      this.placedByComponentType = this.getChunkStoreRegistry()
         .registerComponent(PlacedByInteractionComponent.class, "PlacedByInteraction", PlacedByInteractionComponent.CODEC);
      Interaction.CODEC.register("Simple", SimpleInteraction.class, SimpleInteraction.CODEC);
      Interaction.CODEC.register("PlaceBlock", PlaceBlockInteraction.class, PlaceBlockInteraction.CODEC);
      Interaction.CODEC.register("PlaceFluid", PlaceFluidInteraction.class, PlaceFluidInteraction.CODEC);
      Interaction.CODEC.register("BreakBlock", BreakBlockInteraction.class, BreakBlockInteraction.CODEC);
      Interaction.CODEC.register("PickBlock", PickBlockInteraction.class, PickBlockInteraction.CODEC);
      Interaction.CODEC.register("UseBlock", UseBlockInteraction.class, UseBlockInteraction.CODEC);
      Interaction.CODEC.register("BlockCondition", BlockConditionInteraction.class, BlockConditionInteraction.CODEC);
      Interaction.CODEC.register("ChangeBlock", ChangeBlockInteraction.class, ChangeBlockInteraction.CODEC);
      Interaction.CODEC.register("ChangeState", ChangeStateInteraction.class, ChangeStateInteraction.CODEC);
      Interaction.CODEC.register("UseEntity", UseEntityInteraction.class, UseEntityInteraction.CODEC);
      Interaction.CODEC.register("BuilderTool", BuilderToolInteraction.class, BuilderToolInteraction.CODEC);
      Interaction.CODEC.register("ModifyInventory", ModifyInventoryInteraction.class, ModifyInventoryInteraction.CODEC);
      Interaction.CODEC.register("Charging", ChargingInteraction.class, ChargingInteraction.CODEC);
      Interaction.CODEC.register("DestroyBlock", DestroyBlockInteraction.class, DestroyBlockInteraction.CODEC);
      Interaction.CODEC.register("CycleBlockGroup", CycleBlockGroupInteraction.class, CycleBlockGroupInteraction.CODEC);
      Interaction.CODEC.register("Explode", ExplodeInteraction.class, ExplodeInteraction.CODEC);
      Interaction.CODEC.register("Chaining", ChainingInteraction.class, ChainingInteraction.CODEC);
      Interaction.CODEC.register("ChainFlag", ChainFlagInteraction.class, ChainFlagInteraction.CODEC);
      Interaction.CODEC.register("CancelChain", CancelChainInteraction.class, CancelChainInteraction.CODEC);
      this.chainingDataComponent = this.getEntityStoreRegistry().registerComponent(ChainingInteraction.Data.class, ChainingInteraction.Data::new);
      Interaction.CODEC.register("Condition", ConditionInteraction.class, ConditionInteraction.CODEC);
      Interaction.CODEC.register("FirstClick", FirstClickInteraction.class, FirstClickInteraction.CODEC);
      Interaction.CODEC.register("Repeat", RepeatInteraction.class, RepeatInteraction.CODEC);
      Interaction.CODEC.register("Parallel", ParallelInteraction.class, ParallelInteraction.CODEC);
      Interaction.CODEC.register("Serial", SerialInteraction.class, SerialInteraction.CODEC);
      Interaction.CODEC.register("ChangeActiveSlot", ChangeActiveSlotInteraction.class, ChangeActiveSlotInteraction.CODEC);
      Interaction.CODEC.register("Selector", SelectInteraction.class, SelectInteraction.CODEC);
      Interaction.CODEC.register("DamageEntity", DamageEntityInteraction.class, DamageEntityInteraction.CODEC);
      Interaction.CODEC.register("LaunchProjectile", LaunchProjectileInteraction.class, LaunchProjectileInteraction.CODEC);
      Interaction.CODEC.register("Wielding", WieldingInteraction.class, WieldingInteraction.CODEC);
      Interaction.CODEC.register("Replace", ReplaceInteraction.class, ReplaceInteraction.CODEC);
      Interaction.CODEC.register("StatsCondition", StatsConditionInteraction.class, StatsConditionInteraction.CODEC);
      Interaction.CODEC.register("StatsConditionWithModifier", StatsConditionWithModifierInteraction.class, StatsConditionWithModifierInteraction.CODEC);
      Interaction.CODEC.register("SpawnPrefab", SpawnPrefabInteraction.class, SpawnPrefabInteraction.CODEC);
      Interaction.CODEC.register("SendMessage", SendMessageInteraction.class, SendMessageInteraction.CODEC);
      Interaction.CODEC.register("EquipItem", EquipItemInteraction.class, EquipItemInteraction.CODEC);
      Interaction.CODEC.register("RefillContainer", RefillContainerInteraction.class, RefillContainerInteraction.CODEC);
      Interaction.CODEC.register("Door", DoorInteraction.class, DoorInteraction.CODEC);
      Interaction.CODEC.register("IncreaseBackpackCapacity", IncreaseBackpackCapacityInteraction.class, IncreaseBackpackCapacityInteraction.CODEC);
      Interaction.CODEC.register("CheckUniqueItemUsage", CheckUniqueItemUsageInteraction.class, CheckUniqueItemUsageInteraction.CODEC);
      Interaction.CODEC.register("LaunchPad", LaunchPadInteraction.class, LaunchPadInteraction.CODEC);
      Interaction.CODEC.register("OpenContainer", OpenContainerInteraction.class, OpenContainerInteraction.CODEC);
      Interaction.CODEC.register("OpenItemStackContainer", OpenItemStackContainerInteraction.class, OpenItemStackContainerInteraction.CODEC);
      Interaction.CODEC.register("DestroyCondition", DestroyConditionInteraction.class, DestroyConditionInteraction.CODEC);
      Interaction.CODEC.register("OpenCustomUI", OpenCustomUIInteraction.class, OpenCustomUIInteraction.CODEC);
      Interaction.CODEC.register("OpenPage", OpenPageInteraction.class, OpenPageInteraction.CODEC);
      Interaction.CODEC.register("ApplyEffect", ApplyEffectInteraction.class, ApplyEffectInteraction.CODEC);
      Interaction.CODEC.register("ClearEntityEffect", ClearEntityEffectInteraction.class, ClearEntityEffectInteraction.CODEC);
      Interaction.CODEC.register("RemoveEntity", RemoveEntityInteraction.class, RemoveEntityInteraction.CODEC);
      Interaction.CODEC.register("EffectCondition", EffectConditionInteraction.class, EffectConditionInteraction.CODEC);
      Interaction.CODEC.register("ApplyForce", ApplyForceInteraction.class, ApplyForceInteraction.CODEC);
      Interaction.CODEC.register("ChangeStat", ChangeStatInteraction.class, ChangeStatInteraction.CODEC);
      Interaction.CODEC.register("ChangeStatWithModifier", ChangeStatWithModifierInteraction.class, ChangeStatWithModifierInteraction.CODEC);
      Interaction.CODEC.register("MovementCondition", MovementConditionInteraction.class, MovementConditionInteraction.CODEC);
      Interaction.CODEC.register("ResetCooldown", ResetCooldownInteraction.class, ResetCooldownInteraction.CODEC);
      Interaction.CODEC.register("TriggerCooldown", TriggerCooldownInteraction.class, TriggerCooldownInteraction.CODEC);
      Interaction.CODEC.register("CooldownCondition", CooldownConditionInteraction.class, CooldownConditionInteraction.CODEC);
      Interaction.CODEC.register("IncrementCooldown", IncrementCooldownInteraction.class, IncrementCooldownInteraction.CODEC);
      Interaction.CODEC.register("AddItem", AddItemInteraction.class, AddItemInteraction.CODEC);
      Interaction.CODEC.register("Interrupt", InterruptInteraction.class, InterruptInteraction.CODEC);
      Interaction.CODEC.register("RunRootInteraction", RunRootInteraction.class, RunRootInteraction.CODEC);
      Interaction.CODEC.register("RunOnBlockTypes", RunOnBlockTypesInteraction.class, RunOnBlockTypesInteraction.CODEC);
      Interaction.CODEC.register("Camera", CameraInteraction.class, CameraInteraction.CODEC);
      Interaction.CODEC.register("ToggleGlider", ToggleGliderInteraction.class, ToggleGliderInteraction.CODEC);
      OpenCustomUIInteraction.registerBlockEntityCustomPage(
         this,
         LaunchPad.LaunchPadSettingsPage.class,
         "LaunchPad",
         (playerRef, ref) -> ref.getStore().getArchetype(ref).contains(LaunchPad.getComponentType())
            ? new LaunchPad.LaunchPadSettingsPage(playerRef, ref, CustomPageLifetime.CanDismissOrCloseThroughInteraction)
            : null
      );
      OpenCustomUIInteraction.PAGE_CODEC.register("ItemRepair", ItemRepairPageSupplier.class, ItemRepairPageSupplier.CODEC);
      SelectorType.CODEC.register("Horizontal", HorizontalSelector.class, HorizontalSelector.CODEC);
      SelectorType.CODEC.register("Stab", StabSelector.class, StabSelector.CODEC);
      SelectorType.CODEC.register("AOECircle", AOECircleSelector.class, AOECircleSelector.CODEC);
      SelectorType.CODEC.register("AOECylinder", AOECylinderSelector.class, AOECylinderSelector.CODEC);
      SelectorType.CODEC.register("Raycast", RaycastSelector.class, RaycastSelector.CODEC);
      Knockback.CODEC.register("Directional", DirectionalKnockback.class, DirectionalKnockback.CODEC);
      Knockback.CODEC.register("Point", PointKnockback.class, PointKnockback.CODEC);
      Knockback.CODEC.register("Force", ForceKnockback.class, ForceKnockback.CODEC);
      eventRegistry.register(LoadedAssetsEvent.class, RootInteraction.class, InteractionModule::handledLoadedRootInteractions);
      eventRegistry.register(LoadedAssetsEvent.class, Interaction.class, InteractionModule::handledLoadedInteractions);
      eventRegistry.register(RemovedAssetsEvent.class, Interaction.class, InteractionModule::handledRemovedInteractions);
      entityStoreRegistry.registerSystem(new InteractionSystems.PlayerAddManagerSystem());
      entityStoreRegistry.registerSystem(new InteractionSystems.CleanUpSystem());
      entityStoreRegistry.registerSystem(new InteractionSystems.TickInteractionManagerSystem());
      entityStoreRegistry.registerSystem(new InteractionSystems.TrackerTickSystem());
      entityStoreRegistry.registerSystem(new InteractionSystems.EntityTrackerRemove(EntityTrackerSystems.Visible.getComponentType()));
      this.getCodecRegistry(SelectInteraction.EntityMatcher.CODEC).register("Vulnerable", VulnerableMatcher.class, VulnerableMatcher.CODEC);
      this.getCodecRegistry(SelectInteraction.EntityMatcher.CODEC).register("Player", PlayerMatcher.class, PlayerMatcher.CODEC);
      this.blockCounterResourceType = this.getChunkStoreRegistry().registerResource(BlockCounter.class, "BlockCounter", BlockCounter.CODEC);
      this.trackedPlacementComponentType = this.getChunkStoreRegistry().registerComponent(TrackedPlacement.class, "TrackedPlacement", TrackedPlacement.CODEC);
      this.getChunkStoreRegistry().registerSystem(new TrackedPlacement.OnAddRemove());
      this.getCodecRegistry(Interaction.CODEC)
         .register("PlacementCountCondition", PlacementCountConditionInteraction.class, PlacementCountConditionInteraction.CODEC);
   }

   private static void handledLoadedRootInteractions(@Nonnull LoadedAssetsEvent<String, RootInteraction, ?> event) {
      for (RootInteraction rootInteraction : event.getLoadedAssets().values()) {
         rootInteraction.build();
      }
   }

   private static void handledLoadedInteractions(@Nonnull LoadedAssetsEvent<String, Interaction, ?> event) {
      for (Entry<String, RootInteraction> entry : RootInteraction.getAssetMap().getAssetMap().entrySet()) {
         entry.getValue().build(event.getLoadedAssets().keySet());
      }
   }

   private static void handledRemovedInteractions(@Nonnull RemovedAssetsEvent<String, Interaction, ?> event) {
      for (Entry<String, RootInteraction> entry : RootInteraction.getAssetMap().getAssetMap().entrySet()) {
         entry.getValue().build(event.getRemovedAssets());
      }
   }

   public void doMouseInteraction(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nonnull MouseInteraction packet,
      @Nonnull Player playerComponent,
      @Nonnull PlayerRef playerRefComponent
   ) {
      if (!this.isDisabled()) {
         byte activeHotbarSlot = playerComponent.getInventory().getActiveHotbarSlot();
         if (activeHotbarSlot != packet.activeSlot) {
            playerComponent.sendMessage(
               Message.translation("server.modules.interaction.failedGetActiveSlot").param("server", (int)activeHotbarSlot).param("packet", packet.activeSlot)
            );
         } else {
            MouseButtonType mouseButtonType = packet.mouseButton != null ? packet.mouseButton.mouseButtonType : MouseButtonType.Left;
            Inventory inventory = playerComponent.getInventory();
            ItemStack itemInHand = inventory.getItemInHand();
            ItemStack itemInOffHand = inventory.getUtilityItem();
            Item primaryItem = itemInHand != null && !itemInHand.isEmpty() ? itemInHand.getItem() : null;
            Item secondaryItem = itemInOffHand != null && !itemInOffHand.isEmpty() ? itemInOffHand.getItem() : null;
            Item item;
            if (mouseButtonType == MouseButtonType.Left) {
               item = primaryItem;
            } else if (mouseButtonType == MouseButtonType.Right && secondaryItem != null) {
               item = secondaryItem;
            } else {
               item = primaryItem;
            }

            WorldInteraction worldInteraction_ = packet.worldInteraction;
            BlockPosition blockPositionPacket = worldInteraction_.blockPosition;
            if (ref.isValid()) {
               EntityStore entityComponentStore = componentAccessor.getExternalData();
               Vector3i targetBlock = blockPositionPacket == null ? null : new Vector3i(blockPositionPacket.x, blockPositionPacket.y, blockPositionPacket.z);
               Entity targetEntity;
               if (worldInteraction_.entityId < 0) {
                  targetEntity = null;
               } else {
                  Ref<EntityStore> entityReference = entityComponentStore.getRefFromNetworkId(worldInteraction_.entityId);
                  targetEntity = EntityUtils.getEntity(entityReference, componentAccessor);
               }

               CameraManager cameraManagerComponent = componentAccessor.getComponent(ref, CameraManager.getComponentType());

               assert cameraManagerComponent != null;

               if (packet.mouseButton != null) {
                  IEventDispatcher<PlayerMouseButtonEvent, PlayerMouseButtonEvent> dispatcher = HytaleServer.get()
                     .getEventBus()
                     .dispatchFor(PlayerMouseButtonEvent.class);
                  if (dispatcher.hasListener()) {
                     dispatcher.dispatch(
                        new PlayerMouseButtonEvent(
                           ref,
                           playerComponent,
                           playerRefComponent,
                           packet.clientTimestamp,
                           item,
                           targetBlock,
                           targetEntity,
                           packet.screenPoint,
                           packet.mouseButton
                        )
                     );
                  }

                  cameraManagerComponent.handleMouseButtonState(packet.mouseButton.mouseButtonType, packet.mouseButton.state, targetBlock);
               } else {
                  IEventDispatcher<PlayerMouseMotionEvent, PlayerMouseMotionEvent> dispatcher = HytaleServer.get()
                     .getEventBus()
                     .dispatchFor(PlayerMouseMotionEvent.class);
                  if (dispatcher.hasListener()) {
                     dispatcher.dispatch(
                        new PlayerMouseMotionEvent(
                           ref, playerComponent, packet.clientTimestamp, item, targetBlock, targetEntity, packet.screenPoint, packet.mouseMotion
                        )
                     );
                  }
               }

               cameraManagerComponent.setLastScreenPoint(new Vector2d(packet.screenPoint.x, packet.screenPoint.y));
               cameraManagerComponent.setLastBlockPosition(targetBlock);
            }
         }
      }
   }

   @Nonnull
   public ComponentType<EntityStore, ChainingInteraction.Data> getChainingDataComponent() {
      return this.chainingDataComponent;
   }

   @Nonnull
   public ComponentType<EntityStore, Interactions> getInteractionsComponentType() {
      return this.interactionsComponentType;
   }

   @Nonnull
   public ComponentType<EntityStore, InteractionManager> getInteractionManagerComponent() {
      return this.interactionManagerComponent;
   }

   @Nonnull
   public ComponentType<ChunkStore, PlacedByInteractionComponent> getPlacedByComponentType() {
      return this.placedByComponentType;
   }

   public ResourceType<ChunkStore, BlockCounter> getBlockCounterResourceType() {
      return this.blockCounterResourceType;
   }

   public ComponentType<ChunkStore, TrackedPlacement> getTrackedPlacementComponentType() {
      return this.trackedPlacementComponentType;
   }
}
