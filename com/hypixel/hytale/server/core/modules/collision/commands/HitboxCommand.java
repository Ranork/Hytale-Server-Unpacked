package com.hypixel.hytale.server.core.modules.collision.commands;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.util.message.MessageFormat;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class HitboxCommand extends AbstractCommandCollection {
   public HitboxCommand() {
      super("hitbox", "server.commands.hitbox.desc");
      this.addSubCommand(new HitboxCommand.HitboxExtentsCommand());
      this.addUsageVariant(new HitboxCommand.HitboxGetCommand());
   }

   @Nonnull
   private static Message formatBox(@Nonnull Box box) {
      return Message.translation("server.commands.hitbox.box")
         .param("minX", box.min.x)
         .param("minY", box.min.y)
         .param("minZ", box.min.z)
         .param("maxX", box.max.x)
         .param("maxY", box.max.y)
         .param("maxZ", box.max.z);
   }

   private static class HitboxExtentsCommand extends CommandBase {
      @Nonnull
      private final OptionalArg<Double> thresholdArg = this.withOptionalArg("threshold", "server.commands.hitbox.extents.threshold.desc", ArgTypes.DOUBLE);

      public HitboxExtentsCommand() {
         super("extents", "server.commands.hitbox.extents.desc");
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         BlockTypeAssetMap<String, BlockType> blockTypeAssetMap = BlockType.getAssetMap();
         IndexedLookupTableAssetMap<String, BlockBoundingBoxes> boundingBoxAssetMap = BlockBoundingBoxes.getAssetMap();
         int totalNumberOfFillerBlocks = 0;
         double threshold = this.thresholdArg.provided(context) ? this.thresholdArg.get(context) : 0.5;

         for (BlockType blockType : blockTypeAssetMap.getAssetMap().values()) {
            Box boundingBox = boundingBoxAssetMap.getAsset(blockType.getHitboxTypeIndex()).get(0).getBoundingBox();
            double width = boundingBox.width();
            double height = boundingBox.height();
            double depth = boundingBox.depth();
            int blockWidth = Math.max(MathUtil.floor(width), 1);
            int blockHeight = Math.max(MathUtil.floor(height), 1);
            int blockDepth = Math.max(MathUtil.floor(depth), 1);
            if (width - blockWidth > threshold) {
               blockWidth++;
            }

            if (height - blockHeight > threshold) {
               blockHeight++;
            }

            if (depth - blockDepth > threshold) {
               blockDepth++;
            }

            int numberOfBlocks = blockWidth * blockHeight * blockDepth;
            int numberOfFillerBlocks = numberOfBlocks - 1;
            totalNumberOfFillerBlocks += numberOfFillerBlocks;
         }

         context.sendMessage(
            Message.translation("server.commands.hitbox.extentsThresholdNeeded").param("threshold", threshold).param("nb", totalNumberOfFillerBlocks)
         );
      }
   }

   private static class HitboxGetCommand extends CommandBase {
      @Nonnull
      private final RequiredArg<String> hitboxArg = this.withRequiredArg("hitbox", "server.commands.hitbox.hitbox.desc", ArgTypes.STRING);

      public HitboxGetCommand() {
         super("server.commands.hitbox.desc");
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         String name = this.hitboxArg.get(context);
         BlockBoundingBoxes boundingBox = BlockBoundingBoxes.getAssetMap().getAsset(name);
         if (boundingBox != null) {
            BlockBoundingBoxes.RotatedVariantBoxes rotated = boundingBox.get(Rotation.None, Rotation.None, Rotation.None);
            context.sendMessage(Message.translation("server.commands.hitbox.boundingBox").param("box", HitboxCommand.formatBox(rotated.getBoundingBox())));
            Box[] details = rotated.getDetailBoxes();
            if (details.length > 0) {
               Message header = Message.translation("server.commands.hitbox.details.header");
               Set<Message> detailMessages = Arrays.stream(details).map(HitboxCommand::formatBox).collect(Collectors.toSet());
               context.sendMessage(MessageFormat.list(header, detailMessages));
            }
         } else {
            context.sendMessage(Message.translation("server.commands.hitbox.notFound").param("name", name));
         }
      }
   }
}
