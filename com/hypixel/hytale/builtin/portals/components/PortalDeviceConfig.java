package com.hypixel.hytale.builtin.portals.components;

import com.hypixel.hytale.builtin.portals.utils.BlockTypeUtils;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PortalDeviceConfig {
   @Nonnull
   public static final BuilderCodec<PortalDeviceConfig> CODEC = BuilderCodec.builder(PortalDeviceConfig.class, PortalDeviceConfig::new)
      .appendInherited(
         new KeyedCodec<>("SpawningState", Codec.STRING),
         (config, o) -> config.spawningState = o,
         config -> config.spawningState,
         (config, parent) -> config.spawningState = parent.spawningState
      )
      .documentation("The StateData for the short transition from off to on, when the instance is being created")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("OnState", Codec.STRING),
         (config, o) -> config.onState = o,
         config -> config.onState,
         (config, parent) -> config.onState = parent.onState
      )
      .documentation("The StateData when the portal is summoned and active.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("OffState", Codec.STRING),
         (config, o) -> config.offState = o,
         config -> config.offState,
         (config, parent) -> config.offState = parent.offState
      )
      .documentation("The StateData when there is no portal and the device is inactive.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("ReturnBlockType", Codec.STRING),
         (config, o) -> config.returnBlock = o,
         config -> config.returnBlock,
         (config, parent) -> config.returnBlock = parent.returnBlock
      )
      .documentation("This block type will be placed (once) on the spawn point of the portal world.")
      .add()
      .build();
   private String onState = "Active";
   private String spawningState = "Spawning";
   private String offState = "default";
   private String returnBlock;

   public String getOnState() {
      return this.onState;
   }

   public String getSpawningState() {
      return this.spawningState;
   }

   public String getOffState() {
      return this.offState;
   }

   @Nullable
   public String getReturnBlock() {
      return this.returnBlock;
   }

   @Nonnull
   public String[] getBlockStates() {
      return new String[]{this.onState, this.spawningState, this.offState};
   }

   public boolean areBlockStatesValid(@Nonnull BlockType baseBlockType) {
      for (String stateKey : this.getBlockStates()) {
         BlockType blockType = BlockTypeUtils.getBlockForState(baseBlockType, stateKey);
         if (blockType == null) {
            return false;
         }
      }

      return true;
   }
}
