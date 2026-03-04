package com.hypixel.hytale.builtin.blockspawner;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.blockphysics.PrefabBufferValidator;
import com.hypixel.hytale.builtin.blockspawner.command.BlockSpawnerCommand;
import com.hypixel.hytale.builtin.blockspawner.state.BlockSpawner;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.data.unknown.UnknownComponents;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.VariantRotation;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockRotationUtil;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockSpawnerPlugin extends JavaPlugin {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private ComponentType<ChunkStore, BlockSpawner> blockSpawnerComponentType;
   private static BlockSpawnerPlugin INSTANCE;

   public static BlockSpawnerPlugin get() {
      return INSTANCE;
   }

   public BlockSpawnerPlugin(@Nonnull JavaPluginInit init) {
      super(init);
      INSTANCE = this;
   }

   @Override
   protected void setup() {
      this.getCommandRegistry().registerCommand(new BlockSpawnerCommand());
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                           BlockSpawnerTable.class, new DefaultAssetMap()
                        )
                        .setPath("Item/Block/Spawners"))
                     .setCodec(BlockSpawnerTable.CODEC))
                  .setKeyFunction(BlockSpawnerTable::getId))
               .loadsAfter(Item.class, BlockType.class))
            .build()
      );
      this.blockSpawnerComponentType = this.getChunkStoreRegistry().registerComponent(BlockSpawner.class, "BlockSpawner", BlockSpawner.CODEC);
      this.getChunkStoreRegistry().registerSystem(new BlockSpawnerPlugin.BlockSpawnerSystem());
      this.getChunkStoreRegistry().registerSystem(new BlockSpawnerPlugin.MigrateBlockSpawner());
      this.getEventRegistry().registerGlobal(PrefabBufferValidator.ValidateBlockEvent.class, BlockSpawnerPlugin::validatePrefabBlock);
   }

   private static void validatePrefabBlock(@Nonnull PrefabBufferValidator.ValidateBlockEvent validateBlockEvent) {
      Holder<ChunkStore> holder = validateBlockEvent.holder();
      if (holder != null) {
         BlockSpawner blockSpawnerComponent = holder.getComponent(BlockSpawner.getComponentType());
         if (blockSpawnerComponent != null) {
            BlockType blockType = BlockType.getAssetMap().getAsset(validateBlockEvent.blockId());
            if (blockType != null) {
               if (blockSpawnerComponent.getBlockSpawnerId() == null) {
                  validateBlockEvent.reason()
                     .append("\t Block ")
                     .append(blockType.getId())
                     .append(" at ")
                     .append(validateBlockEvent.x())
                     .append(", ")
                     .append(validateBlockEvent.y())
                     .append(", ")
                     .append(validateBlockEvent.z())
                     .append(" has no defined block spawner id")
                     .append('\n');
               } else {
                  BlockSpawnerTable blockSpawner = BlockSpawnerTable.getAssetMap().getAsset(blockSpawnerComponent.getBlockSpawnerId());
                  if (blockSpawner == null) {
                     validateBlockEvent.reason()
                        .append("\t Block ")
                        .append(blockType.getId())
                        .append(" at ")
                        .append(validateBlockEvent.x())
                        .append(", ")
                        .append(validateBlockEvent.y())
                        .append(", ")
                        .append(validateBlockEvent.z())
                        .append(" has an invalid spawner id ")
                        .append(blockSpawnerComponent.getBlockSpawnerId())
                        .append('\n');
                  }
               }
            }
         }
      }
   }

   public ComponentType<ChunkStore, BlockSpawner> getBlockSpawnerComponentType() {
      return this.blockSpawnerComponentType;
   }

   private static class BlockSpawnerSystem extends RefSystem<ChunkStore> {
      @Nonnull
      private static final ComponentType<ChunkStore, BlockSpawner> COMPONENT_TYPE = BlockSpawner.getComponentType();
      @Nonnull
      private static final ComponentType<ChunkStore, BlockModule.BlockStateInfo> BLOCK_INFO_COMPONENT_TYPE = BlockModule.BlockStateInfo.getComponentType();
      @Nonnull
      private static final Query<ChunkStore> QUERY = Query.and(COMPONENT_TYPE, BLOCK_INFO_COMPONENT_TYPE);

      public BlockSpawnerSystem() {
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return QUERY;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         WorldConfig worldConfig = store.getExternalData().getWorld().getWorldConfig();
         if (worldConfig.getGameMode() != GameMode.Creative) {
            BlockSpawner blockSpawnerComponent = commandBuffer.getComponent(ref, COMPONENT_TYPE);

            assert blockSpawnerComponent != null;

            BlockModule.BlockStateInfo blockStateInfoComponent = commandBuffer.getComponent(ref, BLOCK_INFO_COMPONENT_TYPE);

            assert blockStateInfoComponent != null;

            String blockSpawnerId = blockSpawnerComponent.getBlockSpawnerId();
            if (blockSpawnerId != null) {
               BlockSpawnerTable table = BlockSpawnerTable.getAssetMap().getAsset(blockSpawnerId);
               if (table == null) {
                  BlockSpawnerPlugin.LOGGER.at(Level.WARNING).log("Failed to find BlockSpawner Asset by name: %s", blockSpawnerId);
               } else {
                  Ref<ChunkStore> chunkRef = blockStateInfoComponent.getChunkRef();
                  if (chunkRef.isValid()) {
                     WorldChunk worldChunkComponent = commandBuffer.getComponent(chunkRef, WorldChunk.getComponentType());
                     if (worldChunkComponent != null) {
                        int x = ChunkUtil.worldCoordFromLocalCoord(worldChunkComponent.getX(), ChunkUtil.xFromBlockInColumn(blockStateInfoComponent.getIndex()));
                        int y = ChunkUtil.yFromBlockInColumn(blockStateInfoComponent.getIndex());
                        int z = ChunkUtil.worldCoordFromLocalCoord(worldChunkComponent.getZ(), ChunkUtil.zFromBlockInColumn(blockStateInfoComponent.getIndex()));
                        long seed = worldConfig.getSeed();
                        double randomRnd = HashUtil.random(x, y, z, seed + -1699164769L);
                        BlockSpawnerEntry entry = table.getEntries().get(randomRnd);
                        if (entry != null) {
                           String blockKey = entry.getBlockName();

                           RotationTuple rotation = switch (entry.getRotationMode()) {
                              case NONE -> RotationTuple.NONE;
                              case RANDOM -> {
                                 String key = entry.getBlockName();
                                 VariantRotation variantRotation = BlockType.getAssetMap().getAsset(key).getVariantRotation();
                                 if (variantRotation == VariantRotation.None) {
                                    yield RotationTuple.NONE;
                                 } else {
                                    int randomHash = (int)HashUtil.rehash(x, y, z, seed + -1699164769L);
                                    Rotation rotationYaw = Rotation.NORMAL[(randomHash & 65535) % Rotation.NORMAL.length];
                                    yield BlockRotationUtil.getRotated(RotationTuple.NONE, Axis.Y, rotationYaw, variantRotation);
                                 }
                              }
                              case INHERIT -> {
                                 String key = entry.getBlockName();
                                 VariantRotation variantRotation = BlockType.getAssetMap().getAsset(key).getVariantRotation();
                                 if (variantRotation == VariantRotation.None) {
                                    yield RotationTuple.NONE;
                                 } else {
                                    RotationTuple spawnerRotation = RotationTuple.get(worldChunkComponent.getRotationIndex(x, y, z));
                                    Rotation spawnerYaw = spawnerRotation.yaw();
                                    yield BlockRotationUtil.getRotated(RotationTuple.NONE, Axis.Y, spawnerYaw, variantRotation);
                                 }
                              }
                           };
                           Holder<ChunkStore> holder = entry.getBlockComponents();
                           commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
                           commandBuffer.run(_store -> {
                              int flags = 4;
                              if (holder != null) {
                                 flags |= 2;
                              }

                              int blockId = BlockType.getAssetMap().getIndex(blockKey);
                              BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
                              worldChunkComponent.setBlock(x, y, z, blockId, blockType, rotation.index(), 0, flags);
                              if (holder != null) {
                                 worldChunkComponent.setState(x, y, z, holder.clone());
                              }
                           });
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
   public static class MigrateBlockSpawner extends BlockModule.MigrationSystem {
      @Override
      public void onEntityAdd(@Nonnull Holder<ChunkStore> holder, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store) {
         UnknownComponents<ChunkStore> unknown = holder.getComponent(ChunkStore.REGISTRY.getUnknownComponentType());

         assert unknown != null;

         BlockSpawner blockSpawner = unknown.removeComponent("blockspawner", BlockSpawner.CODEC);
         if (blockSpawner != null) {
            holder.putComponent(BlockSpawner.getComponentType(), blockSpawner);
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
}
