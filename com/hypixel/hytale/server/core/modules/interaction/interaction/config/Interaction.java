package com.hypixel.hytale.server.core.modules.interaction.interaction.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.EnumMapCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector4d;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.ForkedChainId;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.InteractionChainData;
import com.hypixel.hytale.protocol.InteractionSettings;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionSyncData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.ItemAnimation;
import com.hypixel.hytale.protocol.packets.interaction.PlayInteractionFor;
import com.hypixel.hytale.server.core.asset.common.BlockyAnimationCache;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.itemanimation.config.ItemPlayerAnimations;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.meta.MetaKey;
import com.hypixel.hytale.server.core.meta.MetaRegistry;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.Collector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.simple.SendMessageInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.Operation;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.OperationsBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Interaction
   implements Operation,
   JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, Interaction>>,
   NetworkSerializable<com.hypixel.hytale.protocol.Interaction> {
   @Nonnull
   public static final AssetCodecMapCodec<String, Interaction> CODEC = new AssetCodecMapCodec<>(
      Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
   );
   @Nonnull
   public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(Interaction.class, CODEC);
   @Nonnull
   public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<>(CHILD_ASSET_CODEC, String[]::new);
   @Nonnull
   public static final BuilderCodec<Interaction> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(Interaction.class)
      .appendInherited(
         new KeyedCodec<>("ViewDistance", Codec.DOUBLE),
         (damageEffects, s) -> damageEffects.viewDistance = s,
         damageEffects -> damageEffects.viewDistance,
         (damageEffects, parent) -> damageEffects.viewDistance = parent.viewDistance
      )
      .documentation("Configures the distance in which other players will be able to see the effects of this interaction.")
      .add()
      .<InteractionEffects>appendInherited(
         new KeyedCodec<>("Effects", InteractionEffects.CODEC),
         (interaction, o) -> interaction.effects = o,
         interaction -> interaction.effects,
         (interaction, parent) -> interaction.effects = parent.effects
      )
      .documentation("Sets effects that will be applied whilst the interaction is running.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("HorizontalSpeedMultiplier", Codec.FLOAT),
         (activationEffects, s) -> activationEffects.horizontalSpeedMultiplier = s,
         activationEffects -> activationEffects.horizontalSpeedMultiplier,
         (activationEffects, parent) -> activationEffects.horizontalSpeedMultiplier = parent.horizontalSpeedMultiplier
      )
      .documentation("The multiplier to apply to the horizontal speed of the entity whilst this interaction is running.")
      .metadata(new UIEditor(new UIEditor.FormattedNumber(0.1, null, null)))
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("RunTime", Codec.FLOAT),
         (activationEffects, s) -> activationEffects.runTime = s,
         activationEffects -> activationEffects.runTime,
         (activationEffects, parent) -> activationEffects.runTime = parent.runTime
      )
      .documentation(
         "The time in seconds this interaction should run for. \n\nIf *Effects.WaitForAnimationToFinish* is set and the length of the animation is longer than the runtime then the interaction will run for longer than the set time."
      )
      .metadata(new UIEditor(new UIEditor.FormattedNumber(0.01, "s", null)))
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("CancelOnItemChange", Codec.BOOLEAN),
         (damageEffects, s) -> damageEffects.cancelOnItemChange = s,
         damageEffects -> damageEffects.cancelOnItemChange,
         (damageEffects, parent) -> damageEffects.cancelOnItemChange = parent.cancelOnItemChange
      )
      .documentation("Whether the interaction will be cancelled when the entity's held item changes.")
      .add()
      .<InteractionRules>appendInherited(new KeyedCodec<>("Rules", InteractionRules.CODEC), (o, i) -> o.rules = i, o -> o.rules, (o, p) -> o.rules = p.rules)
      .documentation("A set of rules that control when this interaction can run.")
      .addValidator(Validators.nonNull())
      .add()
      .<Map<GameMode, InteractionSettings>>appendInherited(
         new KeyedCodec<>(
            "Settings",
            new EnumMapCodec<>(
               GameMode.class,
               BuilderCodec.builder(InteractionSettings.class, InteractionSettings::new)
                  .appendInherited(
                     new KeyedCodec<>("AllowSkipOnClick", Codec.BOOLEAN),
                     (settings, s) -> settings.allowSkipOnClick = s,
                     settings -> settings.allowSkipOnClick,
                     (settings, parent) -> settings.allowSkipOnClick = parent.allowSkipOnClick
                  )
                  .documentation("Whether to skip this interaction when another click is sent.")
                  .add()
                  .build()
            )
         ),
         (interaction, o) -> interaction.settings = o,
         interaction -> interaction.settings,
         (interaction, parent) -> interaction.settings = parent.settings
      )
      .documentation("Per a gamemode settings.")
      .add()
      .<InteractionCameraSettings>appendInherited(
         new KeyedCodec<>("Camera", InteractionCameraSettings.CODEC), (o, i) -> o.camera = i, o -> o.camera, (o, p) -> o.camera = p.camera
      )
      .documentation("Configures the camera behaviour for this interaction.")
      .add()
      .build();
   @Nonnull
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(Interaction::getAssetStore));
   private static AssetStore<String, Interaction, IndexedLookupTableAssetMap<String, Interaction>> ASSET_STORE;
   public static final MetaRegistry<InteractionContext> CONTEXT_META_REGISTRY = new MetaRegistry<>();
   public static final MetaRegistry<Interaction> META_REGISTRY = new MetaRegistry<>();
   public static final MetaKey<Ref<EntityStore>> TARGET_ENTITY = CONTEXT_META_REGISTRY.registerMetaObject(data -> null);
   public static final MetaKey<Vector4d> HIT_LOCATION = CONTEXT_META_REGISTRY.registerMetaObject(data -> null);
   public static final MetaKey<String> HIT_DETAIL = CONTEXT_META_REGISTRY.registerMetaObject(data -> null);
   public static final MetaKey<BlockPosition> TARGET_BLOCK = CONTEXT_META_REGISTRY.registerMetaObject(data -> null);
   public static final MetaKey<BlockPosition> TARGET_BLOCK_RAW = CONTEXT_META_REGISTRY.registerMetaObject(data -> null);
   public static final MetaKey<Integer> TARGET_SLOT = CONTEXT_META_REGISTRY.registerMetaObject(data -> 0);
   public static final MetaKey<Float> TIME_SHIFT = META_REGISTRY.registerMetaObject(data -> null);
   public static final MetaKey<Damage> DAMAGE = CONTEXT_META_REGISTRY.registerMetaObject(data -> null);
   protected String id;
   protected AssetExtraInfo.Data data;
   protected boolean unknown;
   protected double viewDistance = 96.0;
   @Nonnull
   protected InteractionEffects effects = new InteractionEffects();
   protected float horizontalSpeedMultiplier = 1.0F;
   protected float runTime;
   protected boolean cancelOnItemChange = true;
   @Nonnull
   protected Map<GameMode, InteractionSettings> settings = Collections.emptyMap();
   @Nonnull
   protected InteractionRules rules = InteractionRules.DEFAULT_RULES;
   @Nullable
   protected InteractionCameraSettings camera;
   @Nullable
   private transient SoftReference<com.hypixel.hytale.protocol.Interaction> cachedPacket;

   @Nonnull
   public static AssetStore<String, Interaction, IndexedLookupTableAssetMap<String, Interaction>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(Interaction.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, Interaction> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, Interaction>)getAssetStore().getAssetMap();
   }

   public Interaction() {
   }

   public Interaction(@Nonnull String id) {
      this.id = id;
   }

   public String getId() {
      return this.id;
   }

   public boolean isUnknown() {
      return this.unknown;
   }

   @Nonnull
   public InteractionEffects getEffects() {
      return this.effects;
   }

   public float getHorizontalSpeedMultiplier() {
      return this.horizontalSpeedMultiplier;
   }

   public double getViewDistance() {
      return this.viewDistance;
   }

   public float getRunTime() {
      return this.runTime;
   }

   public boolean isCancelOnItemChange() {
      return this.cancelOnItemChange;
   }

   @Nonnull
   @Override
   public InteractionRules getRules() {
      return this.rules;
   }

   @Nonnull
   public Map<GameMode, InteractionSettings> getSettings() {
      return this.settings;
   }

   @Override
   public final void tick(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull LivingEntity entity,
      boolean firstRun,
      float time,
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nonnull CooldownHandler cooldownHandler
   ) {
      int previousCounter = context.getOperationCounter();
      int previousDepth = context.getChain().getCallDepth();
      if (!this.tickInternal(entity, time, type, context)) {
         this.tick0(firstRun, time, type, context, cooldownHandler);
      }

      InteractionSyncData data = context.getState();
      this.trySkipChain(ref, time, context, data);
      switch (data.state) {
         case Failed:
         case Finished:
         case Skip:
            Float shift = context.getInstanceStore().getMetaObject(TIME_SHIFT);
            if (shift != null) {
               context.setTimeShift(shift);
            }

            if (context.getOperationCounter() == previousCounter && previousDepth == context.getChain().getCallDepth()) {
               context.setOperationCounter(context.getOperationCounter() + 1);
            }
         default:
            return;
         case ItemChanged:
            throw new InteractionManager.ChainCancelledException(data.state);
      }
   }

   @Override
   public final void simulateTick(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull LivingEntity entity,
      boolean firstRun,
      float time,
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nonnull CooldownHandler cooldownHandler
   ) {
      int previousCounter = context.getOperationCounter();
      int previousDepth = context.getChain().getSimulatedCallDepth();
      if (!this.tickInternal(entity, time, type, context)) {
         this.simulateTick0(firstRun, time, type, context, cooldownHandler);
      }

      InteractionSyncData data = context.getState();
      this.trySkipChain(ref, time, context, data);
      switch (data.state) {
         case Failed:
         case Finished:
         case Skip:
            Float shift = context.getInstanceStore().getMetaObject(TIME_SHIFT);
            if (shift != null) {
               context.setTimeShift(shift);
            }

            if (context.getOperationCounter() == previousCounter && previousDepth == context.getChain().getSimulatedCallDepth()) {
               context.setOperationCounter(context.getOperationCounter() + 1);
            }
      }
   }

   private boolean tickInternal(@Nonnull LivingEntity entity, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context) {
      Inventory inventory = entity.getInventory();
      if (!UUIDUtil.isEmptyOrNull(context.getChain().getChainData().proxyId)
         || !this.cancelOnItemChange
         || (type == InteractionType.Equipped || inventory.getActiveSlot(context.getHeldItemSectionId()) == context.getHeldItemSlot())
            && (
               context.getHeldItemSlot() == -1
                  || ItemStack.isEquivalentType(context.getHeldItemContainer().getItemStack(context.getHeldItemSlot()), context.getHeldItem())
            )) {
         if (!failed(context.getState().state)) {
            double animationDuration = 0.0;
            if (this.effects.isWaitForAnimationToFinish()) {
               ItemStack heldItem = context.getHeldItem();
               Item item = heldItem != null ? heldItem.getItem() : null;
               animationDuration = this.getAnimationDuration(item);
            }

            InteractionSyncData data = context.getState();
            float maxTime = Math.max(this.runTime, (float)animationDuration);
            if (time < maxTime) {
               data.state = InteractionState.NotFinished;
            } else {
               if (maxTime > 0.0F) {
                  context.getInstanceStore().putMetaObject(TIME_SHIFT, time - maxTime);
               }

               data.state = InteractionState.Finished;
            }

            data.progress = time;
         }

         return false;
      } else {
         InteractionSyncData data = context.getState();
         data.state = InteractionState.ItemChanged;
         data.progress = time;
         return true;
      }
   }

   private void trySkipChain(@Nonnull Ref<EntityStore> ref, float time, @Nonnull InteractionContext context, @Nonnull InteractionSyncData data) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
      GameMode gameMode = playerComponent != null ? playerComponent.getGameMode() : GameMode.Adventure;
      InteractionSettings interactionSettings = this.settings.get(gameMode);
      InteractionChain chain = context.getChain();

      assert chain != null;

      if (!chain.isFirstRun()
         && data.state == InteractionState.NotFinished
         && (context.allowSkipChainOnClick() || interactionSettings != null && interactionSettings.allowSkipOnClick)
         && context.getClientState() != null
         && context.getClientState().state == InteractionState.Skip) {
         data.state = InteractionState.Skip;
         data.progress = time;
      }
   }

   public void compile(@Nonnull OperationsBuilder builder) {
      builder.addOperation(this);
   }

   protected abstract void tick0(boolean var1, float var2, @Nonnull InteractionType var3, @Nonnull InteractionContext var4, @Nonnull CooldownHandler var5);

   protected abstract void simulateTick0(
      boolean var1, float var2, @Nonnull InteractionType var3, @Nonnull InteractionContext var4, @Nonnull CooldownHandler var5
   );

   public abstract boolean walk(@Nonnull Collector var1, @Nonnull InteractionContext var2);

   @Override
   public void handle(@Nonnull Ref<EntityStore> ref, boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context) {
      Ref<EntityStore> entity = context.getEntity();
      if (entity.isValid()) {
         if (ref.isValid()) {
            InteractionSyncData serverData = context.getState();
            InteractionChain chain = context.getChain();

            assert chain != null;

            if (serverData.state != InteractionState.NotFinished) {
               if (firstRun && serverData.state == InteractionState.Finished) {
                  this.sendPlayInteract(entity, context, chain, false);
               }

               this.sendPlayInteract(entity, context, chain, true);
            } else {
               if (firstRun) {
                  this.sendPlayInteract(entity, context, chain, false);
               }
            }
         }
      }
   }

   @Nullable
   public InteractionChain mapForkChain(@Nonnull InteractionContext context, @Nonnull InteractionChainData data) {
      return null;
   }

   private void sendPlayInteract(@Nonnull Ref<EntityStore> entity, @Nonnull InteractionContext context, @Nonnull InteractionChain chain, boolean cancel) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      String itemId = null;
      InteractionType interactionType = chain.getBaseType();
      int assetId = getInteractionIdOrUnknown(this.id);
      int chainId = chain.getChainId();
      ForkedChainId forkedChainId = chain.getForkedChainId();
      int operationIndex = chain.getOperationIndex();
      NetworkId networkIdComponent = commandBuffer.getComponent(entity, NetworkId.getComponentType());

      assert networkIdComponent != null;

      int entityNetworkId = networkIdComponent.getId();
      PlayInteractionFor packet = new PlayInteractionFor(entityNetworkId, chainId, forkedChainId, operationIndex, assetId, itemId, interactionType, cancel);
      TransformComponent transformComponent = commandBuffer.getComponent(entity, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = commandBuffer.getResource(EntityModule.get().getPlayerSpatialResourceType());
      ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
      playerSpatialResource.getSpatialStructure().collect(position, this.viewDistance, results);
      Ref<EntityStore> owningEntityRef = context.getOwningEntity();
      ComponentType<EntityStore, PlayerRef> playerRefComponentType = PlayerRef.getComponentType();
      ObjectListIterator var21 = results.iterator();

      while (var21.hasNext()) {
         Ref<EntityStore> playerRef = (Ref<EntityStore>)var21.next();
         if (!chain.requiresClient() || !playerRef.equals(owningEntityRef)) {
            PlayerRef playerPlayerRefComponent = commandBuffer.getComponent(playerRef, playerRefComponentType);

            assert playerPlayerRefComponent != null;

            playerPlayerRefComponent.getPacketHandler().writeNoCache(packet);
         }
      }
   }

   @Nonnull
   public final com.hypixel.hytale.protocol.Interaction toPacket() {
      com.hypixel.hytale.protocol.Interaction cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.Interaction packet = this.generatePacket();
         this.configurePacket(packet);
         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   @Nonnull
   protected abstract com.hypixel.hytale.protocol.Interaction generatePacket();

   protected void configurePacket(com.hypixel.hytale.protocol.Interaction packet) {
      packet.waitForDataFrom = this.getWaitForDataFrom();
      packet.effects = this.effects.toPacket();
      packet.horizontalSpeedMultiplier = this.horizontalSpeedMultiplier;
      packet.runTime = this.runTime;
      packet.cancelOnItemChange = this.cancelOnItemChange;
      packet.settings = this.settings;
      packet.rules = this.rules.toPacket();
      if (this.data != null) {
         packet.tags = this.data.getTags().keySet().toIntArray();
      }

      if (this.camera != null) {
         packet.camera = this.camera.toPacket();
      }
   }

   protected double getAnimationDuration(@Nullable Item item) {
      String playerAnimationsId;
      if (this.effects.itemPlayerAnimationsId != null) {
         playerAnimationsId = this.effects.itemPlayerAnimationsId;
      } else if (item != null) {
         playerAnimationsId = item.getPlayerAnimationsId();
      } else {
         playerAnimationsId = "Default";
      }

      ItemPlayerAnimations playerAnimations = ItemPlayerAnimations.getAssetMap().getAsset(playerAnimationsId);
      if (playerAnimations == null) {
         return 0.0;
      } else {
         ItemAnimation itemAnimation = this.effects.getItemAnimationId() != null
            ? playerAnimations.getAnimations().get(this.effects.getItemAnimationId())
            : null;
         if (itemAnimation == null) {
            return 0.0;
         } else {
            BlockyAnimationCache.BlockyAnimation animation = BlockyAnimationCache.getNow(itemAnimation.firstPerson);
            return animation == null ? 0.0 : animation.getDurationSeconds() * itemAnimation.speed;
         }
      }
   }

   public abstract boolean needsRemoteSync();

   @Override
   public String toString() {
      return "Interaction{id='"
         + this.id
         + "', viewDistance="
         + this.viewDistance
         + ", effects="
         + this.effects
         + ", horizontalSpeedMultiplier="
         + this.horizontalSpeedMultiplier
         + ", runTime="
         + this.runTime
         + ", cancelOnItemChange="
         + this.cancelOnItemChange
         + ", settings="
         + this.settings
         + ", rules="
         + this.rules
         + ", camera="
         + this.camera
         + "}";
   }

   public static boolean failed(@Nonnull InteractionState state) {
      return switch (state) {
         case Failed, Skip, ItemChanged -> true;
         case Finished, NotFinished -> false;
      };
   }

   @Nullable
   @Deprecated
   public static Interaction getInteractionOrUnknown(@Nonnull String id) {
      return getAssetMap().getAsset(getInteractionIdOrUnknown(id));
   }

   @Deprecated
   public static int getInteractionIdOrUnknown(@Nullable String id) {
      if (id == null) {
         return Integer.MIN_VALUE;
      } else {
         IndexedLookupTableAssetMap<String, Interaction> assetMap = getAssetMap();
         int interactionId = assetMap.getIndex(id);
         if (interactionId == Integer.MIN_VALUE) {
            HytaleLogger.getLogger().at(Level.WARNING).log("Missing interaction %s", id);
            getAssetStore().loadAssets("Hytale:Hytale", List.of(new SendMessageInteraction(id, "Missing interaction: " + id)));
            int index = assetMap.getIndex(id);
            if (index == Integer.MIN_VALUE) {
               throw new IllegalArgumentException("Unknown key! " + id);
            }

            interactionId = index;
         }

         return interactionId;
      }
   }

   protected static boolean needsRemoteSync(@Nullable String id) {
      if (id == null) {
         return false;
      } else {
         Interaction interaction = getInteractionOrUnknown(id);
         if (interaction == null) {
            throw new IllegalArgumentException("Unknown interaction: " + id);
         } else {
            return interaction.needsRemoteSync();
         }
      }
   }
}
