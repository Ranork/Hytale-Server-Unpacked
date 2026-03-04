package com.hypixel.hytale.server.npc.blackboard.view.attitude;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.npc.config.ItemAttitudeGroup;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemAttitudeMap {
   private final Map<String, Attitude>[] map;

   private ItemAttitudeMap(Map<String, Attitude>[] map) {
      this.map = map;
   }

   @Nullable
   public Attitude getAttitude(@Nonnull NPCEntity parent, @Nullable ItemStack item) {
      if (item == null) {
         return null;
      } else {
         int group = parent.getRole().getWorldSupport().getItemAttitudeGroup();
         if (group == Integer.MIN_VALUE) {
            return null;
         } else {
            Map<String, Attitude> attitudeMap = this.map[group];
            if (attitudeMap == null) {
               return null;
            } else {
               String targetId = item.getItemId();
               return attitudeMap.get(targetId);
            }
         }
      }
   }

   public int getAttitudeGroupCount() {
      return this.map.length;
   }

   public void updateAttitudeGroup(int id, @Nonnull ItemAttitudeGroup group) {
      Map<String, Attitude> groupMap = ItemAttitudeMap.Builder.createGroupMap(group);
      this.map[id] = groupMap;
   }

   public static class Builder {
      private final Map<String, Attitude>[] map = new HashMap[ItemAttitudeGroup.getAssetMap().getNextIndex()];

      public void addAttitudeGroups(@Nonnull Map<String, ItemAttitudeGroup> groups) {
         groups.forEach((id, group) -> this.addAttitudeGroup(group));
      }

      private void addAttitudeGroup(@Nonnull ItemAttitudeGroup group) {
         String key = group.getId();
         int index = ItemAttitudeGroup.getAssetMap().getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         } else {
            this.map[index] = createGroupMap(group);
         }
      }

      @Nonnull
      private static Map<String, Attitude> createGroupMap(@Nonnull ItemAttitudeGroup group) {
         HashMap<String, Attitude> groupMap = new HashMap<>();

         for (Attitude attitude : Attitude.VALUES) {
            putGroups(group.getAttitudes().get(attitude), attitude, groupMap);
         }

         return groupMap;
      }

      private static void putGroups(@Nullable String[] group, Attitude targetAttitude, @Nonnull HashMap<String, Attitude> targetMap) {
         if (group != null) {
            for (String item : group) {
               Set<String> set = Item.getAssetMap().getKeysForTag(AssetRegistry.getOrCreateTagIndex(item));
               if (set != null) {
                  set.forEach(k -> targetMap.put(k, targetAttitude));
               }
            }
         }
      }

      @Nonnull
      public ItemAttitudeMap build() {
         return new ItemAttitudeMap(this.map);
      }
   }
}
