package com.hypixel.hytale.server.core.modules.entity;

import com.hypixel.hytale.codec.ExtraInfo;
import java.util.function.Function;

public class BlockMigrationExtraInfo extends ExtraInfo {
   private final Function<String, String> blockMigration;

   public BlockMigrationExtraInfo(int version, Function<String, String> blockMigration) {
      super(version);
      this.blockMigration = blockMigration;
   }

   public Function<String, String> getBlockMigration() {
      return this.blockMigration;
   }
}
