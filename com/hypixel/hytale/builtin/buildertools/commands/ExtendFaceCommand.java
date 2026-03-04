package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ExtendFaceCommand extends AbstractCommandCollection {
   public ExtendFaceCommand() {
      super("extendface", "server.commands.extendface.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.addUsageVariant(new ExtendFaceCommand.ExtendFaceBasicCommand());
      this.addUsageVariant(new ExtendFaceCommand.ExtendFaceWithRegionCommand());
   }

   private static class ExtendFaceBasicCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<Integer> xArg = this.withRequiredArg("x", "server.commands.extendface.x.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> yArg = this.withRequiredArg("y", "server.commands.extendface.y.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> zArg = this.withRequiredArg("z", "server.commands.extendface.z.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> normalXArg = this.withRequiredArg("normalX", "server.commands.extendface.normalX.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> normalYArg = this.withRequiredArg("normalY", "server.commands.extendface.normalY.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> normalZArg = this.withRequiredArg("normalZ", "server.commands.extendface.normalZ.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> toolParamArg = this.withRequiredArg("toolParam", "server.commands.extendface.toolParam.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> shapeRangeArg = this.withRequiredArg("shapeRange", "server.commands.extendface.shapeRange.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<String> blockTypeArg = this.withRequiredArg("blockType", "server.commands.extendface.blockType.desc", ArgTypes.STRING);

      public ExtendFaceBasicCommand() {
         super("server.commands.extendface.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
            int x = this.xArg.get(context);
            int y = this.yArg.get(context);
            int z = this.zArg.get(context);
            int normalX = this.normalXArg.get(context);
            int normalY = this.normalYArg.get(context);
            int normalZ = this.normalZArg.get(context);
            int toolParam = this.toolParamArg.get(context);
            int shapeRange = this.shapeRangeArg.get(context);
            String key = this.blockTypeArg.get(context);
            if (BlockType.getAssetMap().getAsset(key) == null) {
               context.sendMessage(Message.translation("server.builderTools.invalidBlockType").param("name", key).param("key", key));
            } else {
               int index = BlockType.getAssetMap().getIndex(key);
               if (index == Integer.MIN_VALUE) {
                  context.sendMessage(Message.translation("server.builderTools.invalidBlockType").param("name", key).param("key", key));
               } else {
                  BuilderToolsPlugin.addToQueue(
                     playerComponent,
                     playerRef,
                     (r, s, componentAccessor) -> s.extendFace(x, y, z, normalX, normalY, normalZ, toolParam, shapeRange, index, null, null, componentAccessor)
                  );
               }
            }
         }
      }
   }

   private static class ExtendFaceWithRegionCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<Integer> xArg = this.withRequiredArg("x", "server.commands.extendface.x.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> yArg = this.withRequiredArg("y", "server.commands.extendface.y.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> zArg = this.withRequiredArg("z", "server.commands.extendface.z.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> normalXArg = this.withRequiredArg("normalX", "server.commands.extendface.normalX.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> normalYArg = this.withRequiredArg("normalY", "server.commands.extendface.normalY.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> normalZArg = this.withRequiredArg("normalZ", "server.commands.extendface.normalZ.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> toolParamArg = this.withRequiredArg("toolParam", "server.commands.extendface.toolParam.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> shapeRangeArg = this.withRequiredArg("shapeRange", "server.commands.extendface.shapeRange.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<String> blockTypeArg = this.withRequiredArg("blockType", "server.commands.extendface.blockType.desc", ArgTypes.STRING);
      @Nonnull
      private final RequiredArg<Integer> xMinArg = this.withRequiredArg("xMin", "server.commands.extendface.xMin.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> yMinArg = this.withRequiredArg("yMin", "server.commands.extendface.yMin.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> zMinArg = this.withRequiredArg("zMin", "server.commands.extendface.zMin.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> xMaxArg = this.withRequiredArg("xMax", "server.commands.extendface.xMax.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> yMaxArg = this.withRequiredArg("yMax", "server.commands.extendface.yMax.desc", ArgTypes.INTEGER);
      @Nonnull
      private final RequiredArg<Integer> zMaxArg = this.withRequiredArg("zMax", "server.commands.extendface.zMax.desc", ArgTypes.INTEGER);

      public ExtendFaceWithRegionCommand() {
         super("server.commands.extendface.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
            int x = this.xArg.get(context);
            int y = this.yArg.get(context);
            int z = this.zArg.get(context);
            int normalX = this.normalXArg.get(context);
            int normalY = this.normalYArg.get(context);
            int normalZ = this.normalZArg.get(context);
            int toolParam = this.toolParamArg.get(context);
            int shapeRange = this.shapeRangeArg.get(context);
            String key = this.blockTypeArg.get(context);
            if (BlockType.getAssetMap().getAsset(key) == null) {
               context.sendMessage(Message.translation("server.builderTools.invalidBlockType").param("name", key).param("key", key));
            } else {
               int index = BlockType.getAssetMap().getIndex(key);
               if (index == Integer.MIN_VALUE) {
                  context.sendMessage(Message.translation("server.builderTools.invalidBlockType").param("name", key).param("key", key));
               } else {
                  int xMin = this.xMinArg.get(context);
                  int yMin = this.yMinArg.get(context);
                  int zMin = this.zMinArg.get(context);
                  int xMax = this.xMaxArg.get(context);
                  int yMax = this.yMaxArg.get(context);
                  int zMax = this.zMaxArg.get(context);
                  Vector3i min = new Vector3i(xMin, yMin, zMin);
                  Vector3i max = new Vector3i(xMax, yMax, zMax);
                  BuilderToolsPlugin.addToQueue(
                     playerComponent,
                     playerRef,
                     (r, s, componentAccessor) -> s.extendFace(x, y, z, normalX, normalY, normalZ, toolParam, shapeRange, index, min, max, componentAccessor)
                  );
               }
            }
         }
      }
   }
}
