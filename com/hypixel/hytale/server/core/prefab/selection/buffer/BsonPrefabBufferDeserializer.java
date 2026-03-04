package com.hypixel.hytale.server.core.prefab.selection.buffer;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockMigration;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.prefab.config.SelectionPrefabSerializer;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBufferBlockEntry;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonValue;

public class BsonPrefabBufferDeserializer implements PrefabBufferDeserializer<BsonDocument> {
   public static final BsonPrefabBufferDeserializer INSTANCE = new BsonPrefabBufferDeserializer();
   public static final BsonInt32 LEGACY_BLOCK_ID_VERSION = new BsonInt32(8);
   private static final BsonInt32 DEFAULT_SUPPORT_VALUE = new BsonInt32(0);
   private static final BsonInt32 DEFAULT_FILLER_VALUE = new BsonInt32(0);
   private static final BsonInt32 DEFAULT_ROTATION_VALUE = new BsonInt32(0);

   @Nonnull
   public PrefabBuffer deserialize(Path path, @Nonnull BsonDocument document) {
      BsonValue versionValue = document.get("version");
      int version = versionValue != null ? versionValue.asInt32().getValue() : -1;
      if (version > 8) {
         throw new IllegalArgumentException("Prefab version is too new: " + version + " by expected 8");
      } else {
         int worldVersion = version < 4 ? SelectionPrefabSerializer.readWorldVersion(document) : 0;
         BsonValue entityVersionValue = document.get("entityVersion");
         int entityVersion = entityVersionValue != null ? entityVersionValue.asInt32().getValue() : 0;
         if (version < 1) {
            throw new IllegalArgumentException("Prefab version " + version + " is no longer supported. Please re-save the prefab.");
         } else {
            Vector3i anchor = new Vector3i();
            anchor.x = document.getInt32("anchorX").getValue();
            anchor.y = document.getInt32("anchorY").getValue();
            anchor.z = document.getInt32("anchorZ").getValue();
            int blockIdVersion = document.getInt32("blockIdVersion", LEGACY_BLOCK_ID_VERSION).getValue();
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

            Int2ObjectOpenHashMap<Int2ObjectMap<PrefabBufferBlockEntry>> columnMap = new Int2ObjectOpenHashMap();
            PrefabBuffer.Builder builder = PrefabBuffer.newBuilder();
            builder.setAnchor(anchor);
            BsonValue blocksValue = document.get("blocks");
            if (blocksValue != null) {
               BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();

               for (BsonValue blockValue : blocksValue.asArray()) {
                  BsonDocument blockDocument = blockValue.asDocument();
                  int realX = blockDocument.getInt32("x").getValue();
                  int realY = blockDocument.getInt32("y").getValue();
                  int realZ = blockDocument.getInt32("z").getValue();
                  int x = realX - anchor.x;
                  int y = realY - anchor.y;
                  int z = realZ - anchor.z;
                  if (-32768 > x || x > 32767) {
                     throw new IllegalArgumentException("Violation X: Short.MIN_VALUE < " + x + " < Short.MAX_VALUE");
                  }

                  if (-32768 > y || y > 32767) {
                     throw new IllegalArgumentException("Violation Y: Short.MIN_VALUE < " + y + " < Short.MAX_VALUE");
                  }

                  if (-32768 > z || z > 32767) {
                     throw new IllegalArgumentException("Violation Z: Short.MIN_VALUE < " + z + " < Short.MAX_VALUE");
                  }

                  PrefabBufferBlockEntry blockEntry = builder.newBlockEntry(y);

                  try {
                     deserializeBlockType(blockEntry, blockDocument, assetMap, blockMigration);
                  } catch (Throwable var33) {
                     throw new IllegalStateException("Failed to load block type for " + path + " at " + realX + ", " + realY + ", " + realZ, var33);
                  }

                  deserializeState(blockEntry, blockDocument, version, worldVersion);
                  blockEntry.supportValue = (byte)blockDocument.getInt32("support", DEFAULT_SUPPORT_VALUE).getValue();
                  blockEntry.filler = blockDocument.getInt32("filler", DEFAULT_FILLER_VALUE).getValue();
                  blockEntry.rotation = blockDocument.getInt32("rotation", DEFAULT_ROTATION_VALUE).getValue();
                  int columnIndex = MathUtil.packInt(x, z);
                  Int2ObjectMap<PrefabBufferBlockEntry> column = (Int2ObjectMap<PrefabBufferBlockEntry>)columnMap.get(columnIndex);
                  if (column == null) {
                     columnMap.put(columnIndex, column = new Int2ObjectOpenHashMap());
                  }

                  PrefabBufferBlockEntry existing = (PrefabBufferBlockEntry)column.putIfAbsent(y, blockEntry);
                  if (existing != null) {
                     throw new IllegalStateException(
                        "Block is already present in column. Given: "
                           + realX
                           + ", "
                           + realY
                           + ", "
                           + realZ
                           + ", "
                           + blockEntry.blockTypeKey
                           + " - Existing: "
                           + existing.y
                           + ", "
                           + existing.blockTypeKey
                     );
                  }
               }
            }

            BsonValue fluidsValue = document.get("fluids");
            if (fluidsValue != null) {
               IndexedLookupTableAssetMap<String, Fluid> assetMap = Fluid.getAssetMap();

               for (BsonValue fluidValue : fluidsValue.asArray()) {
                  BsonDocument fluidDocument = fluidValue.asDocument();
                  int realXx = fluidDocument.getInt32("x").getValue();
                  int realYx = fluidDocument.getInt32("y").getValue();
                  int realZx = fluidDocument.getInt32("z").getValue();
                  int xx = realXx - anchor.x;
                  int yx = realYx - anchor.y;
                  int zx = realZx - anchor.z;
                  if (-32768 > xx || xx > 32767) {
                     throw new IllegalArgumentException("Violation X: Short.MIN_VALUE < " + xx + " < Short.MAX_VALUE");
                  }

                  if (-32768 > yx || yx > 32767) {
                     throw new IllegalArgumentException("Violation Y: Short.MIN_VALUE < " + yx + " < Short.MAX_VALUE");
                  }

                  if (-32768 > zx || zx > 32767) {
                     throw new IllegalArgumentException("Violation Z: Short.MIN_VALUE < " + zx + " < Short.MAX_VALUE");
                  }

                  int columnIndexx = MathUtil.packInt(xx, zx);
                  Int2ObjectMap<PrefabBufferBlockEntry> columnx = (Int2ObjectMap<PrefabBufferBlockEntry>)columnMap.get(columnIndexx);
                  if (columnx == null) {
                     columnMap.put(columnIndexx, columnx = new Int2ObjectOpenHashMap());
                  }

                  PrefabBufferBlockEntry entry = (PrefabBufferBlockEntry)columnx.computeIfAbsent(yx, builder::newBlockEntry);
                  String fluidName = fluidDocument.getString("name").getValue();
                  entry.fluidId = Fluid.getFluidIdOrUnknown(fluidName, "Unknown fluid '%s'", fluidName);
                  entry.fluidLevel = (byte)fluidDocument.getInt32("level").getValue();
               }
            }

            Int2ObjectOpenHashMap<List<Holder<EntityStore>>> entityMap = deserializeEntityHolders(document, anchor, version, entityVersion);
            columnMap.int2ObjectEntrySet().fastForEach(entryx -> {
               int columnIndexx = entryx.getIntKey();
               int xxx = MathUtil.unpackLeft(columnIndexx);
               int zxx = MathUtil.unpackRight(columnIndexx);
               Int2ObjectMap<PrefabBufferBlockEntry> columnBlockMap = (Int2ObjectMap<PrefabBufferBlockEntry>)entryx.getValue();
               PrefabBufferBlockEntry[] entries = (PrefabBufferBlockEntry[])columnBlockMap.values().toArray(PrefabBufferBlockEntry[]::new);
               Arrays.sort(entries, Comparator.comparingInt(o -> o.y));
               List<Holder<EntityStore>> entityColumn = (List<Holder<EntityStore>>)entityMap.remove(columnIndexx);
               Holder[] entityArray = entityColumn != null && !entityColumn.isEmpty() ? entityColumn.toArray(Holder[]::new) : null;
               builder.addColumn(xxx, zxx, entries, entityArray);
            });
            entityMap.int2ObjectEntrySet().fastForEach(entryx -> {
               int columnIndexx = entryx.getIntKey();
               int xxx = MathUtil.unpackLeft(columnIndexx);
               int zxx = MathUtil.unpackRight(columnIndexx);
               List<Holder<EntityStore>> entityColumn = (List<Holder<EntityStore>>)entryx.getValue();
               Holder[] entityArray = !entityColumn.isEmpty() ? entityColumn.toArray(Holder[]::new) : null;
               if (entityArray != null) {
                  builder.addColumn(xxx, zxx, PrefabBufferBlockEntry.EMPTY_ARRAY, entityArray);
               }
            });
            return builder.build();
         }
      }
   }

   private static void deserializeBlockType(
      @Nonnull PrefabBufferBlockEntry blockEntry,
      @Nonnull BsonDocument blockDocument,
      @Nonnull BlockTypeAssetMap<String, BlockType> assetMap,
      @Nullable Function<String, String> blockMigration
   ) {
      String blockType = blockDocument.getString("name").getValue();
      int idx = blockType.indexOf(37);
      String blockTypeStr;
      if (idx != -1) {
         blockTypeStr = blockType.substring(idx + 1);
      } else {
         blockTypeStr = blockType;
      }

      blockEntry.blockTypeKey = blockTypeStr;
      if (blockMigration != null) {
         blockEntry.blockTypeKey = blockMigration.apply(blockEntry.blockTypeKey);
      }

      blockEntry.blockId = BlockType.getBlockIdOrUnknown(assetMap, blockEntry.blockTypeKey, "Failed to find block. Given %s", blockTypeStr);
      if (idx != -1) {
         String chanceString = blockType.substring(0, idx);
         float chancePercent = Float.parseFloat(chanceString);
         if (chancePercent < 0.0F) {
            throw new IllegalArgumentException("Chance is smaller than 0%. Given: " + chancePercent);
         }

         if (chancePercent > 100.0F) {
            throw new IllegalArgumentException("Chance is larger than 100%. Given: " + chancePercent);
         }

         blockEntry.chance = chancePercent / 100.0F;
      }
   }

   private static void deserializeState(@Nonnull PrefabBufferBlockEntry blockEntry, @Nonnull BsonDocument blockDocument, int version, int worldVersion) {
      if (version <= 2) {
         BsonValue stateValue = blockDocument.get("state");
         if (stateValue != null) {
            blockEntry.state = SelectionPrefabSerializer.legacyStateDecode(stateValue.asDocument());
         }
      } else {
         BsonValue stateValue = blockDocument.get("components");
         if (stateValue != null) {
            if (version < 4) {
               blockEntry.state = ChunkStore.REGISTRY.deserialize(stateValue.asDocument(), worldVersion);
            } else {
               blockEntry.state = ChunkStore.REGISTRY.deserialize(stateValue.asDocument());
            }
         }
      }
   }

   @Nonnull
   private static Int2ObjectOpenHashMap<List<Holder<EntityStore>>> deserializeEntityHolders(
      @Nonnull BsonDocument document, @Nonnull Vector3i anchor, int version, int entityVersion
   ) {
      BsonValue entitiesValue = document.get("entities");
      Int2ObjectOpenHashMap<List<Holder<EntityStore>>> entityMap = new Int2ObjectOpenHashMap();
      if (entitiesValue == null) {
         return entityMap;
      } else {
         BsonArray entitiesArray = entitiesValue.asArray();
         int i = 0;

         for (int size = entitiesArray.size(); i < size; i++) {
            BsonDocument entityDocument = entitiesArray.get(i).asDocument();

            try {
               Holder<EntityStore> entityHolder;
               if (version <= 1) {
                  entityHolder = SelectionPrefabSerializer.legacyEntityDecode(entityDocument, entityVersion);
               } else {
                  entityHolder = EntityStore.REGISTRY.deserialize(entityDocument);
               }

               TransformComponent transformComponent = entityHolder.getComponent(TransformComponent.getComponentType());

               assert transformComponent != null;

               Vector3d position = transformComponent.getPosition();
               position.add(-anchor.x, -anchor.y, -anchor.z);
               int x = MathUtil.floor(position.getX()) & 65535;
               int z = MathUtil.floor(position.getZ()) & 65535;
               int columnIndex = MathUtil.packInt(x, z);
               List<Holder<EntityStore>> entityColumn = (List<Holder<EntityStore>>)entityMap.get(columnIndex);
               if (entityColumn == null) {
                  entityMap.put(columnIndex, entityColumn = new ObjectArrayList());
               }

               entityColumn.add(entityHolder);
            } catch (Exception var17) {
               throw new IllegalStateException("Failed to load entity wrapper #" + i + ": " + entityDocument, var17);
            }
         }

         return entityMap;
      }
   }
}
