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
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class Pos1Command extends AbstractPlayerCommand {
   @Nonnull
   private final OptionalArg<Integer> xArg = this.withOptionalArg("x", "server.commands.pos1.x.desc", ArgTypes.INTEGER);
   @Nonnull
   private final OptionalArg<Integer> yArg = this.withOptionalArg("y", "server.commands.pos1.y.desc", ArgTypes.INTEGER);
   @Nonnull
   private final OptionalArg<Integer> zArg = this.withOptionalArg("z", "server.commands.pos1.z.desc", ArgTypes.INTEGER);

   public Pos1Command() {
      super("pos1", "server.commands.pos1.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.requirePermission("hytale.editor.selection.use");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
         Vector3i intTriple;
         if (this.xArg.provided(context) && this.yArg.provided(context) && this.zArg.provided(context)) {
            intTriple = new Vector3i(this.xArg.get(context), this.yArg.get(context), this.zArg.get(context));
         } else {
            TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
            if (transformComponent == null) {
               return;
            }

            Vector3d position = transformComponent.getPosition();
            intTriple = new Vector3i(MathUtil.floor(position.getX()), MathUtil.floor(position.getY()), MathUtil.floor(position.getZ()));
         }

         BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.pos1(intTriple, componentAccessor));
      }
   }
}
