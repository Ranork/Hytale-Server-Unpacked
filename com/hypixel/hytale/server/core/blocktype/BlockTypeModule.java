package com.hypixel.hytale.server.core.blocktype;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.DisableProcessingAssert;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BenchType;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.StateData;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.Bench;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.CraftingBench;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.DiagramCraftingBench;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.ProcessingBench;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.StructuralCraftingBench;
import com.hypixel.hytale.server.core.blocktype.component.BlockPhysics;
import com.hypixel.hytale.server.core.modules.LegacyModule;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.modules.migrations.ChunkColumnMigrationSystem;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.BlockAccessor;
import com.hypixel.hytale.server.core.universe.world.accessor.ChunkAccessor;
import com.hypixel.hytale.server.core.universe.world.accessor.LocalCachedChunkAccessor;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkFlag;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.systems.ChunkSystems;
import com.hypixel.hytale.server.core.universe.world.events.ChunkPreLoadProcessEvent;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockTypeModule extends JavaPlugin {
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(BlockTypeModule.class).depends(ItemModule.class).depends(LegacyModule.class).build();
   public static final int SET_BLOCK_SETTINGS = 157;
   public static final String DEBUG_CUBE_TEXTURE_UP = "BlockTextures/_Debug/Up.png";
   public static final String DEBUG_CUBE_TEXTURE_DOWN = "BlockTextures/_Debug/Down.png";
   public static final String DEBUG_CUBE_TEXTURE_NORTH = "BlockTextures/_Debug/North.png";
   public static final String DEBUG_CUBE_TEXTURE_SOUTH = "BlockTextures/_Debug/South.png";
   public static final String DEBUG_CUBE_TEXTURE_EAST = "BlockTextures/_Debug/East.png";
   public static final String DEBUG_CUBE_TEXTURE_WEST = "BlockTextures/_Debug/West.png";
   public static final String DEBUG_MODEL_MODEL = "Blocks/_Debug/Model.blockymodel";
   public static final String DEBUG_MODEL_BLOCK_TEXTURE = "Blocks/_Debug/Texture.png";
   public static final String DEBUG_MODEL_ENTITY_TEXTURE = "Characters/_Debug/Texture.png";
   private static final ThreadLocal<BlockType[]> TEMP_BLOCKS = ThreadLocal.withInitial(() -> new BlockType[327680]);
   private static BlockTypeModule instance;
   private ComponentType<ChunkStore, BlockPhysics> blockPhysicsComponentType;

   public static BlockTypeModule get() {
      return instance;
   }

   public BlockTypeModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      Bench.CODEC.register(BenchType.Crafting, CraftingBench.class, CraftingBench.CODEC);
      Bench.CODEC.register(BenchType.Processing, ProcessingBench.class, ProcessingBench.CODEC);
      Bench.CODEC.register(BenchType.DiagramCrafting, DiagramCraftingBench.class, DiagramCraftingBench.CODEC);
      Bench.CODEC.register(BenchType.StructuralCrafting, StructuralCraftingBench.class, StructuralCraftingBench.CODEC);
      this.blockPhysicsComponentType = this.getChunkStoreRegistry().registerComponent(BlockPhysics.class, "BlockPhysics", BlockPhysics.CODEC);
      this.getChunkStoreRegistry().registerSystem(new BlockTypeModule.MigrateLegacySections());
   }

   public ComponentType<ChunkStore, BlockPhysics> getBlockPhysicsComponentType() {
      return this.blockPhysicsComponentType;
   }

   @Deprecated
   private static void onChunkPreLoadProcessEnsureBlockState(@Nonnull ChunkPreLoadProcessEvent event) {
      if (event.isNewlyGenerated()) {
         BlockTypeAssetMap<String, BlockType> blockTypeAssetMap = BlockType.getAssetMap();
         Holder<ChunkStore> holder = event.getHolder();
         WorldChunk chunk = event.getChunk();
         ChunkColumn column = holder.getComponent(ChunkColumn.getComponentType());
         if (column != null) {
            Holder<ChunkStore>[] sections = column.getSectionHolders();
            if (sections != null) {
               for (int sectionIndex = 0; sectionIndex < 10; sectionIndex++) {
                  BlockSection section = sections[sectionIndex].ensureAndGetComponent(BlockSection.getComponentType());
                  if (!section.isSolidAir()) {
                     int sectionYBlock = sectionIndex << 5;

                     for (int sectionY = 0; sectionY < 32; sectionY++) {
                        int y = sectionYBlock | sectionY;

                        for (int x = 0; x < 32; x++) {
                           for (int z = 0; z < 32; z++) {
                              int blockId = section.get(x, y, z);
                              BlockType blockType = blockTypeAssetMap.getAsset(blockId);
                              if (blockType != null && !blockType.isUnknown() && section.getFiller(x, y, z) == 0) {
                                 StateData state = blockType.getState();
                                 if (state != null && state.getId() != null && chunk.getState(x, y, z) == null) {
                                    Vector3i position = new Vector3i(x, y, z);
                                    BlockState blockState = BlockStateModule.get().createBlockState(state.getId(), chunk, position, blockType);
                                    if (blockState != null) {
                                       chunk.setState(x, y, z, blockState);
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

   private static void onChunkPreLoadProcess(@Nonnull ChunkPreLoadProcessEvent event) {
      if (event.isNewlyGenerated()) {
         WorldChunk chunk = event.getChunk();
         BlockChunk blockChunk = chunk.getBlockChunk();
         Holder<ChunkStore> holder = event.getHolder();
         ChunkColumn column = holder.getComponent(ChunkColumn.getComponentType());
         if (column != null) {
            Holder<ChunkStore>[] sections = column.getSectionHolders();
            if (sections != null) {
               BlockType[] tempBlocks = TEMP_BLOCKS.get();
               Arrays.fill(tempBlocks, null);

               for (int sectionIndex = 0; sectionIndex < 10; sectionIndex++) {
                  BlockSection section = sections[sectionIndex].ensureAndGetComponent(BlockSection.getComponentType());
                  if (!section.isSolidAir() && !(section.getMaximumHitboxExtent() <= 0.0)) {
                     onChunksectionPreLoadProcess(chunk, section, sectionIndex, tempBlocks);
                  }
               }
            }
         }
      }
   }

   private static void onChunksectionPreLoadProcess(@Nonnull WorldChunk chunk, @Nonnull BlockSection section, int sectionIndex, @Nonnull BlockType[] blocks) {
      int sectionYBlock = sectionIndex << 5;
      BlockTypeAssetMap<String, BlockType> blockTypeAssetMap = BlockType.getAssetMap();
      IndexedLookupTableAssetMap<String, BlockBoundingBoxes> hitboxAssetMap = BlockBoundingBoxes.getAssetMap();

      for (int y = 0; y < 32; y++) {
         int finalY = sectionYBlock | y;

         for (int x = 0; x < 32; x++) {
            int finalX = x;

            for (int z = 0; z < 32; z++) {
               int finalZ = z;
               BlockType blockType = getBlockType(blockTypeAssetMap, blocks, section, finalX, finalY, finalZ, true);
               if (blockType != null) {
                  int rotation = section.getRotationIndex(x, y, z);
                  int filler = section.getFiller(x, y, z);
                  if (filler != 0) {
                     int blockX = finalX - FillerBlockUtil.unpackX(filler);
                     if (blockX >= 0 && blockX < 32) {
                        int blockY = finalY - FillerBlockUtil.unpackY(filler);
                        if (blockY >= 0 && blockY < 320) {
                           int blockZ = finalZ - FillerBlockUtil.unpackZ(filler);
                           if (blockZ >= 0 && blockZ < 32) {
                              BlockType originBlockType = getBlockType(blockTypeAssetMap, blocks, section, blockX, blockY, blockZ, false);
                              if (originBlockType != null) {
                                 String blockTypeKey = blockType.getId();
                                 if (blockType.isUnknown() || !blockTypeKey.equals(originBlockType.getId())) {
                                    chunk.breakBlock(finalX, finalY, finalZ, 157);
                                 }
                              }
                           }
                        }
                     }
                  } else {
                     int blockId = blockTypeAssetMap.getIndex(blockType.getId());
                     FillerBlockUtil.forEachFillerBlock(hitboxAssetMap.getAsset(blockType.getHitboxTypeIndex()).get(rotation), (x1, y1, z1) -> {
                        if (x1 != 0 || y1 != 0 || z1 != 0) {
                           int blockX = finalX + x1;
                           if (blockX >= 0 && blockX < 32) {
                              int blockYx = finalY + y1;
                              if (blockYx >= 0 && blockYx < 320) {
                                 int blockZx = finalZ + z1;
                                 if (blockZx >= 0 && blockZx < 32) {
                                    BlockType neighbourBlockType = getBlockType(blockTypeAssetMap, blocks, section, blockX, blockYx, blockZx, false);
                                    if (neighbourBlockType != null && neighbourBlockType.getMaterial() != BlockMaterial.Solid) {
                                       int newFiller = FillerBlockUtil.pack(x1, y1, z1);
                                       chunk.setBlock(blockX, blockYx, blockZx, blockId, blockType, rotation, newFiller, 157);
                                    }
                                 }
                              }
                           }
                        }
                     });
                  }
               }
            }
         }
      }
   }

   @Nullable
   private static BlockType getBlockType(
      @Nonnull BlockTypeAssetMap<String, BlockType> blockTypeAssetMap,
      @Nonnull BlockType[] blocks,
      @Nonnull BlockSection section,
      int blockX,
      int blockY,
      int blockZ,
      boolean skipEmpty
   ) {
      int indexBlock = ChunkUtil.indexBlockInColumn(blockX, blockY, blockZ);
      BlockType blockType = blocks[indexBlock];
      if (blockType == null) {
         int blockId = section.get(blockX, blockY, blockZ);
         if (blockId == 0) {
            blocks[indexBlock] = BlockType.EMPTY;
            return skipEmpty ? null : BlockType.EMPTY;
         } else {
            return blocks[indexBlock] = blockTypeAssetMap.getAsset(blockId);
         }
      } else {
         return skipEmpty && "Empty".equals(blockType.getId()) ? null : blockType;
      }
   }

   public static void breakOrSetFillerBlocks(
      @Nonnull BlockTypeAssetMap<String, BlockType> blockTypeAssetMap,
      @Nonnull IndexedLookupTableAssetMap<String, BlockBoundingBoxes> hitboxAssetMap,
      @Nonnull ChunkAccessor<?> accessor,
      @Nonnull BlockAccessor chunk,
      int finalX,
      int finalY,
      int finalZ,
      @Nonnull BlockType blockType,
      int rotation
   ) {
      int filler = chunk.getFiller(finalX, finalY, finalZ);
      if (filler != 0) {
         if (!isFillerValid(blockTypeAssetMap, accessor, chunk, blockType, filler, finalX, finalY, finalZ)) {
            chunk.breakBlock(finalX, finalY, finalZ, 157);
         } else {
            int originX = finalX - FillerBlockUtil.unpackX(filler);
            int originY = finalY - FillerBlockUtil.unpackY(filler);
            int originZ = finalZ - FillerBlockUtil.unpackZ(filler);
            setFillerBlocks(blockTypeAssetMap, hitboxAssetMap, accessor, chunk, originX, originY, originZ, blockType, rotation);
         }
      } else {
         setFillerBlocks(blockTypeAssetMap, hitboxAssetMap, accessor, chunk, finalX, finalY, finalZ, blockType, rotation);
      }
   }

   @Nullable
   private static BlockType getOriginBlockType(
      @Nonnull BlockTypeAssetMap<String, BlockType> blockTypeAssetMap,
      @Nonnull ChunkAccessor<?> accessor,
      @Nonnull BlockAccessor section,
      int originX,
      int originY,
      int originZ
   ) {
      if (originX >= 0 && originX < 32 && originY >= 0 && originY < 320 && originZ >= 0 && originZ < 32) {
         int originBlockId = section.getBlock(originX, originY, originZ);
         return blockTypeAssetMap.getAsset(originBlockId);
      } else {
         int worldX = (section.getX() << 5) + originX;
         int worldZ = (section.getZ() << 5) + originZ;
         BlockAccessor fillerOriginChunk = accessor.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(worldX, worldZ));
         if (fillerOriginChunk != null) {
            int originBlockId = fillerOriginChunk.getBlock(originX, originY, originZ);
            return blockTypeAssetMap.getAsset(originBlockId);
         } else {
            get()
               .getLogger()
               .at(Level.WARNING)
               .log("Blocking chunk load when trying to get origin block for filler! Origin: %s, %s, %s", originX, originY, originZ);
            fillerOriginChunk = accessor.getNonTickingChunk(ChunkUtil.indexChunkFromBlock(worldX, worldZ));
            int originBlockId = fillerOriginChunk.getBlock(originX, originY, originZ);
            return blockTypeAssetMap.getAsset(originBlockId);
         }
      }
   }

   private static void setFillerBlocks(
      @Nonnull BlockTypeAssetMap<String, BlockType> blockTypeAssetMap,
      @Nonnull IndexedLookupTableAssetMap<String, BlockBoundingBoxes> hitboxAssetMap,
      @Nonnull ChunkAccessor<?> accessor,
      @Nonnull BlockAccessor chunk,
      int finalX,
      int finalY,
      int finalZ,
      @Nonnull BlockType originBlockType,
      int rotation
   ) {
      int originBlockId = blockTypeAssetMap.getIndex(originBlockType.getId());
      FillerBlockUtil.forEachFillerBlock(
         hitboxAssetMap.getAsset(originBlockType.getHitboxTypeIndex()).get(rotation),
         (x1, y1, z1) -> {
            if (x1 != 0 || y1 != 0 || z1 != 0) {
               int blockX = finalX + x1;
               int blockY = finalY + y1;
               int blockZ = finalZ + z1;
               if (blockX >= 0 && blockX < 32 && blockY >= 0 && blockY < 320 && blockZ >= 0 && blockZ < 32) {
                  int blockId = chunk.getBlock(blockX, blockY, blockZ);
                  int currentRotation = chunk.getRotationIndex(blockX, blockY, blockZ);
                  int currentFiller = chunk.getFiller(blockX, blockY, blockZ);
                  BlockType blockType = blockTypeAssetMap.getAsset(blockId);
                  if ((currentFiller == 0 || !isFillerValid(blockTypeAssetMap, accessor, chunk, blockType, currentFiller, blockX, blockY, blockZ))
                     && blockType.getMaterial() != BlockMaterial.Solid) {
                     int filler = FillerBlockUtil.pack(x1, y1, z1);
                     chunk.setBlock(blockX, blockY, blockZ, originBlockId, originBlockType, currentRotation, filler, 157);
                  }
               } else {
                  int worldX = (chunk.getX() << 5) + blockX;
                  int worldZ = (chunk.getZ() << 5) + blockZ;
                  BlockAccessor neighbourChunk = accessor.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(worldX, worldZ));
                  if (neighbourChunk != null) {
                     int blockId = neighbourChunk.getBlock(blockX, blockY, blockZ);
                     int currentRotation = neighbourChunk.getRotationIndex(blockX, blockY, blockZ);
                     int currentFiller = neighbourChunk.getFiller(blockX, blockY, blockZ);
                     BlockType blockType = blockTypeAssetMap.getAsset(blockId);
                     if ((currentFiller == 0 || !isFillerValid(blockTypeAssetMap, accessor, chunk, blockType, currentFiller, blockX, blockY, blockZ))
                        && blockType.getMaterial() != BlockMaterial.Solid) {
                        int filler = FillerBlockUtil.pack(x1, y1, z1);
                        neighbourChunk.setBlock(blockX, blockY, blockZ, originBlockId, originBlockType, currentRotation, filler, 157);
                     }
                  }
               }
            }
         }
      );
   }

   private static boolean isFillerValid(
      @Nonnull BlockTypeAssetMap<String, BlockType> blockTypeAssetMap,
      @Nonnull ChunkAccessor<?> accessor,
      @Nonnull BlockAccessor chunk,
      @Nonnull BlockType blockType,
      int filler,
      int x,
      int y,
      int z
   ) {
      int originX = x - FillerBlockUtil.unpackX(filler);
      int originY = y - FillerBlockUtil.unpackY(filler);
      int originZ = z - FillerBlockUtil.unpackZ(filler);
      BlockType originBlockType = getOriginBlockType(blockTypeAssetMap, accessor, chunk, originX, originY, originZ);
      if (blockType.isUnknown()) {
         return false;
      } else {
         String blockTypeKey = blockType.getId();
         return blockTypeKey.equals(originBlockType.getId());
      }
   }

   @Deprecated
   private static class FixFillerBlocksSystem extends RefSystem<ChunkStore> implements DisableProcessingAssert {
      private static final ComponentType<ChunkStore, WorldChunk> COMPONENT_TYPE = WorldChunk.getComponentType();

      @Override
      public Query<ChunkStore> getQuery() {
         return COMPONENT_TYPE;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         WorldChunk chunk = store.getComponent(ref, COMPONENT_TYPE);
         if (chunk.is(ChunkFlag.NEWLY_GENERATED)) {
            World world = store.getExternalData().getWorld();
            world.execute(() -> fixFillerFor(world, chunk));
         }
      }

      public static void fixFillerFor(@Nonnull World world, @Nonnull WorldChunk chunk) {
         BlockChunk blockChunk = chunk.getBlockChunk();
         BlockTypeAssetMap<String, BlockType> blockTypeAssetMap = BlockType.getAssetMap();
         IndexedLookupTableAssetMap<String, BlockBoundingBoxes> hitboxAssetMap = BlockBoundingBoxes.getAssetMap();
         LocalCachedChunkAccessor accessor = LocalCachedChunkAccessor.atChunk(world, chunk, 1);

         for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
               if (x != 0 || z != 0) {
                  WorldChunk chunkIfInMemory = world.getChunkIfInMemory(ChunkUtil.indexChunk(x + chunk.getX(), z + chunk.getZ()));
                  if (chunkIfInMemory != null) {
                     accessor.overwrite(chunkIfInMemory);
                  }
               }
            }
         }

         for (int sectionIndex = 0; sectionIndex < 10; sectionIndex++) {
            BlockSection section = blockChunk.getSectionAtIndex(sectionIndex);
            boolean skipInsideSection = section.getMaximumHitboxExtent() <= 0.0;
            int sectionYBlock = sectionIndex << 5;

            for (int yInSection = 0; yInSection < 32; yInSection++) {
               int y = sectionYBlock | yInSection;

               for (int x = -1; x < 33; x++) {
                  for (int zx = -1; zx < 33; zx++) {
                     if (x < 1 || x >= 31 || y < 1 || y >= 319 || zx < 1 || zx >= 31) {
                        if (x < 0 || x >= 32 || y < 0 || y >= 320 || zx < 0 || zx >= 32) {
                           int worldX = (chunk.getX() << 5) + x;
                           int worldZ = (chunk.getZ() << 5) + zx;
                           WorldChunk neighbourChunk = accessor.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(worldX, worldZ));
                           if (neighbourChunk != null) {
                              BlockSection neighbourSection = neighbourChunk.getBlockChunk().getSectionAtBlockY(y);
                              if (!(neighbourSection.getMaximumHitboxExtent() <= 0.0)) {
                                 int blockId = neighbourSection.get(x, y, zx);
                                 if (blockId != 0) {
                                    BlockType blockType = blockTypeAssetMap.getAsset(blockId);
                                    int rotation = neighbourSection.getRotationIndex(x, y, zx);
                                    BlockTypeModule.breakOrSetFillerBlocks(blockTypeAssetMap, hitboxAssetMap, accessor, chunk, x, y, zx, blockType, rotation);
                                 }
                              }
                           }
                        } else if (!skipInsideSection) {
                           int blockId = section.get(x, y, zx);
                           if (blockId != 0) {
                              BlockType blockType = blockTypeAssetMap.getAsset(blockId);
                              int rotation = section.getRotationIndex(x, y, zx);
                              BlockTypeModule.breakOrSetFillerBlocks(blockTypeAssetMap, hitboxAssetMap, accessor, chunk, x, y, zx, blockType, rotation);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
      }
   }

   @Deprecated(forRemoval = true)
   private static class MigrateLegacySections extends ChunkColumnMigrationSystem {
      private final Query<ChunkStore> QUERY = Query.and(ChunkColumn.getComponentType(), BlockChunk.getComponentType());
      private final Set<Dependency<ChunkStore>> DEPENDENCIES = Set.of(
         new SystemDependency<>(Order.BEFORE, LegacyModule.MigrateLegacySections.class),
         new SystemDependency<>(Order.AFTER, ChunkSystems.OnNewChunk.class),
         RootDependency.first()
      );

      @Override
      public void onEntityAdd(@Nonnull Holder<ChunkStore> holder, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store) {
         ChunkColumn column = holder.getComponent(ChunkColumn.getComponentType());

         assert column != null;

         BlockChunk blockChunk = holder.getComponent(BlockChunk.getComponentType());

         assert blockChunk != null;

         Holder<ChunkStore>[] sections = column.getSectionHolders();
         BlockSection[] legacySections = blockChunk.getMigratedSections();
         if (legacySections != null) {
            for (int i = 0; i < sections.length; i++) {
               Holder<ChunkStore> section = sections[i];
               BlockSection paletteSection = legacySections[i];
               if (section != null && paletteSection != null) {
                  BlockPhysics phys = paletteSection.takeMigratedDecoBlocks();
                  if (phys != null) {
                     section.putComponent(BlockPhysics.getComponentType(), phys);
                     blockChunk.markNeedsSaving();
                  }
               }
            }
         }
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<ChunkStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store) {
      }

      @Nonnull
      @Override
      public Query<ChunkStore> getQuery() {
         return this.QUERY;
      }

      @Nonnull
      @Override
      public Set<Dependency<ChunkStore>> getDependencies() {
         return this.DEPENDENCIES;
      }
   }
}
