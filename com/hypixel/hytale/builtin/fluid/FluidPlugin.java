package com.hypixel.hytale.builtin.fluid;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.DefaultFluidTicker;
import com.hypixel.hytale.server.core.asset.type.fluid.FiniteFluidTicker;
import com.hypixel.hytale.server.core.asset.type.fluid.FireFluidTicker;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.asset.type.fluid.FluidTicker;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.events.ChunkPreLoadProcessEvent;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.time.Instant;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidPlugin extends JavaPlugin {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static FluidPlugin instance;

   public static FluidPlugin get() {
      return instance;
   }

   public FluidPlugin(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      ComponentRegistryProxy<ChunkStore> chunkStoreRegistry = this.getChunkStoreRegistry();
      FluidTicker.CODEC.register(Priority.DEFAULT, "Default", DefaultFluidTicker.class, DefaultFluidTicker.CODEC);
      FluidTicker.CODEC.register("Fire", FireFluidTicker.class, FireFluidTicker.CODEC);
      FluidTicker.CODEC.register("Finite", FiniteFluidTicker.class, FiniteFluidTicker.CODEC);
      ComponentType<ChunkStore, ChunkSection> chunkSectionComponentType = ChunkSection.getComponentType();
      ComponentType<ChunkStore, FluidSection> fluidSectionComponentType = FluidSection.getComponentType();
      ComponentType<ChunkStore, ChunkColumn> chunkColumnComponentType = ChunkColumn.getComponentType();
      ComponentType<ChunkStore, BlockChunk> blockChunkComponentType = BlockChunk.getComponentType();
      ComponentType<ChunkStore, WorldChunk> worldChunkComponentType = WorldChunk.getComponentType();
      chunkStoreRegistry.registerSystem(new FluidSystems.EnsureFluidSection(chunkSectionComponentType, fluidSectionComponentType));
      chunkStoreRegistry.registerSystem(new FluidSystems.MigrateFromColumn(chunkColumnComponentType, blockChunkComponentType, fluidSectionComponentType));
      chunkStoreRegistry.registerSystem(new FluidSystems.SetupSection(chunkSectionComponentType, fluidSectionComponentType));
      chunkStoreRegistry.registerSystem(new FluidSystems.LoadPacketGenerator(chunkColumnComponentType, fluidSectionComponentType));
      chunkStoreRegistry.registerSystem(new FluidSystems.ReplicateChanges(chunkSectionComponentType, fluidSectionComponentType, worldChunkComponentType));
      chunkStoreRegistry.registerSystem(new FluidSystems.Ticking(chunkSectionComponentType, fluidSectionComponentType, blockChunkComponentType));
      this.getEventRegistry().registerGlobal(EventPriority.FIRST, ChunkPreLoadProcessEvent.class, FluidPlugin::onChunkPreProcess);
      this.getCommandRegistry().registerCommand(new FluidCommand());
   }

   private static void onChunkPreProcess(@Nonnull ChunkPreLoadProcessEvent event) {
      if (event.isNewlyGenerated()) {
         WorldChunk worldChunk = event.getChunk();
         Holder<ChunkStore> holder = event.getHolder();
         ChunkColumn chunkColumnComponent = holder.getComponent(ChunkColumn.getComponentType());
         if (chunkColumnComponent != null) {
            BlockChunk blockChunkComponent = holder.getComponent(BlockChunk.getComponentType());
            if (blockChunkComponent != null) {
               IndexedLookupTableAssetMap<String, Fluid> fluidMap = Fluid.getAssetMap();
               BlockTypeAssetMap<String, BlockType> blockMap = BlockType.getAssetMap();
               Holder<ChunkStore>[] sections = chunkColumnComponent.getSectionHolders();
               if (sections != null) {
                  for (int i = 0; i < sections.length && i < 10; i++) {
                     Holder<ChunkStore> section = sections[i];
                     FluidSection fluidSectionComponent = section.getComponent(FluidSection.getComponentType());
                     if (fluidSectionComponent != null && !fluidSectionComponent.isEmpty()) {
                        BlockSection blockSectionComponent = section.ensureAndGetComponent(BlockSection.getComponentType());

                        for (int idx = 0; idx < 32768; idx++) {
                           int fluidId = fluidSectionComponent.getFluidId(idx);
                           if (fluidId != 0) {
                              Fluid fluidType = fluidMap.getAsset(fluidId);
                              if (fluidType == null) {
                                 LOGGER.at(Level.WARNING)
                                    .log(
                                       "Invalid fluid found in chunk section: %d, %d %d with id %d",
                                       fluidSectionComponent.getX(),
                                       fluidSectionComponent.getY(),
                                       fluidSectionComponent.getZ(),
                                       fluidSectionComponent
                                    );
                                 fluidSectionComponent.setFluid(idx, 0, (byte)0);
                              } else {
                                 FluidTicker ticker = fluidType.getTicker();
                                 if (FluidTicker.isSolid(blockMap.getAsset(blockSectionComponent.get(idx)))) {
                                    fluidSectionComponent.setFluid(idx, 0, (byte)0);
                                 } else {
                                    if (!ticker.canDemote()) {
                                       int x = ChunkUtil.minBlock(fluidSectionComponent.getX()) + ChunkUtil.xFromIndex(idx);
                                       int y = ChunkUtil.minBlock(fluidSectionComponent.getY()) + ChunkUtil.yFromIndex(idx);
                                       int z = ChunkUtil.minBlock(fluidSectionComponent.getZ()) + ChunkUtil.zFromIndex(idx);
                                       boolean canSpread = ChunkUtil.isBorderBlock(x, z)
                                          || fluidSectionComponent.getFluidId(x - 1, y, z) != fluidId
                                             && !FluidTicker.isSolid(blockMap.getAsset(blockSectionComponent.get(x - 1, y, z)))
                                          || fluidSectionComponent.getFluidId(x + 1, y, z) != fluidId
                                             && !FluidTicker.isSolid(blockMap.getAsset(blockSectionComponent.get(x + 1, y, z)))
                                          || fluidSectionComponent.getFluidId(x, y, z - 1) != fluidId
                                             && !FluidTicker.isSolid(blockMap.getAsset(blockSectionComponent.get(x, y, z - 1)))
                                          || fluidSectionComponent.getFluidId(x, y, z + 1) != fluidId
                                             && !FluidTicker.isSolid(blockMap.getAsset(blockSectionComponent.get(x, y, z + 1)));
                                       if (y > 0) {
                                          if (ChunkUtil.chunkCoordinate(y) == ChunkUtil.chunkCoordinate(y - 1)) {
                                             canSpread |= fluidSectionComponent.getFluidId(x, y - 1, z) != fluidId
                                                && !FluidTicker.isSolid(blockMap.getAsset(blockSectionComponent.get(x, y - 1, z)));
                                          } else {
                                             FluidSection fluidSection2 = sections[i - 1].getComponent(FluidSection.getComponentType());
                                             canSpread |= fluidSection2.getFluidId(x, y - 1, z) != fluidId
                                                && !FluidTicker.isSolid(blockMap.getAsset(blockChunkComponent.getBlock(x, y - 1, z)));
                                          }
                                       }

                                       if (!canSpread) {
                                          blockSectionComponent.setTicking(idx, false);
                                          continue;
                                       }
                                    }

                                    blockSectionComponent.setTicking(idx, true);
                                 }
                              }
                           }
                        }
                     }
                  }

                  int tickingBlocks = blockChunkComponent.getTickingBlocksCount();
                  if (tickingBlocks != 0) {
                     FluidPlugin.PreprocesorAccessor accessor = new FluidPlugin.PreprocesorAccessor(worldChunk, blockChunkComponent, sections);

                     do {
                        blockChunkComponent.preTick(Instant.MIN);

                        for (int ix = 0; ix < sections.length; ix++) {
                           Holder<ChunkStore> section = sections[ix];
                           FluidSection fluidSectionComponent = section.getComponent(FluidSection.getComponentType());
                           if (fluidSectionComponent != null && !fluidSectionComponent.isEmpty()) {
                              BlockSection blockSectionComponent = section.ensureAndGetComponent(BlockSection.getComponentType());
                              fluidSectionComponent.preload(worldChunk.getX(), ix, worldChunk.getZ());
                              accessor.blockSection = blockSectionComponent;
                              blockSectionComponent.forEachTicking(
                                 accessor,
                                 fluidSectionComponent,
                                 ix,
                                 (preprocesorAccessor, fluidSection1, xx, yx, zx, block) -> {
                                    int fluidId = fluidSection1.getFluidId(xx, yx, zx);
                                    if (fluidId == 0) {
                                       return BlockTickStrategy.IGNORED;
                                    } else {
                                       int blockX = fluidSection1.getX() << 5 | xx;
                                       int blockZ = fluidSection1.getZ() << 5 | zx;
                                       Fluid fluid = Fluid.getAssetMap().getAsset(fluidId);
                                       return fluid.getTicker()
                                          .process(
                                             preprocesorAccessor.worldChunk.getWorld(),
                                             preprocesorAccessor.tick,
                                             preprocesorAccessor,
                                             fluidSection1,
                                             accessor.blockSection,
                                             fluid,
                                             fluidId,
                                             blockX,
                                             yx,
                                             blockZ
                                          );
                                    }
                                 }
                              );
                           }
                        }

                        tickingBlocks = blockChunkComponent.getTickingBlocksCount();
                        accessor.tick++;
                     } while (tickingBlocks != 0 && accessor.tick <= 100L);

                     blockChunkComponent.mergeTickingBlocks();
                  }
               }
            }
         }
      }
   }

   public static class PreprocesorAccessor implements FluidTicker.Accessor {
      @Nonnull
      private final WorldChunk worldChunk;
      @Nonnull
      private final BlockChunk blockChunk;
      @Nonnull
      private final Holder<ChunkStore>[] sections;
      public long tick;
      public BlockSection blockSection;

      public PreprocesorAccessor(@Nonnull WorldChunk worldChunk, @Nonnull BlockChunk blockChunk, @Nonnull Holder<ChunkStore>[] sections) {
         this.worldChunk = worldChunk;
         this.blockChunk = blockChunk;
         this.sections = sections;
      }

      @Nullable
      @Override
      public FluidSection getFluidSection(int cx, int cy, int cz) {
         return this.blockChunk.getX() == cx && this.blockChunk.getZ() == cz && cy >= 0 && cy < this.sections.length
            ? this.sections[cy].getComponent(FluidSection.getComponentType())
            : null;
      }

      @Nullable
      @Override
      public BlockSection getBlockSection(int cx, int cy, int cz) {
         if (cy >= 0 && cy < 10) {
            return this.blockChunk.getX() == cx && this.blockChunk.getZ() == cz ? this.blockChunk.getSectionAtIndex(cy) : null;
         } else {
            return null;
         }
      }

      @Override
      public void setBlock(int x, int y, int z, int blockId) {
         if (this.worldChunk.getX() == ChunkUtil.chunkCoordinate(x) || this.worldChunk.getZ() == ChunkUtil.chunkCoordinate(z)) {
            this.worldChunk.setBlock(x, y, z, blockId, 157);
         }
      }
   }
}
