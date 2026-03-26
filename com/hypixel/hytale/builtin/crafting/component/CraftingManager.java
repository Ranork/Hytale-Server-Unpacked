package com.hypixel.hytale.builtin.crafting.component;

import com.google.gson.JsonArray;
import com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin;
import com.hypixel.hytale.builtin.crafting.CraftingPlugin;
import com.hypixel.hytale.builtin.crafting.window.BenchWindow;
import com.hypixel.hytale.builtin.crafting.window.CraftingWindow;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.BenchRequirement;
import com.hypixel.hytale.protocol.BenchType;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.ItemQuantity;
import com.hypixel.hytale.protocol.ItemResourceType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.Bench;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.BenchTierLevel;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.BenchUpgradeRequirement;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerConfigData;
import com.hypixel.hytale.server.core.entity.entities.player.windows.MaterialExtraResourcesSection;
import com.hypixel.hytale.server.core.event.events.ecs.CraftRecipeEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerCraftEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.DelegateItemContainer;
import com.hypixel.hytale.server.core.inventory.container.EmptyItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterType;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MaterialSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MaterialTransaction;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;

public class CraftingManager implements Component<EntityStore> {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private final BlockingQueue<CraftingManager.CraftingJob> queuedCraftingJobs = new LinkedBlockingQueue<>();
   @Nullable
   private CraftingManager.BenchUpgradingJob upgradingJob;
   private int x;
   private int y;
   private int z;
   @Nullable
   private BlockType blockType;

   @Nonnull
   public static ComponentType<EntityStore, CraftingManager> getComponentType() {
      return CraftingPlugin.get().getCraftingManagerComponentType();
   }

   public CraftingManager() {
   }

   private CraftingManager(@Nonnull CraftingManager other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
      this.blockType = other.blockType;
      this.queuedCraftingJobs.addAll(other.queuedCraftingJobs);
      this.upgradingJob = other.upgradingJob;
   }

   public boolean hasBenchSet() {
      return this.blockType != null;
   }

   public void setBench(int x, int y, int z, @Nonnull BlockType blockType) {
      Bench bench = blockType.getBench();
      Objects.requireNonNull(bench, "blockType isn't a bench!");
      if (bench.getType() != BenchType.Crafting
         && bench.getType() != BenchType.DiagramCrafting
         && bench.getType() != BenchType.StructuralCrafting
         && bench.getType() != BenchType.Processing) {
         throw new IllegalArgumentException("blockType isn't a crafting bench!");
      } else if (this.blockType != null) {
         throw new IllegalArgumentException("Bench blockType is already set! Must be cleared (close UI).");
      } else if (!this.queuedCraftingJobs.isEmpty()) {
         throw new IllegalArgumentException("Queue already has jobs!");
      } else if (this.upgradingJob != null) {
         throw new IllegalArgumentException("Upgrading job is already set!");
      } else {
         this.x = x;
         this.y = y;
         this.z = z;
         this.blockType = blockType;
      }
   }

   public boolean clearBench(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      boolean result = this.cancelAllCrafting(ref, componentAccessor);
      this.x = 0;
      this.y = 0;
      this.z = 0;
      this.blockType = null;
      this.upgradingJob = null;
      return result;
   }

   public boolean craftItem(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nonnull CraftingRecipe recipe,
      int quantity,
      @Nonnull ItemContainer itemContainer
   ) {
      if (this.upgradingJob != null) {
         return false;
      } else {
         Objects.requireNonNull(recipe, "Recipe can't be null");
         CraftRecipeEvent.Pre preEvent = new CraftRecipeEvent.Pre(recipe, quantity);
         componentAccessor.invoke(ref, preEvent);
         if (preEvent.isCancelled()) {
            return false;
         } else if (!this.isValidBenchForRecipe(ref, componentAccessor, recipe)) {
            return false;
         } else {
            World world = componentAccessor.getExternalData().getWorld();
            Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

            assert playerComponent != null;

            if (playerComponent.getGameMode() != GameMode.Creative && !removeInputFromInventory(itemContainer, recipe, quantity)) {
               PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());

               assert playerRefComponent != null;

               String translationKey = getRecipeOutputTranslationKey(recipe);
               if (translationKey != null) {
                  NotificationUtil.sendNotification(
                     playerRefComponent.getPacketHandler(),
                     Message.translation("server.general.crafting.missingIngredient").param("item", Message.translation(translationKey)),
                     NotificationStyle.Danger
                  );
               }

               LOGGER.at(Level.FINE).log("Missing items required to craft the item: %s", recipe);
               return false;
            } else {
               CraftRecipeEvent.Post postEvent = new CraftRecipeEvent.Post(recipe, quantity);
               componentAccessor.invoke(ref, postEvent);
               if (postEvent.isCancelled()) {
                  return true;
               } else {
                  giveOutput(ref, componentAccessor, recipe, quantity);
                  IEventDispatcher<PlayerCraftEvent, PlayerCraftEvent> dispatcher = HytaleServer.get()
                     .getEventBus()
                     .dispatchFor(PlayerCraftEvent.class, world.getName());
                  if (dispatcher.hasListener()) {
                     dispatcher.dispatch(new PlayerCraftEvent(ref, playerComponent, recipe, quantity));
                  }

                  return true;
               }
            }
         }
      }
   }

   @Nullable
   private static String getRecipeOutputTranslationKey(@Nonnull CraftingRecipe recipe) {
      String itemId = recipe.getPrimaryOutput().getItemId();
      if (itemId == null) {
         return null;
      } else {
         Item itemAsset = Item.getAssetMap().getAsset(itemId);
         return itemAsset != null ? itemAsset.getTranslationKey() : null;
      }
   }

   public boolean queueCraft(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nonnull CraftingWindow window,
      int transactionId,
      @Nonnull CraftingRecipe recipe,
      int quantity,
      @Nonnull ItemContainer inputItemContainer,
      @Nonnull CraftingManager.InputRemovalType inputRemovalType
   ) {
      if (this.upgradingJob != null) {
         return false;
      } else {
         Objects.requireNonNull(recipe, "Recipe can't be null");
         if (!this.isValidBenchForRecipe(ref, componentAccessor, recipe)) {
            return false;
         } else {
            CraftRecipeEvent.Pre preEvent = new CraftRecipeEvent.Pre(recipe, quantity);
            componentAccessor.invoke(ref, preEvent);
            if (preEvent.isCancelled()) {
               return false;
            } else {
               float recipeTime = recipe.getTimeSeconds();
               if (recipeTime > 0.0F) {
                  int level = this.getBenchTierLevel(componentAccessor);
                  if (level > 1) {
                     BenchTierLevel tierLevelData = this.getBenchTierLevelData(level);
                     if (tierLevelData != null) {
                        recipeTime -= recipeTime * tierLevelData.getCraftingTimeReductionModifier();
                     }
                  }
               }

               this.queuedCraftingJobs
                  .offer(new CraftingManager.CraftingJob(window, transactionId, recipe, quantity, recipeTime, inputItemContainer, inputRemovalType));
               return true;
            }
         }
      }
   }

   public int getRemainingQueueSize() {
      int total = 0;

      for (CraftingManager.CraftingJob job : this.queuedCraftingJobs) {
         total += job.quantity - job.quantityCompleted;
      }

      return total;
   }

   public void tick(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor, float dt) {
      if (this.upgradingJob != null) {
         if (dt > 0.0F) {
            this.upgradingJob.timeSecondsCompleted += dt;
         }

         this.upgradingJob.window.updateBenchUpgradeJob(this.upgradingJob.computeLoadingPercent());
         if (this.upgradingJob.timeSecondsCompleted >= this.upgradingJob.timeSeconds) {
            this.upgradingJob.window.updateBenchTierLevel(this.finishTierUpgrade(ref, componentAccessor));
            this.upgradingJob = null;
         }
      } else {
         Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());

         assert playerRefComponent != null;

         while (dt > 0.0F && !this.queuedCraftingJobs.isEmpty()) {
            CraftingManager.CraftingJob currentJob = this.queuedCraftingJobs.peek();
            boolean isCreativeMode = playerComponent.getGameMode() == GameMode.Creative;
            if (currentJob != null && currentJob.quantityStarted < currentJob.quantity && currentJob.quantityStarted <= currentJob.quantityCompleted) {
               LOGGER.at(Level.FINE).log("Removing Items for next quantity: %s", currentJob);
               int currentItemId = currentJob.quantityStarted++;
               if (!isCreativeMode && !removeInputFromInventory(currentJob, currentItemId)) {
                  String translationKey = getRecipeOutputTranslationKey(currentJob.recipe);
                  if (translationKey != null) {
                     NotificationUtil.sendNotification(
                        playerRefComponent.getPacketHandler(),
                        Message.translation("server.general.crafting.missingIngredient").param("item", Message.translation(translationKey)),
                        NotificationStyle.Danger
                     );
                  }

                  LOGGER.at(Level.FINE).log("Missing items required to craft the item: %s", currentJob);
                  currentJob = null;
                  this.queuedCraftingJobs.poll();
               }

               if (!isCreativeMode
                  && currentJob != null
                  && currentJob.quantityStarted < currentJob.quantity
                  && currentJob.quantityStarted <= currentJob.quantityCompleted) {
                  NotificationUtil.sendNotification(
                     playerRefComponent.getPacketHandler(),
                     Message.translation("server.general.crafting.failedTakingCorrectQuantity"),
                     NotificationStyle.Danger
                  );
                  LOGGER.at(Level.SEVERE).log("Failed to remove the correct quantity of input, removing crafting job %s", currentJob);
                  currentJob = null;
                  this.queuedCraftingJobs.poll();
               }
            }

            if (currentJob != null) {
               currentJob.timeSecondsCompleted += dt;
               float percent = currentJob.timeSeconds <= 0.0F ? 1.0F : currentJob.timeSecondsCompleted / currentJob.timeSeconds;
               if (percent > 1.0F) {
                  percent = 1.0F;
               }

               currentJob.window.updateCraftingJob(percent);
               LOGGER.at(Level.FINEST).log("Update time: %s", currentJob);
               dt = 0.0F;
               if (currentJob.timeSecondsCompleted >= currentJob.timeSeconds) {
                  dt = currentJob.timeSecondsCompleted - currentJob.timeSeconds;
                  int currentCompletedItemId = currentJob.quantityCompleted++;
                  currentJob.timeSecondsCompleted = 0.0F;
                  LOGGER.at(Level.FINE).log("Crafted 1 Quantity: %s", currentJob);
                  CraftRecipeEvent.Post postEvent = new CraftRecipeEvent.Post(currentJob.recipe, currentJob.quantity);
                  componentAccessor.invoke(ref, postEvent);
                  if (currentJob.quantityCompleted == currentJob.quantity) {
                     if (!postEvent.isCancelled()) {
                        giveOutput(ref, componentAccessor, currentJob, currentCompletedItemId);
                     }

                     LOGGER.at(Level.FINE).log("Crafting Finished: %s", currentJob);
                     this.queuedCraftingJobs.poll();
                  } else {
                     if (currentJob.quantityCompleted > currentJob.quantity) {
                        this.queuedCraftingJobs.poll();
                        throw new RuntimeException("QuantityCompleted is greater than the Quality! " + currentJob);
                     }

                     if (!postEvent.isCancelled()) {
                        giveOutput(ref, componentAccessor, currentJob, currentCompletedItemId);
                     }
                  }

                  currentJob.window.updateQueueSize(this.getRemainingQueueSize());
                  if (this.queuedCraftingJobs.isEmpty()) {
                     currentJob.window.setBlockInteractionState("default", componentAccessor.getExternalData().getWorld());
                  }
               }
            }
         }
      }
   }

   public boolean cancelAllCrafting(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      LOGGER.at(Level.FINE).log("Cancel Crafting!");
      ObjectList<CraftingManager.CraftingJob> oldJobs = new ObjectArrayList(this.queuedCraftingJobs.size());
      this.queuedCraftingJobs.drainTo(oldJobs);
      if (!oldJobs.isEmpty()) {
         CraftingManager.CraftingJob currentJob = (CraftingManager.CraftingJob)oldJobs.getFirst();
         LOGGER.at(Level.FINE).log("Refunding Items for: %s", currentJob);
         refundInputToInventory(ref, componentAccessor, currentJob, currentJob.quantityStarted - 1);
         return true;
      } else {
         return false;
      }
   }

   private boolean isValidBenchForRecipe(
      @Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull CraftingRecipe recipe
   ) {
      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      PlayerConfigData playerConfigData = playerComponent.getPlayerConfigData();
      String primaryOutputItemId = recipe.getPrimaryOutput() != null ? recipe.getPrimaryOutput().getItemId() : null;
      if (!recipe.isKnowledgeRequired() || primaryOutputItemId != null && playerConfigData.getKnownRecipes().contains(primaryOutputItemId)) {
         World world = componentAccessor.getExternalData().getWorld();
         if (recipe.getRequiredMemoriesLevel() > 1 && MemoriesPlugin.get().getMemoriesLevel(world.getGameplayConfig()) < recipe.getRequiredMemoriesLevel()) {
            LOGGER.at(Level.WARNING).log("Attempted to craft %s but doesn't have the required world memories level!", recipe.getId());
            return false;
         } else {
            BenchType benchType = this.blockType != null ? this.blockType.getBench().getType() : BenchType.Crafting;
            String benchName = this.blockType != null ? this.blockType.getBench().getId() : "Fieldcraft";
            boolean meetsRequirements = false;
            int benchTierLevel = this.getBenchTierLevel(componentAccessor);
            BenchRequirement[] requirements = recipe.getBenchRequirement();
            if (requirements != null) {
               for (BenchRequirement benchRequirement : requirements) {
                  if (benchRequirement.type == benchType && benchName.equals(benchRequirement.id) && benchRequirement.requiredTierLevel <= benchTierLevel) {
                     meetsRequirements = true;
                     break;
                  }
               }
            }

            if (!meetsRequirements) {
               LOGGER.at(Level.WARNING)
                  .log("Attempted to craft %s using %s, %s but requires bench %s but a bench is NOT set!", recipe.getId(), benchType, benchName, requirements);
               return false;
            } else if (benchType == BenchType.Crafting && !"Fieldcraft".equals(benchName)) {
               CraftingManager.CraftingJob craftingJob = this.queuedCraftingJobs.peek();
               return craftingJob == null || craftingJob.recipe.getId().equals(recipe.getId());
            } else {
               return true;
            }
         }
      } else {
         LOGGER.at(Level.WARNING).log("%s - Attempted to craft %s but doesn't know the recipe!", recipe.getId());
         return false;
      }
   }

   private static void giveOutput(
      @Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull CraftingManager.CraftingJob job, int currentItemId
   ) {
      job.removedItems.remove(currentItemId);
      String recipeId = job.recipe.getId();
      CraftingRecipe recipeAsset = CraftingRecipe.getAssetMap().getAsset(recipeId);
      if (recipeAsset == null) {
         throw new RuntimeException("A non-existent item ID was provided! " + recipeId);
      } else {
         giveOutput(ref, componentAccessor, recipeAsset, 1);
      }
   }

   private static void giveOutput(
      @Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull CraftingRecipe craftingRecipe, int quantity
   ) {
      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());
      if (playerComponent == null) {
         LOGGER.at(Level.WARNING).log("Attempted to give output to a non-player entity: %s", ref);
      } else {
         List<ItemStack> itemStacks = getOutputItemStacks(craftingRecipe, quantity);
         Inventory inventory = playerComponent.getInventory();
         PlayerSettings playerSettings = componentAccessor.getComponent(ref, PlayerSettings.getComponentType());
         if (playerSettings == null) {
            playerSettings = PlayerSettings.defaults();
         }

         for (ItemStack itemStack : itemStacks) {
            if (!ItemStack.isEmpty(itemStack)) {
               SimpleItemContainer.addOrDropItemStack(
                  componentAccessor, ref, inventory.getContainerForItemPickup(itemStack.getItem(), playerSettings), itemStack
               );
            }
         }
      }
   }

   private static boolean removeInputFromInventory(@Nonnull CraftingManager.CraftingJob job, int currentItemId) {
      Objects.requireNonNull(job, "Job can't be null!");
      CraftingRecipe craftingRecipe = job.recipe;
      Objects.requireNonNull(craftingRecipe, "CraftingRecipe can't be null!");
      List<MaterialQuantity> materialsToRemove = getInputMaterials(craftingRecipe);
      if (materialsToRemove.isEmpty()) {
         return true;
      } else {
         LOGGER.at(Level.FINEST).log("Removing Materials: %s - %s", job, materialsToRemove);
         ObjectList<ItemStack> itemStackList = new ObjectArrayList();

         boolean succeeded = switch (job.inputRemovalType) {
            case NORMAL -> {
               ListTransaction<MaterialTransaction> materialTransactions = job.inputItemContainer.removeMaterials(materialsToRemove, true, true, true);

               for (MaterialTransaction transaction : materialTransactions.getList()) {
                  for (MaterialSlotTransaction slotTransaction : transaction.getList()) {
                     if (!ItemStack.isEmpty(slotTransaction.getOutput())) {
                        itemStackList.add(slotTransaction.getOutput());
                     }
                  }
               }

               yield materialTransactions.succeeded();
            }
            case ORDERED -> {
               ListTransaction<MaterialSlotTransaction> materialTransactions = job.inputItemContainer
                  .removeMaterialsOrdered(materialsToRemove, true, true, true);

               for (MaterialSlotTransaction transaction : materialTransactions.getList()) {
                  if (!ItemStack.isEmpty(transaction.getOutput())) {
                     itemStackList.add(transaction.getOutput());
                  }
               }

               yield materialTransactions.succeeded();
            }
            default -> throw new IllegalArgumentException("Unknown enum: " + job.inputRemovalType);
         };
         job.removedItems.put(currentItemId, itemStackList);
         job.window.invalidateExtraResources();
         return succeeded;
      }
   }

   private static boolean removeInputFromInventory(@Nonnull ItemContainer itemContainer, @Nonnull CraftingRecipe craftingRecipe, int quantity) {
      List<MaterialQuantity> materialsToRemove = getInputMaterials(craftingRecipe, quantity);
      if (materialsToRemove.isEmpty()) {
         return true;
      } else {
         LOGGER.at(Level.FINEST).log("Removing Materials: %s - %s", craftingRecipe, materialsToRemove);
         ListTransaction<MaterialTransaction> materialTransactions = itemContainer.removeMaterials(materialsToRemove, true, true, true);
         return materialTransactions.succeeded();
      }
   }

   private static void refundInputToInventory(
      @Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull CraftingManager.CraftingJob job, int currentItemId
   ) {
      Objects.requireNonNull(job, "Job can't be null!");
      List<ItemStack> itemStacks = (List<ItemStack>)job.removedItems.get(currentItemId);
      if (itemStacks != null) {
         Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         SimpleItemContainer.addOrDropItemStacks(componentAccessor, ref, playerComponent.getInventory().getCombinedHotbarFirst(), itemStacks);
      }
   }

   @Nonnull
   public static List<ItemStack> getOutputItemStacks(@Nonnull CraftingRecipe recipe) {
      return getOutputItemStacks(recipe, 1);
   }

   @Nonnull
   public static List<ItemStack> getOutputItemStacks(@Nonnull CraftingRecipe recipe, int quantity) {
      Objects.requireNonNull(recipe);
      MaterialQuantity[] output = recipe.getOutputs();
      if (output == null) {
         return List.of();
      } else {
         ObjectList<ItemStack> outputItemStacks = new ObjectArrayList();

         for (MaterialQuantity outputMaterial : output) {
            ItemStack outputItemStack = getOutputItemStack(outputMaterial, quantity);
            if (outputItemStack != null) {
               outputItemStacks.add(outputItemStack);
            }
         }

         return outputItemStacks;
      }
   }

   @Nullable
   public static ItemStack getOutputItemStack(@Nonnull MaterialQuantity outputMaterial, @Nonnull String id) {
      return getOutputItemStack(outputMaterial, 1);
   }

   @Nullable
   public static ItemStack getOutputItemStack(@Nonnull MaterialQuantity outputMaterial, int quantity) {
      String itemId = outputMaterial.getItemId();
      if (itemId == null) {
         return null;
      } else {
         int materialQuantity = outputMaterial.getQuantity() <= 0 ? 1 : outputMaterial.getQuantity();
         return new ItemStack(itemId, materialQuantity * quantity, outputMaterial.getMetadata());
      }
   }

   @Nonnull
   public static List<MaterialQuantity> getInputMaterials(@Nonnull CraftingRecipe recipe) {
      return getInputMaterials(recipe, 1);
   }

   @Nonnull
   private static List<MaterialQuantity> getInputMaterials(@Nonnull MaterialQuantity[] input) {
      return getInputMaterials(input, 1);
   }

   @Nonnull
   public static List<MaterialQuantity> getInputMaterials(@Nonnull CraftingRecipe recipe, int quantity) {
      Objects.requireNonNull(recipe);
      return recipe.getInput() == null ? Collections.emptyList() : getInputMaterials(recipe.getInput(), quantity);
   }

   @Nonnull
   private static List<MaterialQuantity> getInputMaterials(@Nonnull MaterialQuantity[] input, int quantity) {
      ObjectList<MaterialQuantity> materials = new ObjectArrayList();

      for (MaterialQuantity craftingMaterial : input) {
         String itemId = craftingMaterial.getItemId();
         String resourceTypeId = craftingMaterial.getResourceTypeId();
         int materialQuantity = craftingMaterial.getQuantity();
         BsonDocument metadata = craftingMaterial.getMetadata();
         materials.add(new MaterialQuantity(itemId, resourceTypeId, null, materialQuantity * quantity, metadata));
      }

      return materials;
   }

   public static boolean matches(@Nonnull MaterialQuantity craftingMaterial, @Nonnull ItemStack itemStack) {
      String itemId = craftingMaterial.getItemId();
      if (itemId != null) {
         return itemId.equals(itemStack.getItemId());
      } else {
         String resourceTypeId = craftingMaterial.getResourceTypeId();
         if (resourceTypeId != null && itemStack.getItem().getResourceTypes() != null) {
            for (ItemResourceType itemResourceType : itemStack.getItem().getResourceTypes()) {
               if (resourceTypeId.equals(itemResourceType.id)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   @Nonnull
   public static JsonArray generateInventoryHints(@Nonnull List<CraftingRecipe> recipes, int inputSlotIndex, @Nonnull ItemContainer container) {
      JsonArray inventoryHints = new JsonArray();
      short storageSlotIndex = 0;

      for (short bound = container.getCapacity(); storageSlotIndex < bound; storageSlotIndex++) {
         ItemStack itemStack = container.getItemStack(storageSlotIndex);
         if (itemStack != null && !itemStack.isEmpty() && matchesAnyRecipe(recipes, inputSlotIndex, itemStack)) {
            inventoryHints.add(storageSlotIndex);
         }
      }

      return inventoryHints;
   }

   public static boolean matchesAnyRecipe(@Nonnull List<CraftingRecipe> recipes, int inputSlotIndex, @Nonnull ItemStack slotItemStack) {
      for (CraftingRecipe recipe : recipes) {
         MaterialQuantity[] input = recipe.getInput();
         if (inputSlotIndex < input.length) {
            MaterialQuantity slotCraftingMaterial = input[inputSlotIndex];
            if (slotCraftingMaterial.getItemId() != null && slotCraftingMaterial.getItemId().equals(slotItemStack.getItemId())) {
               return true;
            }

            if (slotCraftingMaterial.getResourceTypeId() != null && slotItemStack.getItem().getResourceTypes() != null) {
               for (ItemResourceType itemResourceType : slotItemStack.getItem().getResourceTypes()) {
                  if (slotCraftingMaterial.getResourceTypeId().equals(itemResourceType.id)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   public boolean startTierUpgrade(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull BenchWindow window) {
      if (this.upgradingJob != null) {
         return false;
      } else {
         BenchUpgradeRequirement requirements = this.getBenchUpgradeRequirement(this.getBenchTierLevel(componentAccessor));
         if (requirements == null) {
            return false;
         } else {
            List<MaterialQuantity> input = getInputMaterials(requirements.getInput());
            if (input.isEmpty()) {
               return false;
            } else {
               Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

               assert playerComponent != null;

               if (playerComponent.getGameMode() != GameMode.Creative) {
                  CombinedItemContainer combined = new CombinedItemContainer(
                     playerComponent.getInventory().getCombinedBackpackStorageHotbar(), window.getExtraResourcesSection().getItemContainer()
                  );
                  if (!combined.canRemoveMaterials(input)) {
                     return false;
                  }
               }

               this.upgradingJob = new CraftingManager.BenchUpgradingJob(window, requirements.getTimeSeconds());
               this.cancelAllCrafting(ref, componentAccessor);
               return true;
            }
         }
      }
   }

   private int finishTierUpgrade(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.upgradingJob == null) {
         return 0;
      } else {
         World world = componentAccessor.getExternalData().getWorld();
         ChunkStore chunkStore = world.getChunkStore();
         Ref<ChunkStore> chunk = chunkStore.getChunkReference(ChunkUtil.indexChunkFromBlock(this.x, this.z));
         if (chunk != null && chunk.isValid()) {
            BlockComponentChunk blockComponentChunk = chunkStore.getStore().getComponent(chunk, BlockComponentChunk.getComponentType());

            assert blockComponentChunk != null;

            BlockChunk blockChunk = chunkStore.getStore().getComponent(chunk, BlockChunk.getComponentType());

            assert blockChunk != null;

            Ref<ChunkStore> blockEntityRef = blockComponentChunk.getEntityReference(ChunkUtil.indexBlockInColumn(this.x, this.y, this.z));
            if (blockEntityRef != null && blockEntityRef.isValid()) {
               BlockModule.BlockStateInfo blockStateInfo = chunkStore.getStore().getComponent(blockEntityRef, BlockModule.BlockStateInfo.getComponentType());
               if (blockStateInfo == null) {
                  return 0;
               } else {
                  BenchBlock benchBlock = chunkStore.getStore().getComponent(blockEntityRef, BenchBlock.getComponentType());
                  if (benchBlock != null && benchBlock.getTierLevel() != 0) {
                     BenchUpgradeRequirement requirements = this.getBenchUpgradeRequirement(benchBlock.getTierLevel());
                     if (requirements == null) {
                        return benchBlock.getTierLevel();
                     } else {
                        List<MaterialQuantity> input = getInputMaterials(requirements.getInput());
                        if (input.isEmpty()) {
                           return benchBlock.getTierLevel();
                        } else {
                           Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

                           assert playerComponent != null;

                           boolean canUpgrade = playerComponent.getGameMode() == GameMode.Creative;
                           if (!canUpgrade) {
                              CombinedItemContainer combined = new CombinedItemContainer(
                                 playerComponent.getInventory().getCombinedBackpackStorageHotbar(),
                                 this.upgradingJob.window.getExtraResourcesSection().getItemContainer()
                              );
                              combined = new CombinedItemContainer(combined, this.upgradingJob.window.getExtraResourcesSection().getItemContainer());
                              ListTransaction<MaterialTransaction> materialTransactions = combined.removeMaterials(input);
                              if (materialTransactions.succeeded()) {
                                 List<ItemStack> consumed = new ObjectArrayList();

                                 for (MaterialTransaction transaction : materialTransactions.getList()) {
                                    for (MaterialSlotTransaction matSlot : transaction.getList()) {
                                       consumed.add(matSlot.getOutput());
                                    }
                                 }

                                 benchBlock.addUpgradeItems(consumed);
                                 blockStateInfo.markNeedsSaving();
                                 canUpgrade = true;
                              }
                           }

                           if (canUpgrade) {
                              benchBlock.setTierLevel(benchBlock.getTierLevel() + 1);
                              blockStateInfo.markNeedsSaving();
                              BlockType baseBlockType = BenchBlock.getBaseBlockType(this.blockType);
                              WorldChunk worldChunk = world.getChunk(ChunkUtil.indexChunkFromBlock(this.x, this.z));
                              if (worldChunk != null) {
                                 worldChunk.setBlockInteractionState(this.x, this.y, this.z, baseBlockType, benchBlock.getTierStateName(), true);
                              }

                              ProcessingBenchBlock processingBlock = chunkStore.getStore()
                                 .getComponent(blockEntityRef, ProcessingBenchBlock.getComponentType());
                              if (processingBlock != null && worldChunk != null) {
                                 int rotationIndex = worldChunk.getRotationIndex(this.x, this.y, this.z);
                                 processingBlock.setupSlots(world, benchBlock, blockStateInfo, this.x, this.y, this.z, this.blockType, rotationIndex);
                              }

                              int blockId = blockChunk.getBlock(this.x, this.y, this.z);
                              BlockType block = BlockType.getAssetMap().getAsset(blockId);
                              if (block != null && block.getBench().getBenchUpgradeCompletedSoundEventIndex() != 0) {
                                 SoundUtil.playSoundEvent3d(
                                    block.getBench().getBenchUpgradeCompletedSoundEventIndex(),
                                    SoundCategory.SFX,
                                    this.x + 0.5,
                                    this.y + 0.5,
                                    this.z + 0.5,
                                    componentAccessor
                                 );
                              }
                           }

                           return benchBlock.getTierLevel();
                        }
                     }
                  } else {
                     return 0;
                  }
               }
            } else {
               return 0;
            }
         } else {
            return 0;
         }
      }
   }

   @Nullable
   private BenchTierLevel getBenchTierLevelData(int level) {
      if (this.blockType == null) {
         return null;
      } else {
         Bench bench = this.blockType.getBench();
         return bench == null ? null : bench.getTierLevel(level);
      }
   }

   @Nullable
   private BenchUpgradeRequirement getBenchUpgradeRequirement(int tierLevel) {
      BenchTierLevel tierData = this.getBenchTierLevelData(tierLevel);
      return tierData == null ? null : tierData.getUpgradeRequirement();
   }

   private int getBenchTierLevel(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      World world = componentAccessor.getExternalData().getWorld();
      ChunkStore chunkStore = world.getChunkStore();
      Ref<ChunkStore> chunk = chunkStore.getChunkReference(ChunkUtil.indexChunkFromBlock(this.x, this.z));
      if (chunk != null && chunk.isValid()) {
         BlockComponentChunk blockComponentChunk = chunkStore.getStore().getComponent(chunk, BlockComponentChunk.getComponentType());

         assert blockComponentChunk != null;

         Ref<ChunkStore> blockEntityRef = blockComponentChunk.getEntityReference(ChunkUtil.indexBlockInColumn(this.x, this.y, this.z));
         if (blockEntityRef != null && blockEntityRef.isValid()) {
            BenchBlock benchBlock = chunkStore.getStore().getComponent(blockEntityRef, BenchBlock.getComponentType());
            return benchBlock != null && benchBlock.getTierLevel() != 0 ? benchBlock.getTierLevel() : 0;
         } else {
            return 0;
         }
      } else {
         return 0;
      }
   }

   public static int feedExtraResourcesSection(
      @Nonnull World world,
      int x,
      int y,
      int z,
      @Nonnull BlockType blockType,
      int rotationIndex,
      @Nonnull Bench benchAsset,
      int tierLevel,
      @Nonnull MaterialExtraResourcesSection extraResourcesSection
   ) {
      CraftingManager.ChestLookupResult result = getContainersAroundBench(world, x, y, z, blockType, rotationIndex);
      List<ItemContainer> chests = result.containers;
      List<ItemContainerBlock> chestStates = result.states;
      ItemContainer itemContainer = EmptyItemContainer.INSTANCE;
      if (!chests.isEmpty()) {
         itemContainer = new CombinedItemContainer(chests.stream().map(container -> {
            DelegateItemContainer<ItemContainer> delegate = new DelegateItemContainer<>(container);
            delegate.setGlobalFilter(FilterType.ALLOW_OUTPUT_ONLY);
            return delegate;
         }).toArray(ItemContainer[]::new));
      }

      Map<String, ItemQuantity> materials = new Object2ObjectOpenHashMap();

      for (ItemContainer chest : chests) {
         chest.forEach(
            (i, itemStack) -> {
               if (CraftingPlugin.isValidUpgradeMaterialForBench(benchAsset, tierLevel, itemStack)
                  || CraftingPlugin.isValidCraftingMaterialForBench(benchAsset, itemStack)) {
                  ItemQuantity var10000 = materials.computeIfAbsent(itemStack.getItemId(), k -> new ItemQuantity(itemStack.getItemId(), 0));
                  var10000.quantity = var10000.quantity + itemStack.getQuantity();
               }
            }
         );
      }

      extraResourcesSection.setItemContainer(itemContainer);
      extraResourcesSection.setExtraMaterials(materials.values().toArray(new ItemQuantity[0]));
      extraResourcesSection.setValid(true);
      return chestStates.size();
   }

   @Nonnull
   protected static CraftingManager.ChestLookupResult getContainersAroundBench(
      @Nonnull World world, int x, int y, int z, @Nonnull BlockType blockType, int rotationIndex
   ) {
      List<ItemContainer> containers = new ObjectArrayList();
      List<ItemContainerBlock> states = new ObjectArrayList();
      List<ItemContainerBlock> filteredOut = new ObjectArrayList();
      Store<ChunkStore> store = world.getChunkStore().getStore();
      int limit = world.getGameplayConfig().getCraftingConfig().getBenchMaterialChestLimit();
      double horizontalRadius = world.getGameplayConfig().getCraftingConfig().getBenchMaterialHorizontalChestSearchRadius();
      double verticalRadius = world.getGameplayConfig().getCraftingConfig().getBenchMaterialVerticalChestSearchRadius();
      Vector3d blockPos = new Vector3d(x, y, z);
      BlockBoundingBoxes hitboxAsset = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
      BlockBoundingBoxes.RotatedVariantBoxes rotatedHitbox = hitboxAsset.get(rotationIndex);
      Box boundingBox = rotatedHitbox.getBoundingBox();
      double benchWidth = boundingBox.width();
      double benchHeight = boundingBox.height();
      double benchDepth = boundingBox.depth();
      double extraSearchRadius = Math.max(benchWidth, Math.max(benchDepth, benchHeight)) - 1.0;
      SpatialResource<Ref<ChunkStore>, ChunkStore> blockStateSpatialStructure = store.getResource(BlockModule.get().getItemContainerSpatialResourceType());
      List<Ref<ChunkStore>> results = SpatialResource.getThreadLocalReferenceList();
      blockStateSpatialStructure.getSpatialStructure()
         .ordered3DAxis(blockPos, horizontalRadius + extraSearchRadius, verticalRadius + extraSearchRadius, horizontalRadius + extraSearchRadius, results);
      if (!results.isEmpty()) {
         int benchMinBlockX = (int)Math.floor(boundingBox.min.x);
         int benchMinBlockY = (int)Math.floor(boundingBox.min.y);
         int benchMinBlockZ = (int)Math.floor(boundingBox.min.z);
         int benchMaxBlockX = (int)Math.ceil(boundingBox.max.x) - 1;
         int benchMaxBlockY = (int)Math.ceil(boundingBox.max.y) - 1;
         int benchMaxBlockZ = (int)Math.ceil(boundingBox.max.z) - 1;
         double minX = blockPos.x + benchMinBlockX - horizontalRadius;
         double minY = blockPos.y + benchMinBlockY - verticalRadius;
         double minZ = blockPos.z + benchMinBlockZ - horizontalRadius;
         double maxX = blockPos.x + benchMaxBlockX + horizontalRadius;
         double maxY = blockPos.y + benchMaxBlockY + verticalRadius;
         double maxZ = blockPos.z + benchMaxBlockZ + horizontalRadius;

         for (Ref<ChunkStore> ref : results) {
            if (ref.isValid()) {
               ItemContainerBlock chest = store.getComponent(ref, ItemContainerBlock.getComponentType());
               if (chest != null) {
                  BlockModule.BlockStateInfo blockStateInfo = store.getComponent(ref, BlockModule.BlockStateInfo.getComponentType());
                  if (blockStateInfo != null) {
                     WorldChunk wc = store.getComponent(blockStateInfo.getChunkRef(), WorldChunk.getComponentType());
                     if (wc != null) {
                        int cx = ChunkUtil.worldCoordFromLocalCoord(wc.getX(), ChunkUtil.xFromBlockInColumn(blockStateInfo.getIndex()));
                        int cy = ChunkUtil.yFromBlockInColumn(blockStateInfo.getIndex());
                        int cz = ChunkUtil.worldCoordFromLocalCoord(wc.getZ(), ChunkUtil.zFromBlockInColumn(blockStateInfo.getIndex()));
                        if (cx >= minX && cx <= maxX && cy >= minY && cy <= maxY && cz >= minZ && cz <= maxZ) {
                           containers.add(chest.getItemContainer());
                           states.add(chest);
                           if (containers.size() >= limit) {
                              break;
                           }
                        } else {
                           filteredOut.add(chest);
                        }
                     }
                  }
               }
            }
         }
      }

      return new CraftingManager.ChestLookupResult(containers, states, filteredOut, blockPos);
   }

   @Nonnull
   @Override
   public String toString() {
      return "CraftingManager{queuedCraftingJobs="
         + this.queuedCraftingJobs
         + ", x="
         + this.x
         + ", y="
         + this.y
         + ", z="
         + this.z
         + ", blockType="
         + this.blockType
         + "}";
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new CraftingManager(this);
   }

   private static class BenchUpgradingJob {
      @Nonnull
      private final BenchWindow window;
      private final float timeSeconds;
      private float timeSecondsCompleted;
      private float lastSentPercent;

      private BenchUpgradingJob(@Nonnull BenchWindow window, float timeSeconds) {
         this.window = window;
         this.timeSeconds = timeSeconds;
      }

      @Nonnull
      @Override
      public String toString() {
         return "BenchUpgradingJob{window=" + this.window + ", timeSeconds=" + this.timeSeconds + "}";
      }

      public float computeLoadingPercent() {
         return this.timeSeconds <= 0.0F ? 1.0F : Math.min(this.timeSecondsCompleted / this.timeSeconds, 1.0F);
      }
   }

   protected record ChestLookupResult(
      List<ItemContainer> containers, List<ItemContainerBlock> states, List<ItemContainerBlock> filteredOut, Vector3d benchCenteredPos
   ) {
   }

   private static class CraftingJob {
      @Nonnull
      private final CraftingWindow window;
      private final int transactionId;
      @Nonnull
      private final CraftingRecipe recipe;
      private final int quantity;
      private final float timeSeconds;
      @Nonnull
      private final ItemContainer inputItemContainer;
      @Nonnull
      private final CraftingManager.InputRemovalType inputRemovalType;
      @Nonnull
      private final Int2ObjectMap<List<ItemStack>> removedItems = new Int2ObjectOpenHashMap();
      private int quantityStarted;
      private int quantityCompleted;
      private float timeSecondsCompleted;

      public CraftingJob(
         @Nonnull CraftingWindow window,
         int transactionId,
         @Nonnull CraftingRecipe recipe,
         int quantity,
         float timeSeconds,
         @Nonnull ItemContainer inputItemContainer,
         @Nonnull CraftingManager.InputRemovalType inputRemovalType
      ) {
         this.window = window;
         this.transactionId = transactionId;
         this.recipe = recipe;
         this.quantity = quantity;
         this.timeSeconds = timeSeconds;
         this.inputItemContainer = inputItemContainer;
         this.inputRemovalType = inputRemovalType;
      }

      @Nonnull
      @Override
      public String toString() {
         return "CraftingJob{window="
            + this.window
            + ", transactionId="
            + this.transactionId
            + ", recipe="
            + this.recipe
            + ", quantity="
            + this.quantity
            + ", timeSeconds="
            + this.timeSeconds
            + ", inputItemContainer="
            + this.inputItemContainer
            + ", inputRemovalType="
            + this.inputRemovalType
            + ", removedItems="
            + this.removedItems
            + ", quantityStarted="
            + this.quantityStarted
            + ", quantityCompleted="
            + this.quantityCompleted
            + ", timeSecondsCompleted="
            + this.timeSecondsCompleted
            + "}";
      }
   }

   public static enum InputRemovalType {
      NORMAL,
      ORDERED;
   }
}
