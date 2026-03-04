package com.hypixel.hytale.builtin.buildertools.snapshot;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class BlockSelectionSnapshot implements SelectionSnapshot<BlockSelectionSnapshot> {
   private final BlockSelection selection;

   public BlockSelectionSnapshot(BlockSelection snapshot) {
      this.selection = snapshot;
   }

   public BlockSelection getBlockSelection() {
      return this.selection;
   }

   public BlockSelectionSnapshot restore(Ref<EntityStore> ref, Player player, @Nonnull World world, ComponentAccessor<EntityStore> componentAccessor) {
      BlockSelection before = this.selection.place(player, world);
      BuilderToolsPlugin.invalidateWorldMapForSelection(before, world);
      return new BlockSelectionSnapshot(before);
   }

   @Nonnull
   public static BlockSelectionSnapshot copyOf(@Nonnull BlockSelection selection) {
      return new BlockSelectionSnapshot(selection.cloneSelection());
   }
}
