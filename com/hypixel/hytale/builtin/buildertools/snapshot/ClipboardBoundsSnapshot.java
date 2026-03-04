package com.hypixel.hytale.builtin.buildertools.snapshot;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ClipboardBoundsSnapshot implements ClipboardSnapshot<ClipboardBoundsSnapshot> {
   public static final ClipboardBoundsSnapshot EMPTY = new ClipboardBoundsSnapshot(Vector3i.ZERO, Vector3i.ZERO);
   private final Vector3i min;
   private final Vector3i max;

   public ClipboardBoundsSnapshot(@Nonnull BlockSelection selection) {
      this(selection.getSelectionMin(), selection.getSelectionMax());
   }

   public ClipboardBoundsSnapshot(Vector3i min, Vector3i max) {
      this.min = min;
      this.max = max;
   }

   public Vector3i getMin() {
      return this.min;
   }

   public Vector3i getMax() {
      return this.max;
   }

   public ClipboardBoundsSnapshot restoreClipboard(
      Ref<EntityStore> ref, Player player, World world, @Nonnull BuilderToolsPlugin.BuilderState state, ComponentAccessor<EntityStore> componentAccessor
   ) {
      ClipboardBoundsSnapshot snapshot = new ClipboardBoundsSnapshot(state.getSelection());
      state.getSelection().setSelectionArea(this.min, this.max);
      state.sendArea();
      return snapshot;
   }
}
