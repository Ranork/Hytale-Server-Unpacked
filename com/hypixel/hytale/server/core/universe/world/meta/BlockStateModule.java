package com.hypixel.hytale.server.core.universe.world.meta;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.DisableProcessingAssert;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.KDTree;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.MetricSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.metrics.MetricResults;
import com.hypixel.hytale.metrics.MetricsRegistry;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.StateData;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.system.ItemContainerStateSpatialSystem;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginState;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkFlag;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.state.TickableBlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.DestroyableBlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.meta.state.SendableBlockState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;

@Deprecated(forRemoval = true)
public class BlockStateModule extends JavaPlugin {
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(BlockStateModule.class).depends(BlockModule.class).build();
   private static BlockStateModule instance;
   @Deprecated
   private final Map<Class<? extends BlockState>, ComponentType<ChunkStore, ? extends BlockState>> classToComponentType = new ConcurrentHashMap<>();
   private ResourceType<ChunkStore, SpatialResource<Ref<ChunkStore>, ChunkStore>> itemContainerSpatialResourceType;

   public static BlockStateModule get() {
      return instance;
   }

   public ResourceType<ChunkStore, SpatialResource<Ref<ChunkStore>, ChunkStore>> getItemContainerSpatialResourceType() {
      return this.itemContainerSpatialResourceType;
   }

   public BlockStateModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      this.registerBlockState(
         ItemContainerState.class,
         "container",
         ItemContainerState.CODEC,
         ItemContainerState.ItemContainerStateData.class,
         ItemContainerState.ItemContainerStateData.CODEC
      );
      this.itemContainerSpatialResourceType = this.getChunkStoreRegistry().registerSpatialResource(() -> new KDTree<>(Ref::isValid));
      this.getChunkStoreRegistry().registerSystem(new ItemContainerStateSpatialSystem(this.itemContainerSpatialResourceType));
      this.getChunkStoreRegistry().registerSystem(new BlockStateModule.ItemContainerStateRefSystem());
   }

   @Nullable
   public <T extends BlockState> BlockStateRegistration registerBlockState(@Nonnull Class<T> clazz, @Nonnull String key, Codec<T> codec) {
      return this.registerBlockState(clazz, key, codec, null, null);
   }

   @Nullable
   public <T extends BlockState, D extends StateData> BlockStateRegistration registerBlockState(
      @Nonnull Class<T> clazz, @Nonnull String key, @Nullable Codec<T> codec, Class<D> dataClass, @Nullable Codec<D> dataCodec
   ) {
      if (this.isDisabled()) {
         return null;
      } else {
         BlockState.CODEC.register(key, clazz, codec);
         if (dataCodec != null) {
            StateData.CODEC.register(key, dataClass, dataCodec);
         }

         ComponentType<ChunkStore, T> componentType;
         if (codec != null) {
            componentType = this.getChunkStoreRegistry().registerComponent(clazz, key, (BuilderCodec<T>)codec);
         } else {
            componentType = this.getChunkStoreRegistry().registerComponent(clazz, () -> {
               throw new UnsupportedOperationException("Not implemented!");
            });
         }

         this.classToComponentType.put(clazz, componentType);
         this.getChunkStoreRegistry().registerSystem(new BlockStateModule.LegacyLateInitBlockStateSystem<>(componentType), true);
         this.getChunkStoreRegistry().registerSystem(new BlockStateModule.LegacyBlockStateHolderSystem<>(componentType), true);
         this.getChunkStoreRegistry().registerSystem(new BlockStateModule.LegacyBlockStateRefSystem<>(componentType), true);
         if (TickableBlockState.class.isAssignableFrom(clazz)) {
            this.getChunkStoreRegistry().registerSystem(new BlockStateModule.LegacyTickingBlockStateSystem<>(componentType), true);
         }

         if (SendableBlockState.class.isAssignableFrom(clazz)) {
            this.getChunkStoreRegistry().registerSystem(new BlockStateModule.LegacyLoadPacketBlockStateSystem<>(componentType), true);
            this.getChunkStoreRegistry().registerSystem(new BlockStateModule.LegacyUnloadPacketBlockStateSystem<>(componentType), true);
         }

         return new BlockStateRegistration(clazz, () -> this.getState() == PluginState.ENABLED, () -> this.unregisterBlockState(clazz, dataClass));
      }
   }

   public <T extends BlockState, D extends StateData> void unregisterBlockState(Class<T> clazz, @Nullable Class<D> dataClass) {
      if (!HytaleServer.get().isShuttingDown()) {
         BlockState.CODEC.remove(clazz);
         ChunkStore.REGISTRY.unregisterComponent(this.classToComponentType.remove(clazz));
         if (dataClass != null) {
            StateData.CODEC.remove(dataClass);
         }
      }
   }

   @Nullable
   public <T extends BlockState> T createBlockState(Class<T> clazz, WorldChunk chunk, Vector3i pos, BlockType blockType) {
      String id = BlockState.CODEC.getIdFor(clazz);
      return (T)this.createBlockState(id, chunk, pos, blockType);
   }

   @Nullable
   public BlockState createBlockState(String key, WorldChunk chunk, Vector3i pos, BlockType blockType) {
      Codec<? extends BlockState> codec = BlockState.CODEC.getCodecFor(key);
      if (codec == null) {
         this.getLogger().at(Level.WARNING).log("Failed to create BlockState for '%s' null codec", key);
         return null;
      } else {
         BlockState blockState = codec.decode(new BsonDocument());
         if (blockState == null) {
            this.getLogger().at(Level.WARNING).log("Failed to create BlockState for '%s' null value from supplier", key);
            return null;
         } else {
            blockState.setPosition(chunk, pos);
            if (!blockState.initialize(blockType)) {
               return null;
            } else {
               blockState.initialized.set(true);
               return blockState;
            }
         }
      }
   }

   @Nullable
   public <T extends BlockState> ComponentType<ChunkStore, T> getComponentType(@Nullable Class<T> entityClass) {
      if (this.isDisabled()) {
         return null;
      } else {
         return (ComponentType<ChunkStore, T>)(entityClass == null ? null : this.classToComponentType.get(entityClass));
      }
   }

   public static class ItemContainerStateRefSystem extends RefSystem<ChunkStore> {
      private static final Query<ChunkStore> query = BlockStateModule.get().getComponentType(ItemContainerState.class);

      @Override
      public Query<ChunkStore> getQuery() {
         return query;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         commandBuffer.getExternalData()
            .getWorld()
            .getChunkStore()
            .getStore()
            .getResource(BlockModule.BlockStateInfoNeedRebuild.getResourceType())
            .markAsNeedRebuild();
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         commandBuffer.getExternalData()
            .getWorld()
            .getChunkStore()
            .getStore()
            .getResource(BlockModule.BlockStateInfoNeedRebuild.getResourceType())
            .markAsNeedRebuild();
      }

      @Nonnull
      @Override
      public String toString() {
         return "ItemContainerStateRefSystem{}";
      }
   }

   public static class LegacyBlockStateHolderSystem<T extends BlockState> extends HolderSystem<ChunkStore> implements DisableProcessingAssert {
      private final ComponentType<ChunkStore, T> componentType;

      public LegacyBlockStateHolderSystem(ComponentType<ChunkStore, T> componentType) {
         this.componentType = componentType;
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return this.componentType;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<ChunkStore> holder, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store) {
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<ChunkStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store) {
         T blockState = holder.getComponent(this.componentType);
         switch (reason) {
            case REMOVE:
               if (blockState instanceof DestroyableBlockState destroyableBlockState) {
                  destroyableBlockState.onDestroy();
               }

               blockState.unloadFromWorld();
               break;
            case UNLOAD:
               blockState.onUnload();
               blockState.unloadFromWorld();
         }
      }

      @Nonnull
      @Override
      public String toString() {
         return "LegacyBlockStateSystem{componentType=" + this.componentType + "}";
      }
   }

   public static class LegacyBlockStateRefSystem<T extends BlockState> extends RefSystem<ChunkStore> implements DisableProcessingAssert {
      private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
      private final ComponentType<ChunkStore, T> componentType;

      public LegacyBlockStateRefSystem(ComponentType<ChunkStore, T> componentType) {
         this.componentType = componentType;
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return this.componentType;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         T blockState = store.getComponent(ref, this.componentType);
         int index = blockState.getIndex();
         WorldChunk chunk = blockState.getChunk();
         if (chunk == null) {
            Vector3i position = blockState.getBlockPosition();
            int chunkX = MathUtil.floor(position.getX()) >> 5;
            int chunkZ = MathUtil.floor(position.getZ()) >> 5;
            World world = store.getExternalData().getWorld();
            WorldChunk worldChunk = world.getChunkIfInMemory(ChunkUtil.indexChunk(chunkX, chunkZ));
            if (worldChunk != null && !worldChunk.not(ChunkFlag.INIT)) {
               if (worldChunk.not(ChunkFlag.TICKING)) {
                  commandBuffer.run(_store -> {
                     Holder<ChunkStore> holder = _store.removeEntity(ref, RemoveReason.UNLOAD);
                     worldChunk.getBlockComponentChunk().addEntityHolder(index, holder);
                  });
               }

               int x = ChunkUtil.xFromBlockInColumn(index);
               int y = ChunkUtil.yFromBlockInColumn(index);
               int z = ChunkUtil.zFromBlockInColumn(index);
               blockState.setPosition(worldChunk, new Vector3i(x, y, z));
            }
         }

         blockState.setReference(ref);
         if (!blockState.initialized.get()) {
            if (!blockState.initialize(blockState.getChunk().getBlockType(blockState.getPosition()))) {
               LOGGER.at(Level.WARNING).log("Block State failed initialize: %s, %s, %s", blockState, blockState.getPosition(), chunk);
               commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
            } else {
               blockState.initialized.set(true);
            }
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
      }

      @Nonnull
      @Override
      public String toString() {
         return "LegacyBlockStateSystem{componentType=" + this.componentType + "}";
      }
   }

   public static class LegacyLateInitBlockStateSystem<T extends BlockState> extends EntityTickingSystem<ChunkStore> implements DisableProcessingAssert {
      private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
      private final ComponentType<ChunkStore, T> componentType;
      @Nonnull
      private final Query<ChunkStore> query;

      public LegacyLateInitBlockStateSystem(ComponentType<ChunkStore, T> componentType) {
         this.componentType = componentType;
         this.query = Query.and(componentType, BlockModule.BlockStateInfo.getComponentType());
      }

      @Nonnull
      @Override
      public Query<ChunkStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public Set<Dependency<ChunkStore>> getDependencies() {
         return RootDependency.firstSet();
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
         @Nonnull Store<ChunkStore> store,
         @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         T blockStateComponent = archetypeChunk.getComponent(index, this.componentType);

         assert blockStateComponent != null;

         BlockModule.BlockStateInfo blockStateInfoComponent = archetypeChunk.getComponent(index, BlockModule.BlockStateInfo.getComponentType());

         assert blockStateInfoComponent != null;

         try {
            if (!blockStateComponent.initialized.get()) {
               blockStateComponent.initialized.set(true);
               if (blockStateComponent.getReference() == null || !blockStateComponent.getReference().isValid()) {
                  blockStateComponent.setReference(archetypeChunk.getReferenceTo(index));
               }

               World world = store.getExternalData().getWorld();
               Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
               WorldChunk worldChunkComponent = chunkStore.getComponent(blockStateInfoComponent.getChunkRef(), WorldChunk.getComponentType());

               assert worldChunkComponent != null;

               int x = ChunkUtil.xFromBlockInColumn(blockStateInfoComponent.getIndex());
               int y = ChunkUtil.yFromBlockInColumn(blockStateInfoComponent.getIndex());
               int z = ChunkUtil.zFromBlockInColumn(blockStateInfoComponent.getIndex());
               blockStateComponent.setPosition(worldChunkComponent, new Vector3i(x, y, z));
               int blockIndex = worldChunkComponent.getBlock(x, y, z);
               BlockType blockType = BlockType.getAssetMap().getAsset(blockIndex);
               if (!blockStateComponent.initialize(blockType)) {
                  LOGGER.at(Level.SEVERE).log("Removing invalid block state %s", blockStateComponent);
                  commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
               }
            }
         } catch (Exception var16) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var16)).log("Exception while re-init BlockState! Removing!! %s", blockStateComponent);
            commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
         }
      }

      @Nonnull
      @Override
      public String toString() {
         return "LegacyLateInitBlockStateSystem{componentType=" + this.componentType + "}";
      }
   }

   public static class LegacyLoadPacketBlockStateSystem<T extends BlockState> extends ChunkStore.LoadPacketDataQuerySystem {
      private final ComponentType<ChunkStore, T> componentType;

      public LegacyLoadPacketBlockStateSystem(ComponentType<ChunkStore, T> componentType) {
         this.componentType = componentType;
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return this.componentType;
      }

      public void fetch(
         int index,
         @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
         Store<ChunkStore> store,
         CommandBuffer<ChunkStore> commandBuffer,
         PlayerRef player,
         List<ToClientPacket> results
      ) {
         SendableBlockState state = (SendableBlockState)BlockState.getBlockState(index, archetypeChunk);
         if (state.canPlayerSee(player)) {
            state.sendTo(results);
         }
      }

      @Nonnull
      @Override
      public String toString() {
         return "LegacyLoadPacketBlockStateSystem{componentType=" + this.componentType + "}";
      }
   }

   public static class LegacyTickingBlockStateSystem<T extends BlockState>
      extends EntityTickingSystem<ChunkStore>
      implements DisableProcessingAssert,
      MetricSystem<ChunkStore> {
      private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
      private static final MetricsRegistry<BlockStateModule.LegacyTickingBlockStateSystem<?>> METRICS_REGISTRY = new MetricsRegistry<BlockStateModule.LegacyTickingBlockStateSystem<?>>()
         .register("ComponentType", o -> o.componentType.getTypeClass().toString(), Codec.STRING);
      private final ComponentType<ChunkStore, T> componentType;

      public LegacyTickingBlockStateSystem(ComponentType<ChunkStore, T> componentType) {
         this.componentType = componentType;
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return this.componentType;
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
         @Nonnull Store<ChunkStore> store,
         @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         T blockState = archetypeChunk.getComponent(index, this.componentType);

         try {
            ((TickableBlockState)blockState).tick(dt, index, archetypeChunk, store, commandBuffer);
         } catch (Throwable var8) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var8)).log("Exception while ticking BlockState! Removing!! %s", blockState);
            commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
         }
      }

      @Nonnull
      @Override
      public MetricResults toMetricResults(Store<ChunkStore> store) {
         return METRICS_REGISTRY.toMetricResults(this);
      }

      @Nonnull
      @Override
      public String toString() {
         return "LegacyTickingBlockStateSystem{componentType=" + this.componentType + "}";
      }
   }

   public static class LegacyUnloadPacketBlockStateSystem<T extends BlockState> extends ChunkStore.UnloadPacketDataQuerySystem {
      private final ComponentType<ChunkStore, T> componentType;

      public LegacyUnloadPacketBlockStateSystem(ComponentType<ChunkStore, T> componentType) {
         this.componentType = componentType;
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return this.componentType;
      }

      public void fetch(
         int index,
         @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
         Store<ChunkStore> store,
         CommandBuffer<ChunkStore> commandBuffer,
         PlayerRef player,
         List<ToClientPacket> results
      ) {
         SendableBlockState state = (SendableBlockState)BlockState.getBlockState(index, archetypeChunk);
         if (state.canPlayerSee(player)) {
            state.unloadFrom(results);
         }
      }

      @Nonnull
      @Override
      public String toString() {
         return "LegacyUnloadPacketBlockStateSystem{componentType=" + this.componentType + "}";
      }
   }
}
