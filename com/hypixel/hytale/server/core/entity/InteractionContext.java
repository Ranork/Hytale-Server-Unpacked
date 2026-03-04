package com.hypixel.hytale.server.core.entity;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector4d;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.ForkedChainId;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.InteractionChainData;
import com.hypixel.hytale.protocol.InteractionSyncData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.PrioritySlot;
import com.hypixel.hytale.protocol.RootInteractionSettings;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.meta.DynamicMetaStore;
import com.hypixel.hytale.server.core.modules.entity.component.SnapshotBuffer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.modules.interaction.interaction.UnarmedInteractions;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.Label;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InteractionContext {
   @Nonnull
   private static final Function<InteractionContext, Map<String, String>> DEFAULT_VAR_GETTER = InteractionContext::defaultGetVars;
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private final int heldItemSectionId;
   @Nullable
   private final ItemContainer heldItemContainer;
   private final byte heldItemSlot;
   @Nullable
   private ItemStack heldItem;
   @Nullable
   private final Item originalItemType;
   private Function<InteractionContext, Map<String, String>> interactionVarsGetter = DEFAULT_VAR_GETTER;
   @Nullable
   private final InteractionManager interactionManager;
   @Nullable
   private final Ref<EntityStore> owningEntity;
   @Nullable
   private final Ref<EntityStore> runningForEntity;
   @Nullable
   private LivingEntity entity;
   @Nullable
   private InteractionChain chain;
   @Nullable
   private InteractionEntry entry;
   @Nullable
   private Label[] labels;
   @Nullable
   private InteractionContext.SnapshotProvider snapshotProvider;
   @Nonnull
   private final DynamicMetaStore<InteractionContext> metaStore;

   private InteractionContext(
      @Nullable InteractionManager interactionManager,
      @Nullable Ref<EntityStore> owningEntity,
      int heldItemSectionId,
      @Nullable ItemContainer heldItemContainer,
      byte heldItemSlot,
      @Nullable ItemStack heldItem
   ) {
      this(interactionManager, owningEntity, owningEntity, heldItemSectionId, heldItemContainer, heldItemSlot, heldItem);
   }

   private InteractionContext(
      @Nullable InteractionManager interactionManager,
      @Nullable Ref<EntityStore> owningEntity,
      @Nullable Ref<EntityStore> runningForEntity,
      int heldItemSectionId,
      @Nullable ItemContainer heldItemContainer,
      byte heldItemSlot,
      @Nullable ItemStack heldItem
   ) {
      this.interactionManager = interactionManager;
      this.owningEntity = owningEntity;
      this.runningForEntity = runningForEntity;
      this.heldItemSectionId = heldItemSectionId;
      this.heldItemContainer = heldItemContainer;
      this.heldItemSlot = heldItemSlot;
      this.heldItem = heldItem;
      this.originalItemType = heldItem != null ? heldItem.getItem() : null;
      this.metaStore = new DynamicMetaStore<>(this, Interaction.CONTEXT_META_REGISTRY);
   }

   @Nonnull
   public InteractionChain fork(@Nonnull InteractionContext context, @Nonnull RootInteraction rootInteraction, boolean predicted) {
      assert this.chain != null;

      return this.fork(this.chain.getType(), context, rootInteraction, predicted);
   }

   @Nonnull
   public InteractionChain fork(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull RootInteraction rootInteraction, boolean predicted) {
      InteractionChainData data = new InteractionChainData(this.chain.getChainData());
      return this.fork(data, type, context, rootInteraction, predicted);
   }

   @Nonnull
   public InteractionChain fork(
      @Nonnull InteractionChainData data,
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nonnull RootInteraction rootInteraction,
      boolean predicted
   ) {
      if (context == this) {
         throw new IllegalArgumentException("Cannot use current context");
      } else {
         Integer slot = context.metaStore.getMetaObject(Interaction.TARGET_SLOT);
         if (slot == null) {
            slot = this.metaStore.getMetaObject(Interaction.TARGET_SLOT);
            context.metaStore.putMetaObject(Interaction.TARGET_SLOT, slot);
         }

         if (slot != null) {
            data.targetSlot = slot;
         }

         Ref<EntityStore> targetEntity = context.metaStore.getIfPresentMetaObject(Interaction.TARGET_ENTITY);
         if (targetEntity != null && targetEntity.isValid()) {
            CommandBuffer<EntityStore> commandBuffer = this.getCommandBuffer();

            assert commandBuffer != null;

            NetworkId networkComponent = commandBuffer.getComponent(targetEntity, NetworkId.getComponentType());
            if (networkComponent != null) {
               data.entityId = networkComponent.getId();
            }
         }

         Vector4d hitLocation = context.metaStore.getIfPresentMetaObject(Interaction.HIT_LOCATION);
         if (hitLocation != null) {
            data.hitLocation = new Vector3f((float)hitLocation.x, (float)hitLocation.y, (float)hitLocation.z);
         }

         String hitDetail = context.metaStore.getIfPresentMetaObject(Interaction.HIT_DETAIL);
         if (hitDetail != null) {
            data.hitDetail = hitDetail;
         }

         BlockPosition targetBlock = context.metaStore.getIfPresentMetaObject(Interaction.TARGET_BLOCK_RAW);
         if (targetBlock != null) {
            data.blockPosition = targetBlock;
         }

         int index = this.chain.getChainId();
         ForkedChainId forkedChainId = this.chain.getForkedChainId();
         ForkedChainId newChainId = new ForkedChainId(this.entry.getIndex(), this.entry.nextForkId(), null);
         if (forkedChainId != null) {
            ForkedChainId root = forkedChainId = new ForkedChainId(forkedChainId);

            while (root.forkedId != null) {
               root = root.forkedId;
            }

            root.forkedId = newChainId;
         } else {
            forkedChainId = newChainId;
         }

         InteractionChain forkChain = new InteractionChain(forkedChainId, newChainId, type, context, data, rootInteraction, null, true);
         forkChain.setChainId(index);
         forkChain.setBaseType(this.chain.getBaseType());
         forkChain.setPredicted(predicted);
         forkChain.skipChainOnClick = this.allowSkipChainOnClick();
         forkChain.setTimeShift(this.chain.getTimeShift());
         this.chain.putForkedChain(newChainId, forkChain);
         InteractionChain.TempChain tempData = this.chain.removeTempForkedChain(newChainId, forkChain);
         if (tempData != null) {
            LOGGER.at(Level.FINEST).log("Loading temp chain data for fork %s", newChainId);
            forkChain.copyTempFrom(tempData);
         }

         return forkChain;
      }
   }

   @Nonnull
   public InteractionContext duplicate() {
      InteractionContext ctx = new InteractionContext(
         this.interactionManager, this.owningEntity, this.runningForEntity, this.heldItemSectionId, this.heldItemContainer, this.heldItemSlot, this.heldItem
      );
      ctx.interactionVarsGetter = this.interactionVarsGetter;
      ctx.metaStore.copyFrom(this.metaStore);
      return ctx;
   }

   @Nonnull
   public Ref<EntityStore> getEntity() {
      return this.runningForEntity;
   }

   @Nonnull
   public Ref<EntityStore> getOwningEntity() {
      return this.owningEntity;
   }

   public void execute(@Nonnull RootInteraction nextInteraction) {
      this.chain.getContext().getState().enteredRootInteraction = RootInteraction.getAssetMap().getIndex(nextInteraction.getId());
      this.chain.pushRoot(nextInteraction, this.entry.isUseSimulationState());
   }

   @Nullable
   public InteractionChain getChain() {
      return this.chain;
   }

   @Nullable
   public InteractionEntry getEntry() {
      return this.entry;
   }

   public int getOperationCounter() {
      return this.entry.isUseSimulationState() ? this.chain.getSimulatedOperationCounter() : this.chain.getOperationCounter();
   }

   public void setOperationCounter(int operationCounter) {
      if (this.entry.isUseSimulationState()) {
         this.chain.setSimulatedOperationCounter(operationCounter);
      } else {
         this.chain.setOperationCounter(operationCounter);
      }
   }

   public void jump(@Nonnull Label label) {
      this.setOperationCounter(label.getIndex());
   }

   @Nullable
   public Item getOriginalItemType() {
      return this.originalItemType;
   }

   public int getHeldItemSectionId() {
      return this.heldItemSectionId;
   }

   @Nullable
   public ItemContainer getHeldItemContainer() {
      return this.heldItemContainer;
   }

   public byte getHeldItemSlot() {
      return this.heldItemSlot;
   }

   @Nullable
   public ItemStack getHeldItem() {
      return this.heldItem;
   }

   public void setHeldItem(@Nullable ItemStack heldItem) {
      this.heldItem = heldItem;
   }

   @Nullable
   public ItemContext createHeldItemContext() {
      return this.heldItemContainer != null && this.heldItem != null ? new ItemContext(this.heldItemContainer, this.heldItemSlot, this.heldItem) : null;
   }

   public Function<InteractionContext, Map<String, String>> getInteractionVarsGetter() {
      return this.interactionVarsGetter;
   }

   public Map<String, String> getInteractionVars() {
      return this.interactionVarsGetter.apply(this);
   }

   public void setInteractionVarsGetter(Function<InteractionContext, Map<String, String>> interactionVarsGetter) {
      this.interactionVarsGetter = interactionVarsGetter;
   }

   public InteractionManager getInteractionManager() {
      return this.interactionManager;
   }

   @Nullable
   public Ref<EntityStore> getTargetEntity() {
      return this.metaStore.getIfPresentMetaObject(Interaction.TARGET_ENTITY);
   }

   @Nullable
   public BlockPosition getTargetBlock() {
      return this.metaStore.getIfPresentMetaObject(Interaction.TARGET_BLOCK);
   }

   @Nonnull
   public DynamicMetaStore<InteractionContext> getMetaStore() {
      return this.metaStore;
   }

   @Nonnull
   public InteractionSyncData getState() {
      return this.entry.getState();
   }

   @Nullable
   public InteractionSyncData getClientState() {
      return this.entry.getClientState();
   }

   @Nonnull
   public InteractionSyncData getServerState() {
      return this.entry.getServerState();
   }

   @Nonnull
   public DynamicMetaStore<Interaction> getInstanceStore() {
      return this.entry.getMetaStore();
   }

   public boolean allowSkipChainOnClick() {
      return this.chain.skipChainOnClick;
   }

   public void setLabels(Label[] labels) {
      this.labels = labels;
   }

   public boolean hasLabels() {
      return this.labels != null;
   }

   public Label getLabel(int index) {
      return this.labels[index];
   }

   public EntitySnapshot getSnapshot(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      NetworkId networkIdComponent = componentAccessor.getComponent(ref, NetworkId.getComponentType());

      assert networkIdComponent != null;

      int networkId = networkIdComponent.getId();
      if (this.snapshotProvider != null) {
         return this.snapshotProvider.getSnapshot(this.getCommandBuffer(), this.runningForEntity, networkId);
      } else {
         SnapshotBuffer snapshotBufferComponent = componentAccessor.getComponent(ref, SnapshotBuffer.getComponentType());

         assert snapshotBufferComponent != null;

         EntitySnapshot snapshot = snapshotBufferComponent.getSnapshot(snapshotBufferComponent.getCurrentTickIndex());
         if (snapshot != null) {
            return snapshot;
         } else {
            TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

            assert transformComponent != null;

            return new EntitySnapshot(transformComponent.getPosition(), transformComponent.getRotation());
         }
      }
   }

   public void setSnapshotProvider(@Nullable InteractionContext.SnapshotProvider snapshotProvider) {
      this.snapshotProvider = snapshotProvider;
   }

   public void setTimeShift(float shift) {
      if (!this.entry.isUseSimulationState()) {
         this.chain.setTimeShift(shift);
         if (this.chain.getForkedChainId() == null) {
            this.interactionManager.setGlobalTimeShift(this.chain.getType(), shift);
         }
      }
   }

   @Nullable
   public CommandBuffer<EntityStore> getCommandBuffer() {
      return this.interactionManager.commandBuffer;
   }

   @Nullable
   public String getRootInteractionId(@Nonnull InteractionType type) {
      if (this.runningForEntity != null && this.runningForEntity.isValid()) {
         Interactions interactions = this.runningForEntity.getStore().getComponent(this.runningForEntity, Interactions.getComponentType());
         if (interactions != null) {
            String interactionId = interactions.getInteractionId(type);
            if (interactionId != null) {
               return interactionId;
            }
         }
      }

      Item heldItem = this.originalItemType;
      String interactionIds;
      if (heldItem == null) {
         UnarmedInteractions unarmedInteraction = UnarmedInteractions.getAssetMap().getAsset("Empty");
         interactionIds = unarmedInteraction != null ? unarmedInteraction.getInteractions().get(type) : null;
      } else {
         interactionIds = heldItem.getInteractions().get(type);
      }

      return interactionIds;
   }

   void initEntry(@Nonnull InteractionChain chain, InteractionEntry entry, @Nullable LivingEntity entity) {
      CommandBuffer<EntityStore> commandBuffer = this.getCommandBuffer();

      assert commandBuffer != null;

      this.chain = chain;
      this.entry = entry;
      this.entity = entity;
      this.labels = null;
      Player playerComponent = null;
      if (entity != null) {
         playerComponent = commandBuffer.getComponent(entity.getReference(), Player.getComponentType());
      }

      GameMode gameMode = playerComponent != null ? playerComponent.getGameMode() : GameMode.Adventure;
      RootInteractionSettings settings = chain.getRootInteraction().getSettings().get(gameMode);
      chain.skipChainOnClick = chain.skipChainOnClick | (settings != null && settings.allowSkipChainOnClick);
   }

   void deinitEntry(InteractionChain chain, InteractionEntry entry, LivingEntity entity) {
      this.chain = null;
      this.entry = null;
      this.entity = null;
      this.labels = null;
   }

   @Nonnull
   @Override
   public String toString() {
      return "InteractionContext{heldItemSectionId="
         + this.heldItemSectionId
         + ", heldItemContainer="
         + this.heldItemContainer
         + ", heldItemSlot="
         + this.heldItemSlot
         + ", heldItem="
         + this.heldItem
         + ", originalItemType="
         + this.originalItemType
         + ", interactionVarsGetter="
         + this.interactionVarsGetter
         + ", entity="
         + this.entity
         + ", labels="
         + Arrays.toString((Object[])this.labels)
         + ", snapshotProvider="
         + this.snapshotProvider
         + ", metaStore="
         + this.metaStore
         + "}";
   }

   @Nonnull
   public static InteractionContext forProxyEntity(InteractionManager manager, @Nonnull LivingEntity entity, Ref<EntityStore> runningForEntity) {
      Inventory entityInventory = entity.getInventory();
      return new InteractionContext(
         manager,
         entity.getReference(),
         runningForEntity,
         -1,
         entityInventory.getHotbar(),
         entityInventory.getActiveHotbarSlot(),
         entityInventory.getItemInHand()
      );
   }

   @Nonnull
   public static InteractionContext forInteraction(
      @Nonnull InteractionManager manager,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull InteractionType type,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (type == InteractionType.Equipped) {
         throw new IllegalArgumentException("Equipped interaction type requires a slot set");
      } else {
         return forInteraction(manager, ref, type, 0, componentAccessor);
      }
   }

   @Nonnull
   public static InteractionContext forInteraction(
      @Nonnull InteractionManager manager,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull InteractionType type,
      int equipSlot,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      LivingEntity entity = (LivingEntity)EntityUtils.getEntity(ref, componentAccessor);
      Inventory entityInventory = entity.getInventory();
      switch (type) {
         case Equipped:
            return new InteractionContext(
               manager, ref, -3, entityInventory.getArmor(), (byte)equipSlot, entityInventory.getArmor().getItemStack((short)equipSlot)
            );
         case HeldOffhand:
            return new InteractionContext(
               manager, ref, -5, entityInventory.getUtility(), entityInventory.getActiveUtilitySlot(), entityInventory.getUtilityItem()
            );
         case Ability1:
         case Ability2:
         case Ability3:
         case Pick:
         case Primary:
         case Secondary:
            if (entityInventory.usingToolsItem()) {
               return new InteractionContext(manager, ref, -8, entityInventory.getTools(), entityInventory.getActiveToolsSlot(), entityInventory.getToolsItem());
            } else {
               ItemStack primary = entityInventory.getItemInHand();
               ItemStack secondary = entityInventory.getUtilityItem();
               int selectedInventory = -1;
               if (primary == null && secondary != null) {
                  selectedInventory = -5;
               } else if (primary != null && secondary != null) {
                  int prioPrimary = primary.getItem().getInteractionConfig().getPriorityFor(type, PrioritySlot.MainHand);
                  int prioSecondary = secondary.getItem().getInteractionConfig().getPriorityFor(type, PrioritySlot.OffHand);
                  if (prioPrimary == prioSecondary) {
                     if (type == InteractionType.Secondary && primary.getItem().getUtility().isCompatible()) {
                        selectedInventory = -5;
                     }
                  } else if (prioPrimary < prioSecondary) {
                     selectedInventory = -5;
                  } else {
                     if (type == InteractionType.Primary && !primary.getItem().getInteractions().containsKey(InteractionType.Primary)) {
                        selectedInventory = -5;
                     }

                     if (type == InteractionType.Secondary && !primary.getItem().getInteractions().containsKey(InteractionType.Secondary)) {
                        selectedInventory = -5;
                     }
                  }
               }

               if (selectedInventory == -5) {
                  return new InteractionContext(
                     manager, ref, -5, entityInventory.getUtility(), entityInventory.getActiveUtilitySlot(), entityInventory.getUtilityItem()
                  );
               }

               return new InteractionContext(
                  manager, ref, -1, entityInventory.getHotbar(), entityInventory.getActiveHotbarSlot(), entityInventory.getItemInHand()
               );
            }
         case Held:
         default:
            return new InteractionContext(manager, ref, -1, entityInventory.getHotbar(), entityInventory.getActiveHotbarSlot(), entityInventory.getItemInHand());
      }
   }

   @Nonnull
   public static InteractionContext withoutEntity() {
      return new InteractionContext(null, null, -1, null, (byte)-1, null);
   }

   @Nullable
   private static Map<String, String> defaultGetVars(@Nonnull InteractionContext c) {
      Item item = c.originalItemType;
      return item != null ? item.getInteractionVars() : null;
   }

   @Deprecated
   @FunctionalInterface
   public interface SnapshotProvider {
      EntitySnapshot getSnapshot(CommandBuffer<EntityStore> var1, Ref<EntityStore> var2, int var3);
   }
}
