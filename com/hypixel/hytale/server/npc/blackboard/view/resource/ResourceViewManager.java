package com.hypixel.hytale.server.npc.blackboard.view.resource;

import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.blackboard.view.BlockRegionViewManager;
import javax.annotation.Nonnull;

public class ResourceViewManager extends BlockRegionViewManager<ResourceView> {
   @Nonnull
   protected ResourceView createView(long index, Blackboard blackboard) {
      return new ResourceView(index);
   }

   protected boolean shouldCleanup(@Nonnull ResourceView view) {
      return view.getReservationsByEntity().isEmpty();
   }
}
