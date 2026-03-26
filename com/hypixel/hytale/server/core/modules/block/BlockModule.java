package com.hypixel.hytale.server.core.modules.block;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemType;
import com.hypixel.hytale.component.data.unknown.UnknownComponents;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.KDTree;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.function.consumer.BiIntConsumer;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.LegacyModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.modules.block.system.ItemContainerBlockSpatialSystem;
import com.hypixel.hytale.server.core.modules.block.system.ItemContainerSystems;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import com.hypixel.hytale.server.core.universe.world.events.ChunkPreLoadProcessEvent;
import com.hypixel.hytale.server.core.universe.world.meta.state.BlockMapMarker;
import com.hypixel.hytale.server.core.universe.world.meta.state.BlockMapMarkersResource;
import com.hypixel.hytale.server.core.universe.world.meta.state.LaunchPad;
import com.hypixel.hytale.server.core.universe.world.meta.state.RespawnBlock;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockModule extends JavaPlugin {
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(BlockModule.class).depends(LegacyModule.class).build();
   private static BlockModule instance;
   private SystemType<ChunkStore, BlockModule.MigrationSystem> migrationSystemType;
   private ComponentType<ChunkStore, LaunchPad> launchPadComponentType;
   private ComponentType<ChunkStore, RespawnBlock> respawnBlockComponentType;
   private ComponentType<ChunkStore, BlockMapMarker> blockMapMarkerComponentType;
   private ResourceType<ChunkStore, BlockMapMarkersResource> blockMapMarkersResourceType;
   private ComponentType<ChunkStore, ItemContainerBlock> itemContainerBlockComponentType;
   private ComponentType<ChunkStore, BlockModule.BlockStateInfo> blockStateInfoComponentType;
   private ResourceType<ChunkStore, BlockModule.BlockStateInfoNeedRebuild> blockStateInfoNeedRebuildResourceType;
   private ResourceType<ChunkStore, SpatialResource<Ref<ChunkStore>, ChunkStore>> itemContainerSpatialResourceType;

   public static BlockModule get() {
      return instance;
   }

   public BlockModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      ComponentRegistryProxy<ChunkStore> chunkStoreRegistry = this.getChunkStoreRegistry();
      this.migrationSystemType = chunkStoreRegistry.registerSystemType(BlockModule.MigrationSystem.class);
      this.blockStateInfoComponentType = chunkStoreRegistry.registerComponent(BlockModule.BlockStateInfo.class, () -> {
         throw new UnsupportedOperationException();
      });
      chunkStoreRegistry.registerSystem(new BlockModule.BlockStateInfoRefSystem(this.blockStateInfoComponentType));
      this.launchPadComponentType = chunkStoreRegistry.registerComponent(LaunchPad.class, "LaunchPad", LaunchPad.CODEC);
      chunkStoreRegistry.registerSystem(new BlockModule.MigrateLaunchPad());
      this.respawnBlockComponentType = chunkStoreRegistry.registerComponent(RespawnBlock.class, "RespawnBlock", RespawnBlock.CODEC);
      chunkStoreRegistry.registerSystem(new RespawnBlock.OnRemove());
      this.itemContainerBlockComponentType = chunkStoreRegistry.registerComponent(ItemContainerBlock.class, "ItemContainerBlock", ItemContainerBlock.CODEC);
      chunkStoreRegistry.registerSystem(new BlockModule.MigrateItemContainer());
      chunkStoreRegistry.registerSystem(new ItemContainerSystems.OnAddedOrRemoved());
      chunkStoreRegistry.registerSystem(new ItemContainerSystems.OnReplaced());
      chunkStoreRegistry.registerSystem(new ItemContainerSystems.OnReplacedHolder());
      this.blockMapMarkerComponentType = chunkStoreRegistry.registerComponent(BlockMapMarker.class, "BlockMapMarker", BlockMapMarker.CODEC);
      this.blockMapMarkersResourceType = chunkStoreRegistry.registerResource(BlockMapMarkersResource.class, "BlockMapMarkers", BlockMapMarkersResource.CODEC);
      chunkStoreRegistry.registerSystem(new BlockMapMarker.OnAddRemove());
      this.getEventRegistry()
         .registerGlobal(
            AddWorldEvent.class,
            event -> event.getWorld().getWorldMapManager().getMarkerProviders().put("blockMapMarkers", BlockMapMarker.MarkerProvider.INSTANCE)
         );
      this.blockStateInfoNeedRebuildResourceType = chunkStoreRegistry.registerResource(
         BlockModule.BlockStateInfoNeedRebuild.class, BlockModule.BlockStateInfoNeedRebuild::new
      );
      this.itemContainerSpatialResourceType = this.getChunkStoreRegistry().registerSpatialResource(() -> new KDTree<>(Ref::isValid));
      this.getChunkStoreRegistry().registerSystem(new ItemContainerBlockSpatialSystem(this.itemContainerSpatialResourceType));
      this.getChunkStoreRegistry().registerSystem(new BlockModule.ItemContainerStateRefSystem());
      this.getEventRegistry().registerGlobal(EventPriority.EARLY, ChunkPreLoadProcessEvent.class, BlockModule::onChunkPreLoadProcessEnsureBlockEntity);
   }

   @Deprecated
   @Nullable
   public static Ref<ChunkStore> ensureBlockEntity(@Nonnull WorldChunk chunk, int x, int y, int z) {
      Ref<ChunkStore> blockRef = chunk.getBlockComponentEntity(x, y, z);
      if (blockRef != null) {
         return blockRef;
      } else {
         BlockType blockType = chunk.getBlockType(x, y, z);
         if (blockType == null) {
            return null;
         } else if (blockType.getBlockEntity() != null) {
            Holder<ChunkStore> data = blockType.getBlockEntity().clone();
            data.putComponent(
               BlockModule.BlockStateInfo.getComponentType(), new BlockModule.BlockStateInfo(ChunkUtil.indexBlockInColumn(x, y, z), chunk.getReference())
            );
            return chunk.getWorld().getChunkStore().getStore().addEntity(data, AddReason.SPAWN);
         } else {
            return null;
         }
      }
   }

   private static void onChunkPreLoadProcessEnsureBlockEntity(@Nonnull ChunkPreLoadProcessEvent event) {
      if (event.isNewlyGenerated()) {
         Holder<ChunkStore> holder = event.getHolder();
         ChunkColumn chunkColumnComponent = holder.getComponent(ChunkColumn.getComponentType());
         if (chunkColumnComponent != null) {
            Holder<ChunkStore>[] sectionHolders = chunkColumnComponent.getSectionHolders();
            if (sectionHolders != null) {
               BlockComponentChunk blockComponentModule = holder.getComponent(BlockComponentChunk.getComponentType());
               if (blockComponentModule != null) {
                  BlockModule.BlockEntityPreprocessor preprocessor = BlockModule.BlockEntityPreprocessor.LOCAL.get();

                  for (int sectionIndex = 0; sectionIndex < 10; sectionIndex++) {
                     BlockSection section = sectionHolders[sectionIndex].ensureAndGetComponent(BlockSection.getComponentType());
                     if (!section.isSolidAir()) {
                        preprocessor.clear();
                        section.forEachValue(preprocessor.typeCollector);
                        if (!preprocessor.ids.isEmpty()) {
                           section.find(preprocessor.ids, preprocessor.blockCollector);

                           assert preprocessor.indices.size() == preprocessor.blockIds.size();

                           int sectionMinBlockY = ChunkUtil.minBlock(sectionIndex);

                           for (int i = 0; i < preprocessor.indices.size(); i++) {
                              int index = preprocessor.indices.getInt(i);
                              int blockId = preprocessor.blockIds.getInt(i);
                              Holder<ChunkStore> entity = (Holder<ChunkStore>)preprocessor.blockEntities.get(blockId);
                              if (entity != null) {
                                 int x = ChunkUtil.xFromIndex(index);
                                 int z = ChunkUtil.zFromIndex(index);
                                 int y = ChunkUtil.yFromIndex(index) | sectionMinBlockY;
                                 int chunkIndex = ChunkUtil.indexBlockInColumn(x, y, z);
                                 if (section.getFiller(index) == 0 && blockComponentModule.getEntityHolder(chunkIndex) == null) {
                                    blockComponentModule.addEntityHolder(chunkIndex, entity.clone());
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public SystemType<ChunkStore, BlockModule.MigrationSystem> getMigrationSystemType() {
      return this.migrationSystemType;
   }

   public ComponentType<ChunkStore, BlockModule.BlockStateInfo> getBlockStateInfoComponentType() {
      return this.blockStateInfoComponentType;
   }

   public ComponentType<ChunkStore, LaunchPad> getLaunchPadComponentType() {
      return this.launchPadComponentType;
   }

   public ComponentType<ChunkStore, RespawnBlock> getRespawnBlockComponentType() {
      return this.respawnBlockComponentType;
   }

   public ComponentType<ChunkStore, BlockMapMarker> getBlockMapMarkerComponentType() {
      return this.blockMapMarkerComponentType;
   }

   public ResourceType<ChunkStore, BlockMapMarkersResource> getBlockMapMarkersResourceType() {
      return this.blockMapMarkersResourceType;
   }

   public ResourceType<ChunkStore, BlockModule.BlockStateInfoNeedRebuild> getBlockStateInfoNeedRebuildResourceType() {
      return this.blockStateInfoNeedRebuildResourceType;
   }

   public ComponentType<ChunkStore, ItemContainerBlock> getItemContainerBlockComponentType() {
      return this.itemContainerBlockComponentType;
   }

   public ResourceType<ChunkStore, SpatialResource<Ref<ChunkStore>, ChunkStore>> getItemContainerSpatialResourceType() {
      return this.itemContainerSpatialResourceType;
   }

   @Nullable
   public static Ref<ChunkStore> getBlockEntity(@Nonnull World world, int x, int y, int z) {
      ChunkStore chunkStore = world.getChunkStore();
      Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(ChunkUtil.indexChunkFromBlock(x, z));
      if (chunkRef == null) {
         return null;
      } else {
         BlockComponentChunk blockComponentChunk = chunkStore.getStore().getComponent(chunkRef, BlockComponentChunk.getComponentType());
         if (blockComponentChunk == null) {
            return null;
         } else {
            int blockIndex = ChunkUtil.indexBlockInColumn(x, y, z);
            Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndex);
            return blockRef != null && blockRef.isValid() ? blockRef : null;
         }
      }
   }

   @Nullable
   public static <T extends Component<ChunkStore>> T getComponent(ComponentType<ChunkStore, T> componentType, World world, int x, int y, int z) {
      Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
      Ref<ChunkStore> chunkRef = world.getChunkStore().getChunkReference(ChunkUtil.indexChunkFromBlock(x, z));
      if (chunkRef != null && chunkRef.isValid()) {
         BlockComponentChunk blockComponentChunk = chunkStore.getComponent(chunkRef, BlockComponentChunk.getComponentType());
         if (blockComponentChunk == null) {
            return null;
         } else {
            int blockIndex = ChunkUtil.indexBlockInColumn(x, y, z);
            Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndex);
            return blockRef != null && blockRef.isValid() ? chunkStore.getComponent(blockRef, componentType) : null;
         }
      } else {
         return null;
      }
   }

   public static final class BlockEntityPreprocessor {
      public static final ThreadLocal<BlockModule.BlockEntityPreprocessor> LOCAL = ThreadLocal.withInitial(BlockModule.BlockEntityPreprocessor::new);
      public final IntList ids = new IntArrayList();
      public final Int2ObjectMap<Holder<ChunkStore>> blockEntities = new Int2ObjectOpenHashMap();
      public final IntList indices = new IntArrayList();
      public final IntList blockIds = new IntArrayList();
      public final IntConsumer typeCollector = this::collectType;
      public final BiIntConsumer blockCollector = this::collectBlock;

      public void clear() {
         this.ids.clear();
         this.blockEntities.clear();
         this.indices.clear();
         this.blockIds.clear();
      }

      private void collectType(int value) {
         BlockType type = BlockType.getAssetMap().getAsset(value);
         if (type != null && !type.isUnknown() && type.getBlockEntity() != null) {
            this.ids.add(value);
            this.blockEntities.put(value, type.getBlockEntity());
         }
      }

      private void collectBlock(int index, int blockId) {
         this.indices.add(index);
         this.blockIds.add(blockId);
      }
   }

   public static class BlockStateInfo implements Component<ChunkStore> {
      private final int index;
      @Nonnull
      private final Ref<ChunkStore> chunkRef;

      public static ComponentType<ChunkStore, BlockModule.BlockStateInfo> getComponentType() {
         return BlockModule.get().getBlockStateInfoComponentType();
      }

      public BlockStateInfo(int index, @Nonnull Ref<ChunkStore> chunkRef) {
         Objects.requireNonNull(chunkRef);
         this.index = index;
         this.chunkRef = chunkRef;
      }

      public int getIndex() {
         return this.index;
      }

      @Nonnull
      public Ref<ChunkStore> getChunkRef() {
         return this.chunkRef;
      }

      public void markNeedsSaving() {
         if (this.chunkRef.isValid()) {
            this.markNeedsSaving(this.chunkRef.getStore());
         }
      }

      public void markNeedsSaving(ComponentAccessor<ChunkStore> accessor) {
         if (this.chunkRef.isValid()) {
            BlockComponentChunk blockComponentChunk = accessor.getComponent(this.chunkRef, BlockComponentChunk.getComponentType());
            if (blockComponentChunk != null) {
               blockComponentChunk.markNeedsSaving();
            }
         }
      }

      @Nonnull
      @Override
      public Component<ChunkStore> clone() {
         return new BlockModule.BlockStateInfo(this.index, this.chunkRef);
      }
   }

   public static class BlockStateInfoNeedRebuild implements Resource<ChunkStore> {
      private boolean needRebuild;

      public static ResourceType<ChunkStore, BlockModule.BlockStateInfoNeedRebuild> getResourceType() {
         return BlockModule.get().getBlockStateInfoNeedRebuildResourceType();
      }

      public BlockStateInfoNeedRebuild() {
         this.needRebuild = false;
      }

      public BlockStateInfoNeedRebuild(boolean needRebuild) {
         this.needRebuild = needRebuild;
      }

      public boolean invalidateAndReturnIfNeedRebuild() {
         if (this.needRebuild) {
            this.needRebuild = false;
            return true;
         } else {
            return false;
         }
      }

      public void markAsNeedRebuild() {
         this.needRebuild = true;
      }

      @Override
      public Resource<ChunkStore> clone() {
         return new BlockModule.BlockStateInfoNeedRebuild(this.needRebuild);
      }
   }

   public static class BlockStateInfoRefSystem extends RefSystem<ChunkStore> {
      @Nonnull
      private final ComponentType<ChunkStore, BlockModule.BlockStateInfo> blockStateInfoComponentType;

      public BlockStateInfoRefSystem(@Nonnull ComponentType<ChunkStore, BlockModule.BlockStateInfo> blockStateInfoComponentType) {
         this.blockStateInfoComponentType = blockStateInfoComponentType;
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return this.blockStateInfoComponentType;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         BlockModule.BlockStateInfo blockStateInfoComponent = commandBuffer.getComponent(ref, this.blockStateInfoComponentType);

         assert blockStateInfoComponent != null;

         Ref<ChunkStore> chunkRef = blockStateInfoComponent.chunkRef;
         if (chunkRef.isValid()) {
            BlockComponentChunk blockComponentChunk = commandBuffer.getComponent(chunkRef, BlockComponentChunk.getComponentType());
            if (blockComponentChunk != null) {
               switch (reason) {
                  case SPAWN:
                     blockComponentChunk.addEntityReference(blockStateInfoComponent.getIndex(), ref);
                     break;
                  case LOAD:
                     blockComponentChunk.loadEntityReference(blockStateInfoComponent.getIndex(), ref);
               }
            }
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         BlockModule.BlockStateInfo blockStateInfoComponent = commandBuffer.getComponent(ref, this.blockStateInfoComponentType);

         assert blockStateInfoComponent != null;

         Ref<ChunkStore> chunkRef = blockStateInfoComponent.chunkRef;
         if (chunkRef.isValid()) {
            BlockComponentChunk blockComponentChunk = commandBuffer.getComponent(chunkRef, BlockComponentChunk.getComponentType());
            if (blockComponentChunk != null) {
               switch (reason) {
                  case REMOVE:
                     blockComponentChunk.removeEntityReference(blockStateInfoComponent.getIndex(), ref);
                     break;
                  case UNLOAD:
                     blockComponentChunk.unloadEntityReference(blockStateInfoComponent.getIndex(), ref);
               }
            }
         }
      }

      @Nonnull
      @Override
      public String toString() {
         return "BlockStateInfoRefSystem{componentType=" + this.blockStateInfoComponentType + "}";
      }
   }

   public static class ItemContainerStateRefSystem extends RefSystem<ChunkStore> {
      private static final Query<ChunkStore> query = ItemContainerBlock.getComponentType();

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

   public static class MigrateItemContainer extends BlockModule.MigrationSystem {
      @Override
      public void onEntityAdd(@Nonnull Holder<ChunkStore> holder, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store) {
         UnknownComponents<ChunkStore> unknownComponents = holder.getComponent(ChunkStore.REGISTRY.getUnknownComponentType());

         assert unknownComponents != null;

         ItemContainerBlock itemContainerBlock = unknownComponents.removeComponent("container", ItemContainerBlock.CODEC);
         if (itemContainerBlock != null) {
            holder.putComponent(ItemContainerBlock.getComponentType(), itemContainerBlock);
         }
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<ChunkStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store) {
      }

      @Nullable
      @Override
      public Query<ChunkStore> getQuery() {
         return ChunkStore.REGISTRY.getUnknownComponentType();
      }
   }

   @Deprecated(forRemoval = true)
   public static class MigrateLaunchPad extends BlockModule.MigrationSystem {
      @Override
      public void onEntityAdd(@Nonnull Holder<ChunkStore> holder, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store) {
         UnknownComponents<ChunkStore> unknownComponents = holder.getComponent(ChunkStore.REGISTRY.getUnknownComponentType());

         assert unknownComponents != null;

         LaunchPad launchPadComponent = unknownComponents.removeComponent("launchPad", LaunchPad.CODEC);
         if (launchPadComponent != null) {
            holder.putComponent(LaunchPad.getComponentType(), launchPadComponent);
         }
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<ChunkStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store) {
      }

      @Nullable
      @Override
      public Query<ChunkStore> getQuery() {
         return ChunkStore.REGISTRY.getUnknownComponentType();
      }
   }

   public abstract static class MigrationSystem extends HolderSystem<ChunkStore> {
   }
}
