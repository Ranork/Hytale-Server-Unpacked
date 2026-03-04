package com.hypixel.hytale.server.core.prefab.selection.buffer;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.block.BlockUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockMigration;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.prefab.config.SelectionPrefabSerializer;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBufferBlockEntry;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.BsonUtil;
import com.hypixel.hytale.server.core.util.io.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;

public class BinaryPrefabBufferCodec implements PrefabBufferCodec<ByteBuf> {
   public static final BinaryPrefabBufferCodec INSTANCE = new BinaryPrefabBufferCodec();
   public static final int VERSION = 21;
   private static final int MASK_CHANCE = 1;
   private static final int MASK_COMPONENTS = 2;
   private static final int MASK_FLUID = 4;
   private static final int MASK_SUPPORT_VALUE = 8;
   private static final int MASK_FILLER = 16;
   private static final int MASK_ROTATION = 32;

   @Nonnull
   public PrefabBuffer deserialize(Path path, @Nonnull ByteBuf buffer) {
      int version = buffer.readUnsignedShort();
      if (version == 18553) {
         throw new UpdateBinaryPrefabException("Old prefab format!");
      } else if (21 < version) {
         throw new IllegalStateException("Prefab version is newer than supported. Given: " + version);
      } else {
         int worldVersion = version < 17 ? buffer.readUnsignedShort() : 0;
         if (version == 11) {
            buffer.readUnsignedShort();
         }

         int entityVersion = version >= 14 && version < 17 ? buffer.readUnsignedShort() : 0;
         BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
         int blockIdVersion = 8;
         if (version >= 13) {
            blockIdVersion = buffer.readShort();
         }

         Vector3i anchor = Vector3i.ZERO;
         if (version >= 16) {
            long packedAnchor = buffer.readLong();
            anchor = new Vector3i(BlockUtil.unpackX(packedAnchor), BlockUtil.unpackY(packedAnchor), BlockUtil.unpackZ(packedAnchor));
         }

         Function<String, String> blockMigration = null;
         Map<Integer, BlockMigration> blockMigrationMap = BlockMigration.getAssetMap().getAssetMap();
         int v = blockIdVersion;

         for (BlockMigration migration = blockMigrationMap.get(blockIdVersion); migration != null; migration = blockMigrationMap.get(++v)) {
            if (blockMigration == null) {
               blockMigration = migration::getMigration;
            } else {
               blockMigration = blockMigration.andThen(migration::getMigration);
            }
         }

         int blockNameCount = buffer.readInt();
         Int2ObjectOpenHashMap<BinaryPrefabBufferCodec.BlockIdEntry> blockIdMapping = new Int2ObjectOpenHashMap(blockNameCount);

         for (int i = 0; i < blockNameCount; i++) {
            try {
               int readId = buffer.readInt();
               BinaryPrefabBufferCodec.BlockIdEntry block = this.deserializeBlock(buffer, assetMap, blockMigration);
               blockIdMapping.put(readId, block);
            } catch (Exception var44) {
               throw new IllegalStateException("Failed to deserialize block name #" + i, var44);
            }
         }

         IndexedLookupTableAssetMap<String, Fluid> fluidMap = Fluid.getAssetMap();
         int fluidNameCount = version >= 18 ? buffer.readInt() : 0;
         Int2ObjectOpenHashMap<BinaryPrefabBufferCodec.FluidIdEntry> fluidIdMapping = new Int2ObjectOpenHashMap(fluidNameCount);

         for (int i = 0; i < fluidNameCount; i++) {
            try {
               int readId = buffer.readInt();
               BinaryPrefabBufferCodec.FluidIdEntry fluid = this.deserializeFluid(buffer, fluidMap);
               fluidIdMapping.put(readId, fluid);
            } catch (Exception var43) {
               throw new IllegalStateException("Failed to deserialize block name #" + i, var43);
            }
         }

         PrefabBuffer.Builder builder = PrefabBuffer.newBuilder();
         builder.setAnchor(anchor);
         int columnCount = buffer.readInt();

         for (int i = 0; i < columnCount; i++) {
            int columnIndex = buffer.readInt();
            int blocks = buffer.readInt();
            PrefabBufferBlockEntry[] blockEntries = new PrefabBufferBlockEntry[blocks];

            for (int j = 0; j < blocks; j++) {
               int y = buffer.readShort();
               int readId = buffer.readInt();
               BinaryPrefabBufferCodec.BlockIdEntry block = (BinaryPrefabBufferCodec.BlockIdEntry)blockIdMapping.get(readId);
               int mask = buffer.readUnsignedByte();
               boolean hasChance = (mask & 1) == 1;
               boolean hasState = (mask & 2) == 2;
               boolean hasFluid = (mask & 4) == 4;
               boolean hasSupportValue = (mask & 8) == 8;
               boolean hasFiller = (mask & 16) == 16;
               boolean hasRotation = (mask & 32) == 32;
               float chance = hasChance ? buffer.readFloat() : 1.0F;
               Holder<ChunkStore> holder = null;
               if (hasState) {
                  BsonDocument doc = BsonUtil.readFromBinaryStream(buffer);
                  if (version < 15) {
                     holder = SelectionPrefabSerializer.legacyStateDecode(doc);
                  } else if (version < 17) {
                     holder = ChunkStore.REGISTRY.deserialize(doc, worldVersion);
                  } else {
                     holder = ChunkStore.REGISTRY.deserialize(doc);
                  }
               }

               byte supportValue = 0;
               if (hasSupportValue) {
                  supportValue = (byte)(buffer.readByte() & 15);
               }

               int filler = 0;
               if (hasFiller) {
                  filler = buffer.readUnsignedShort();
               }

               int rotation = 0;
               if (hasRotation) {
                  rotation = buffer.readUnsignedByte();
               }

               int fluidId = 0;
               byte fluidLevel = 0;
               if (hasFluid) {
                  int id = buffer.readInt();
                  fluidId = ((BinaryPrefabBufferCodec.FluidIdEntry)fluidIdMapping.get(id)).id;
                  fluidLevel = buffer.readByte();
               }

               blockEntries[j] = new PrefabBufferBlockEntry(y, block.id, block.key, chance, holder, fluidId, fluidLevel, supportValue, rotation, filler);
            }

            int entityCount = buffer.readUnsignedShort();
            Holder<EntityStore>[] entityHolders = null;
            if (entityCount > 0) {
               entityHolders = new Holder[entityCount];

               for (int j = 0; j < entityCount; j++) {
                  try {
                     if (version >= 12 && version < 14) {
                        entityVersion = buffer.readUnsignedShort();
                     }

                     BsonDocument entityDocument = BsonUtil.readFromBinaryStream(buffer);
                     Holder<EntityStore> entityHolder;
                     if (version < 14) {
                        entityHolder = SelectionPrefabSerializer.legacyEntityDecode(entityDocument, entityVersion);
                     } else if (version < 17) {
                        entityHolder = EntityStore.REGISTRY.deserialize(entityDocument, entityVersion);
                     } else {
                        entityHolder = EntityStore.REGISTRY.deserialize(entityDocument);
                     }

                     entityHolders[j] = entityHolder;
                  } catch (Exception var45) {
                     throw new IllegalStateException("Failed to deserialize entity wrapper #" + i, var45);
                  }
               }
            }

            int x = MathUtil.unpackLeft(columnIndex);
            int z = MathUtil.unpackRight(columnIndex);
            builder.addColumn(x, z, blockEntries, entityHolders);
         }

         return builder.build();
      }
   }

   @Nonnull
   private BinaryPrefabBufferCodec.BlockIdEntry deserializeBlock(
      @Nonnull ByteBuf buffer, @Nonnull BlockTypeAssetMap<String, BlockType> assetMap, @Nullable Function<String, String> blockMigration
   ) {
      String blockTypeString = ByteBufUtil.readUTF(buffer);
      String blockTypeKey = blockTypeString;
      if (blockMigration != null) {
         blockTypeKey = blockMigration.apply(blockTypeString);
      }

      int blockId = BlockType.getBlockIdOrUnknown(assetMap, blockTypeKey, "Failed to find block '%s'", blockTypeString);
      return new BinaryPrefabBufferCodec.BlockIdEntry(blockId, blockTypeKey);
   }

   @Nonnull
   private BinaryPrefabBufferCodec.FluidIdEntry deserializeFluid(@Nonnull ByteBuf buffer, @Nonnull IndexedLookupTableAssetMap<String, Fluid> assetMap) {
      String fluidName = ByteBufUtil.readUTF(buffer);
      int fluidId = Fluid.getFluidIdOrUnknown(assetMap, fluidName, "Failed to find fluid '%s'", fluidName);
      return new BinaryPrefabBufferCodec.FluidIdEntry(fluidId, fluidName);
   }

   @Nonnull
   public ByteBuf serialize(@Nonnull PrefabBuffer prefabBuffer) {
      PrefabBuffer.PrefabBufferAccessor access = prefabBuffer.newAccess();
      Int2ObjectOpenHashMap<String> blockNameMapping = new Int2ObjectOpenHashMap();
      Int2ObjectOpenHashMap<String> fluidNameMapping = new Int2ObjectOpenHashMap();
      int[] counts = new int[3];
      access.forEachRaw((x, z, blocks, o) -> {
         counts[0]++;
         counts[1] += blocks;
         return true;
      }, (x, y, z, mask, blockId, chance, holder, support, rotation, filler, o) -> {
         if (!blockNameMapping.containsKey(blockId)) {
            BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
            BlockType blockType = assetMap.getAsset(blockId);
            if (blockType == null) {
               blockType = BlockType.UNKNOWN;
            }

            blockNameMapping.put(blockId, blockType.getId().toString());
         }
      }, (x, y, z, fluidId, level, o) -> {
         if (!fluidNameMapping.containsKey(fluidId)) {
            IndexedLookupTableAssetMap<String, Fluid> assetMap = Fluid.getAssetMap();
            Fluid fluidType = assetMap.getAsset(fluidId);
            if (fluidType == null) {
               fluidType = Fluid.UNKNOWN;
            }

            fluidNameMapping.put(fluidId, fluidType.getId());
         }
      }, (x, z, entityHolders, o) -> {
         if (entityHolders != null) {
            counts[2] += entityHolders.length;
         }
      }, null);
      ByteBuf buffer = Unpooled.buffer(4 + blockNameMapping.size() * 261 + counts[0] * 8 + counts[1] * 13 + counts[2] * 2048);
      buffer.writeShort(21);
      buffer.writeShort(BlockMigration.getAssetMap().getAssetCount());
      buffer.writeLong(BlockUtil.pack(prefabBuffer.getAnchorX(), prefabBuffer.getAnchorY(), prefabBuffer.getAnchorZ()));
      buffer.writeInt(blockNameMapping.size());
      blockNameMapping.int2ObjectEntrySet().fastForEach(entry -> {
         buffer.writeInt(entry.getIntKey());
         ByteBufUtil.writeUTF(buffer, (String)entry.getValue());
      });
      buffer.writeInt(fluidNameMapping.size());
      fluidNameMapping.int2ObjectEntrySet().fastForEach(entry -> {
         buffer.writeInt(entry.getIntKey());
         ByteBufUtil.writeUTF(buffer, (String)entry.getValue());
      });
      buffer.writeInt(access.getColumnCount());
      access.forEachRaw((x, z, blocks, o) -> {
         buffer.writeInt(MathUtil.packInt(x, z));
         buffer.writeInt(blocks);
         return true;
      }, (x, y, z, entryMask, blockId, chance, holder, supportValue, rotation, filler, o) -> {
         buffer.writeShort((short)y);
         buffer.writeInt(blockId);
         boolean hasChance = chance < 1.0F;
         boolean hasComponents = holder != null;
         int mask = 0;
         if (hasChance) {
            mask |= 1;
         }

         if (hasComponents) {
            mask |= 2;
         }

         if ((entryMask & 192) != 0) {
            mask |= 4;
         }

         if (supportValue != 0) {
            mask |= 8;
         }

         if (filler != 0) {
            mask |= 16;
         }

         if (rotation != 0) {
            mask |= 32;
         }

         buffer.writeByte(mask);
         if (hasChance) {
            buffer.writeFloat(chance);
         }

         if (hasComponents) {
            try {
               BsonUtil.writeToBinaryStream(buffer, ChunkStore.REGISTRY.serialize(holder));
            } catch (Throwable var16) {
               throw new IllegalStateException(String.format("Exception while writing %d, %d, %d state!", x, y, z), var16);
            }
         }

         if (supportValue != 0) {
            buffer.writeByte(supportValue);
         }

         if (filler != 0) {
            buffer.writeShort(filler);
         }

         if (rotation != 0) {
            buffer.writeByte(rotation);
         }
      }, (x, y, z, fluidId, level, o) -> {
         buffer.writeInt(fluidId);
         buffer.writeByte(level);
      }, (x, z, entityHolders, o) -> {
         int entities = entityHolders != null ? entityHolders.length : 0;
         buffer.writeShort(entities);

         for (int i = 0; i < entities; i++) {
            Holder<EntityStore> entityHolder = entityHolders[i];

            try {
               BsonDocument document = EntityStore.REGISTRY.serialize(entityHolder);
               BsonUtil.writeToBinaryStream(buffer, document);
            } catch (Exception var9) {
               throw new IllegalStateException(String.format("Failed to write EntityWrapper at %d, %d #%d", x, z, i), var9);
            }
         }
      }, null);
      return buffer;
   }

   private static class BlockIdEntry {
      public int id;
      public String key;

      public BlockIdEntry(int id, String key) {
         this.id = id;
         this.key = key;
      }
   }

   private static class FluidIdEntry {
      public int id;
      public String key;

      public FluidIdEntry(int id, String key) {
         this.id = id;
         this.key = key;
      }
   }
}
