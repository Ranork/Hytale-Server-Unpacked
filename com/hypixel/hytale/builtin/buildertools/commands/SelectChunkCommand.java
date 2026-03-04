package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class SelectChunkCommand extends AbstractPlayerCommand {
   public SelectChunkCommand() {
      super("selectchunk", "server.commands.selectchunk.desc");
      this.setPermissionGroup(GameMode.Creative);
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         Vector3d position = transformComponent.getPosition();
         int chunkX = MathUtil.floor(position.getX()) >> 5;
         int chunkZ = MathUtil.floor(position.getZ()) >> 5;
         Vector3i min = new Vector3i(chunkX << 5, 0, chunkZ << 5);
         Vector3i max = new Vector3i((chunkX + 1 << 5) - 1, 319, (chunkZ + 1 << 5) - 1);
         BuilderToolsPlugin.addToQueue(
            playerComponent, playerRef, (r, s, componentAccessor) -> s.select(min, max, "server.builderTools.selectReasons.chunk", componentAccessor)
         );
      }
   }
}
