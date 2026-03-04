package com.hypixel.hytale.server.npc.navigation;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;

public class AStarNodePoolSimple implements AStarNodePool {
   protected final List<AStarNode> nodePool = new ObjectArrayList();
   private final int childCount;

   public AStarNodePoolSimple(int childCount) {
      this.childCount = childCount;
   }

   @Override
   public AStarNode allocate() {
      return this.nodePool.isEmpty() ? new AStarNode(this.childCount) : this.nodePool.removeLast();
   }

   @Override
   public void deallocate(AStarNode node) {
      this.nodePool.add(node);
   }
}
