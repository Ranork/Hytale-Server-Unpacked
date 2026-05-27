package com.hypixel.hytale.server.core.prefab.selection.buffer;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.block.BlockUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockMigration;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBufferBlockEntry;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.BsonUtil;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;
import org.joml.Vector3i;

public class BinaryPrefabBufferCodec {
   public static final BinaryPrefabBufferCodec INSTANCE = new BinaryPrefabBufferCodec();
   public static final int VERSION = 21;
   private static final int MASK_CHANCE = 1;
   private static final int MASK_COMPONENTS = 2;
   private static final int MASK_FLUID = 4;
   private static final int MASK_SUPPORT_VALUE = 8;
   private static final int MASK_FILLER = 16;
   private static final int MASK_ROTATION = 32;

   public static void writeUTF(@Nonnull DataOutputStream out, @Nonnull String string) throws IOException {
      byte[] str = string.getBytes(StandardCharsets.UTF_8);
      if (str.length >= 65535) {
         throw new IllegalArgumentException("String is too large");
      } else {
         out.writeShort(str.length);
         out.write(str);
      }
   }

   @Nonnull
   public static String readUTF(@Nonnull ByteBuffer buffer) {
      int length = Short.toUnsignedInt(buffer.getShort());
      byte[] bytes = new byte[length];
      buffer.get(bytes);
      return new String(bytes, StandardCharsets.UTF_8);
   }

   @Nonnull
   public PrefabBuffer deserialize(@Nonnull ByteBuffer buffer) {
      int version = Short.toUnsignedInt(buffer.getShort());
      if (version != 18553 && version >= 21) {
         if (21 < version) {
            throw new IllegalStateException("Prefab version is newer than supported. Given: " + version);
         } else {
            BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
            int blockIdVersion = buffer.getShort();
            long packedAnchor = buffer.getLong();
            Vector3i anchor = new Vector3i(BlockUtil.unpackX(packedAnchor), BlockUtil.unpackY(packedAnchor), BlockUtil.unpackZ(packedAnchor));
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

            int blockNameCount = buffer.getInt();
            Int2ObjectOpenHashMap<BinaryPrefabBufferCodec.BlockIdEntry> blockIdMapping = new Int2ObjectOpenHashMap(blockNameCount);

            for (int i = 0; i < blockNameCount; i++) {
               try {
                  int readId = buffer.getInt();
                  BinaryPrefabBufferCodec.BlockIdEntry block = this.deserializeBlock(buffer, assetMap, blockMigration);
                  blockIdMapping.put(readId, block);
               } catch (Exception var44) {
                  throw new IllegalStateException("Failed to deserialize block name #" + i, var44);
               }
            }

            IndexedLookupTableAssetMap<String, Fluid> fluidMap = Fluid.getAssetMap();
            int fluidNameCount = buffer.getInt();
            Int2ObjectOpenHashMap<BinaryPrefabBufferCodec.FluidIdEntry> fluidIdMapping = new Int2ObjectOpenHashMap(fluidNameCount);

            for (int i = 0; i < fluidNameCount; i++) {
               try {
                  int readId = buffer.getInt();
                  BinaryPrefabBufferCodec.FluidIdEntry fluid = this.deserializeFluid(buffer, fluidMap);
                  fluidIdMapping.put(readId, fluid);
               } catch (Exception var43) {
                  throw new IllegalStateException("Failed to deserialize block name #" + i, var43);
               }
            }

            PrefabBuffer.Builder builder = PrefabBuffer.newBuilder();
            builder.setAnchor(anchor);
            int columnCount = buffer.getInt();

            for (int i = 0; i < columnCount; i++) {
               int columnIndex = buffer.getInt();
               int blocks = buffer.getInt();
               PrefabBufferBlockEntry[] blockEntries = new PrefabBufferBlockEntry[blocks];

               for (int j = 0; j < blocks; j++) {
                  int y = buffer.getShort();
                  int readId = buffer.getInt();
                  BinaryPrefabBufferCodec.BlockIdEntry block = (BinaryPrefabBufferCodec.BlockIdEntry)blockIdMapping.get(readId);
                  int mask = Byte.toUnsignedInt(buffer.get());
                  boolean hasChance = (mask & 1) == 1;
                  boolean hasState = (mask & 2) == 2;
                  boolean hasFluid = (mask & 4) == 4;
                  boolean hasSupportValue = (mask & 8) == 8;
                  boolean hasFiller = (mask & 16) == 16;
                  boolean hasRotation = (mask & 32) == 32;
                  float chance = hasChance ? buffer.getFloat() : 1.0F;
                  Holder<ChunkStore> holder = null;
                  if (hasState) {
                     BsonDocument doc = BsonUtil.readFromBinaryStream(buffer);
                     holder = ChunkStore.REGISTRY.deserialize(doc);
                  }

                  byte supportValue = 0;
                  if (hasSupportValue) {
                     supportValue = (byte)(buffer.get() & 15);
                  }

                  int filler = 0;
                  if (hasFiller) {
                     filler = Short.toUnsignedInt(buffer.getShort());
                  }

                  int rotation = 0;
                  if (hasRotation) {
                     rotation = Byte.toUnsignedInt(buffer.get());
                  }

                  int fluidId = 0;
                  byte fluidLevel = 0;
                  if (hasFluid) {
                     int id = buffer.getInt();
                     fluidId = ((BinaryPrefabBufferCodec.FluidIdEntry)fluidIdMapping.get(id)).id;
                     fluidLevel = buffer.get();
                  }

                  blockEntries[j] = new PrefabBufferBlockEntry(y, block.id, block.key, chance, holder, fluidId, fluidLevel, supportValue, rotation, filler);
               }

               int entityCount = Short.toUnsignedInt(buffer.getShort());
               Holder<EntityStore>[] entityHolders = null;
               if (entityCount > 0) {
                  entityHolders = new Holder[entityCount];

                  for (int j = 0; j < entityCount; j++) {
                     try {
                        BsonDocument entityDocument = BsonUtil.readFromBinaryStream(buffer);
                        Holder<EntityStore> entityHolder = EntityStore.REGISTRY.deserialize(entityDocument);
                        entityHolders[j] = entityHolder;
                     } catch (Exception var42) {
                        throw new IllegalStateException("Failed to deserialize entity wrapper #" + i, var42);
                     }
                  }
               }

               int x = MathUtil.unpackLeft(columnIndex);
               int z = MathUtil.unpackRight(columnIndex);
               builder.addColumn(x, z, blockEntries, entityHolders);
            }

            return builder.build();
         }
      } else {
         throw new UpdateBinaryPrefabException("Old prefab format!");
      }
   }

   @Nonnull
   private BinaryPrefabBufferCodec.BlockIdEntry deserializeBlock(
      @Nonnull ByteBuffer buffer, @Nonnull BlockTypeAssetMap<String, BlockType> assetMap, @Nullable Function<String, String> blockMigration
   ) {
      String blockTypeString = readUTF(buffer);
      String blockTypeKey = blockTypeString;
      if (blockMigration != null) {
         blockTypeKey = blockMigration.apply(blockTypeString);
      }

      int blockId = BlockType.getBlockIdOrUnknown(assetMap, blockTypeKey, "Failed to find block '%s'", blockTypeString);
      return new BinaryPrefabBufferCodec.BlockIdEntry(blockId, blockTypeKey);
   }

   @Nonnull
   private BinaryPrefabBufferCodec.FluidIdEntry deserializeFluid(@Nonnull ByteBuffer buffer, @Nonnull IndexedLookupTableAssetMap<String, Fluid> assetMap) {
      String fluidName = readUTF(buffer);
      int fluidId = Fluid.getFluidIdOrUnknown(assetMap, fluidName, "Failed to find fluid '%s'", fluidName);
      return new BinaryPrefabBufferCodec.FluidIdEntry(fluidId, fluidName);
   }

   public void serialize(@Nonnull PrefabBuffer prefabBuffer, DataOutputStream out) throws IOException {
      PrefabBuffer.PrefabBufferAccessor access = prefabBuffer.newAccess();
      Int2ObjectOpenHashMap<String> blockNameMapping = new Int2ObjectOpenHashMap();
      Int2ObjectOpenHashMap<String> fluidNameMapping = new Int2ObjectOpenHashMap();
      access.forEachRaw((x, z, blocks, o) -> true, (x, y, z, mask, blockId, chance, holder, support, rotation, filler, o) -> {
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
      }, (x, z, entityHolders, o) -> {}, null);
      out.writeShort(21);
      out.writeShort(BlockMigration.getAssetMap().getAssetCount());
      out.writeLong(BlockUtil.pack(prefabBuffer.getAnchorX(), prefabBuffer.getAnchorY(), prefabBuffer.getAnchorZ()));
      out.writeInt(blockNameMapping.size());
      blockNameMapping.int2ObjectEntrySet().fastForEach(SneakyThrow.sneakyConsumer(entry -> {
         out.writeInt(entry.getIntKey());
         writeUTF(out, (String)entry.getValue());
      }));
      out.writeInt(fluidNameMapping.size());
      fluidNameMapping.int2ObjectEntrySet().fastForEach(SneakyThrow.sneakyConsumer(entry -> {
         out.writeInt(entry.getIntKey());
         writeUTF(out, (String)entry.getValue());
      }));
      out.writeInt(access.getColumnCount());
      access.forEachRaw((x, z, blocks, o) -> {
         try {
            out.writeInt(MathUtil.packInt(x, z));
            out.writeInt(blocks);
            return true;
         } catch (IOException var6) {
            throw SneakyThrow.sneakyThrow(var6);
         }
      }, (x, y, z, entryMask, blockId, chance, holder, supportValue, rotation, filler, o) -> {
         try {
            out.writeShort((short)y);
            out.writeInt(blockId);
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

            out.writeByte(mask);
            if (hasChance) {
               out.writeFloat(chance);
            }

            if (hasComponents) {
               try {
                  BsonUtil.writeToBinaryStream(out, ChunkStore.REGISTRY.serialize(holder));
               } catch (Throwable var16) {
                  throw new IllegalStateException(String.format("Exception while writing %d, %d, %d state!", x, y, z), var16);
               }
            }

            if (supportValue != 0) {
               out.writeByte(supportValue);
            }

            if (filler != 0) {
               out.writeShort(filler);
            }

            if (rotation != 0) {
               out.writeByte(rotation);
            }
         } catch (IOException var17) {
            throw SneakyThrow.sneakyThrow(var17);
         }
      }, (x, y, z, fluidId, level, o) -> {
         try {
            out.writeInt(fluidId);
            out.writeByte(level);
         } catch (IOException var8) {
            throw SneakyThrow.sneakyThrow(var8);
         }
      }, (x, z, entityHolders, o) -> {
         try {
            int entities = entityHolders != null ? entityHolders.length : 0;
            out.writeShort(entities);

            for (int i = 0; i < entities; i++) {
               Holder<EntityStore> entityHolder = entityHolders[i];

               try {
                  BsonDocument document = EntityStore.REGISTRY.serialize(entityHolder);
                  BsonUtil.writeToBinaryStream(out, document);
               } catch (Exception var9) {
                  throw new IllegalStateException(String.format("Failed to write EntityWrapper at %d, %d #%d", x, z, i), var9);
               }
            }
         } catch (IOException var10) {
            throw SneakyThrow.sneakyThrow(var10);
         }
      }, null);
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
