package com.hypixel.hytale.builtin.buildertools.snapshot;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ClipboardSnapshot<T extends SelectionSnapshot<?>> extends SelectionSnapshot<T> {
   @Nullable
   T restoreClipboard(Ref<EntityStore> var1, PlayerRef var2, World var3, BuilderToolsPlugin.BuilderState var4, ComponentAccessor<EntityStore> var5);

   @Override
   default T restore(Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, World world, ComponentAccessor<EntityStore> componentAccessor) {
      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());
      if (!<unrepresentable>.$assertionsDisabled && playerComponent == null) {
         throw new AssertionError();
      } else {
         BuilderToolsPlugin.BuilderState state = BuilderToolsPlugin.getState(playerComponent, playerRef);
         return state == null ? null : this.restoreClipboard(ref, playerRef, world, state, componentAccessor);
      }
   }

   static {
      if (<unrepresentable>.$assertionsDisabled) {
      }
   }
}
