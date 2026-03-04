package com.hypixel.hytale.builtin.adventure.objectives.historydata;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.common.util.ArrayUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

public final class ObjectiveLineHistoryData extends CommonObjectiveHistoryData {
   @Nonnull
   public static final BuilderCodec<ObjectiveLineHistoryData> CODEC = BuilderCodec.builder(
         ObjectiveLineHistoryData.class, ObjectiveLineHistoryData::new, BASE_CODEC
      )
      .append(
         new KeyedCodec<>("Objectives", new ArrayCodec<>(ObjectiveHistoryData.CODEC, ObjectiveHistoryData[]::new)),
         (objectiveLineDetails, objectiveDetails) -> objectiveLineDetails.objectiveHistoryDataArray = objectiveDetails,
         objectiveLineDetails -> objectiveLineDetails.objectiveHistoryDataArray
      )
      .add()
      .build();
   private ObjectiveHistoryData[] objectiveHistoryDataArray;
   private String[] nextObjectiveLineIds;

   public ObjectiveLineHistoryData(String id, String category, String[] nextObjectiveLineIds) {
      super(id, category);
      this.nextObjectiveLineIds = nextObjectiveLineIds;
   }

   private ObjectiveLineHistoryData() {
   }

   public ObjectiveHistoryData[] getObjectiveHistoryDataArray() {
      return this.objectiveHistoryDataArray;
   }

   public String[] getNextObjectiveLineIds() {
      return this.nextObjectiveLineIds;
   }

   public void addObjectiveHistoryData(@Nonnull ObjectiveHistoryData objectiveHistoryData) {
      this.objectiveHistoryDataArray = ArrayUtil.append(this.objectiveHistoryDataArray, objectiveHistoryData);
   }

   @Nonnull
   public Map<UUID, ObjectiveLineHistoryData> cloneForPlayers(@Nonnull Set<UUID> playerUUIDs) {
      Map<UUID, ObjectiveLineHistoryData> objectiveLineDataPerPlayer = new Object2ObjectOpenHashMap();

      for (ObjectiveHistoryData objectiveHistoryData : this.objectiveHistoryDataArray) {
         for (UUID playerUUID : playerUUIDs) {
            objectiveLineDataPerPlayer.computeIfAbsent(playerUUID, k -> new ObjectiveLineHistoryData())
               .addObjectiveHistoryData(objectiveHistoryData.cloneForPlayer(playerUUID));
         }
      }

      return objectiveLineDataPerPlayer;
   }

   public void completed(UUID playerUUID, @Nonnull ObjectiveLineHistoryData objectiveLineHistoryData) {
      this.completed();

      for (ObjectiveHistoryData latestObjectiveHistoryData : objectiveLineHistoryData.objectiveHistoryDataArray) {
         boolean updated = false;

         for (ObjectiveHistoryData savedObjectiveHistoryData : this.objectiveHistoryDataArray) {
            if (savedObjectiveHistoryData.id.equals(latestObjectiveHistoryData.id)) {
               savedObjectiveHistoryData.completed(playerUUID, latestObjectiveHistoryData);
               updated = true;
               break;
            }
         }

         if (!updated) {
            this.addObjectiveHistoryData(latestObjectiveHistoryData);
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ObjectiveLineHistoryData{objectiveHistoryDataArray="
         + Arrays.toString((Object[])this.objectiveHistoryDataArray)
         + ", nextObjectiveLineIds="
         + Arrays.toString((Object[])this.nextObjectiveLineIds)
         + "} "
         + super.toString();
   }
}
