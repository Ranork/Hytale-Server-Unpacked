package com.hypixel.hytale.builtin.buildertools.snapshot;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ClipboardContentsSnapshot implements ClipboardSnapshot<ClipboardContentsSnapshot> {
   private final BlockSelection selection;

   public ClipboardContentsSnapshot(BlockSelection selection) {
      this.selection = selection;
   }

   public ClipboardContentsSnapshot restoreClipboard(
      Ref<EntityStore> ref, Player player, World world, @Nonnull BuilderToolsPlugin.BuilderState builderState, ComponentAccessor<EntityStore> componentAccessor
   ) {
      ClipboardContentsSnapshot snapshot = new ClipboardContentsSnapshot(builderState.getSelection());
      builderState.setSelection(this.selection);
      return snapshot;
   }

   @Nonnull
   public static ClipboardContentsSnapshot copyOf(@Nonnull BlockSelection selection) {
      return new ClipboardContentsSnapshot(selection.cloneSelection());
   }
}
