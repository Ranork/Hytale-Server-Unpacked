package com.hypixel.hytale.builtin.teleport;

import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.teleport.commands.teleport.SpawnCommand;
import com.hypixel.hytale.builtin.teleport.commands.teleport.TeleportCommand;
import com.hypixel.hytale.builtin.teleport.commands.warp.WarpCommand;
import com.hypixel.hytale.builtin.teleport.components.TeleportHistory;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.command.system.CommandRegistry;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HiddenFromAdventurePlayers;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;
import com.hypixel.hytale.server.core.universe.world.events.ChunkPreLoadProcessEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MapMarkerBuilder;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MarkersCollector;
import com.hypixel.hytale.server.core.util.BsonUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import org.bson.BsonArray;
import org.bson.BsonDocument;

public class TeleportPlugin extends JavaPlugin {
   private static TeleportPlugin instance;
   public static final String WARP_MODEL_ID = "Warp";
   private ComponentType<EntityStore, TeleportHistory> teleportHistoryComponentType;
   private ComponentType<EntityStore, TeleportPlugin.WarpComponent> warpComponentType;
   @Nonnull
   private final AtomicBoolean loaded = new AtomicBoolean();
   @Nonnull
   private final ReentrantLock saveLock = new ReentrantLock();
   @Nonnull
   private final AtomicBoolean postSaveRedo = new AtomicBoolean(false);
   @Nonnull
   private final Map<String, Warp> warps = new ConcurrentHashMap<>();
   private Model warpModel;

   @Nonnull
   public static TeleportPlugin get() {
      return instance;
   }

   public TeleportPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Nonnull
   public ComponentType<EntityStore, TeleportHistory> getTeleportHistoryComponentType() {
      return this.teleportHistoryComponentType;
   }

   public boolean isWarpsLoaded() {
      return this.loaded.get();
   }

   @Override
   protected void setup() {
      instance = this;
      CommandRegistry commandRegistry = this.getCommandRegistry();
      EventRegistry eventRegistry = this.getEventRegistry();
      commandRegistry.registerCommand(new TeleportCommand());
      commandRegistry.registerCommand(new WarpCommand());
      commandRegistry.registerCommand(new SpawnCommand());
      eventRegistry.register(LoadedAssetsEvent.class, ModelAsset.class, this::onModelAssetChange);
      eventRegistry.registerGlobal(ChunkPreLoadProcessEvent.class, this::onChunkPreLoadProcess);
      eventRegistry.registerGlobal(
         AddWorldEvent.class, event -> event.getWorld().getWorldMapManager().addMarkerProvider("warps", TeleportPlugin.WarpMarkerProvider.INSTANCE)
      );
      eventRegistry.registerGlobal(AllWorldsLoadedEvent.class, event -> this.loadWarps());
      this.teleportHistoryComponentType = EntityStore.REGISTRY.registerComponent(TeleportHistory.class, TeleportHistory::new);
      this.warpComponentType = EntityStore.REGISTRY.registerComponent(TeleportPlugin.WarpComponent.class, () -> {
         throw new UnsupportedOperationException("WarpComponent must be created manually");
      });
   }

   @Override
   protected void start() {
      ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("Warp");
      if (modelAsset == null) {
         throw new IllegalStateException(String.format("Default warp model '%s' not found", "Warp"));
      } else {
         this.warpModel = Model.createUnitScaleModel(modelAsset);
      }
   }

   @Override
   protected void shutdown() {
   }

   public void loadWarps() {
      BsonDocument document = null;
      Path universePath = Universe.get().getPath();
      Path oldPath = universePath.resolve("warps.bson");
      Path path = universePath.resolve("warps.json");
      if (Files.exists(oldPath) && !Files.exists(path)) {
         try {
            Files.move(oldPath, path);
         } catch (IOException var10) {
         }
      }

      if (Files.exists(path)) {
         document = BsonUtil.readDocument(path).join();
      }

      if (document != null) {
         BsonArray bsonWarps = document.containsKey("Warps") ? document.getArray("Warps") : document.getArray("warps");
         this.warps.clear();

         for (Warp warp : Warp.ARRAY_CODEC.decode(bsonWarps)) {
            this.warps.put(warp.getId().toLowerCase(), warp);
         }

         this.getLogger().at(Level.INFO).log("Loaded %d warps", bsonWarps.size());
      } else {
         this.getLogger().at(Level.INFO).log("Loaded 0 warps (No warps.json found)");
      }

      this.loaded.set(true);
   }

   private void saveWarps0() {
      Warp[] array = this.warps.values().toArray(Warp[]::new);
      BsonDocument document = new BsonDocument("Warps", Warp.ARRAY_CODEC.encode(array));
      Path path = Universe.get().getPath().resolve("warps.json");
      BsonUtil.writeDocument(path, document).join();
      this.getLogger().at(Level.INFO).log("Saved %d warps to warps.json", array.length);
   }

   public void saveWarps() {
      if (this.saveLock.tryLock()) {
         try {
            this.saveWarps0();
         } catch (Throwable var5) {
            ((HytaleLogger.Api)this.getLogger().at(Level.SEVERE).withCause(var5)).log("Failed to save warps:");
         } finally {
            this.saveLock.unlock();
         }

         if (this.postSaveRedo.getAndSet(false)) {
            this.saveWarps();
         }
      } else {
         this.postSaveRedo.set(true);
      }
   }

   public Map<String, Warp> getWarps() {
      return this.warps;
   }

   private void onModelAssetChange(@Nonnull LoadedAssetsEvent<String, ModelAsset, DefaultAssetMap<String, ModelAsset>> event) {
      Map<String, ModelAsset> modelMap = event.getLoadedAssets();
      ModelAsset modelAsset = modelMap.get("Warp");
      if (modelAsset != null) {
         this.warpModel = Model.createUnitScaleModel(modelAsset);
      }
   }

   private void onChunkPreLoadProcess(@Nonnull ChunkPreLoadProcessEvent event) {
      WorldChunk chunk = event.getChunk();
      BlockChunk blockChunk = chunk.getBlockChunk();
      if (blockChunk != null) {
         int chunkX = blockChunk.getX();
         int chunkZ = blockChunk.getZ();
         World world = chunk.getWorld();
         String worldName = world.getName();

         for (Entry<String, Warp> warpEntry : this.warps.entrySet()) {
            Warp warp = warpEntry.getValue();
            Transform transform = warp.getTransform();
            if (transform != null) {
               Vector3d position = transform.getPosition();
               if (ChunkUtil.isInsideChunk(chunkX, chunkZ, MathUtil.floor(position.x), MathUtil.floor(position.z)) && warp.getWorld().equals(worldName)) {
                  world.execute(() -> {
                     Store<EntityStore> store = world.getEntityStore().getStore();
                     store.addEntity(this.createWarp(warp, store), AddReason.LOAD);
                  });
               }
            }
         }
      }
   }

   @Nonnull
   public Holder<EntityStore> createWarp(@Nonnull Warp warp, @Nonnull Store<EntityStore> store) {
      Transform transform = warp.getTransform();
      Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
      holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(transform.getPosition(), transform.getRotation()));
      holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
      holder.ensureComponent(Intangible.getComponentType());
      holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(this.warpModel.getBoundingBox()));
      holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(this.warpModel));
      holder.addComponent(Nameplate.getComponentType(), new Nameplate(warp.getId()));
      holder.ensureComponent(HiddenFromAdventurePlayers.getComponentType());
      holder.ensureComponent(EntityStore.REGISTRY.getNonSerializedComponentType());
      holder.addComponent(this.warpComponentType, new TeleportPlugin.WarpComponent(warp));
      return holder;
   }

   public record WarpComponent(Warp warp) implements Component<EntityStore> {
      public static ComponentType<EntityStore, TeleportPlugin.WarpComponent> getComponentType() {
         return TeleportPlugin.get().warpComponentType;
      }

      @Nonnull
      @Override
      public Component<EntityStore> clone() {
         return new TeleportPlugin.WarpComponent(this.warp);
      }
   }

   public static class WarpMarkerProvider implements WorldMapManager.MarkerProvider {
      public static final TeleportPlugin.WarpMarkerProvider INSTANCE = new TeleportPlugin.WarpMarkerProvider();

      @Override
      public void update(@Nonnull World world, @Nonnull Player player, @Nonnull MarkersCollector collector) {
         Map<String, Warp> warps = TeleportPlugin.get().getWarps();
         if (!warps.isEmpty()) {
            GameplayConfig gameplayConfig = world.getGameplayConfig();
            if (gameplayConfig.getWorldMapConfig().isDisplayWarps()) {
               for (Warp warp : warps.values()) {
                  if (warp.getWorld().equals(world.getName())) {
                     MapMarker marker = new MapMarkerBuilder("Warp-" + warp.getId(), "Warp.png", warp.getTransform())
                        .withCustomName("Warp: " + warp.getId())
                        .build();
                     collector.add(marker);
                  }
               }
            }
         }
      }
   }
}
