package com.hypixel.hytale.builtin.buildertools.tooloperations;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolOnUseInteraction;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class FloodOperation extends ToolOperation {
   public FloodOperation(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Player player,
      @Nonnull BuilderToolOnUseInteraction packet,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      super(ref, packet, componentAccessor);
   }

   @Override
   public void execute(ComponentAccessor<EntityStore> componentAccessor) {
      BlockPattern targetPattern = (BlockPattern)this.args.tool().get("TargetBlock");
      int targetBlock = targetPattern.isEmpty() ? this.edit.getAccessor().getBlock(this.x, this.y, this.z) : targetPattern.firstBlock();
      Player playerComponent = componentAccessor.getComponent(this.playerRef, Player.getComponentType());

      assert playerComponent != null;

      PlayerRef playerRefComponent = componentAccessor.getComponent(this.playerRef, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      BuilderToolsPlugin.getState(playerComponent, playerRefComponent)
         .flood(this.edit, this.x, this.y + this.originOffsetY, this.z, this.shapeRange, this.shapeHeight, this.pattern, targetBlock);
   }

   @Override
   boolean execute0(int x, int y, int z) {
      return true;
   }
}
