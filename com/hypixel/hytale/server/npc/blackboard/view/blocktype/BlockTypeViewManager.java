package com.hypixel.hytale.server.npc.blackboard.view.blocktype;

import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.blackboard.view.BlockRegionViewManager;
import javax.annotation.Nonnull;

public class BlockTypeViewManager extends BlockRegionViewManager<BlockTypeView> {
   private final BlockPositionEntryGenerator generator = new BlockPositionEntryGenerator();

   @Nonnull
   protected BlockTypeView createView(long index, Blackboard blackboard) {
      return new BlockTypeView(index, blackboard, this.generator);
   }

   protected boolean shouldCleanup(@Nonnull BlockTypeView view) {
      return view.getEntities().isEmpty();
   }
}
