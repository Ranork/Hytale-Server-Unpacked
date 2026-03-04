package com.hypixel.hytale.server.core.universe.world.commands.block;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.WildcardMatch;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nonnull;

public class BlockRowCommand extends AbstractPlayerCommand {
   private final RequiredArg<String> queryArg = this.withRequiredArg("wildcard block query", "server.commands.block.row.arg.desc", ArgTypes.STRING);
   private static final int MAX_MATCHES = 64;

   public BlockRowCommand() {
      super("row", "server.commands.block.row.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

      assert headRotationComponent != null;

      Vector3d playerPos = transformComponent.getPosition();
      Vector3i axisDirection = getDominantCardinal(headRotationComponent.getDirection());
      String query = context.get(this.queryArg);
      List<BlockType> blockTypes = this.findBlockTypes(query);
      if (blockTypes.isEmpty()) {
         playerRef.sendMessage(Message.translation("server.commands.block.row.nonefound").param("query", query));
         List<String> fuzzyMatches = StringUtil.sortByFuzzyDistance(query, BlockType.getAssetMap().getAssetMap().keySet(), CommandUtil.RECOMMEND_COUNT);
         if (!fuzzyMatches.isEmpty()) {
            playerRef.sendMessage(Message.translation("server.commands.block.row.fuzzymatches").param("choices", fuzzyMatches.toString()));
         }
      } else if (blockTypes.size() >= 64000) {
         playerRef.sendMessage(Message.translation("server.commands.block.row.toomanymatches").param("max", 64));
      } else {
         this.spawnBlocksRow(world, playerPos, axisDirection, blockTypes);
      }
   }

   private void spawnBlocksRow(@Nonnull World world, @Nonnull Vector3d origin, @Nonnull Vector3i direction, @Nonnull List<BlockType> blockTypes) {
      IndexedLookupTableAssetMap<String, BlockBoundingBoxes> boundingBoxes = BlockBoundingBoxes.getAssetMap();
      Axis axis = getAxis(direction);
      int step = 25;

      for (int x = 0; x < blockTypes.size(); x += step) {
         double distance = 1.0;

         for (int i = 0; i < step; i++) {
            BlockType blockType = blockTypes.get(i + x);
            Box boundingBox = boundingBoxes.getAsset(blockType.getHitboxTypeIndex()).get(0).getBoundingBox();
            double dimension = Math.ceil(boundingBox.dimension(axis));
            distance += Math.floor(dimension) + 1.0;
            Vector3i blockPos = origin.clone()
               .add(direction.clone().scale(distance))
               .add(Rotation.Ninety.rotateY(direction, new Vector3i()).scale(x / step * 2))
               .toVector3i();
            long chunkIndex = ChunkUtil.indexChunkFromBlock(blockPos.x, blockPos.z);
            world.getChunkAsync(chunkIndex).thenAccept(chunk -> {
               int settings = 196;
               chunk.setBlock(blockPos.x, blockPos.y, blockPos.z, blockType, 196);
            });
         }
      }
   }

   @Nonnull
   private static Vector3i getDominantCardinal(@Nonnull Vector3d direction) {
      double ax = Math.abs(direction.x);
      double ay = Math.abs(direction.y);
      double az = Math.abs(direction.z);
      if (ax > ay && ax > az) {
         return new Vector3i((int)Math.signum(direction.x), 0, 0);
      } else {
         return ay > az ? new Vector3i(0, (int)Math.signum(direction.y), 0) : new Vector3i(0, 0, (int)Math.signum(direction.z));
      }
   }

   @Nonnull
   private static Axis getAxis(@Nonnull Vector3i direction) {
      if (direction.x != 0) {
         return Axis.X;
      } else {
         return direction.z != 0 ? Axis.Z : Axis.Y;
      }
   }

   @Nonnull
   private List<BlockType> findBlockTypes(String wildcardQuery) {
      List<BlockType> results = new ObjectArrayList();
      BlockTypeAssetMap<String, BlockType> blockTypeAssets = BlockType.getAssetMap();

      for (String blockName : blockTypeAssets.getAssetMap().keySet()) {
         if (WildcardMatch.test(blockName, wildcardQuery)) {
            BlockType blockType = blockTypeAssets.getAsset(blockName);
            results.add(blockType);
         }
      }

      return results.stream().sorted(Comparator.comparing(BlockType::getId)).toList();
   }
}
