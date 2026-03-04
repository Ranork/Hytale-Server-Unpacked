package com.hypixel.hytale.server.npc.blackboard.view.attitude;

import com.hypixel.hytale.builtin.tagset.TagSetPlugin;
import com.hypixel.hytale.builtin.tagset.config.NPCGroup;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderManager;
import com.hypixel.hytale.server.npc.config.AttitudeGroup;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AttitudeMap {
   @Nullable
   private static final ComponentType<EntityStore, NPCEntity> NPC_COMPONENT_TYPE = NPCEntity.getComponentType();
   private static final ComponentType<EntityStore, Player> PLAYER_COMPONENT_TYPE = Player.getComponentType();
   private final Int2ObjectMap<Attitude>[] map;

   private AttitudeMap(Int2ObjectMap<Attitude>[] map) {
      this.map = map;
   }

   @Nullable
   public Attitude getAttitude(@Nonnull Role role, @Nonnull Ref<EntityStore> target, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      int group = role.getWorldSupport().getAttitudeGroup();
      if (group == Integer.MIN_VALUE) {
         return null;
      } else {
         Int2ObjectMap<Attitude> attitudeMap = this.map[group];
         if (attitudeMap == null) {
            return null;
         } else {
            NPCEntity npc = componentAccessor.getComponent(target, NPC_COMPONENT_TYPE);
            int targetId;
            if (npc != null) {
               targetId = npc.getRoleIndex();
            } else {
               if (!componentAccessor.getArchetype(target).contains(PLAYER_COMPONENT_TYPE)) {
                  return null;
               }

               targetId = BuilderManager.getPlayerGroupID();
            }

            return targetId == role.getRoleIndex() ? (Attitude)attitudeMap.get(BuilderManager.getSelfGroupID()) : (Attitude)attitudeMap.get(targetId);
         }
      }
   }

   public int getAttitudeGroupCount() {
      return this.map.length;
   }

   public void updateAttitudeGroup(int id, @Nonnull AttitudeGroup group) {
      Int2ObjectMap<Attitude> groupMap = AttitudeMap.Builder.createGroupMap(group);
      this.map[id] = groupMap;
   }

   public static class Builder {
      private final Int2ObjectMap<Attitude>[] map = new Int2ObjectMap[AttitudeGroup.getAssetMap().getNextIndex()];

      public void addAttitudeGroups(@Nonnull Map<String, AttitudeGroup> groups) {
         groups.forEach((id, group) -> this.addAttitudeGroup(group));
      }

      private void addAttitudeGroup(@Nonnull AttitudeGroup group) {
         String key = group.getId();
         int index = AttitudeGroup.getAssetMap().getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         } else {
            this.map[index] = createGroupMap(group);
         }
      }

      @Nonnull
      private static Int2ObjectMap<Attitude> createGroupMap(@Nonnull AttitudeGroup group) {
         TagSetPlugin.TagSetLookup npcGroups = TagSetPlugin.get(NPCGroup.class);
         Int2ObjectOpenHashMap<Attitude> groupMap = new Int2ObjectOpenHashMap();

         for (Attitude attitude : Attitude.VALUES) {
            putGroups(group.getId(), npcGroups, group.getAttitudeGroups().get(attitude), attitude, groupMap);
         }

         groupMap.trim();
         return groupMap;
      }

      private static void putGroups(
         String attitudeGroup,
         @Nonnull TagSetPlugin.TagSetLookup npcGroupLookup,
         @Nullable String[] group,
         Attitude targetAttitude,
         @Nonnull Int2ObjectMap<Attitude> targetMap
      ) {
         if (group != null) {
            for (String item : group) {
               int index = NPCGroup.getAssetMap().getIndex(item);
               if (index == Integer.MIN_VALUE) {
                  NPCPlugin.get()
                     .getLogger()
                     .at(Level.SEVERE)
                     .log("Creating attitude groups: NPC Group '%s' does not exist in attitude group '%s'!", item, attitudeGroup);
               } else {
                  IntSet set = npcGroupLookup.getSet(index);
                  if (set != null) {
                     set.forEach(i -> targetMap.put(i, targetAttitude));
                  }
               }
            }
         }
      }

      @Nonnull
      public AttitudeMap build() {
         return new AttitudeMap(this.map);
      }
   }
}
