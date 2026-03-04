package com.hypixel.hytale.server.worldgen.chunk;

public interface BlockPriorityModifier {
   BlockPriorityModifier NONE = new BlockPriorityModifier() {
      @Override
      public byte modifyCurrent(byte current, byte target) {
         return current;
      }

      @Override
      public byte modifyTarget(byte original, byte target) {
         return target;
      }
   };

   byte modifyCurrent(byte var1, byte var2);

   byte modifyTarget(byte var1, byte var2);
}
