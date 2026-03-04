package com.hypixel.hytale.server.core.modules.block;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
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
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.StateData;
import com.hypixel.hytale.server.core.modules.LegacyModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import com.hypixel.hytale.server.core.universe.world.events.ChunkPreLoadProcessEvent;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.meta.state.BlockMapMarker;
import com.hypixel.hytale.server.core.universe.world.meta.state.BlockMapMarkersResource;
import com.hypixel.hytale.server.core.universe.world.meta.state.LaunchPad;
import com.hypixel.hytale.server.core.universe.world.meta.state.RespawnBlock;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
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
   private ComponentType<ChunkStore, BlockModule.BlockStateInfo> blockStateInfoComponentType;
   private ResourceType<ChunkStore, BlockModule.BlockStateInfoNeedRebuild> blockStateInfoNeedRebuildResourceType;

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
            BlockState state = BlockState.ensureState(chunk, x, y, z);
            return state != null ? state.getReference() : null;
         }
      }
   }

   private static void onChunkPreLoadProcessEnsureBlockEntity(@Nonnull ChunkPreLoadProcessEvent event) {
      if (event.isNewlyGenerated()) {
         BlockTypeAssetMap<String, BlockType> blockTypeAssetMap = BlockType.getAssetMap();
         Holder<ChunkStore> holder = event.getHolder();
         WorldChunk chunk = event.getChunk();
         ChunkColumn chunkColumnComponent = holder.getComponent(ChunkColumn.getComponentType());
         if (chunkColumnComponent != null) {
            Holder<ChunkStore>[] sectionHolders = chunkColumnComponent.getSectionHolders();
            if (sectionHolders != null) {
               BlockComponentChunk blockComponentModule = holder.getComponent(BlockComponentChunk.getComponentType());
               if (blockComponentModule != null) {
                  for (int sectionIndex = 0; sectionIndex < 10; sectionIndex++) {
                     BlockSection section = sectionHolders[sectionIndex].ensureAndGetComponent(BlockSection.getComponentType());
                     if (!section.isSolidAir()) {
                        int sectionYBlock = sectionIndex << 5;

                        for (int sectionY = 0; sectionY < 32; sectionY++) {
                           int y = sectionYBlock | sectionY;

                           for (int z = 0; z < 32; z++) {
                              for (int x = 0; x < 32; x++) {
                                 int blockId = section.get(x, y, z);
                                 BlockType blockType = blockTypeAssetMap.getAsset(blockId);
                                 if (blockType != null && !blockType.isUnknown() && section.getFiller(x, y, z) == 0) {
                                    int index = ChunkUtil.indexBlockInColumn(x, y, z);
                                    if (blockType.getBlockEntity() != null) {
                                       if (blockComponentModule.getEntityHolder(index) != null) {
                                          continue;
                                       }

                                       blockComponentModule.addEntityHolder(index, blockType.getBlockEntity().clone());
                                    }

                                    StateData state = blockType.getState();
                                    if (state != null && state.getId() != null && blockComponentModule.getEntityHolder(index) == null) {
                                       Vector3i position = new Vector3i(x, y, z);
                                       BlockState blockState = BlockStateModule.get().createBlockState(state.getId(), chunk, position, blockType);
                                       if (blockState != null) {
                                          blockComponentModule.addEntityHolder(index, blockState.toHolder());
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
