package com.hypixel.hytale.server.npc.interactions;

import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.math.block.BlockUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import javax.annotation.Nonnull;
import org.joml.Vector3ic;

public class SpawnNPCInteractionFailureTracker implements Resource<EntityStore> {
   @Nonnull
   private final LongOpenHashSet failedTargetBlocks = new LongOpenHashSet();

   public static ResourceType<EntityStore, SpawnNPCInteractionFailureTracker> getResourceType() {
      return NPCPlugin.get().getSpawnNPCInteractionFailureTrackerResourceType();
   }

   public boolean recordFailure(@Nonnull Vector3ic block) {
      return this.failedTargetBlocks.add(BlockUtil.pack(block));
   }

   public void clearFailureForBlock(@Nonnull Vector3ic block) {
      this.failedTargetBlocks.remove(BlockUtil.pack(block));
   }

   @Nonnull
   @Override
   public Resource<EntityStore> clone() {
      return new SpawnNPCInteractionFailureTracker();
   }
}
