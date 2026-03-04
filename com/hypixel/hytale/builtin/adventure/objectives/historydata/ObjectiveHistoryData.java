package com.hypixel.hytale.builtin.adventure.objectives.historydata;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

public final class ObjectiveHistoryData extends CommonObjectiveHistoryData {
   @Nonnull
   public static final BuilderCodec<ObjectiveHistoryData> CODEC = BuilderCodec.builder(ObjectiveHistoryData.class, ObjectiveHistoryData::new, BASE_CODEC)
      .append(
         new KeyedCodec<>("Rewards", new ArrayCodec<>(ObjectiveRewardHistoryData.CODEC, ObjectiveRewardHistoryData[]::new)),
         (objectiveDetails, objectiveRewardHistoryData) -> objectiveDetails.rewards = objectiveRewardHistoryData,
         objectiveDetails -> objectiveDetails.rewards
      )
      .add()
      .build();
   @Nonnull
   protected Map<UUID, List<ObjectiveRewardHistoryData>> rewardsPerPlayer = new ConcurrentHashMap<>();
   protected ObjectiveRewardHistoryData[] rewards;

   public ObjectiveHistoryData(String id, String category) {
      super(id, category);
   }

   public ObjectiveHistoryData(String id, String category, ObjectiveRewardHistoryData[] rewards) {
      super(id, category);
      this.rewards = rewards;
   }

   protected ObjectiveHistoryData() {
   }

   public ObjectiveRewardHistoryData[] getRewards() {
      return this.rewards;
   }

   public void addRewardForPlayerUUID(UUID playerUUID, ObjectiveRewardHistoryData objectiveRewardHistoryData) {
      this.rewardsPerPlayer.computeIfAbsent(playerUUID, k -> new ObjectArrayList()).add(objectiveRewardHistoryData);
   }

   @Nonnull
   public ObjectiveHistoryData cloneForPlayer(UUID playerUUID) {
      List<ObjectiveRewardHistoryData> playerRewards = this.rewardsPerPlayer.get(playerUUID);
      return playerRewards == null
         ? new ObjectiveHistoryData(this.id, this.category)
         : new ObjectiveHistoryData(this.id, this.category, playerRewards.toArray(ObjectiveRewardHistoryData[]::new));
   }

   public void completed(UUID playerUUID, @Nonnull ObjectiveHistoryData objectiveHistoryData) {
      this.completed();
      List<ObjectiveRewardHistoryData> lastRewards = objectiveHistoryData.rewardsPerPlayer.get(playerUUID);
      if (lastRewards != null) {
         this.rewards = lastRewards.toArray(ObjectiveRewardHistoryData[]::new);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ObjectiveHistoryData{rewardsPerPlayer="
         + this.rewardsPerPlayer
         + ", rewards="
         + Arrays.toString((Object[])this.rewards)
         + "} "
         + super.toString();
   }
}
