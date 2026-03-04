package com.hypixel.hytale.server.worldgen.cave;

import com.hypixel.hytale.server.worldgen.chunk.BlockPriorityModifier;

public class CaveBlockPriorityModifier implements BlockPriorityModifier {
   public static final BlockPriorityModifier INSTANCE = new CaveBlockPriorityModifier();

   @Override
   public byte modifyCurrent(byte current, byte target) {
      if (current == 8 && target == 6) {
         return 6;
      } else {
         return current == 6 && target == 5 ? 5 : current;
      }
   }

   @Override
   public byte modifyTarget(byte current, byte target) {
      return current == 8 && target == 6 ? 8 : target;
   }
}
