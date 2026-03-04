package com.hypixel.hytale.builtin.adventure.memories;

import com.hypixel.hytale.builtin.adventure.memories.commands.MemoriesCommand;
import com.hypixel.hytale.builtin.adventure.memories.component.PlayerMemories;
import com.hypixel.hytale.builtin.adventure.memories.interactions.MemoriesConditionInteraction;
import com.hypixel.hytale.builtin.adventure.memories.interactions.SetMemoriesCapacityInteraction;
import com.hypixel.hytale.builtin.adventure.memories.memories.Memory;
import com.hypixel.hytale.builtin.adventure.memories.memories.MemoryProvider;
import com.hypixel.hytale.builtin.adventure.memories.memories.npc.NPCMemory;
import com.hypixel.hytale.builtin.adventure.memories.memories.npc.NPCMemoryProvider;
import com.hypixel.hytale.builtin.adventure.memories.page.MemoriesPage;
import com.hypixel.hytale.builtin.adventure.memories.page.MemoriesPageSupplier;
import com.hypixel.hytale.builtin.adventure.memories.page.MemoriesUnlockedPage;
import com.hypixel.hytale.builtin.adventure.memories.page.MemoriesUnlockedPageSuplier;
import com.hypixel.hytale.builtin.adventure.memories.temple.ForgottenTempleConfig;
import com.hypixel.hytale.builtin.adventure.memories.temple.TempleRespawnPlayersSystem;
import com.hypixel.hytale.builtin.adventure.memories.window.MemoriesWindow;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.Object2DoubleMapCodec;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.protocol.packets.player.UpdateMemoriesFeatureStatus;
import com.hypixel.hytale.protocol.packets.window.WindowType;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.Window;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSystems;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.BsonUtil;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.npc.AllNPCsLoadedEvent;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MemoriesPlugin extends JavaPlugin {
   @Nonnull
   public static final String MEMORIES_JSON_PATH = "memories.json";
   private static MemoriesPlugin instance;
   @Nonnull
   private final Config<MemoriesPlugin.MemoriesPluginConfig> config = this.withConfig(MemoriesPlugin.MemoriesPluginConfig.CODEC);
   @Nonnull
   private final List<MemoryProvider<?>> providers = new ObjectArrayList();
   @Nonnull
   private final Map<String, Set<Memory>> allMemories = new Object2ObjectRBTreeMap();
   private ComponentType<EntityStore, PlayerMemories> playerMemoriesComponentType;
   @Nullable
   private MemoriesPlugin.RecordedMemories recordedMemories;
   private boolean hasInitializedMemories;

   public static MemoriesPlugin get() {
      return instance;
   }

   public MemoriesPlugin(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
      this.getCommandRegistry().registerCommand(new MemoriesCommand());
      OpenCustomUIInteraction.registerCustomPageSupplier(this, MemoriesPage.class, "Memories", new MemoriesPageSupplier());
      OpenCustomUIInteraction.registerCustomPageSupplier(this, MemoriesUnlockedPage.class, "MemoriesUnlocked", new MemoriesUnlockedPageSuplier());
      Window.CLIENT_REQUESTABLE_WINDOW_TYPES.put(WindowType.Memories, MemoriesWindow::new);
      this.playerMemoriesComponentType = entityStoreRegistry.registerComponent(PlayerMemories.class, "PlayerMemories", PlayerMemories.CODEC);
      ComponentType<EntityStore, Player> playerComponentType = Player.getComponentType();
      ComponentType<EntityStore, PlayerRef> playerRefComponentType = PlayerRef.getComponentType();
      ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();
      NPCMemoryProvider npcMemoryProvider = new NPCMemoryProvider();
      this.registerMemoryProvider(npcMemoryProvider);
      entityStoreRegistry.registerSystem(
         new NPCMemory.GatherMemoriesSystem(
            transformComponentType, playerComponentType, playerRefComponentType, this.playerMemoriesComponentType, npcMemoryProvider.getCollectionRadius()
         )
      );

      for (MemoryProvider<?> provider : this.providers) {
         BuilderCodec<? extends Memory> codec = (BuilderCodec<? extends Memory>)provider.getCodec();
         this.getCodecRegistry(Memory.CODEC).register(provider.getId(), codec.getInnerClass(), codec);
      }

      this.getEventRegistry().register(AllNPCsLoadedEvent.class, event -> this.onAssetsLoad());
      entityStoreRegistry.registerSystem(new MemoriesPlugin.PlayerAddedSystem(playerComponentType, playerRefComponentType, this.playerMemoriesComponentType));
      this.getCodecRegistry(Interaction.CODEC).register("SetMemoriesCapacity", SetMemoriesCapacityInteraction.class, SetMemoriesCapacityInteraction.CODEC);
      this.getCodecRegistry(GameplayConfig.PLUGIN_CODEC).register(MemoriesGameplayConfig.class, "Memories", MemoriesGameplayConfig.CODEC);
      this.getCodecRegistry(Interaction.CODEC).register("MemoriesCondition", MemoriesConditionInteraction.class, MemoriesConditionInteraction.CODEC);
      entityStoreRegistry.registerSystem(new TempleRespawnPlayersSystem(playerRefComponentType, transformComponentType));
      this.getCodecRegistry(GameplayConfig.PLUGIN_CODEC).register(ForgottenTempleConfig.class, "ForgottenTemple", ForgottenTempleConfig.CODEC);
   }

   @Override
   protected void start() {
      try {
         Path path = Constants.UNIVERSE_PATH.resolve("memories.json");
         if (Files.exists(path)) {
            this.recordedMemories = RawJsonReader.readSync(path, MemoriesPlugin.RecordedMemories.CODEC, this.getLogger());
         } else {
            this.recordedMemories = new MemoriesPlugin.RecordedMemories();
         }
      } catch (IOException var2) {
         throw new RuntimeException(var2);
      }

      this.hasInitializedMemories = true;
      this.onAssetsLoad();
   }

   @Override
   protected void shutdown() {
      if (this.hasInitializedMemories) {
         this.recordedMemories.lock.readLock().lock();

         try {
            BsonUtil.writeSync(Constants.UNIVERSE_PATH.resolve("memories.json"), MemoriesPlugin.RecordedMemories.CODEC, this.recordedMemories, this.getLogger());
         } catch (IOException var5) {
            throw new RuntimeException(var5);
         } finally {
            this.recordedMemories.lock.readLock().unlock();
         }
      }
   }

   private void onAssetsLoad() {
      if (this.hasInitializedMemories) {
         this.allMemories.clear();

         for (MemoryProvider<?> provider : this.providers) {
            for (Entry<String, Set<Memory>> entry : provider.getAllMemories().entrySet()) {
               this.allMemories.computeIfAbsent(entry.getKey(), k -> new HashSet<>()).addAll(entry.getValue());
            }
         }
      }
   }

   public MemoriesPlugin.MemoriesPluginConfig getConfig() {
      return this.config.get();
   }

   public ComponentType<EntityStore, PlayerMemories> getPlayerMemoriesComponentType() {
      return this.playerMemoriesComponentType;
   }

   public <T extends Memory> void registerMemoryProvider(MemoryProvider<T> memoryProvider) {
      this.providers.add(memoryProvider);
   }

   @Nonnull
   public Map<String, Set<Memory>> getAllMemories() {
      return this.allMemories;
   }

   public int getMemoriesLevel(@Nonnull GameplayConfig gameplayConfig) {
      MemoriesGameplayConfig config = MemoriesGameplayConfig.get(gameplayConfig);
      int memoriesLevel = 1;
      if (config == null) {
         return memoriesLevel;
      } else {
         int recordedMemoriesCount = this.getRecordedMemories().size();
         int[] memoriesAmountPerLevel = config.getMemoriesAmountPerLevel();

         for (int i = memoriesAmountPerLevel.length - 1; i >= 0; i--) {
            if (recordedMemoriesCount >= memoriesAmountPerLevel[i]) {
               return i + 2;
            }
         }

         return memoriesLevel;
      }
   }

   public boolean hasRecordedMemory(Memory memory) {
      this.recordedMemories.lock.readLock().lock();

      boolean var2;
      try {
         var2 = this.recordedMemories.memories.contains(memory);
      } finally {
         this.recordedMemories.lock.readLock().unlock();
      }

      return var2;
   }

   public boolean recordPlayerMemories(@Nonnull PlayerMemories playerMemories) {
      this.recordedMemories.lock.writeLock().lock();

      try {
         if (playerMemories.takeMemories(this.recordedMemories.memories)) {
            BsonUtil.writeSync(Constants.UNIVERSE_PATH.resolve("memories.json"), MemoriesPlugin.RecordedMemories.CODEC, this.recordedMemories, this.getLogger());
            return true;
         }
      } catch (IOException var6) {
         throw new RuntimeException(var6);
      } finally {
         this.recordedMemories.lock.writeLock().unlock();
      }

      return false;
   }

   @Nonnull
   public Set<Memory> getRecordedMemories() {
      this.recordedMemories.lock.readLock().lock();

      HashSet var1;
      try {
         var1 = new HashSet<>(this.recordedMemories.memories);
      } finally {
         this.recordedMemories.lock.readLock().unlock();
      }

      return var1;
   }

   public void clearRecordedMemories() {
      this.recordedMemories.lock.writeLock().lock();

      try {
         this.recordedMemories.memories.clear();
         BsonUtil.writeSync(Constants.UNIVERSE_PATH.resolve("memories.json"), MemoriesPlugin.RecordedMemories.CODEC, this.recordedMemories, this.getLogger());
      } catch (IOException var5) {
         throw new RuntimeException(var5);
      } finally {
         this.recordedMemories.lock.writeLock().unlock();
      }
   }

   public void recordAllMemories() {
      this.recordedMemories.lock.writeLock().lock();

      try {
         for (Entry<String, Set<Memory>> entry : this.allMemories.entrySet()) {
            this.recordedMemories.memories.addAll(entry.getValue());
         }

         BsonUtil.writeSync(Constants.UNIVERSE_PATH.resolve("memories.json"), MemoriesPlugin.RecordedMemories.CODEC, this.recordedMemories, this.getLogger());
      } catch (IOException var6) {
         throw new RuntimeException(var6);
      } finally {
         this.recordedMemories.lock.writeLock().unlock();
      }
   }

   public int setRecordedMemoriesCount(int count) {
      if (count < 0) {
         count = 0;
      }

      this.recordedMemories.lock.writeLock().lock();

      int var12;
      try {
         this.recordedMemories.memories.clear();
         List<Memory> allAvailableMemories = new ObjectArrayList();

         for (Entry<String, Set<Memory>> entry : this.allMemories.entrySet()) {
            allAvailableMemories.addAll(entry.getValue());
         }

         int actualCount = Math.min(count, allAvailableMemories.size());

         for (int i = 0; i < actualCount; i++) {
            this.recordedMemories.memories.add(allAvailableMemories.get(i));
         }

         BsonUtil.writeSync(Constants.UNIVERSE_PATH.resolve("memories.json"), MemoriesPlugin.RecordedMemories.CODEC, this.recordedMemories, this.getLogger());
         var12 = actualCount;
      } catch (IOException var8) {
         throw new RuntimeException(var8);
      } finally {
         this.recordedMemories.lock.writeLock().unlock();
      }

      return var12;
   }

   public static class MemoriesPluginConfig {
      @Nonnull
      public static final BuilderCodec<MemoriesPlugin.MemoriesPluginConfig> CODEC = BuilderCodec.builder(
            MemoriesPlugin.MemoriesPluginConfig.class, MemoriesPlugin.MemoriesPluginConfig::new
         )
         .append(
            new KeyedCodec<>("CollectionRadius", new Object2DoubleMapCodec<>(Codec.STRING, Object2DoubleOpenHashMap::new)),
            (config, map) -> config.collectionRadius = map,
            config -> config.collectionRadius
         )
         .add()
         .build();
      private Object2DoubleMap<String> collectionRadius;

      @Nonnull
      public Object2DoubleMap<String> getCollectionRadius() {
         return (Object2DoubleMap<String>)(this.collectionRadius != null ? this.collectionRadius : Object2DoubleMaps.EMPTY_MAP);
      }
   }

   public static class PlayerAddedSystem extends RefSystem<EntityStore> {
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies = Set.of(new SystemDependency<>(Order.AFTER, PlayerSystems.PlayerSpawnedSystem.class));
      @Nonnull
      private final ComponentType<EntityStore, Player> playerComponentType;
      @Nonnull
      private final ComponentType<EntityStore, PlayerRef> playerRefComponentType;
      @Nonnull
      private final ComponentType<EntityStore, PlayerMemories> playerMemoriesComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public PlayerAddedSystem(
         @Nonnull ComponentType<EntityStore, Player> playerComponentType,
         @Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType,
         @Nonnull ComponentType<EntityStore, PlayerMemories> playerMemoriesComponentType
      ) {
         this.playerComponentType = playerComponentType;
         this.playerRefComponentType = playerRefComponentType;
         this.playerMemoriesComponentType = playerMemoriesComponentType;
         this.query = Query.and(playerComponentType, playerRefComponentType);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Player playerComponent = store.getComponent(ref, this.playerComponentType);

         assert playerComponent != null;

         PlayerRef playerRefComponent = store.getComponent(ref, this.playerRefComponentType);

         assert playerRefComponent != null;

         PlayerMemories playerMemoriesComponent = store.getComponent(ref, this.playerMemoriesComponentType);
         boolean isFeatureUnlockedByPlayer = playerMemoriesComponent != null;
         PacketHandler playerConnection = playerRefComponent.getPacketHandler();
         playerConnection.writeNoCache(new UpdateMemoriesFeatureStatus(isFeatureUnlockedByPlayer));
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }
   }

   private static class RecordedMemories {
      @Nonnull
      public static final BuilderCodec<MemoriesPlugin.RecordedMemories> CODEC = BuilderCodec.builder(
            MemoriesPlugin.RecordedMemories.class, MemoriesPlugin.RecordedMemories::new
         )
         .append(new KeyedCodec<>("Memories", new ArrayCodec<>(Memory.CODEC, Memory[]::new)), (recordedMemories, memories) -> {
            if (memories != null) {
               Collections.addAll(recordedMemories.memories, memories);
            }
         }, recordedMemories -> recordedMemories.memories.toArray(Memory[]::new))
         .add()
         .build();
      @Nonnull
      private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
      @Nonnull
      private final Set<Memory> memories = new HashSet<>();
   }
}
