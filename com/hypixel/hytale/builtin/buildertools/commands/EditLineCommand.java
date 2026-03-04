package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.buildertools.BrushOrigin;
import com.hypixel.hytale.protocol.packets.buildertools.BrushShape;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeVector3i;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class EditLineCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<RelativeVector3i> startArg = this.withRequiredArg("start", "server.commands.editline.start.desc", ArgTypes.RELATIVE_VECTOR3I);
   @Nonnull
   private final RequiredArg<RelativeVector3i> endArg = this.withRequiredArg("end", "server.commands.editline.end.desc", ArgTypes.RELATIVE_VECTOR3I);
   @Nonnull
   private final RequiredArg<String> materialArg = this.withRequiredArg("material", "server.commands.editline.material.desc", ArgTypes.STRING);
   @Nonnull
   private final DefaultArg<Integer> widthArg = this.withDefaultArg("width", "server.commands.editline.width.desc", ArgTypes.INTEGER, 1, "1");
   @Nonnull
   private final DefaultArg<Integer> heightArg = this.withDefaultArg("height", "server.commands.editline.height.desc", ArgTypes.INTEGER, 1, "1");
   @Nonnull
   private final DefaultArg<Integer> wallThicknessArg = this.withDefaultArg(
      "wallThickness", "server.commands.editline.wallThickness.desc", ArgTypes.INTEGER, 0, "0"
   );
   @Nonnull
   private final DefaultArg<String> shapeArg = this.withDefaultArg("shape", "server.commands.editline.shape.desc", ArgTypes.STRING, "Cube", "Cube");
   @Nonnull
   private final DefaultArg<String> originArg = this.withDefaultArg("origin", "server.commands.editline.origin.desc", ArgTypes.STRING, "Center", "Center");
   @Nonnull
   private final DefaultArg<Integer> spacingArg = this.withDefaultArg("spacing", "server.commands.editline.spacing.desc", ArgTypes.INTEGER, 1, "1");
   @Nonnull
   private final DefaultArg<Integer> densityArg = this.withDefaultArg("density", "server.commands.editline.density.desc", ArgTypes.INTEGER, 100, "100");

   public EditLineCommand() {
      super("editline", "server.commands.editline.desc");
      this.setPermissionGroup(GameMode.Creative);
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d playerPos = transformComponent.getPosition();
      int baseX = MathUtil.floor(playerPos.getX());
      int baseY = MathUtil.floor(playerPos.getY());
      int baseZ = MathUtil.floor(playerPos.getZ());
      Vector3i start = this.startArg.get(context).resolve(baseX, baseY, baseZ);
      Vector3i end = this.endArg.get(context).resolve(baseX, baseY, baseZ);
      BlockPattern material = BlockPattern.parse(this.materialArg.get(context));
      int width = this.widthArg.get(context);
      int height = this.heightArg.get(context);
      int wallThickness = this.wallThicknessArg.get(context);
      BrushShape shape = BrushShape.valueOf(this.shapeArg.get(context));
      BrushOrigin origin = BrushOrigin.valueOf(this.originArg.get(context));
      int spacing = this.spacingArg.get(context);
      int density = this.densityArg.get(context);
      BuilderToolsPlugin.addToQueue(
         playerComponent,
         playerRef,
         (r, s, componentAccessor) -> s.editLine(
            start.x,
            start.y,
            start.z,
            end.x,
            end.y,
            end.z,
            material,
            width,
            height,
            wallThickness,
            shape,
            origin,
            spacing,
            density,
            s.getGlobalMask(),
            componentAccessor
         )
      );
   }
}
