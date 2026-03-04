package com.hypixel.hytale.server.core.modules.entity.item;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.iterator.CircleIterator;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemEntityConfig;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.entity.BlockMigrationExtraInfo;
import com.hypixel.hytale.server.core.modules.entity.DespawnComponent;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemComponent implements Component<EntityStore> {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   public static final BuilderCodec<ItemComponent> CODEC = BuilderCodec.builder(ItemComponent.class, ItemComponent::new)
      .append(new KeyedCodec<>("Item", ItemStack.CODEC), (item, itemStack, extraInfo) -> {
         item.itemStack = itemStack;
         if (extraInfo instanceof BlockMigrationExtraInfo) {
            String newItemId = ((BlockMigrationExtraInfo)extraInfo).getBlockMigration().apply(itemStack.getItemId());
            if (!newItemId.equals(itemStack.getItemId())) {
               item.itemStack = new ItemStack(newItemId, itemStack.getQuantity(), itemStack.getMetadata());
            }
         }
      }, (item, extraInfo) -> item.itemStack)
      .add()
      .append(new KeyedCodec<>("StackDelay", Codec.FLOAT), (item, v) -> item.mergeDelay = v, item -> item.mergeDelay)
      .add()
      .append(new KeyedCodec<>("PickupDelay", Codec.FLOAT), (item, v) -> item.pickupDelay = v, item -> item.pickupDelay)
      .add()
      .append(new KeyedCodec<>("PickupThrottle", Codec.FLOAT), (item, v) -> item.pickupThrottle = v, item -> item.pickupThrottle)
      .add()
      .append(new KeyedCodec<>("RemovedByPlayerPickup", Codec.BOOLEAN), (item, v) -> item.removedByPlayerPickup = v, item -> item.removedByPlayerPickup)
      .add()
      .build();
   private static final float DROPPED_ITEM_VERTICAL_BOUNCE_VELOCITY = 3.25F;
   private static final float DROPPED_ITEM_HORIZONTAL_BOUNCE_VELOCITY = 3.0F;
   public static final float DEFAULT_PICKUP_DELAY = 0.5F;
   public static final float PICKUP_DELAY_DROPPED = 1.5F;
   public static final float PICKUP_THROTTLE = 0.25F;
   public static final float DEFAULT_MERGE_DELAY = 1.5F;
   @Nullable
   private ItemStack itemStack;
   private boolean isNetworkOutdated;
   private float mergeDelay = 1.5F;
   private float pickupDelay = 0.5F;
   private float pickupThrottle;
   private boolean removedByPlayerPickup;
   private float pickupRange = -1.0F;

   @Nonnull
   public static ComponentType<EntityStore, ItemComponent> getComponentType() {
      return EntityModule.get().getItemComponentType();
   }

   public ItemComponent() {
   }

   public ItemComponent(@Nullable ItemStack itemStack) {
      this.itemStack = itemStack;
   }

   public ItemComponent(@Nullable ItemStack itemStack, float mergeDelay, float pickupDelay, float pickupThrottle, boolean removedByPlayerPickup) {
      this.itemStack = itemStack;
      this.mergeDelay = mergeDelay;
      this.pickupDelay = pickupDelay;
      this.pickupThrottle = pickupThrottle;
      this.removedByPlayerPickup = removedByPlayerPickup;
   }

   @Nullable
   public ItemStack getItemStack() {
      return this.itemStack;
   }

   public void setItemStack(@Nullable ItemStack itemStack) {
      this.itemStack = itemStack;
      this.isNetworkOutdated = true;
      this.pickupRange = -1.0F;
   }

   public void setPickupDelay(float pickupDelay) {
      this.pickupDelay = pickupDelay;
   }

   public float getPickupRadius(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.pickupRange < 0.0F) {
         World world = componentAccessor.getExternalData().getWorld();
         ItemEntityConfig defaultConfig = world.getGameplayConfig().getItemEntityConfig();
         ItemEntityConfig config = this.itemStack != null ? this.itemStack.getItem().getItemEntityConfig() : null;
         this.pickupRange = config != null && config.getPickupRadius() != -1.0F ? config.getPickupRadius() : defaultConfig.getPickupRadius();
      }

      return this.pickupRange;
   }

   public float computeLifetimeSeconds(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      ItemEntityConfig itemEntityConfig = this.itemStack != null ? this.itemStack.getItem().getItemEntityConfig() : null;
      ItemEntityConfig defaultConfig = componentAccessor.getExternalData().getWorld().getGameplayConfig().getItemEntityConfig();
      Float ttl = itemEntityConfig != null && itemEntityConfig.getTtl() != null ? itemEntityConfig.getTtl() : defaultConfig.getTtl();
      return ttl != null ? ttl : 120.0F;
   }

   @Nullable
   public ColorLight computeDynamicLight() {
      ColorLight dynamicLight = null;
      Item item = this.itemStack != null ? this.itemStack.getItem() : null;
      if (item != null) {
         if (item.hasBlockType()) {
            BlockType blockType = BlockType.getAssetMap().getAsset(this.itemStack.getBlockKey());
            if (blockType != null && blockType.getLight() != null) {
               dynamicLight = blockType.getLight();
            }
         } else if (item.getLight() != null) {
            dynamicLight = item.getLight();
         }
      }

      return dynamicLight;
   }

   public boolean pollPickupDelay(float dt) {
      if (this.pickupDelay <= 0.0F) {
         return true;
      } else {
         this.pickupDelay -= dt;
         return this.pickupDelay <= 0.0F;
      }
   }

   public boolean pollPickupThrottle(float dt) {
      this.pickupThrottle -= dt;
      if (this.pickupThrottle <= 0.0F) {
         this.pickupThrottle = 0.25F;
         return true;
      } else {
         return false;
      }
   }

   public boolean pollMergeDelay(float dt) {
      this.mergeDelay -= dt;
      if (this.mergeDelay <= 0.0F) {
         this.mergeDelay = 1.5F;
         return true;
      } else {
         return false;
      }
   }

   public boolean canPickUp() {
      return this.pickupDelay <= 0.0F;
   }

   public boolean isRemovedByPlayerPickup() {
      return this.removedByPlayerPickup;
   }

   public void setRemovedByPlayerPickup(boolean removedByPlayerPickup) {
      this.removedByPlayerPickup = removedByPlayerPickup;
   }

   public boolean consumeNetworkOutdated() {
      boolean temp = this.isNetworkOutdated;
      this.isNetworkOutdated = false;
      return temp;
   }

   @Nonnull
   public ItemComponent clone() {
      return new ItemComponent(this.itemStack, this.mergeDelay, this.pickupDelay, this.pickupThrottle, this.removedByPlayerPickup);
   }

   @Nonnull
   public static Holder<EntityStore>[] generateItemDrops(
      @Nonnull ComponentAccessor<EntityStore> accessor, @Nonnull List<ItemStack> itemStacks, @Nonnull Vector3d position, @Nonnull Vector3f rotation
   ) {
      if (itemStacks.size() == 1) {
         Holder<EntityStore> itemEntityHolder = generateItemDrop(accessor, itemStacks.getFirst(), position, rotation, 0.0F, 3.25F, 0.0F);
         return itemEntityHolder == null ? Holder.emptyArray() : new Holder[]{itemEntityHolder};
      } else {
         float randomAngleOffset = ThreadLocalRandom.current().nextFloat() * (float) (Math.PI * 2);
         CircleIterator iterator = new CircleIterator(Vector3d.ZERO, 3.0, itemStacks.size(), randomAngleOffset);
         return itemStacks.stream().map(item -> {
            Vector3d circlePos = iterator.next();
            return generateItemDrop(accessor, item, position, rotation, (float)circlePos.getX(), 3.25F, (float)circlePos.getZ());
         }).filter(Objects::nonNull).toArray(Holder[]::new);
      }
   }

   @Nullable
   public static Holder<EntityStore> generateItemDrop(
      @Nonnull ComponentAccessor<EntityStore> accessor,
      @Nullable ItemStack itemStack,
      @Nonnull Vector3d position,
      @Nonnull Vector3f rotation,
      float velocityX,
      float velocityY,
      float velocityZ
   ) {
      if (itemStack != null && !itemStack.isEmpty() && itemStack.isValid()) {
         Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
         ItemComponent itemComponent = new ItemComponent(itemStack);
         holder.addComponent(getComponentType(), itemComponent);
         holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(position, rotation));
         holder.ensureAndGetComponent(Velocity.getComponentType()).set(velocityX, velocityY, velocityZ);
         holder.ensureComponent(PhysicsValues.getComponentType());
         holder.ensureComponent(UUIDComponent.getComponentType());
         holder.ensureComponent(Intangible.getComponentType());
         float tempTtl = itemComponent.computeLifetimeSeconds(accessor);
         TimeResource timeResource = accessor.getResource(TimeResource.getResourceType());
         holder.addComponent(DespawnComponent.getComponentType(), DespawnComponent.despawnInSeconds(timeResource, tempTtl));
         return holder;
      } else {
         LOGGER.at(Level.WARNING).log("Attempted to drop invalid item %s at %s", itemStack, position);
         return null;
      }
   }

   @Nullable
   public static Holder<EntityStore> generatePickedUpItem(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nonnull Ref<EntityStore> targetRef,
      @Nonnull Vector3d targetPosition
   ) {
      if (!ref.isValid()) {
         LOGGER.at(Level.WARNING).log("Attempted to generate picked up item from invalid entity reference %s", ref.getIndex());
         return null;
      } else {
         TransformComponent itemTransformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());
         if (itemTransformComponent == null) {
            LOGGER.at(Level.WARNING).log("Attempted to generate picked up item from entity %s without a TransformComponent", ref.getIndex());
            return null;
         } else {
            ItemComponent itemItemComponent = componentAccessor.getComponent(ref, getComponentType());
            if (itemItemComponent == null) {
               LOGGER.at(Level.WARNING).log("Attempted to generate picked up item from entity %s without an ItemComponent", ref.getIndex());
               return null;
            } else {
               Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
               PickupItemComponent pickupItemComponent = new PickupItemComponent(targetRef, targetPosition.clone());
               holder.addComponent(PickupItemComponent.getComponentType(), pickupItemComponent);
               holder.addComponent(getComponentType(), itemItemComponent.clone());
               holder.addComponent(TransformComponent.getComponentType(), itemTransformComponent.clone());
               holder.ensureComponent(PreventItemMerging.getComponentType());
               holder.ensureComponent(Intangible.getComponentType());
               holder.addComponent(NetworkId.getComponentType(), new NetworkId(ref.getStore().getExternalData().takeNextNetworkId()));
               holder.ensureComponent(EntityStore.REGISTRY.getNonSerializedComponentType());
               return holder;
            }
         }
      }
   }

   @Nonnull
   public static Holder<EntityStore> generatePickedUpItem(
      @Nonnull ItemStack itemStack, @Nonnull Vector3d position, @Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> targetRef
   ) {
      Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
      PickupItemComponent pickupItemComponent = new PickupItemComponent(targetRef, position.clone());
      holder.addComponent(PickupItemComponent.getComponentType(), pickupItemComponent);
      holder.addComponent(getComponentType(), new ItemComponent(new ItemStack(itemStack.getItemId())));
      holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(position.clone(), Vector3f.ZERO.clone()));
      holder.ensureComponent(PreventItemMerging.getComponentType());
      holder.ensureComponent(Intangible.getComponentType());
      holder.addComponent(NetworkId.getComponentType(), new NetworkId(componentAccessor.getExternalData().takeNextNetworkId()));
      holder.ensureComponent(EntityStore.REGISTRY.getNonSerializedComponentType());
      return holder;
   }

   @Nullable
   public static ItemStack addToItemContainer(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> itemRef, @Nonnull ItemContainer itemContainer) {
      if (!itemRef.isValid()) {
         return null;
      } else {
         ItemComponent itemComponent = store.getComponent(itemRef, getComponentType());
         if (itemComponent != null && !(itemComponent.pickupDelay > 0.0F)) {
            ItemStack itemStack = itemComponent.getItemStack();
            if (itemStack == null) {
               return null;
            } else {
               ItemStackTransaction transaction = itemContainer.addItemStack(itemStack);
               ItemStack remainder = transaction.getRemainder();
               if (remainder != null && !remainder.isEmpty()) {
                  itemComponent.setPickupDelay(0.25F);
                  itemComponent.setItemStack(remainder);
                  int quantity = itemStack.getQuantity() - remainder.getQuantity();
                  return quantity <= 0 ? null : itemStack.withQuantity(quantity);
               } else {
                  store.removeEntity(itemRef, RemoveReason.REMOVE);
                  return itemStack;
               }
            }
         } else {
            return null;
         }
      }
   }
}
