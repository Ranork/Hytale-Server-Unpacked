package com.hypixel.hytale.builtin.crafting.state;

import com.google.common.flogger.LazyArgs;
import com.hypixel.hytale.builtin.crafting.CraftingPlugin;
import com.hypixel.hytale.builtin.crafting.component.CraftingManager;
import com.hypixel.hytale.builtin.crafting.window.ProcessingBenchWindow;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.BenchTierLevel;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.ProcessingBench;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.inventory.ResourceQuantity;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.InternalContainerUtilMaterial;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.inventory.container.TestRemoveItemSlotResult;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterType;
import com.hypixel.hytale.server.core.inventory.container.filter.ResourceFilter;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MaterialSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MaterialTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ResourceTransaction;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.BlockAccessor;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.state.TickableBlockState;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.DestroyableBlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerBlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.PlacedByBlockState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;

public class ProcessingBenchState extends BenchState implements TickableBlockState, ItemContainerBlockState, DestroyableBlockState, PlacedByBlockState {
   @Nonnull
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   public static final boolean EXACT_RESOURCE_AMOUNTS = true;
   @Nonnull
   public static final Codec<ProcessingBenchState> CODEC = BuilderCodec.builder(ProcessingBenchState.class, ProcessingBenchState::new, BenchState.CODEC)
      .append(new KeyedCodec<>("InputContainer", ItemContainer.CODEC), (state, o) -> state.inputContainer = o, state -> state.inputContainer)
      .add()
      .append(new KeyedCodec<>("FuelContainer", ItemContainer.CODEC), (state, o) -> state.fuelContainer = o, state -> state.fuelContainer)
      .add()
      .append(new KeyedCodec<>("OutputContainer", ItemContainer.CODEC), (state, o) -> state.outputContainer = o, state -> state.outputContainer)
      .add()
      .append(new KeyedCodec<>("Progress", Codec.DOUBLE), (state, d) -> state.inputProgress = d.floatValue(), state -> (double)state.inputProgress)
      .add()
      .append(new KeyedCodec<>("FuelTime", Codec.DOUBLE), (state, d) -> state.fuelTime = d.floatValue(), state -> (double)state.fuelTime)
      .add()
      .append(new KeyedCodec<>("Active", Codec.BOOLEAN), (state, b) -> state.active = b, state -> state.active)
      .add()
      .append(new KeyedCodec<>("NextExtra", Codec.INTEGER), (state, b) -> state.nextExtra = b, state -> state.nextExtra)
      .add()
      .append(new KeyedCodec<>("RecipeId", Codec.STRING), (state, o) -> state.recipeId = o, state -> state.recipeId)
      .add()
      .build();
   private static final float EJECT_VELOCITY = 2.0F;
   private static final float EJECT_SPREAD_VELOCITY = 1.0F;
   private static final float EJECT_VERTICAL_VELOCITY = 3.25F;
   @Nonnull
   public static final String PROCESSING = "Processing";
   @Nonnull
   public static final String PROCESS_COMPLETED = "ProcessCompleted";
   private ProcessingBench processingBench;
   private ItemContainer inputContainer;
   private ItemContainer fuelContainer;
   private ItemContainer outputContainer;
   private CombinedItemContainer combinedItemContainer;
   private float inputProgress;
   private float fuelTime;
   private int lastConsumedFuelTotal;
   private int nextExtra = -1;
   @Nonnull
   private final Set<Short> processingSlots = new HashSet<>();
   @Nonnull
   private final Set<Short> processingFuelSlots = new HashSet<>();
   @Nullable
   private String recipeId;
   @Nullable
   private CraftingRecipe recipe;
   private boolean active = false;

   @Override
   public boolean initialize(@Nonnull BlockType blockType) {
      if (!super.initialize(blockType)) {
         if (this.bench == null) {
            List<ItemStack> itemStacks = new ObjectArrayList();
            if (this.inputContainer != null) {
               itemStacks.addAll(this.inputContainer.dropAllItemStacks());
            }

            if (this.fuelContainer != null) {
               itemStacks.addAll(this.fuelContainer.dropAllItemStacks());
            }

            if (this.outputContainer != null) {
               itemStacks.addAll(this.outputContainer.dropAllItemStacks());
            }

            World world = this.getChunk().getWorld();
            Store<EntityStore> store = world.getEntityStore().getStore();
            Holder<EntityStore>[] itemEntityHolders = this.ejectItems(store, itemStacks);
            if (itemEntityHolders.length > 0) {
               world.execute(() -> store.addEntities(itemEntityHolders, AddReason.SPAWN));
            }
         }

         return false;
      } else if (!(this.bench instanceof ProcessingBench)) {
         LOGGER.at(Level.SEVERE).log("Wrong bench type for processing. Got %s", this.bench.getClass().getName());
         return false;
      } else {
         this.processingBench = (ProcessingBench)this.bench;
         if (this.nextExtra == -1) {
            this.nextExtra = this.processingBench.getExtraOutput() != null ? this.processingBench.getExtraOutput().getPerFuelItemsConsumed() : 0;
         }

         this.setupSlots();
         return true;
      }
   }

   private void setupSlots() {
      List<ItemStack> remainder = new ObjectArrayList();
      int tierLevel = this.getTierLevel();
      ProcessingBench.ProcessingSlot[] input = this.processingBench.getInput(tierLevel);
      short inputSlotsCount = (short)input.length;
      this.inputContainer = ItemContainer.ensureContainerCapacity(this.inputContainer, inputSlotsCount, SimpleItemContainer::getNewContainer, remainder);
      this.inputContainer.registerChangeEvent(EventPriority.LAST, this::onItemChange);

      for (short slot = 0; slot < inputSlotsCount; slot++) {
         ProcessingBench.ProcessingSlot inputSlot = input[slot];
         String resourceTypeId = inputSlot.getResourceTypeId();
         boolean shouldFilterValidIngredients = inputSlot.shouldFilterValidIngredients();
         if (resourceTypeId != null) {
            this.inputContainer.setSlotFilter(FilterActionType.ADD, slot, new ResourceFilter(new ResourceQuantity(resourceTypeId, 1)));
         } else if (shouldFilterValidIngredients) {
            ObjectArrayList<MaterialQuantity> validIngredients = new ObjectArrayList();

            for (CraftingRecipe recipe : CraftingPlugin.getBenchRecipes(this.bench.getType(), this.bench.getId())) {
               if (!recipe.isRestrictedByBenchTierLevel(this.bench.getId(), tierLevel)) {
                  List<MaterialQuantity> inputMaterials = CraftingManager.getInputMaterials(recipe);
                  validIngredients.addAll(inputMaterials);
               }
            }

            this.inputContainer.setSlotFilter(FilterActionType.ADD, slot, (actionType, container, slotIndex, itemStack) -> {
               if (itemStack == null) {
                  return true;
               } else {
                  Iterator i$ = validIngredients.iterator();

                  while (i$.hasNext()) {
                     MaterialQuantity ingredient = (MaterialQuantity)i$.next();
                     if (CraftingManager.matches(ingredient, itemStack)) {
                        return true;
                     }
                  }

                  return false;
               }
            });
         }
      }

      input = this.processingBench.getFuel();
      inputSlotsCount = (short)(input != null ? input.length : 0);
      this.fuelContainer = ItemContainer.ensureContainerCapacity(this.fuelContainer, inputSlotsCount, SimpleItemContainer::getNewContainer, remainder);
      this.fuelContainer.registerChangeEvent(EventPriority.LAST, this::onItemChange);
      if (inputSlotsCount > 0) {
         for (int i = 0; i < input.length; i++) {
            ProcessingBench.ProcessingSlot fuel = input[i];
            String resourceTypeId = fuel.getResourceTypeId();
            if (resourceTypeId != null) {
               this.fuelContainer.setSlotFilter(FilterActionType.ADD, (short)i, new ResourceFilter(new ResourceQuantity(resourceTypeId, 1)));
            }
         }
      }

      short outputSlotsCount = (short)this.processingBench.getOutputSlotsCount(tierLevel);
      this.outputContainer = ItemContainer.ensureContainerCapacity(this.outputContainer, outputSlotsCount, SimpleItemContainer::getNewContainer, remainder);
      this.outputContainer.registerChangeEvent(EventPriority.LAST, this::onItemChange);
      if (outputSlotsCount > 0) {
         this.outputContainer.setGlobalFilter(FilterType.ALLOW_OUTPUT_ONLY);
      }

      this.combinedItemContainer = new CombinedItemContainer(this.fuelContainer, this.inputContainer, this.outputContainer);
      World world = this.getChunk().getWorld();
      Store<EntityStore> store = world.getEntityStore().getStore();
      Holder<EntityStore>[] itemEntityHolders = this.ejectItems(store, remainder);
      if (itemEntityHolders.length > 0) {
         world.execute(() -> store.addEntities(itemEntityHolders, AddReason.SPAWN));
      }

      this.inputContainer.registerChangeEvent(EventPriority.LAST, event -> this.updateRecipe());
      if (this.processingBench.getFuel() == null) {
         this.setActive(true);
      }
   }

   @Override
   public void tick(float dt, int index, ArchetypeChunk<ChunkStore> archetypeChunk, @Nonnull Store<ChunkStore> store, CommandBuffer<ChunkStore> commandBuffer) {
      World world = store.getExternalData().getWorld();
      Store<EntityStore> entityStore = world.getEntityStore().getStore();
      BlockType blockType = this.getBlockType();
      String currentState = BlockAccessor.getCurrentInteractionState(blockType);
      List<ItemStack> outputItemStacks = null;
      List<MaterialQuantity> inputMaterials = null;
      this.processingSlots.clear();
      this.checkForRecipeUpdate();
      if (this.recipe != null) {
         outputItemStacks = CraftingManager.getOutputItemStacks(this.recipe);
         if (!this.outputContainer.canAddItemStacks(outputItemStacks, false, false)) {
            if ("Processing".equals(currentState)) {
               this.setBlockInteractionState("default", blockType);
               this.playSound(world, this.processingBench.getFailedSoundEventIndex(), entityStore);
            } else if ("ProcessCompleted".equals(currentState)) {
               this.setBlockInteractionState("default", blockType);
               this.playSound(world, this.processingBench.getEndSoundEventIndex(), entityStore);
            }

            this.setActive(false);
            return;
         }

         inputMaterials = CraftingManager.getInputMaterials(this.recipe);
         List<TestRemoveItemSlotResult> result = this.inputContainer.getSlotMaterialsToRemove(inputMaterials, true, true);
         if (result.isEmpty()) {
            if ("Processing".equals(currentState)) {
               this.setBlockInteractionState("default", blockType);
               this.playSound(world, this.processingBench.getFailedSoundEventIndex(), entityStore);
            } else if ("ProcessCompleted".equals(currentState)) {
               this.setBlockInteractionState("default", blockType);
               this.playSound(world, this.processingBench.getEndSoundEventIndex(), entityStore);
            }

            this.inputProgress = 0.0F;
            this.setActive(false);
            this.recipeId = null;
            this.recipe = null;
            return;
         }

         for (TestRemoveItemSlotResult item : result) {
            this.processingSlots.addAll(item.getPickedSlots());
         }

         this.sendProcessingSlots();
      } else {
         if (this.processingBench.getFuel() == null) {
            if ("Processing".equals(currentState)) {
               this.setBlockInteractionState("default", blockType);
               this.playSound(world, this.processingBench.getFailedSoundEventIndex(), entityStore);
            } else if ("ProcessCompleted".equals(currentState)) {
               this.setBlockInteractionState("default", blockType);
               this.playSound(world, this.processingBench.getEndSoundEventIndex(), entityStore);
            }

            return;
         }

         boolean allowNoInputProcessing = this.processingBench.shouldAllowNoInputProcessing();
         if (!allowNoInputProcessing && "Processing".equals(currentState)) {
            this.setBlockInteractionState("default", blockType);
            this.playSound(world, this.processingBench.getFailedSoundEventIndex(), entityStore);
         } else if ("ProcessCompleted".equals(currentState)) {
            this.setBlockInteractionState("default", blockType);
            this.playSound(world, this.processingBench.getEndSoundEventIndex(), entityStore);
            this.setActive(false);
            this.sendProgress(0.0F);
            return;
         }

         this.sendProgress(0.0F);
         if (!allowNoInputProcessing) {
            this.setActive(false);
            return;
         }
      }

      boolean needsUpdate = false;
      if (this.fuelTime > 0.0F && this.active) {
         this.fuelTime -= dt;
         if (this.fuelTime < 0.0F) {
            this.fuelTime = 0.0F;
         }

         needsUpdate = true;
      }

      ProcessingBench.ProcessingSlot[] fuelSlots = this.processingBench.getFuel();
      boolean hasFuelSlots = fuelSlots != null && fuelSlots.length > 0;
      if ((this.processingBench.getMaxFuel() <= 0 || this.fuelTime < this.processingBench.getMaxFuel()) && !this.fuelContainer.isEmpty()) {
         if (!hasFuelSlots) {
            return;
         }

         if (this.active) {
            if (this.fuelTime > 0.0F) {
               for (int i = 0; i < fuelSlots.length; i++) {
                  ItemStack itemInSlot = this.fuelContainer.getItemStack((short)i);
                  if (itemInSlot != null) {
                     this.processingFuelSlots.add((short)i);
                     break;
                  }
               }
            } else {
               if (this.fuelTime < 0.0F) {
                  this.fuelTime = 0.0F;
               }

               this.processingFuelSlots.clear();

               for (int ix = 0; ix < fuelSlots.length; ix++) {
                  ProcessingBench.ProcessingSlot fuelSlot = fuelSlots[ix];
                  String resourceTypeId = fuelSlot.getResourceTypeId() != null ? fuelSlot.getResourceTypeId() : "Fuel";
                  ResourceQuantity resourceQuantity = new ResourceQuantity(resourceTypeId, 1);
                  ItemStack slot = this.fuelContainer.getItemStack((short)ix);
                  if (slot != null) {
                     double fuelQuality = slot.getItem().getFuelQuality();
                     ResourceTransaction transaction = this.fuelContainer.removeResource(resourceQuantity, true, true, true);
                     this.processingFuelSlots.add((short)ix);
                     if (transaction.getRemainder() <= 0) {
                        ProcessingBench.ExtraOutput extra = this.processingBench.getExtraOutput();
                        if (extra != null && !extra.isIgnoredFuelSource(slot.getItem())) {
                           this.nextExtra--;
                           if (this.nextExtra <= 0) {
                              this.nextExtra = extra.getPerFuelItemsConsumed();
                              ObjectArrayList<ItemStack> extraItemStacks = new ObjectArrayList(extra.getOutputs().length);

                              for (MaterialQuantity e : extra.getOutputs()) {
                                 extraItemStacks.add(e.toItemStack());
                              }

                              ListTransaction<ItemStackTransaction> addTransaction = this.outputContainer.addItemStacks(extraItemStacks, false, false, false);
                              List<ItemStack> remainderItems = new ObjectArrayList();

                              for (ItemStackTransaction itemStackTransaction : addTransaction.getList()) {
                                 ItemStack remainder = itemStackTransaction.getRemainder();
                                 if (remainder != null && !remainder.isEmpty()) {
                                    remainderItems.add(remainder);
                                 }
                              }

                              if (!remainderItems.isEmpty()) {
                                 LOGGER.at(Level.WARNING).log("Dropping excess items at %s", this.getBlockPosition());
                                 Holder<EntityStore>[] itemEntityHolders = this.ejectItems(entityStore, remainderItems);
                                 entityStore.addEntities(itemEntityHolders, AddReason.SPAWN);
                              }
                           }
                        }

                        this.fuelTime = (float)(this.fuelTime + transaction.getConsumed() * fuelQuality);
                        needsUpdate = true;
                        break;
                     }
                  }
               }
            }
         }
      }

      if (needsUpdate) {
         this.updateFuelValues();
      }

      if (!hasFuelSlots || this.active && !(this.fuelTime <= 0.0F)) {
         if (!"Processing".equals(currentState)) {
            this.setBlockInteractionState("Processing", blockType);
         }

         if (this.recipe != null && (this.fuelTime > 0.0F || this.processingBench.getFuel() == null)) {
            this.inputProgress += dt;
         }

         if (this.recipe != null) {
            float recipeTime = this.recipe.getTimeSeconds();
            float craftingTimeReductionModifier = this.getCraftingTimeReductionModifier();
            if (craftingTimeReductionModifier > 0.0F) {
               recipeTime -= recipeTime * craftingTimeReductionModifier;
            }

            if (this.inputProgress > recipeTime) {
               if (recipeTime > 0.0F) {
                  this.inputProgress -= recipeTime;
                  float progressPercent = this.inputProgress / recipeTime;
                  this.sendProgress(progressPercent);
               } else {
                  this.inputProgress = 0.0F;
                  this.sendProgress(0.0F);
               }

               LOGGER.at(Level.FINE).log("Do Process for %s %s", this.recipeId, this.recipe);
               if (inputMaterials != null) {
                  List<ItemStack> remainderItems = new ObjectArrayList();
                  int success = 0;
                  IntArrayList slots = new IntArrayList();

                  for (int j = 0; j < this.inputContainer.getCapacity(); j++) {
                     slots.add(j);
                  }

                  for (MaterialQuantity material : inputMaterials) {
                     for (int ixx = 0; ixx < slots.size(); ixx++) {
                        int slot = slots.getInt(ixx);
                        MaterialSlotTransaction transaction = this.inputContainer.removeMaterialFromSlot((short)slot, material, true, true, true);
                        if (transaction.succeeded()) {
                           success++;
                           slots.removeInt(ixx);
                           break;
                        }
                     }
                  }

                  ListTransaction<ItemStackTransaction> addTransaction = this.outputContainer.addItemStacks(outputItemStacks, false, false, false);
                  if (!addTransaction.succeeded()) {
                     return;
                  }

                  for (ItemStackTransaction itemStackTransactionx : addTransaction.getList()) {
                     ItemStack remainder = itemStackTransactionx.getRemainder();
                     if (remainder != null && !remainder.isEmpty()) {
                        remainderItems.add(remainder);
                     }
                  }

                  if (success == inputMaterials.size()) {
                     this.setBlockInteractionState("ProcessCompleted", blockType);
                     this.playSound(world, this.bench.getCompletedSoundEventIndex(), entityStore);
                     if (!remainderItems.isEmpty()) {
                        LOGGER.at(Level.WARNING).log("Dropping excess items at %s", this.getBlockPosition());
                        Holder<EntityStore>[] itemEntityHolders = this.ejectItems(entityStore, remainderItems);
                        entityStore.addEntities(itemEntityHolders, AddReason.SPAWN);
                     }

                     return;
                  }
               }

               List<ItemStack> remainderItems = new ObjectArrayList();
               ListTransaction<MaterialTransaction> transaction = this.inputContainer.removeMaterials(inputMaterials, true, true, true);
               if (!transaction.succeeded()) {
                  LOGGER.at(Level.WARNING).log("Failed to remove input materials at %s", this.getBlockPosition());
                  this.setBlockInteractionState("default", blockType);
                  this.playSound(world, this.processingBench.getFailedSoundEventIndex(), entityStore);
                  return;
               }

               this.setBlockInteractionState("ProcessCompleted", blockType);
               this.playSound(world, this.bench.getCompletedSoundEventIndex(), entityStore);
               ListTransaction<ItemStackTransaction> addTransactionx = this.outputContainer.addItemStacks(outputItemStacks, false, false, false);
               if (addTransactionx.succeeded()) {
                  return;
               }

               LOGGER.at(Level.WARNING).log("Dropping excess items at %s", this.getBlockPosition());

               for (ItemStackTransaction itemStackTransactionxx : addTransactionx.getList()) {
                  ItemStack remainder = itemStackTransactionxx.getRemainder();
                  if (remainder != null && !remainder.isEmpty()) {
                     remainderItems.add(remainder);
                  }
               }

               Holder<EntityStore>[] itemEntityHolders = this.ejectItems(entityStore, remainderItems);
               entityStore.addEntities(itemEntityHolders, AddReason.SPAWN);
            } else if (this.recipe != null && recipeTime > 0.0F) {
               float progressPercent = this.inputProgress / recipeTime;
               this.sendProgress(progressPercent);
            } else {
               this.sendProgress(0.0F);
            }
         }
      } else {
         this.lastConsumedFuelTotal = 0;
         if ("Processing".equals(currentState)) {
            this.setBlockInteractionState("default", blockType);
            this.playSound(world, this.processingBench.getFailedSoundEventIndex(), entityStore);
            if (this.processingBench.getFuel() != null) {
               this.setActive(false);
            }
         } else if ("ProcessCompleted".equals(currentState)) {
            this.setBlockInteractionState("default", blockType);
            this.playSound(world, this.processingBench.getFailedSoundEventIndex(), entityStore);
            if (this.processingBench.getFuel() != null) {
               this.setActive(false);
            }
         }
      }
   }

   private float getCraftingTimeReductionModifier() {
      BenchTierLevel levelData = this.bench.getTierLevel(this.getTierLevel());
      return levelData != null ? levelData.getCraftingTimeReductionModifier() : 0.0F;
   }

   @Nonnull
   private Holder<EntityStore>[] ejectItems(@Nonnull ComponentAccessor<EntityStore> accessor, @Nonnull List<ItemStack> itemStacks) {
      if (itemStacks.isEmpty()) {
         return Holder.emptyArray();
      } else {
         RotationTuple rotation = RotationTuple.get(this.getRotationIndex());
         Vector3d frontDir = new Vector3d(0.0, 0.0, 1.0);
         rotation.yaw().rotateY(frontDir, frontDir);
         BlockType blockType = this.getBlockType();
         Vector3d dropPosition;
         if (blockType == null) {
            dropPosition = this.getBlockPosition().toVector3d().add(0.5, 0.0, 0.5);
         } else {
            BlockBoundingBoxes hitboxAsset = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
            if (hitboxAsset == null) {
               dropPosition = this.getBlockPosition().toVector3d().add(0.5, 0.0, 0.5);
            } else {
               double depth = hitboxAsset.get(0).getBoundingBox().depth();
               double frontOffset = depth / 2.0 + 0.1F;
               dropPosition = this.getCenteredBlockPosition();
               dropPosition.add(frontDir.x * frontOffset, 0.0, frontDir.z * frontOffset);
            }
         }

         ThreadLocalRandom random = ThreadLocalRandom.current();
         ObjectArrayList<Holder<EntityStore>> result = new ObjectArrayList(itemStacks.size());

         for (ItemStack item : itemStacks) {
            float velocityX = (float)(frontDir.x * 2.0 + 2.0 * (random.nextDouble() - 0.5));
            float velocityZ = (float)(frontDir.z * 2.0 + 2.0 * (random.nextDouble() - 0.5));
            Holder<EntityStore> holder = ItemComponent.generateItemDrop(accessor, item, dropPosition, Vector3f.ZERO, velocityX, 3.25F, velocityZ);
            if (holder != null) {
               result.add(holder);
            }
         }

         return (Holder<EntityStore>[])result.toArray(Holder[]::new);
      }
   }

   private void sendProgress(float progress) {
      this.windows.forEach((uuid, window) -> ((ProcessingBenchWindow)window).setProgress(progress));
   }

   private void sendProcessingSlots() {
      this.windows.forEach((uuid, window) -> ((ProcessingBenchWindow)window).setProcessingSlots(this.processingSlots));
   }

   private void sendProcessingFuelSlots() {
      this.windows.forEach((uuid, window) -> ((ProcessingBenchWindow)window).setProcessingFuelSlots(this.processingFuelSlots));
   }

   public boolean isActive() {
      return this.active;
   }

   public boolean setActive(boolean active) {
      if (this.active != active) {
         if (active && this.processingBench.getFuel() != null && this.fuelContainer.isEmpty()) {
            return false;
         } else {
            this.active = active;
            if (!active) {
               this.processingSlots.clear();
               this.processingFuelSlots.clear();
               this.sendProcessingSlots();
               this.sendProcessingFuelSlots();
            }

            this.updateRecipe();
            this.windows.forEach((uuid, window) -> ((ProcessingBenchWindow)window).setActive(active));
            this.markNeedsSave();
            return true;
         }
      } else {
         return false;
      }
   }

   public void updateFuelValues() {
      if (this.fuelTime > this.lastConsumedFuelTotal) {
         this.lastConsumedFuelTotal = MathUtil.ceil(this.fuelTime);
      }

      float fuelPercent = this.lastConsumedFuelTotal > 0 ? this.fuelTime / this.lastConsumedFuelTotal : 0.0F;
      this.windows.forEach((uuid, window) -> {
         ProcessingBenchWindow processingBenchWindow = (ProcessingBenchWindow)window;
         processingBenchWindow.setFuelTime(fuelPercent);
         processingBenchWindow.setMaxFuel(this.lastConsumedFuelTotal);
         processingBenchWindow.setProcessingFuelSlots(this.processingFuelSlots);
      });
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      if (this.combinedItemContainer != null) {
         List<ItemStack> itemStacks = this.combinedItemContainer.dropAllItemStacks();
         this.dropFuelItems(itemStacks);
         World world = this.getChunk().getWorld();
         Store<EntityStore> entityStore = world.getEntityStore().getStore();
         Vector3d dropPosition = this.getBlockPosition().toVector3d().add(0.5, 0.0, 0.5);
         Holder<EntityStore>[] itemEntityHolders = ItemComponent.generateItemDrops(entityStore, itemStacks, dropPosition, Vector3f.ZERO);
         if (itemEntityHolders.length > 0) {
            world.execute(() -> entityStore.addEntities(itemEntityHolders, AddReason.SPAWN));
         }
      }
   }

   public CombinedItemContainer getItemContainer() {
      return this.combinedItemContainer;
   }

   private void checkForRecipeUpdate() {
      if (this.recipe == null && this.recipeId != null) {
         this.updateRecipe();
      }
   }

   private void updateRecipe() {
      List<CraftingRecipe> recipes = CraftingPlugin.getBenchRecipes(this.bench.getType(), this.bench.getId());
      if (recipes.isEmpty()) {
         this.clearRecipe();
      } else {
         List<CraftingRecipe> matching = new ObjectArrayList();

         for (CraftingRecipe recipe : recipes) {
            if (!recipe.isRestrictedByBenchTierLevel(this.bench.getId(), this.getTierLevel())) {
               MaterialQuantity[] input = recipe.getInput();
               int matches = 0;
               IntArrayList slots = new IntArrayList();

               for (int j = 0; j < this.inputContainer.getCapacity(); j++) {
                  slots.add(j);
               }

               for (MaterialQuantity craftingMaterial : input) {
                  String itemId = craftingMaterial.getItemId();
                  String resourceTypeId = craftingMaterial.getResourceTypeId();
                  int materialQuantity = craftingMaterial.getQuantity();
                  BsonDocument metadata = craftingMaterial.getMetadata();
                  MaterialQuantity material = new MaterialQuantity(itemId, resourceTypeId, null, materialQuantity, metadata);

                  for (int k = 0; k < slots.size(); k++) {
                     int j = slots.getInt(k);
                     int out = InternalContainerUtilMaterial.testRemoveMaterialFromSlot(this.inputContainer, (short)j, material, material.getQuantity(), true);
                     if (out == 0) {
                        matches++;
                        slots.removeInt(k);
                        break;
                     }
                  }
               }

               if (matches == input.length) {
                  matching.add(recipe);
               }
            }
         }

         if (matching.isEmpty()) {
            this.clearRecipe();
         } else {
            matching.sort(Comparator.comparingInt(o -> CraftingManager.getInputMaterials(o).size()));
            Collections.reverse(matching);
            if (this.recipeId != null) {
               for (CraftingRecipe rec : matching) {
                  if (Objects.equals(this.recipeId, rec.getId())) {
                     LOGGER.at(Level.FINE).log("%s - Keeping existing Recipe %s %s", LazyArgs.lazy(this::getBlockPosition), this.recipeId, rec);
                     this.recipe = rec;
                     return;
                  }
               }
            }

            CraftingRecipe recipex = matching.getFirst();
            if (this.recipeId == null || !Objects.equals(this.recipeId, recipex.getId())) {
               this.inputProgress = 0.0F;
               this.sendProgress(0.0F);
            }

            this.recipeId = recipex.getId();
            this.recipe = recipex;
            LOGGER.at(Level.FINE).log("%s - Found Recipe %s %s", LazyArgs.lazy(this::getBlockPosition), this.recipeId, this.recipe);
         }
      }
   }

   private void clearRecipe() {
      this.recipeId = null;
      this.recipe = null;
      this.lastConsumedFuelTotal = 0;
      this.inputProgress = 0.0F;
      this.sendProgress(0.0F);
      LOGGER.at(Level.FINE).log("%s - Cleared Recipe", LazyArgs.lazy(this::getBlockPosition));
   }

   public void dropFuelItems(@Nonnull List<ItemStack> itemStacks) {
      String fuelDropItemId = this.processingBench.getFuelDropItemId();
      if (fuelDropItemId != null) {
         Item item = Item.getAssetMap().getAsset(fuelDropItemId);
         int dropAmount = (int)this.fuelTime;
         this.fuelTime = 0.0F;

         while (dropAmount > 0) {
            int quantity = Math.min(dropAmount, item.getMaxStack());
            itemStacks.add(new ItemStack(fuelDropItemId, quantity));
            dropAmount -= quantity;
         }
      } else {
         LOGGER.at(Level.WARNING).log("No FuelDropItemId defined for %s fuel value of %s will be lost!", this.bench.getId(), this.fuelTime);
      }
   }

   @Nullable
   public CraftingRecipe getRecipe() {
      return this.recipe;
   }

   public float getInputProgress() {
      return this.inputProgress;
   }

   public void onItemChange(ItemContainer.ItemContainerChangeEvent event) {
      this.markNeedsSave();
   }

   public void setBlockInteractionState(@Nonnull String state, @Nonnull BlockType blockType) {
      this.getChunk().setBlockInteractionState(this.getBlockPosition(), blockType, state);
   }

   @Override
   public void placedBy(
      @Nonnull Ref<EntityStore> playerRef,
      @Nonnull String blockTypeKey,
      @Nonnull BlockState blockState,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
   }

   private void playSound(@Nonnull World world, int soundEventIndex, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (soundEventIndex != 0) {
         Vector3i pos = this.getBlockPosition();
         WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
         int rotationIndex = chunk.getRotationIndex(pos.x, pos.y, pos.z);
         Vector3d soundPos = new Vector3d();
         BlockType blockType = this.getBlockType();
         if (blockType != null) {
            blockType.getBlockCenter(rotationIndex, soundPos);
         }

         soundPos.add(pos);
         SoundUtil.playSoundEvent3d(soundEventIndex, SoundCategory.SFX, soundPos, componentAccessor);
      }
   }

   @Override
   protected void onTierLevelChange() {
      super.onTierLevelChange();
      this.setupSlots();
   }
}
