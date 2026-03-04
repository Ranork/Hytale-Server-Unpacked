package com.hypixel.hytale.builtin.crafting;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.event.RemovedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.crafting.commands.RecipeCommand;
import com.hypixel.hytale.builtin.crafting.component.CraftingManager;
import com.hypixel.hytale.builtin.crafting.interaction.LearnRecipeInteraction;
import com.hypixel.hytale.builtin.crafting.interaction.OpenBenchPageInteraction;
import com.hypixel.hytale.builtin.crafting.interaction.OpenProcessingBenchInteraction;
import com.hypixel.hytale.builtin.crafting.state.BenchState;
import com.hypixel.hytale.builtin.crafting.state.ProcessingBenchState;
import com.hypixel.hytale.builtin.crafting.system.PlayerCraftingSystems;
import com.hypixel.hytale.builtin.crafting.window.FieldCraftingWindow;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.protocol.BenchRequirement;
import com.hypixel.hytale.protocol.BenchType;
import com.hypixel.hytale.protocol.ItemResourceType;
import com.hypixel.hytale.protocol.packets.interface_.UpdateKnownRecipes;
import com.hypixel.hytale.protocol.packets.window.WindowType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.Bench;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.BenchUpgradeRequirement;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerConfigData;
import com.hypixel.hytale.server.core.entity.entities.player.windows.Window;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateRegistry;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CraftingPlugin extends JavaPlugin {
   private static CraftingPlugin instance;
   @Nonnull
   private static final Map<String, BenchRecipeRegistry> registries = new Object2ObjectOpenHashMap();
   @Nonnull
   private static final Map<String, String[]> itemGeneratedRecipes = new Object2ObjectOpenHashMap();
   private ComponentType<EntityStore, CraftingManager> craftingManagerComponentType;

   public CraftingPlugin(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Nullable
   public static Set<String> getAvailableRecipesForCategory(@Nonnull String benchId, @Nonnull String benchCategoryId) {
      BenchRecipeRegistry benchRecipeRegistry = registries.get(benchId);
      return benchRecipeRegistry == null ? null : benchRecipeRegistry.getRecipesForCategory(benchCategoryId);
   }

   public static boolean isValidCraftingMaterialForBench(@Nonnull BenchState benchState, @Nonnull ItemStack itemStack) {
      BenchRecipeRegistry benchRecipeRegistry = registries.get(benchState.getBench().getId());
      return benchRecipeRegistry == null ? false : benchRecipeRegistry.isValidCraftingMaterial(itemStack);
   }

   public static boolean isValidUpgradeMaterialForBench(@Nonnull BenchState benchState, @Nonnull ItemStack itemStack) {
      BenchUpgradeRequirement nextLevelUpgradeMaterials = benchState.getNextLevelUpgradeMaterials();
      if (nextLevelUpgradeMaterials == null) {
         return false;
      } else {
         for (MaterialQuantity upgradeMaterial : nextLevelUpgradeMaterials.getInput()) {
            if (itemStack.getItemId().equals(upgradeMaterial.getItemId())) {
               return true;
            }

            ItemResourceType[] resourceTypeId = itemStack.getItem().getResourceTypes();
            if (resourceTypeId != null) {
               for (ItemResourceType resTypeId : resourceTypeId) {
                  if (resTypeId.id != null && resTypeId.id.equals(upgradeMaterial.getResourceTypeId())) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   @Override
   protected void setup() {
      AssetRegistry.getAssetStore(Interaction.class)
         .loadAssets(
            "Hytale:Hytale",
            List.of(OpenBenchPageInteraction.SIMPLE_CRAFTING, OpenBenchPageInteraction.DIAGRAM_CRAFTING, OpenBenchPageInteraction.STRUCTURAL_CRAFTING)
         );
      AssetRegistry.getAssetStore(RootInteraction.class)
         .loadAssets(
            "Hytale:Hytale",
            List.of(
               OpenBenchPageInteraction.SIMPLE_CRAFTING_ROOT, OpenBenchPageInteraction.DIAGRAM_CRAFTING_ROOT, OpenBenchPageInteraction.STRUCTURAL_CRAFTING_ROOT
            )
         );
      ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
      EventRegistry eventRegistry = this.getEventRegistry();
      this.craftingManagerComponentType = entityStoreRegistry.registerComponent(CraftingManager.class, CraftingManager::new);
      this.getCodecRegistry(Interaction.CODEC)
         .register("OpenBenchPage", OpenBenchPageInteraction.class, OpenBenchPageInteraction.CODEC)
         .register("OpenProcessingBench", OpenProcessingBenchInteraction.class, OpenProcessingBenchInteraction.CODEC);
      Bench.registerRootInteraction(BenchType.Crafting, OpenBenchPageInteraction.SIMPLE_CRAFTING_ROOT);
      Bench.registerRootInteraction(BenchType.DiagramCrafting, OpenBenchPageInteraction.DIAGRAM_CRAFTING_ROOT);
      Bench.registerRootInteraction(BenchType.StructuralCrafting, OpenBenchPageInteraction.STRUCTURAL_CRAFTING_ROOT);
      BlockStateRegistry blockStateRegistry = this.getBlockStateRegistry();
      blockStateRegistry.registerBlockState(ProcessingBenchState.class, "processingBench", ProcessingBenchState.CODEC);
      blockStateRegistry.registerBlockState(BenchState.class, "crafting", BenchState.CODEC);
      Window.CLIENT_REQUESTABLE_WINDOW_TYPES.put(WindowType.PocketCrafting, FieldCraftingWindow::new);
      eventRegistry.register(LoadedAssetsEvent.class, CraftingRecipe.class, CraftingPlugin::onRecipeLoad);
      eventRegistry.register(RemovedAssetsEvent.class, CraftingRecipe.class, CraftingPlugin::onRecipeRemove);
      eventRegistry.register(LoadedAssetsEvent.class, Item.class, CraftingPlugin::onItemAssetLoad);
      eventRegistry.register(RemovedAssetsEvent.class, Item.class, CraftingPlugin::onItemAssetRemove);
      Interaction.CODEC.register("LearnRecipe", LearnRecipeInteraction.class, LearnRecipeInteraction.CODEC);
      CommandManager.get().registerSystemCommand(new RecipeCommand());
      ComponentType<EntityStore, Player> playerComponentType = Player.getComponentType();
      ComponentType<EntityStore, PlayerRef> playerRefComponentType = PlayerRef.getComponentType();
      entityStoreRegistry.registerSystem(new PlayerCraftingSystems.CraftingTickingSystem(this.craftingManagerComponentType));
      entityStoreRegistry.registerSystem(new PlayerCraftingSystems.CraftingHolderSystem(playerComponentType, this.craftingManagerComponentType));
      entityStoreRegistry.registerSystem(new PlayerCraftingSystems.CraftingRefSystem(playerComponentType, this.craftingManagerComponentType));
      entityStoreRegistry.registerSystem(new CraftingPlugin.PlayerAddedSystem(playerComponentType, playerRefComponentType));
   }

   private static void onItemAssetLoad(@Nonnull LoadedAssetsEvent<String, Item, DefaultAssetMap<String, Item>> event) {
      List<CraftingRecipe> recipesToLoad = new ObjectArrayList();

      for (Item item : event.getLoadedAssets().values()) {
         if (item.hasRecipesToGenerate()) {
            List<CraftingRecipe> generatedRecipes = new ObjectArrayList();
            item.collectRecipesToGenerate(generatedRecipes);
            List<String> generatedIds = new ObjectArrayList();

            for (CraftingRecipe generatedRecipe : generatedRecipes) {
               String id = generatedRecipe.getId();
               generatedIds.add(id);
            }

            itemGeneratedRecipes.put(item.getId(), generatedIds.toArray(String[]::new));
            recipesToLoad.addAll(generatedRecipes);
         }
      }

      if (!recipesToLoad.isEmpty()) {
         CraftingRecipe.getAssetStore().loadAssets("Hytale:Hytale", recipesToLoad);
      }
   }

   private static void onItemAssetRemove(@Nonnull RemovedAssetsEvent<String, Item, DefaultAssetMap<String, Item>> event) {
      for (String id : event.getRemovedAssets()) {
         String[] generatedRecipes = itemGeneratedRecipes.get(id);
         if (generatedRecipes != null) {
            CraftingRecipe.getAssetStore().removeAssets(List.of(generatedRecipes));
         }
      }
   }

   private static void onRecipeLoad(@Nonnull LoadedAssetsEvent<String, CraftingRecipe, DefaultAssetMap<String, CraftingRecipe>> event) {
      for (CraftingRecipe recipe : event.getLoadedAssets().values()) {
         for (BenchRecipeRegistry registry : registries.values()) {
            registry.removeRecipe(recipe.getId());
         }

         if (recipe.getBenchRequirement() != null) {
            for (BenchRequirement benchRequirement : recipe.getBenchRequirement()) {
               BenchRecipeRegistry benchRecipeRegistry = registries.computeIfAbsent(benchRequirement.id, BenchRecipeRegistry::new);
               benchRecipeRegistry.addRecipe(benchRequirement, recipe);
            }
         }
      }

      computeBenchRecipeRegistries();
   }

   private static void onRecipeRemove(@Nonnull RemovedAssetsEvent<String, CraftingRecipe, DefaultAssetMap<String, CraftingRecipe>> event) {
      for (String removedRecipeId : event.getRemovedAssets()) {
         for (BenchRecipeRegistry registry : registries.values()) {
            registry.removeRecipe(removedRecipeId);
         }
      }

      computeBenchRecipeRegistries();
   }

   private static void computeBenchRecipeRegistries() {
      for (BenchRecipeRegistry registry : registries.values()) {
         registry.recompute();
      }
   }

   @Nonnull
   public static List<CraftingRecipe> getBenchRecipes(@Nonnull Bench bench) {
      return getBenchRecipes(bench.getType(), bench.getId());
   }

   @Nonnull
   public static List<CraftingRecipe> getBenchRecipes(BenchType benchType, String name) {
      return getBenchRecipes(benchType, name, null);
   }

   @Nonnull
   public static List<CraftingRecipe> getBenchRecipes(BenchType benchType, String benchId, @Nullable String category) {
      BenchRecipeRegistry registry = registries.get(benchId);
      if (registry == null) {
         return List.of();
      } else {
         List<CraftingRecipe> list = new ObjectArrayList();

         for (CraftingRecipe recipe : registry.getAllRecipes()) {
            BenchRequirement[] benchRequirement = recipe.getBenchRequirement();
            if (benchRequirement != null) {
               for (BenchRequirement requirement : benchRequirement) {
                  if (requirement.type == benchType && requirement.id.equals(benchId) && (category == null || hasCategory(recipe, category))) {
                     list.add(recipe);
                     break;
                  }
               }
            }
         }

         return list;
      }
   }

   private static boolean hasCategory(@Nonnull CraftingRecipe recipe, String category) {
      for (BenchRequirement benchRequirement : recipe.getBenchRequirement()) {
         if (benchRequirement.categories != null && ArrayUtil.contains(benchRequirement.categories, category)) {
            return true;
         }
      }

      return false;
   }

   public static boolean learnRecipe(@Nonnull Ref<EntityStore> ref, @Nonnull String recipeId, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      PlayerConfigData playerConfigData = playerComponent.getPlayerConfigData();
      Set<String> knownRecipes = new HashSet<>(playerConfigData.getKnownRecipes());
      if (knownRecipes.add(recipeId)) {
         playerConfigData.setKnownRecipes(knownRecipes);
         sendKnownRecipes(ref, componentAccessor);
         return true;
      } else {
         return false;
      }
   }

   public static boolean forgetRecipe(@Nonnull Ref<EntityStore> ref, @Nonnull String itemId, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      PlayerConfigData playerConfigData = playerComponent.getPlayerConfigData();
      Set<String> knownRecipes = new ObjectOpenHashSet(playerConfigData.getKnownRecipes());
      if (knownRecipes.remove(itemId)) {
         playerConfigData.setKnownRecipes(knownRecipes);
         sendKnownRecipes(ref, componentAccessor);
         return true;
      } else {
         return false;
      }
   }

   public static void sendKnownRecipes(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      PlayerConfigData playerConfigData = playerComponent.getPlayerConfigData();
      DefaultAssetMap<String, Item> itemAssetMap = Item.getAssetMap();
      Map<String, com.hypixel.hytale.protocol.CraftingRecipe> knownRecipes = new Object2ObjectOpenHashMap();

      for (String id : playerConfigData.getKnownRecipes()) {
         Item item = itemAssetMap.getAsset(id);
         if (item != null) {
            for (BenchRecipeRegistry registry : registries.values()) {
               for (String recipeId : registry.getIncomingRecipesForItem(item.getId())) {
                  CraftingRecipe recipe = CraftingRecipe.getAssetMap().getAsset(recipeId);
                  if (recipe != null) {
                     knownRecipes.put(id, recipe.toPacket(id));
                  }
               }
            }
         }
      }

      playerRefComponent.getPacketHandler().writeNoCache(new UpdateKnownRecipes(knownRecipes));
   }

   public ComponentType<EntityStore, CraftingManager> getCraftingManagerComponentType() {
      return this.craftingManagerComponentType;
   }

   public static CraftingPlugin get() {
      return instance;
   }

   public static class PlayerAddedSystem extends RefSystem<EntityStore> {
      @Nonnull
      private final Query<EntityStore> query;

      public PlayerAddedSystem(
         @Nonnull ComponentType<EntityStore, Player> playerComponentType, @Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType
      ) {
         this.query = Archetype.of(playerComponentType, playerRefComponentType);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         CraftingPlugin.sendKnownRecipes(ref, commandBuffer);
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }
   }
}
