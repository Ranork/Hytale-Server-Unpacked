package com.hypixel.hytale.server.core.modules.blockset;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class BlockSetLookupTable {
   @Nonnull
   private final Object2ObjectMap<String, IntSet> blockNameIdMap;
   @Nonnull
   private final Object2ObjectMap<String, IntSet> groupNameIdMap;
   @Nonnull
   private final Object2ObjectMap<String, IntSet> hitboxNameIdMap;
   @Nonnull
   private final Object2ObjectMap<String, IntSet> categoryIdMap;

   public BlockSetLookupTable(@Nonnull Map<String, BlockType> blockTypeMap) {
      Object2ObjectMap<String, IntSet> blockNameIdMap = new Object2ObjectOpenHashMap();
      Object2ObjectMap<String, IntSet> groupNameIdMap = new Object2ObjectOpenHashMap();
      Object2ObjectMap<String, IntSet> hitboxNameIdMap = new Object2ObjectOpenHashMap();
      Object2ObjectMap<String, IntSet> categoryIdMap = new Object2ObjectOpenHashMap();
      BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
      blockTypeMap.keySet().forEach(blockName -> {
         int index = assetMap.getIndex(blockName);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + blockName);
         } else {
            ((IntSet)blockNameIdMap.computeIfAbsent(blockName, s -> new IntOpenHashSet())).add(index);
         }
      });
      blockTypeMap.forEach((blockTypeKey, blockType) -> {
         String group = blockType.getGroup();
         if (group != null && !group.isEmpty()) {
            int index = assetMap.getIndex(blockTypeKey);
            if (index == Integer.MIN_VALUE) {
               throw new IllegalArgumentException("Unknown key! " + blockTypeKey);
            }

            ((IntSet)groupNameIdMap.computeIfAbsent(group, s -> new IntOpenHashSet())).add(index);
         }

         String hitboxType = blockType.getHitboxType();
         if (hitboxType != null && !hitboxType.isEmpty()) {
            int index = hitboxType.indexOf(124);
            if (index != 0) {
               if (index > 0) {
                  hitboxType = hitboxType.substring(0, index);
               }

               int index1 = assetMap.getIndex(blockTypeKey);
               if (index1 == Integer.MIN_VALUE) {
                  throw new IllegalArgumentException("Unknown key! " + blockTypeKey);
               }

               ((IntSet)hitboxNameIdMap.computeIfAbsent(hitboxType, s -> new IntOpenHashSet())).add(index1);
            }
         }

         String name = blockType.getId();
         Item item = Item.getAssetMap().getAsset(name);
         if (item != null) {
            String[] categories = item.getCategories();
            if (categories != null) {
               for (String category : categories) {
                  int index = assetMap.getIndex(blockTypeKey);
                  if (index == Integer.MIN_VALUE) {
                     throw new IllegalArgumentException("Unknown key! " + blockTypeKey);
                  }

                  ((IntSet)categoryIdMap.computeIfAbsent(category, s -> new IntOpenHashSet())).add(index);
               }
            }
         }
      });
      blockNameIdMap.replaceAll((s, tIntSet) -> {
         ((IntOpenHashSet)tIntSet).trim();
         return IntSets.unmodifiable(tIntSet);
      });
      groupNameIdMap.replaceAll((s, tIntSet) -> {
         ((IntOpenHashSet)tIntSet).trim();
         return IntSets.unmodifiable(tIntSet);
      });
      hitboxNameIdMap.replaceAll((s, tIntSet) -> {
         ((IntOpenHashSet)tIntSet).trim();
         return IntSets.unmodifiable(tIntSet);
      });
      categoryIdMap.replaceAll((s, tIntSet) -> {
         ((IntOpenHashSet)tIntSet).trim();
         return IntSets.unmodifiable(tIntSet);
      });
      this.blockNameIdMap = Object2ObjectMaps.unmodifiable(blockNameIdMap);
      this.groupNameIdMap = Object2ObjectMaps.unmodifiable(groupNameIdMap);
      this.hitboxNameIdMap = Object2ObjectMaps.unmodifiable(hitboxNameIdMap);
      this.categoryIdMap = Object2ObjectMaps.unmodifiable(categoryIdMap);
   }

   public void addAll(@Nonnull IntSet result) {
      this.blockNameIdMap.values().forEach(result::addAll);
   }

   @Nonnull
   public Object2ObjectMap<String, IntSet> getBlockNameIdMap() {
      return this.blockNameIdMap;
   }

   @Nonnull
   public Object2ObjectMap<String, IntSet> getGroupNameIdMap() {
      return this.groupNameIdMap;
   }

   @Nonnull
   public Object2ObjectMap<String, IntSet> getHitboxNameIdMap() {
      return this.hitboxNameIdMap;
   }

   @Nonnull
   public Object2ObjectMap<String, IntSet> getCategoryIdMap() {
      return this.categoryIdMap;
   }

   public boolean isEmpty() {
      return this.blockNameIdMap.isEmpty() && this.groupNameIdMap.isEmpty() && this.hitboxNameIdMap.isEmpty() && this.categoryIdMap.isEmpty();
   }
}
