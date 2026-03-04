package com.hypixel.hytale.server.core.universe.world.commands.block;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockFlipType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.VariantRotation;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EnumArgumentType;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.SelectionManager;
import com.hypixel.hytale.server.core.prefab.selection.SelectionProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class BlockSelectCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final SingleArgumentType<BlockFlipType> BLOCK_FLIP_TYPE = new EnumArgumentType<>(
      "server.commands.parsing.argtype.blockfliptype.name", BlockFlipType.class
   );
   @Nonnull
   private static final SingleArgumentType<VariantRotation> VARIANT_ROTATION = new EnumArgumentType<>(
      "server.commands.parsing.argtype.variantrotation.name", VariantRotation.class
   );
   @Nonnull
   private static final Message MESSAGE_COMMANDS_BLOCK_SELECT_DONE = Message.translation("server.commands.block.select.done");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_BLOCK_SELECT_NO_SELECTION_PROVIDER = Message.translation("server.commands.block.select.noSelectionProvider");
   @Nonnull
   private final OptionalArg<String> regexArg = this.withOptionalArg("regex", "server.commands.block.select.regex.desc", ArgTypes.STRING);
   @Nonnull
   private final FlagArg allFlag = this.withFlagArg("all", "server.commands.block.select.all.desc");
   @Nonnull
   private final OptionalArg<String> sortArg = this.withOptionalArg("sort", "server.commands.block.select.sort.desc", ArgTypes.STRING);
   @Nonnull
   private final OptionalArg<BlockFlipType> flipTypeArg = this.withOptionalArg("fliptype", "server.commands.block.select.fliptype.desc", BLOCK_FLIP_TYPE);
   @Nonnull
   private final OptionalArg<VariantRotation> variantRotationArg = this.withOptionalArg(
      "variantrotation", "server.commands.block.select.variantrotation.desc", VARIANT_ROTATION
   );
   @Nonnull
   private final DefaultArg<Integer> paddingArg = this.withDefaultArg("padding", "server.commands.block.select.padding.desc", ArgTypes.INTEGER, 1, "1");
   @Nonnull
   private final OptionalArg<String> groundArg = this.withOptionalArg("ground", "server.commands.block.select.ground.desc", ArgTypes.STRING);

   public BlockSelectCommand() {
      super("blockselect", "server.commands.block.select.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      SelectionProvider selectionProvider = SelectionManager.getSelectionProvider();
      if (selectionProvider == null) {
         context.sendMessage(MESSAGE_COMMANDS_BLOCK_SELECT_NO_SELECTION_PROVIDER);
      } else {
         Pattern pattern = this.regexArg.provided(context) ? Pattern.compile(this.regexArg.get(context)) : null;
         Stream<Entry<String, BlockType>> stream = BlockType.getAssetMap()
            .getAssetMap()
            .entrySet()
            .parallelStream()
            .filter(e -> !e.getValue().isUnknown())
            .filter(e -> pattern == null || pattern.matcher(e.getKey()).matches());
         if (!this.allFlag.get(context)) {
            stream = stream.filter(e -> !e.getValue().isState());
         }

         if (this.flipTypeArg.provided(context)) {
            BlockFlipType flipType = this.flipTypeArg.get(context);
            stream = stream.filter(e -> e.getValue().getFlipType() == flipType);
         }

         if (this.variantRotationArg.provided(context)) {
            VariantRotation variantRotation = this.variantRotationArg.get(context);
            stream = stream.filter(e -> e.getValue().getVariantRotation() == variantRotation);
         }

         if (this.sortArg.provided(context)) {
            String sort = this.sortArg.get(context);
            if (sort.isEmpty()) {
               stream = stream.sorted(Entry.comparingByKey());
            } else {
               for (String sortType : sort.split(",")) {
                  stream = switch (sortType) {
                     case "key" -> stream.sorted(Entry.comparingByKey());
                     case "name" -> stream.sorted(Entry.comparingByKey());
                     case "reverse" -> stream.sorted(Collections.reverseOrder());
                     default -> throw new GeneralCommandException(
                        Message.translation("server.commands.block.select.invalidSortType").param("sortType", sortType)
                     );
                  };
               }
            }
         }

         List<Entry<String, BlockBoundingBoxes>> blocks = stream.map(
               e -> Map.entry(e.getKey(), BlockBoundingBoxes.getAssetMap().getAsset(e.getValue().getHitboxTypeIndex()))
            )
            .toList();
         context.sendMessage(Message.translation("server.commands.block.select.select").param("count", blocks.size()));
         Box largestBox = new Box();

         for (Entry<String, BlockBoundingBoxes> block : blocks) {
            largestBox.union(block.getValue().get(0).getBoundingBox());
         }

         int paddingSize = this.paddingArg.get(context);
         int sqrt = MathUtil.ceil(Math.sqrt(blocks.size())) + 1;
         int strideX = MathUtil.ceil(largestBox.width()) + paddingSize;
         int strideZ = MathUtil.ceil(largestBox.depth()) + paddingSize;
         int halfStrideX = strideX / 2;
         int halfStrideZ = strideZ / 2;
         double height = largestBox.height();
         String groundBlock;
         if (this.groundArg.provided(context)) {
            groundBlock = this.groundArg.get(context);
         } else {
            String rockStone = "Rock_Stone";
            if (BlockType.getAssetMap().getAsset("Rock_Stone") != null) {
               groundBlock = "Rock_Stone";
            } else {
               groundBlock = "Unknown";
            }
         }

         selectionProvider.computeSelectionCopy(ref, playerComponent, selection -> {
            BlockTypeAssetMap<String, BlockType> blockTypeAssetMap = BlockType.getAssetMap();
            int groundId = blockTypeAssetMap.getIndex(groundBlock);

            for (int x = -paddingSize; x < sqrt * strideX; x++) {
               for (int z = -paddingSize; z < sqrt * strideZ; z++) {
                  selection.addBlockAtWorldPos(x, 0, z, groundId, 0, 0, 0);

                  for (int y = 1; y < height; y++) {
                     selection.addBlockAtWorldPos(x, y, z, 0, 0, 0, 0);
                  }
               }
            }

            for (int i = 0; i < blocks.size(); i++) {
               Entry<String, BlockBoundingBoxes> entry = blocks.get(i);
               BlockBoundingBoxes.RotatedVariantBoxes rotatedBoxes = entry.getValue().get(0);
               Box boundingBox = rotatedBoxes.getBoundingBox();
               int x = i % sqrt * strideX + halfStrideX + MathUtil.floor(boundingBox.middleX());
               int z = i / sqrt * strideZ + halfStrideZ + MathUtil.floor(boundingBox.middleZ());
               int blockId = blockTypeAssetMap.getIndex(entry.getKey());
               selection.addBlockAtWorldPos(x, 1, z, blockId, 0, 0, 0);
               if (entry.getValue().protrudesUnitBox()) {
                  FillerBlockUtil.forEachFillerBlock(rotatedBoxes, (x1, y1, z1) -> {
                     if (x1 != 0 || y1 != 0 || z1 != 0) {
                        int filler = FillerBlockUtil.pack(x1, y1, z1);
                        selection.addBlockAtWorldPos(x + x1, 1 + y1, z + z1, blockId, 0, filler, 0);
                     }
                  });
               }
            }

            context.sendMessage(MESSAGE_COMMANDS_BLOCK_SELECT_DONE);
         }, store);
      }
   }
}
