package com.hypixel.hytale.server.core.inventory;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.InteractionChainData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.ItemArmorSlot;
import com.hypixel.hytale.protocol.PickupLocation;
import com.hypixel.hytale.protocol.SmartMoveType;
import com.hypixel.hytale.protocol.packets.inventory.UpdatePlayerInventory;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemArmor;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemUtility;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemWeapon;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.StatModifiersManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ItemContainerWindow;
import com.hypixel.hytale.server.core.entity.entities.player.windows.Window;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.EmptyItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainerUtil;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SortType;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MoveTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.SlotTransaction;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.ChangeActiveSlotInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Inventory implements NetworkSerializable<UpdatePlayerInventory> {
   public static final short DEFAULT_HOTBAR_CAPACITY = 9;
   public static final short DEFAULT_UTILITY_CAPACITY = 4;
   public static final short DEFAULT_TOOLS_CAPACITY = 23;
   public static final short DEFAULT_ARMOR_CAPACITY = (short)ItemArmorSlot.VALUES.length;
   public static final short DEFAULT_STORAGE_ROWS = 4;
   public static final short DEFAULT_STORAGE_COLUMNS = 9;
   public static final short DEFAULT_STORAGE_CAPACITY = 36;
   public static final int HOTBAR_SECTION_ID = -1;
   public static final int STORAGE_SECTION_ID = -2;
   public static final int ARMOR_SECTION_ID = -3;
   public static final int UTILITY_SECTION_ID = -5;
   public static final int TOOLS_SECTION_ID = -8;
   public static final int BACKPACK_SECTION_ID = -9;
   public static final byte INACTIVE_SLOT_INDEX = -1;
   public static final int VERSION = 4;
   public static final BuilderCodec<Inventory> CODEC = BuilderCodec.builder(Inventory.class, () -> new Inventory(null))
      .versioned()
      .codecVersion(4)
      .append(new KeyedCodec<>("Storage", ItemContainer.CODEC), (o, i) -> o.storage = i, o -> o.storage)
      .add()
      .append(new KeyedCodec<>("Armor", ItemContainer.CODEC), (o, i) -> o.armor = i, o -> o.armor)
      .add()
      .append(new KeyedCodec<>("HotBar", ItemContainer.CODEC), (o, i) -> o.hotbar = i, o -> o.hotbar)
      .add()
      .append(new KeyedCodec<>("Utility", ItemContainer.CODEC), (o, i) -> o.utility = i, o -> o.utility)
      .add()
      .append(new KeyedCodec<>("Backpack", ItemContainer.CODEC), (o, i) -> o.backpack = i, o -> o.backpack)
      .add()
      .append(new KeyedCodec<>("ActiveHotbarSlot", Codec.BYTE), (o, i) -> o.activeHotbarSlot = i, o -> o.activeHotbarSlot)
      .add()
      .append(new KeyedCodec<>("Tool", ItemContainer.CODEC), (o, i) -> o.tools = i, o -> o.tools)
      .add()
      .append(new KeyedCodec<>("ActiveToolsSlot", Codec.BYTE), (o, i) -> o.activeToolsSlot = i, o -> o.activeToolsSlot)
      .add()
      .append(new KeyedCodec<>("ActiveUtilitySlot", Codec.BYTE), (o, i) -> o.activeUtilitySlot = i, o -> o.activeUtilitySlot)
      .add()
      .<SortType>append(new KeyedCodec<>("SortType", new EnumCodec<>(SortType.class, EnumCodec.EnumStyle.LEGACY)), (o, i) -> o.sortType = i, o -> o.sortType)
      .setVersionRange(0, 3)
      .add()
      .<SortType>append(new KeyedCodec<>("SortType", new EnumCodec<>(SortType.class)), (o, i) -> o.sortType = i, o -> o.sortType)
      .setVersionRange(4, 4)
      .add()
      .afterDecode(Inventory::postDecode)
      .build();
   private final AtomicBoolean isDirty = new AtomicBoolean();
   private final AtomicBoolean needsSaving = new AtomicBoolean();
   private ItemContainer storage = EmptyItemContainer.INSTANCE;
   private ItemContainer armor = EmptyItemContainer.INSTANCE;
   private ItemContainer hotbar = EmptyItemContainer.INSTANCE;
   private ItemContainer utility = EmptyItemContainer.INSTANCE;
   @Deprecated
   private ItemContainer tools = EmptyItemContainer.INSTANCE;
   private ItemContainer backpack = EmptyItemContainer.INSTANCE;
   private CombinedItemContainer combinedHotbarStorageBackpack;
   private CombinedItemContainer combinedHotbarFirst;
   private CombinedItemContainer combinedStorageFirst;
   private CombinedItemContainer combinedBackpackStorageHotbar;
   private CombinedItemContainer combinedBackpackHotbarStorage;
   private CombinedItemContainer combinedStorageHotbarBackpack;
   private CombinedItemContainer combinedArmorHotbarStorage;
   private CombinedItemContainer combinedArmorHotbarUtilityStorage;
   private CombinedItemContainer combinedHotbarUtilityConsumableStorage;
   private CombinedItemContainer combinedEverything;
   private byte activeHotbarSlot;
   private byte activeUtilitySlot = -1;
   private byte activeToolsSlot = -1;
   @Nullable
   private LivingEntity entity;
   @Deprecated
   private SortType sortType = SortType.NAME;
   @Nullable
   private EventRegistration armorChange;
   @Nullable
   private EventRegistration storageChange;
   @Nullable
   private EventRegistration hotbarChange;
   @Nullable
   private EventRegistration utilityChange;
   @Nullable
   private EventRegistration toolChange;
   @Nullable
   private EventRegistration backpackChange;
   private boolean _usingToolsItem = false;

   private Inventory(Void dummy) {
   }

   public Inventory() {
      this((short)36, DEFAULT_ARMOR_CAPACITY, (short)9, (short)4, (short)23);
   }

   public Inventory(short storageCapacity, short armorCapacity, short hotbarCapacity, short utilityCapacity, short toolCapacity) {
      this(
         (ItemContainer)(storageCapacity == 0 ? EmptyItemContainer.INSTANCE : new SimpleItemContainer(storageCapacity)),
         (ItemContainer)(armorCapacity == 0 ? EmptyItemContainer.INSTANCE : new SimpleItemContainer(armorCapacity)),
         (ItemContainer)(hotbarCapacity == 0 ? EmptyItemContainer.INSTANCE : new SimpleItemContainer(hotbarCapacity)),
         (ItemContainer)(utilityCapacity == 0 ? EmptyItemContainer.INSTANCE : new SimpleItemContainer(utilityCapacity)),
         (ItemContainer)(toolCapacity == 0 ? EmptyItemContainer.INSTANCE : new SimpleItemContainer(toolCapacity)),
         EmptyItemContainer.INSTANCE
      );
   }

   public Inventory(ItemContainer storage, ItemContainer armor, ItemContainer hotbar, ItemContainer utility, ItemContainer tools, ItemContainer backpack) {
      this.storage = storage;
      this.armor = ItemContainerUtil.trySetArmorFilters(armor);
      this.hotbar = hotbar;
      this.utility = ItemContainerUtil.trySetSlotFilters(
         utility, (type, container, slot, itemStack) -> itemStack == null || itemStack.getItem().getUtility().isUsable()
      );
      this.tools = tools;
      this.backpack = backpack;
      this.buildCombinedContains();
      this.registerChangeEvents();
   }

   protected void registerChangeEvents() {
      this.storageChange = this.storage
         .registerChangeEvent(
            e -> {
               this.markChanged();
               IEventDispatcher<LivingEntityInventoryChangeEvent, LivingEntityInventoryChangeEvent> dispatcher = HytaleServer.get()
                  .getEventBus()
                  .dispatchFor(LivingEntityInventoryChangeEvent.class, this.entity.getWorld().getName());
               if (dispatcher.hasListener()) {
                  dispatcher.dispatch(new LivingEntityInventoryChangeEvent(this.entity, e.container(), e.transaction()));
               }
            }
         );
      this.armorChange = this.armor
         .registerChangeEvent(
            e -> {
               this.markChanged();
               IEventDispatcher<LivingEntityInventoryChangeEvent, LivingEntityInventoryChangeEvent> dispatcher = HytaleServer.get()
                  .getEventBus()
                  .dispatchFor(LivingEntityInventoryChangeEvent.class, this.entity.getWorld().getName());
               if (dispatcher.hasListener()) {
                  dispatcher.dispatch(new LivingEntityInventoryChangeEvent(this.entity, e.container(), e.transaction()));
               }

               this.entity.invalidateEquipmentNetwork();
            }
         );
      this.hotbarChange = this.hotbar
         .registerChangeEvent(
            e -> {
               this.markChanged();
               IEventDispatcher<LivingEntityInventoryChangeEvent, LivingEntityInventoryChangeEvent> dispatcher = HytaleServer.get()
                  .getEventBus()
                  .dispatchFor(LivingEntityInventoryChangeEvent.class, this.entity.getWorld().getName());
               if (dispatcher.hasListener()) {
                  dispatcher.dispatch(new LivingEntityInventoryChangeEvent(this.entity, e.container(), e.transaction()));
               }

               if (this.activeHotbarSlot != -1 && this.entity != null && e.transaction().wasSlotModified(this.activeHotbarSlot)) {
                  if (e.transaction() instanceof SlotTransaction slot && ItemStack.isEquivalentType(slot.getSlotBefore(), slot.getSlotAfter())) {
                     return;
                  }

                  StatModifiersManager statModifiersManager = this.entity.getStatModifiersManager();
                  this.entity.invalidateEquipmentNetwork();
                  statModifiersManager.setRecalculate(true);
                  ItemStack itemStack = this.getItemInHand();
                  if (itemStack == null) {
                     return;
                  }

                  ItemWeapon itemWeapon = itemStack.getItem().getWeapon();
                  if (itemWeapon == null) {
                     return;
                  }

                  int[] entityStatsToClear = itemWeapon.getEntityStatsToClear();
                  if (entityStatsToClear == null) {
                     return;
                  }

                  statModifiersManager.queueEntityStatsToClear(entityStatsToClear);
               }
            }
         );
      this.utilityChange = this.utility
         .registerChangeEvent(
            e -> {
               this.markChanged();
               IEventDispatcher<LivingEntityInventoryChangeEvent, LivingEntityInventoryChangeEvent> dispatcher = HytaleServer.get()
                  .getEventBus()
                  .dispatchFor(LivingEntityInventoryChangeEvent.class, this.entity.getWorld().getName());
               if (dispatcher.hasListener()) {
                  dispatcher.dispatch(new LivingEntityInventoryChangeEvent(this.entity, e.container(), e.transaction()));
               }

               if (this.activeUtilitySlot != -1 && this.entity != null && e.transaction().wasSlotModified(this.activeUtilitySlot)) {
                  if (e.transaction() instanceof SlotTransaction slot && ItemStack.isEquivalentType(slot.getSlotBefore(), slot.getSlotAfter())) {
                     return;
                  }

                  StatModifiersManager statModifiersManager = this.entity.getStatModifiersManager();
                  this.entity.invalidateEquipmentNetwork();
                  statModifiersManager.setRecalculate(true);
                  ItemStack itemStack = this.getUtilityItem();
                  if (itemStack == null) {
                     return;
                  }

                  ItemUtility itemUtility = itemStack.getItem().getUtility();
                  if (itemUtility == null) {
                     return;
                  }

                  int[] entityStatsToClear = itemUtility.getEntityStatsToClear();
                  if (entityStatsToClear == null) {
                     return;
                  }

                  statModifiersManager.queueEntityStatsToClear(entityStatsToClear);
               }
            }
         );
      this.toolChange = this.tools
         .registerChangeEvent(
            e -> {
               this.markChanged();
               IEventDispatcher<LivingEntityInventoryChangeEvent, LivingEntityInventoryChangeEvent> dispatcher = HytaleServer.get()
                  .getEventBus()
                  .dispatchFor(LivingEntityInventoryChangeEvent.class, this.entity.getWorld().getName());
               if (dispatcher.hasListener()) {
                  dispatcher.dispatch(new LivingEntityInventoryChangeEvent(this.entity, e.container(), e.transaction()));
               }
            }
         );
      this.registerBackpackListener();
   }

   private void registerBackpackListener() {
      this.unregisterBackpackChange();
      this.backpackChange = this.backpack
         .registerChangeEvent(
            e -> {
               this.markChanged();
               IEventDispatcher<LivingEntityInventoryChangeEvent, LivingEntityInventoryChangeEvent> dispatcher = HytaleServer.get()
                  .getEventBus()
                  .dispatchFor(LivingEntityInventoryChangeEvent.class, this.entity.getWorld().getName());
               if (dispatcher.hasListener()) {
                  dispatcher.dispatch(new LivingEntityInventoryChangeEvent(this.entity, e.container(), e.transaction()));
               }
            }
         );
   }

   public void unregister() {
      this.entity = null;
      if (this.storageChange != null) {
         this.storageChange.unregister();
         this.storageChange = null;
      }

      if (this.armorChange != null) {
         this.armorChange.unregister();
         this.armorChange = null;
      }

      if (this.hotbarChange != null) {
         this.hotbarChange.unregister();
         this.hotbarChange = null;
      }

      if (this.utilityChange != null) {
         this.utilityChange.unregister();
         this.utilityChange = null;
      }

      if (this.toolChange != null) {
         this.toolChange.unregister();
         this.toolChange = null;
      }

      this.unregisterBackpackChange();
   }

   private void unregisterBackpackChange() {
      if (this.backpackChange != null) {
         this.backpackChange.unregister();
         this.backpackChange = null;
      }
   }

   public void markChanged() {
      this.isDirty.set(true);
      this.needsSaving.set(true);
   }

   public void moveItem(int fromSectionId, int fromSlotId, int quantity, int toSectionId, int toSlotId) {
      ItemContainer fromContainer = this.getSectionById(fromSectionId);
      if (fromContainer != null) {
         ItemContainer toContainer = this.getSectionById(toSectionId);
         if (toContainer != null) {
            if (this.entity instanceof Player
               && (toSectionId == -1 && this.activeHotbarSlot == toSlotId || fromSectionId == -1 && this.activeHotbarSlot == fromSlotId)) {
               ItemStack fromItem = fromContainer.getItemStack((short)fromSlotId);
               ItemStack currentItem = toContainer.getItemStack((short)toSlotId);
               if (ItemStack.isStackableWith(fromItem, currentItem) || ItemStack.isSameItemType(fromItem, currentItem)) {
                  fromContainer.moveItemStackFromSlotToSlot((short)fromSlotId, quantity, toContainer, (short)toSlotId);
                  return;
               }

               int interactionSlot = toSectionId == -1 && this.activeHotbarSlot == toSlotId ? toSlotId : this.activeHotbarSlot;
               Ref<EntityStore> ref = this.entity.getReference();
               Store<EntityStore> store = ref.getStore();
               InteractionManager interactionManagerComponent = store.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());
               if (interactionManagerComponent != null) {
                  PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());

                  assert playerRefComponent != null;

                  InteractionContext context = InteractionContext.forInteraction(interactionManagerComponent, ref, InteractionType.SwapFrom, store);
                  context.getMetaStore().putMetaObject(Interaction.TARGET_SLOT, interactionSlot);
                  context.getMetaStore().putMetaObject(ChangeActiveSlotInteraction.PLACE_MOVED_ITEM, () -> {
                     fromContainer.moveItemStackFromSlotToSlot((short)fromSlotId, quantity, toContainer, (short)toSlotId);
                     playerRefComponent.getPacketHandler().write(this.toPacket());
                  });
                  String interactions = context.getRootInteractionId(InteractionType.SwapFrom);
                  InteractionChainData data = new InteractionChainData(-1, UUIDUtil.EMPTY_UUID, null, null, null, -interactionSlot - 1, null);
                  InteractionChain chain = interactionManagerComponent.initChain(
                     data, InteractionType.SwapFrom, context, RootInteraction.getRootInteractionOrUnknown(interactions), null, false
                  );
                  interactionManagerComponent.queueExecuteChain(chain);
                  return;
               }
            }

            fromContainer.moveItemStackFromSlotToSlot((short)fromSlotId, quantity, toContainer, (short)toSlotId);
         }
      }
   }

   public void smartMoveItem(int fromSectionId, int fromSlotId, int quantity, @Nonnull SmartMoveType moveType, PlayerSettings settings) {
      ItemContainer targetContainer = this.getSectionById(fromSectionId);
      if (targetContainer != null) {
         ItemStack itemStack = targetContainer.getItemStack((short)fromSlotId);
         if (!ItemStack.isEmpty(itemStack)) {
            switch (moveType) {
               case EquipOrMergeStack:
                  if (this.tryEquipArmorPart(itemStack, fromSectionId, (short)fromSlotId, quantity, targetContainer, true)) {
                     return;
                  }

                  if (this.entity instanceof Player) {
                     for (Window window : ((Player)this.entity).getWindowManager().getWindows()) {
                        if (window instanceof ItemContainerWindow) {
                           ((ItemContainerWindow)window).getItemContainer().combineItemStacksIntoSlot(targetContainer, (short)fromSlotId);
                        }
                     }
                  }

                  this.combinedEverything.combineItemStacksIntoSlot(targetContainer, (short)fromSlotId);
                  break;
               case PutInHotbarOrWindow:
                  if (fromSectionId >= 0) {
                     this.moveItemFromCheckToInventory(itemStack, targetContainer, (short)fromSlotId, quantity, settings);
                     return;
                  }

                  if (this.entity instanceof Player) {
                     for (Window windowx : ((Player)this.entity).getWindowManager().getWindows()) {
                        if (windowx instanceof ItemContainerWindow) {
                           ItemContainer itemContainer = ((ItemContainerWindow)windowx).getItemContainer();
                           MoveTransaction<ItemStackTransaction> transaction = targetContainer.moveItemStackFromSlot((short)fromSlotId, quantity, itemContainer);
                           ItemStack remainder = transaction.getAddTransaction().getRemainder();
                           if (ItemStack.isEmpty(remainder) || remainder.getQuantity() != quantity) {
                              return;
                           }
                        }
                     }
                  }

                  if (this.tryEquipArmorPart(itemStack, fromSectionId, (short)fromSlotId, quantity, targetContainer, false)) {
                     return;
                  }

                  switch (fromSectionId) {
                     case -2:
                        targetContainer.moveItemStackFromSlot((short)fromSlotId, quantity, this.hotbar);
                        return;
                     case -1:
                        targetContainer.moveItemStackFromSlot((short)fromSlotId, quantity, this.storage);
                        return;
                     default:
                        this.moveItemFromCheckToInventory(itemStack, targetContainer, (short)fromSlotId, quantity, settings);
                        return;
                  }
               case PutInHotbarOrBackpack:
                  if (fromSectionId == -9) {
                     this.moveItemFromCheckToInventory(itemStack, targetContainer, (short)fromSlotId, quantity, settings);
                  } else {
                     targetContainer.moveItemStackFromSlot(
                        (short)fromSlotId, quantity, this.getContainerForItemPickup(itemStack.getItem(), settings, PickupLocation.Backpack)
                     );
                  }
            }
         }
      }
   }

   private boolean tryEquipArmorPart(
      @Nonnull ItemStack itemStack, int fromSectionId, short fromSlotId, int quantity, ItemContainer targetContainer, boolean forceEquip
   ) {
      Item item = itemStack.getItem();
      ItemArmor itemArmor = item.getArmor();
      if (itemArmor == null || fromSectionId == -3 || !forceEquip && this.armor.getItemStack((short)itemArmor.getArmorSlot().ordinal()) != null) {
         return false;
      } else {
         targetContainer.moveItemStackFromSlotToSlot(fromSlotId, quantity, this.armor, (short)itemArmor.getArmorSlot().ordinal());
         return true;
      }
   }

   private MoveTransaction<ItemStackTransaction> moveItemFromCheckToInventory(
      @Nonnull ItemStack itemStack, @Nonnull ItemContainer targetContainer, short fromSlotId, int quantity, PlayerSettings settings
   ) {
      return targetContainer.moveItemStackFromSlot(fromSlotId, quantity, this.getContainerForItemPickup(itemStack.getItem(), settings));
   }

   @Nullable
   public ListTransaction<MoveTransaction<ItemStackTransaction>> takeAll(int inventorySectionId, PlayerSettings settings) {
      ItemContainer container = this.getSectionById(inventorySectionId);
      return container == null ? null : this.takeAllWithPriority(container, settings);
   }

   public ListTransaction<MoveTransaction<ItemStackTransaction>> takeAllWithPriority(ItemContainer fromContainer, PlayerSettings settings) {
      List<MoveTransaction<ItemStackTransaction>> transactions = new ObjectArrayList();

      for (int slot = 0; slot < fromContainer.getCapacity(); slot++) {
         ItemStack stack = fromContainer.getItemStack((short)slot);
         if (!ItemStack.isEmpty(stack)) {
            transactions.add(this.moveItemFromCheckToInventory(stack, fromContainer, (short)slot, stack.getQuantity(), settings));
         }
      }

      return new ListTransaction<>(true, transactions);
   }

   @Nullable
   public ListTransaction<MoveTransaction<ItemStackTransaction>> putAll(int inventorySectionId) {
      ItemContainer sectionById = this.getSectionById(inventorySectionId);
      return sectionById != null ? this.storage.moveAllItemStacksTo(sectionById) : null;
   }

   @Nullable
   public ListTransaction<MoveTransaction<ItemStackTransaction>> quickStack(int inventorySectionId) {
      ItemContainer sectionById = this.getSectionById(inventorySectionId);
      return sectionById != null ? this.combinedHotbarFirst.quickStackTo(sectionById) : null;
   }

   @Nonnull
   public List<ItemStack> dropAllItemStacks() {
      List<ItemStack> items = new ObjectArrayList();
      items.addAll(this.storage.dropAllItemStacks());
      items.addAll(this.armor.dropAllItemStacks());
      items.addAll(this.hotbar.dropAllItemStacks());
      items.addAll(this.utility.dropAllItemStacks());
      items.addAll(this.backpack.dropAllItemStacks());
      return items;
   }

   public void clear() {
      this.storage.clear();
      this.armor.clear();
      this.hotbar.clear();
      this.utility.clear();
      this.backpack.clear();
   }

   public ItemContainer getStorage() {
      return this.storage;
   }

   public ItemContainer getArmor() {
      return this.armor;
   }

   public ItemContainer getHotbar() {
      return this.hotbar;
   }

   public ItemContainer getUtility() {
      return this.utility;
   }

   public ItemContainer getTools() {
      return this.tools;
   }

   public ItemContainer getBackpack() {
      return this.backpack;
   }

   public void resizeBackpack(short capacity, List<ItemStack> remainder) {
      if (capacity > 0) {
         this.backpack = ItemContainer.ensureContainerCapacity(this.backpack, capacity, SimpleItemContainer::new, remainder);
      } else {
         this.backpack = ItemContainer.copy(this.backpack, EmptyItemContainer.INSTANCE, remainder);
      }

      this.buildCombinedContains();
      if (this.entity != null) {
         this.registerBackpackListener();
      }

      this.markChanged();
   }

   public CombinedItemContainer getCombinedHotbarFirst() {
      return this.combinedHotbarFirst;
   }

   public CombinedItemContainer getCombinedStorageFirst() {
      return this.combinedStorageFirst;
   }

   public CombinedItemContainer getCombinedBackpackStorageHotbar() {
      return this.combinedBackpackStorageHotbar;
   }

   public CombinedItemContainer getCombinedBackpackStorageHotbarFirst() {
      return this.combinedHotbarStorageBackpack;
   }

   public CombinedItemContainer getCombinedArmorHotbarUtilityStorage() {
      return this.combinedArmorHotbarUtilityStorage;
   }

   public CombinedItemContainer getCombinedHotbarUtilityConsumableStorage() {
      return this.combinedHotbarUtilityConsumableStorage;
   }

   public CombinedItemContainer getCombinedEverything() {
      return this.combinedEverything;
   }

   private ItemContainer getItemContainerForPickupLocation(@Nonnull PickupLocation pickupLocation) {
      return switch (pickupLocation) {
         case Hotbar -> this.combinedHotbarStorageBackpack;
         case Storage -> this.combinedStorageHotbarBackpack;
         case Backpack -> this.combinedBackpackStorageHotbar;
         default -> this.combinedHotbarStorageBackpack;
      };
   }

   @Nonnull
   public ItemContainer getContainerForItemPickup(@Nonnull Item item, PlayerSettings playerSettings) {
      return this.getContainerForItemPickup(item, playerSettings, null);
   }

   @Nonnull
   public ItemContainer getContainerForItemPickup(@Nonnull Item item, PlayerSettings playerSettings, @Nullable PickupLocation overridePickupLocation) {
      if (overridePickupLocation != null) {
         return this.getItemContainerForPickupLocation(overridePickupLocation);
      } else if (item.getArmor() != null) {
         return this.getItemContainerForPickupLocation(playerSettings.armorItemsPreferredPickupLocation());
      } else if (item.getWeapon() != null || item.getTool() != null) {
         return this.getItemContainerForPickupLocation(playerSettings.weaponAndToolItemsPreferredPickupLocation());
      } else if (item.getUtility().isUsable()) {
         return this.getItemContainerForPickupLocation(playerSettings.usableItemsItemsPreferredPickupLocation());
      } else {
         BlockType blockType = item.hasBlockType() ? BlockType.getAssetMap().getAsset(item.getBlockId()) : BlockType.EMPTY;
         if (blockType == null) {
            blockType = BlockType.EMPTY;
         }

         return blockType.getMaterial() == BlockMaterial.Solid
            ? this.getItemContainerForPickupLocation(playerSettings.solidBlockItemsPreferredPickupLocation())
            : this.getItemContainerForPickupLocation(playerSettings.miscItemsPreferredPickupLocation());
      }
   }

   public void setActiveSlot(int inventorySectionId, byte slot) {
      int[] entityStatsToClear = null;
      switch (inventorySectionId) {
         case -8:
            this.activeToolsSlot = slot;
            break;
         case -5:
            this.activeUtilitySlot = slot;
            ItemStack itemStack = this.getUtilityItem();
            if (itemStack != null) {
               ItemUtility utility = itemStack.getItem().getUtility();
               entityStatsToClear = utility.getEntityStatsToClear();
            }
            break;
         case -1:
            this.activeHotbarSlot = slot;
            ItemStack itemStackx = this.getItemInHand();
            if (itemStackx != null) {
               ItemWeapon weapon = itemStackx.getItem().getWeapon();
               if (weapon != null) {
                  entityStatsToClear = weapon.getEntityStatsToClear();
               }
            }
            break;
         default:
            throw new IllegalArgumentException("Inventory section with id " + inventorySectionId + " cannot select an active slot");
      }

      StatModifiersManager statModifiersManager = this.entity.getStatModifiersManager();
      this.entity.invalidateEquipmentNetwork();
      statModifiersManager.setRecalculate(true);
      if (entityStatsToClear != null) {
         statModifiersManager.queueEntityStatsToClear(entityStatsToClear);
      }
   }

   public byte getActiveSlot(int inventorySectionId) {
      return switch (inventorySectionId) {
         case -8 -> this.activeToolsSlot;
         case -5 -> this.activeUtilitySlot;
         case -1 -> this.activeHotbarSlot;
         default -> throw new IllegalArgumentException("Inventory section with id " + inventorySectionId + " cannot select an active slot");
      };
   }

   public byte getActiveHotbarSlot() {
      return this.activeHotbarSlot;
   }

   public void setActiveHotbarSlot(byte slot) {
      this.setUsingToolsItem(false);
      this.setActiveSlot(-1, slot);
   }

   @Nullable
   public ItemStack getActiveHotbarItem() {
      return this.activeHotbarSlot == -1 ? null : this.hotbar.getItemStack(this.activeHotbarSlot);
   }

   @Nullable
   public ItemStack getActiveToolItem() {
      return this.activeToolsSlot == -1 ? null : this.tools.getItemStack(this.activeToolsSlot);
   }

   @Nullable
   public ItemStack getItemInHand() {
      return this._usingToolsItem ? this.getActiveToolItem() : this.getActiveHotbarItem();
   }

   public byte getActiveUtilitySlot() {
      return this.activeUtilitySlot;
   }

   public void setActiveUtilitySlot(byte slot) {
      this.setActiveSlot(-5, slot);
   }

   @Nullable
   public ItemStack getUtilityItem() {
      return this.activeUtilitySlot == -1 ? null : this.utility.getItemStack(this.activeUtilitySlot);
   }

   public byte getActiveToolsSlot() {
      return this.activeToolsSlot;
   }

   public void setActiveToolsSlot(byte slot) {
      this.setUsingToolsItem(true);
      this.setActiveSlot(-8, slot);
   }

   @Nullable
   public ItemStack getToolsItem() {
      return this.activeToolsSlot == -1 ? null : this.tools.getItemStack(this.activeToolsSlot);
   }

   @Nullable
   public ItemContainer getSectionById(int id) {
      if (id >= 0) {
         if (this.entity instanceof Player) {
            Window window = ((Player)this.entity).getWindowManager().getWindow(id);
            if (window instanceof ItemContainerWindow) {
               return ((ItemContainerWindow)window).getItemContainer();
            }
         }

         return null;
      } else {
         return switch (id) {
            case -9 -> this.backpack;
            case -8 -> this.tools;
            default -> null;
            case -5 -> this.utility;
            case -3 -> this.armor;
            case -2 -> this.storage;
            case -1 -> this.hotbar;
         };
      }
   }

   public boolean consumeIsDirty() {
      return this.isDirty.getAndSet(false);
   }

   public boolean consumeNeedsSaving() {
      return this.needsSaving.getAndSet(false);
   }

   public void setEntity(LivingEntity entity) {
      this.entity = entity;
   }

   public void sortStorage(@Nonnull SortType type) {
      this.sortType = type;
      this.storage.sortItems(type);
      this.markChanged();
   }

   public void setSortType(SortType type) {
      this.sortType = type;
      this.markChanged();
   }

   public boolean containsBrokenItem() {
      boolean hasBrokenItem = false;

      for (short i = 0; i < this.combinedEverything.getCapacity(); i++) {
         ItemStack itemStack = this.combinedEverything.getItemStack(i);
         if (!ItemStack.isEmpty(itemStack) && itemStack.isBroken()) {
            hasBrokenItem = true;
         }
      }

      return hasBrokenItem;
   }

   @Nonnull
   public UpdatePlayerInventory toPacket() {
      UpdatePlayerInventory packet = new UpdatePlayerInventory();
      packet.storage = this.storage.toPacket();
      packet.armor = this.armor.toPacket();
      packet.hotbar = this.hotbar.toPacket();
      packet.utility = this.utility.toPacket();
      packet.tools = this.tools.toPacket();
      packet.backpack = this.backpack.toPacket();
      packet.sortType = this.sortType.toPacket();
      return packet;
   }

   public void doMigration(Function<String, String> blockMigration) {
      Objects.requireNonNull(blockMigration);
      this.hotbar.doMigration(blockMigration);
      this.storage.doMigration(blockMigration);
      this.armor.doMigration(blockMigration);
      this.utility.doMigration(blockMigration);
      this.tools.doMigration(blockMigration);
      this.backpack.doMigration(blockMigration);
   }

   private void postDecode() {
      this.armor = ItemContainerUtil.trySetArmorFilters(this.armor);
      this.utility = ItemContainerUtil.trySetSlotFilters(
         this.utility, (type, container, slot, itemStack) -> itemStack == null || itemStack.getItem().getUtility().isUsable()
      );
      this.activeHotbarSlot = (byte)(this.activeHotbarSlot < this.hotbar.getCapacity() ? this.activeHotbarSlot : (this.hotbar.getCapacity() > 0 ? 0 : -1));
      this.activeUtilitySlot = this.activeUtilitySlot < this.utility.getCapacity() ? this.activeUtilitySlot : -1;
      this.activeToolsSlot = this.activeToolsSlot < this.tools.getCapacity() ? this.activeToolsSlot : -1;
      this.buildCombinedContains();
      this.registerChangeEvents();
   }

   private void buildCombinedContains() {
      this.combinedHotbarFirst = new CombinedItemContainer(this.hotbar, this.storage);
      this.combinedStorageFirst = new CombinedItemContainer(this.storage, this.hotbar);
      this.combinedBackpackStorageHotbar = new CombinedItemContainer(this.backpack, this.storage, this.hotbar);
      this.combinedBackpackHotbarStorage = new CombinedItemContainer(this.backpack, this.hotbar, this.storage);
      this.combinedStorageHotbarBackpack = new CombinedItemContainer(this.storage, this.hotbar, this.backpack);
      this.combinedHotbarStorageBackpack = new CombinedItemContainer(this.hotbar, this.storage, this.backpack);
      this.combinedArmorHotbarStorage = new CombinedItemContainer(this.armor, this.hotbar, this.storage);
      this.combinedArmorHotbarUtilityStorage = new CombinedItemContainer(this.armor, this.hotbar, this.utility, this.storage);
      this.combinedHotbarUtilityConsumableStorage = new CombinedItemContainer(this.hotbar, this.utility, this.storage);
      this.combinedEverything = new CombinedItemContainer(this.armor, this.hotbar, this.utility, this.storage, this.backpack);
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Inventory inventory = (Inventory)o;
         if (this.activeHotbarSlot != inventory.activeHotbarSlot) {
            return false;
         } else if (this.activeUtilitySlot != inventory.activeUtilitySlot) {
            return false;
         } else if (this.isDirty != inventory.isDirty) {
            return false;
         } else if (this.needsSaving != inventory.needsSaving) {
            return false;
         } else if (!Objects.equals(this.storage, inventory.storage)) {
            return false;
         } else if (!Objects.equals(this.armor, inventory.armor)) {
            return false;
         } else if (!Objects.equals(this.utility, inventory.utility)) {
            return false;
         } else if (!Objects.equals(this.tools, inventory.tools)) {
            return false;
         } else {
            return !Objects.equals(this.backpack, inventory.backpack) ? false : Objects.equals(this.hotbar, inventory.hotbar);
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.storage != null ? this.storage.hashCode() : 0;
      result = 31 * result + (this.armor != null ? this.armor.hashCode() : 0);
      result = 31 * result + (this.hotbar != null ? this.hotbar.hashCode() : 0);
      result = 31 * result + (this.utility != null ? this.utility.hashCode() : 0);
      result = 31 * result + (this.tools != null ? this.tools.hashCode() : 0);
      result = 31 * result + (this.backpack != null ? this.backpack.hashCode() : 0);
      result = 31 * result + this.activeHotbarSlot;
      result = 31 * result + this.activeUtilitySlot;
      result = 31 * result + this.activeToolsSlot;
      result = 31 * result + this.isDirty.hashCode();
      return 31 * result + this.needsSaving.hashCode();
   }

   @Nonnull
   @Override
   public String toString() {
      return "Inventory{, storage="
         + this.storage
         + ", armor="
         + this.armor
         + ", hotbar="
         + this.hotbar
         + ", utility="
         + this.utility
         + ", activeHotbarSlot="
         + this.activeHotbarSlot
         + ", activeUtilitySlot="
         + this.activeUtilitySlot
         + ", activeToolsSlot="
         + this.activeToolsSlot
         + ", isDirty="
         + this.isDirty
         + ", needsSaving="
         + this.needsSaving
         + "}";
   }

   @Nonnull
   public static Inventory ensureCapacity(@Nonnull Inventory inventory, List<ItemStack> remainder) {
      ItemContainer storage = inventory.getStorage();
      ItemContainer armor = inventory.getArmor();
      ItemContainer hotbar = inventory.getHotbar();
      ItemContainer utility = inventory.getUtility();
      ItemContainer tool = inventory.getTools();
      if (storage.getCapacity() == 36
         && armor.getCapacity() == DEFAULT_ARMOR_CAPACITY
         && hotbar.getCapacity() == 9
         && utility.getCapacity() == 4
         && tool.getCapacity() == 23) {
         return inventory;
      } else {
         ItemContainer newStorage = ItemContainer.ensureContainerCapacity(storage, (short)36, SimpleItemContainer::new, remainder);
         ItemContainer newArmor = ItemContainer.ensureContainerCapacity(armor, DEFAULT_ARMOR_CAPACITY, SimpleItemContainer::new, remainder);
         ItemContainer newHotbar = ItemContainer.ensureContainerCapacity(hotbar, (short)9, SimpleItemContainer::new, remainder);
         ItemContainer newUtility = ItemContainer.ensureContainerCapacity(utility, (short)4, SimpleItemContainer::new, remainder);
         ItemContainer newTool = ItemContainer.ensureContainerCapacity(tool, (short)23, SimpleItemContainer::new, remainder);
         byte activeHotbarSlot = inventory.getActiveHotbarSlot();
         if (activeHotbarSlot > newHotbar.getCapacity()) {
            activeHotbarSlot = (byte)(hotbar.getCapacity() > 0 ? 0 : -1);
         }

         byte activeUtilitySlot = inventory.getActiveUtilitySlot();
         if (activeUtilitySlot > newUtility.getCapacity()) {
            activeUtilitySlot = -1;
         }

         byte activeToolsSlot = inventory.getActiveToolsSlot();
         if (activeToolsSlot > newTool.getCapacity()) {
            activeToolsSlot = -1;
         }

         inventory.unregister();
         Inventory newInventory = new Inventory(newStorage, newArmor, newHotbar, newUtility, newTool, EmptyItemContainer.INSTANCE);
         newInventory.activeHotbarSlot = activeHotbarSlot;
         newInventory.activeUtilitySlot = activeUtilitySlot;
         newInventory.activeToolsSlot = activeToolsSlot;
         newInventory.setSortType(inventory.sortType);
         return newInventory;
      }
   }

   public void setUsingToolsItem(boolean value) {
      this._usingToolsItem = value;
   }

   public boolean usingToolsItem() {
      return this._usingToolsItem;
   }

   public static enum ItemPickupType {
      PASSIVE,
      INTERACTION;
   }
}
