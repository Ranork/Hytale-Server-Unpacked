package com.hypixel.hytale.server.core.prefab.config;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockMigration;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.prefab.selection.buffer.BsonPrefabBufferDeserializer;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.BsonValue;

public class SelectionPrefabSerializer {
   public static final int VERSION = 8;
   private static final Comparator<BsonDocument> COMPARE_BLOCK_POSITION = Comparator.<BsonDocument>comparingInt(doc -> doc.getInt32("x").getValue())
      .thenComparingInt(doc -> doc.getInt32("z").getValue())
      .thenComparingInt(doc -> doc.getInt32("y").getValue());
   private static final BsonInt32 DEFAULT_SUPPORT_VALUE = new BsonInt32(0);
   private static final BsonInt32 DEFAULT_FILLER_VALUE = new BsonInt32(0);
   private static final BsonInt32 DEFAULT_ROTATION_VALUE = new BsonInt32(0);

   private SelectionPrefabSerializer() {
   }

   @Nonnull
   public static BlockSelection deserialize(@Nonnull BsonDocument doc) {
      BsonValue versionValue = doc.get("version");
      int version = versionValue != null ? versionValue.asInt32().getValue() : -1;
      if (version < 8) {
         throw new IllegalArgumentException("Prefab version is too old: " + version);
      } else if (version > 8) {
         throw new IllegalArgumentException("Prefab version is too new: " + version + " by expected 8");
      } else {
         int anchorX = doc.getInt32("anchorX").getValue();
         int anchorY = doc.getInt32("anchorY").getValue();
         int anchorZ = doc.getInt32("anchorZ").getValue();
         BlockSelection selection = new BlockSelection();
         selection.setAnchor(anchorX, anchorY, anchorZ);
         int blockIdVersion = doc.getInt32("blockIdVersion", BsonPrefabBufferDeserializer.LEGACY_BLOCK_ID_VERSION).getValue();
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

         BsonValue blocksValue = doc.get("blocks");
         if (blocksValue != null) {
            BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
            BsonArray bsonArray = blocksValue.asArray();

            for (int i = 0; i < bsonArray.size(); i++) {
               BsonDocument innerObj = bsonArray.get(i).asDocument();
               int x = innerObj.getInt32("x").getValue();
               int y = innerObj.getInt32("y").getValue();
               int z = innerObj.getInt32("z").getValue();
               String blockTypeStr = innerObj.getString("name").getValue();
               int support = innerObj.getInt32("support", DEFAULT_SUPPORT_VALUE).getValue();
               int filler = innerObj.getInt32("filler", DEFAULT_FILLER_VALUE).getValue();
               int rotation = innerObj.getInt32("rotation", DEFAULT_ROTATION_VALUE).getValue();
               String blockTypeKey = blockTypeStr;
               if (blockMigration != null) {
                  blockTypeKey = blockMigration.apply(blockTypeStr);
               }

               int blockId = BlockType.getBlockIdOrUnknown(assetMap, blockTypeKey, "Failed to find block '%s' in unknown legacy prefab!", blockTypeStr);
               Holder<ChunkStore> wrapper = null;
               BsonValue stateValue = innerObj.get("components");
               if (stateValue != null) {
                  wrapper = ChunkStore.REGISTRY.deserialize(stateValue.asDocument());
               }

               selection.addBlockAtLocalPos(x, y, z, blockId, rotation, filler, support, wrapper);
            }
         }

         BsonValue fluidsValue = doc.get("fluids");
         if (fluidsValue != null) {
            IndexedLookupTableAssetMap<String, Fluid> assetMap = Fluid.getAssetMap();
            BsonArray bsonArray = fluidsValue.asArray();

            for (int i = 0; i < bsonArray.size(); i++) {
               BsonDocument innerObjx = bsonArray.get(i).asDocument();
               int xx = innerObjx.getInt32("x").getValue();
               int yx = innerObjx.getInt32("y").getValue();
               int zx = innerObjx.getInt32("z").getValue();
               String fluidName = innerObjx.getString("name").getValue();
               int fluidId = Fluid.getFluidIdOrUnknown(assetMap, fluidName, "Failed to find fluid '%s' in unknown legacy prefab!", fluidName);
               byte fluidLevel = (byte)innerObjx.getInt32("level").getValue();
               selection.addFluidAtLocalPos(xx, yx, zx, fluidId, fluidLevel);
            }
         }

         BsonValue entitiesValues = doc.get("entities");
         if (entitiesValues != null) {
            BsonArray entities = entitiesValues.asArray();

            for (int i = 0; i < entities.size(); i++) {
               BsonDocument bsonDocument = entities.get(i).asDocument();
               selection.addEntityHolderRaw(EntityStore.REGISTRY.deserialize(bsonDocument));
            }
         }

         return selection;
      }
   }

   @Nonnull
   public static BsonDocument serialize(@Nonnull BlockSelection prefab) {
      Objects.requireNonNull(prefab, "null prefab");
      BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
      IndexedLookupTableAssetMap<String, Fluid> fluidMap = Fluid.getAssetMap();
      BsonDocument out = new BsonDocument();
      out.put("version", new BsonInt32(8));
      out.put("blockIdVersion", new BsonInt32(BlockMigration.getAssetMap().getAssetCount()));
      out.put("anchorX", new BsonInt32(prefab.getAnchorX()));
      out.put("anchorY", new BsonInt32(prefab.getAnchorY()));
      out.put("anchorZ", new BsonInt32(prefab.getAnchorZ()));
      BsonArray contentOut = new BsonArray();
      prefab.forEachBlock((x, y, z, block) -> {
         BsonDocument innerObj = new BsonDocument();
         innerObj.put("x", new BsonInt32(x));
         innerObj.put("y", new BsonInt32(y));
         innerObj.put("z", new BsonInt32(z));
         innerObj.put("name", new BsonString(assetMap.getAsset(block.blockId()).getId().toString()));
         if (block.holder() != null) {
            innerObj.put("components", ChunkStore.REGISTRY.serialize(block.holder()));
         }

         if (block.supportValue() != 0) {
            innerObj.put("support", new BsonInt32(block.supportValue()));
         }

         if (block.filler() != 0) {
            innerObj.put("filler", new BsonInt32(block.filler()));
         }

         if (block.rotation() != 0) {
            innerObj.put("rotation", new BsonInt32(block.rotation()));
         }

         contentOut.add(innerObj);
      });
      contentOut.sort((a, b) -> {
         BsonDocument aDoc = a.asDocument();
         BsonDocument bDoc = b.asDocument();
         return COMPARE_BLOCK_POSITION.compare(aDoc, bDoc);
      });
      out.put("blocks", contentOut);
      BsonArray fluidContentOut = new BsonArray();
      prefab.forEachFluid((x, y, z, fluid, level) -> {
         BsonDocument innerObj = new BsonDocument();
         innerObj.put("x", new BsonInt32(x));
         innerObj.put("y", new BsonInt32(y));
         innerObj.put("z", new BsonInt32(z));
         innerObj.put("name", new BsonString(fluidMap.getAsset(fluid).getId()));
         innerObj.put("level", new BsonInt32(level));
         fluidContentOut.add(innerObj);
      });
      fluidContentOut.sort((a, b) -> {
         BsonDocument aDoc = a.asDocument();
         BsonDocument bDoc = b.asDocument();
         return COMPARE_BLOCK_POSITION.compare(aDoc, bDoc);
      });
      if (!fluidContentOut.isEmpty()) {
         out.put("fluids", fluidContentOut);
      }

      List<BsonDocument> entityList = new ArrayList<>();
      prefab.forEachEntity(holder -> entityList.add(EntityStore.REGISTRY.serialize(holder)));
      if (!entityList.isEmpty()) {
         BsonArray entities = new BsonArray();
         entityList.forEach(entities::add);
         out.put("entities", entities);
      }

      return out;
   }
}
