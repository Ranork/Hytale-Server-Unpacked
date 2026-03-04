package com.hypixel.hytale.server.core.universe.world.connectedblocks.builtin;

import javax.annotation.Nullable;

public interface StairLikeConnectedBlockRuleSet {
   StairConnectedBlockRuleSet.StairType getStairType(int var1);

   @Nullable
   String getMaterialName();
}
