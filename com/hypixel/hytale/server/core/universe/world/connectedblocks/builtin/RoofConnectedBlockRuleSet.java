package com.hypixel.hytale.server.core.universe.world.connectedblocks.builtin;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.simple.IntegerCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.ConnectedBlockRuleSetType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockFace;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockFaceSupport;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.connectedblocks.ConnectedBlockRuleSet;
import com.hypixel.hytale.server.core.universe.world.connectedblocks.ConnectedBlocksUtil;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

public class RoofConnectedBlockRuleSet extends ConnectedBlockRuleSet implements StairLikeConnectedBlockRuleSet {
   public static final BuilderCodec<RoofConnectedBlockRuleSet> CODEC = BuilderCodec.builder(RoofConnectedBlockRuleSet.class, RoofConnectedBlockRuleSet::new)
      .append(new KeyedCodec<>("Regular", StairConnectedBlockRuleSet.CODEC), (ruleSet, output) -> ruleSet.regular = output, ruleSet -> ruleSet.regular)
      .addValidator(Validators.nonNull())
      .add()
      .append(new KeyedCodec<>("Hollow", StairConnectedBlockRuleSet.CODEC), (ruleSet, output) -> ruleSet.hollow = output, ruleSet -> ruleSet.hollow)
      .add()
      .append(new KeyedCodec<>("Topper", ConnectedBlockOutput.CODEC), (ruleSet, output) -> ruleSet.topper = output, ruleSet -> ruleSet.topper)
      .add()
      .append(new KeyedCodec<>("Width", new IntegerCodec()), (ruleSet, output) -> ruleSet.width = output, ruleSet -> ruleSet.width)
      .add()
      .append(new KeyedCodec<>("MaterialName", Codec.STRING), (ruleSet, materialName) -> ruleSet.materialName = materialName, ruleSet -> ruleSet.materialName)
      .add()
      .build();
   private StairConnectedBlockRuleSet regular;
   private StairConnectedBlockRuleSet hollow;
   private ConnectedBlockOutput topper;
   private String materialName;
   private int width = 1;

   private static StairConnectedBlockRuleSet.StairType getConnectedBlockStairType(
      World world, Vector3i coordinate, StairLikeConnectedBlockRuleSet currentRuleSet, int blockId, int rotation, int width
   ) {
      RotationTuple currentRotation = RotationTuple.get(rotation);
      Rotation currentYaw = currentRotation.yaw();
      Rotation currentPitch = currentRotation.pitch();
      boolean upsideDown = currentPitch != Rotation.None;
      if (upsideDown) {
         currentYaw = currentYaw.flip();
      }

      Vector3i mutablePos = new Vector3i();
      StairConnectedBlockRuleSet.StairType resultingStair = StairConnectedBlockRuleSet.StairType.STRAIGHT;
      StairConnectedBlockRuleSet.StairConnection frontConnection = StairConnectedBlockRuleSet.getInvertedCornerConnection(
         world, currentRuleSet, coordinate, mutablePos, currentYaw, upsideDown
      );
      if (frontConnection != null) {
         boolean valid = isWidthFulfilled(world, coordinate, mutablePos, frontConnection, currentYaw, blockId, rotation, width);
         if (valid) {
            resultingStair = frontConnection.getStairType(true);
         }
      }

      StairConnectedBlockRuleSet.StairConnection backConnection = StairConnectedBlockRuleSet.getCornerConnection(
         world, currentRuleSet, coordinate, mutablePos, rotation, currentYaw, upsideDown, width
      );
      if (backConnection != null) {
         boolean valid = isWidthFulfilled(world, coordinate, mutablePos, backConnection, currentYaw, blockId, rotation, width);
         if (valid) {
            resultingStair = backConnection.getStairType(false);
         }
      }

      if (resultingStair == StairConnectedBlockRuleSet.StairType.STRAIGHT) {
         Vector3i aboveCoordinate = new Vector3i(coordinate).add(0, 1, 0);
         StairConnectedBlockRuleSet.StairConnection resultingConnection = getValleyConnection(
            world, coordinate, aboveCoordinate, currentRuleSet, currentRotation, mutablePos, false, blockId, rotation, width
         );
         if (resultingConnection != null) {
            resultingStair = resultingConnection.getStairType(true);
         }
      }

      if (resultingStair == StairConnectedBlockRuleSet.StairType.STRAIGHT) {
         Vector3i belowCoordinate = new Vector3i(coordinate).add(0, -1, 0);
         StairConnectedBlockRuleSet.StairConnection resultingConnection = getValleyConnection(
            world, coordinate, belowCoordinate, currentRuleSet, currentRotation, mutablePos, true, blockId, rotation, width
         );
         if (resultingConnection != null) {
            resultingStair = resultingConnection.getStairType(false);
         }
      }

      if (upsideDown) {
         resultingStair = switch (resultingStair) {
            case CORNER_LEFT -> StairConnectedBlockRuleSet.StairType.CORNER_RIGHT;
            case CORNER_RIGHT -> StairConnectedBlockRuleSet.StairType.CORNER_LEFT;
            case INVERTED_CORNER_LEFT -> StairConnectedBlockRuleSet.StairType.INVERTED_CORNER_RIGHT;
            case INVERTED_CORNER_RIGHT -> StairConnectedBlockRuleSet.StairType.INVERTED_CORNER_LEFT;
            default -> resultingStair;
         };
      }

      return resultingStair;
   }

   private static boolean isWidthFulfilled(
      World world,
      Vector3i coordinate,
      Vector3i mutablePos,
      StairConnectedBlockRuleSet.StairConnection backConnection,
      Rotation currentYaw,
      int blockId,
      int rotation,
      int width
   ) {
      boolean valid = true;

      for (int i = 0; i < width - 1; i++) {
         mutablePos.assign(backConnection == StairConnectedBlockRuleSet.StairConnection.CORNER_LEFT ? Vector3i.WEST : Vector3i.EAST).scale(i + 1);
         currentYaw.rotateY(mutablePos, mutablePos);
         int requiredFiller = FillerBlockUtil.pack(mutablePos.x, mutablePos.y, mutablePos.z);
         mutablePos.add(coordinate.x, coordinate.y, coordinate.z);
         WorldChunk chunk = world.getChunkIfLoaded(ChunkUtil.indexChunkFromBlock(mutablePos.x, mutablePos.z));
         if (chunk != null) {
            int otherRotation = chunk.getRotationIndex(mutablePos.x, mutablePos.y, mutablePos.z);
            int otherFiller = chunk.getFiller(mutablePos.x, mutablePos.y, mutablePos.z);
            int otherBlockId = chunk.getBlock(mutablePos);
            if ((otherFiller != 0 || otherBlockId != blockId || otherRotation != rotation)
               && (otherFiller != requiredFiller || otherBlockId != blockId || otherRotation != rotation)) {
               valid = false;
               break;
            }
         }
      }

      return valid;
   }

   private static StairConnectedBlockRuleSet.StairConnection getValleyConnection(
      World world,
      Vector3i placementCoordinate,
      Vector3i checkCoordinate,
      StairLikeConnectedBlockRuleSet currentRuleSet,
      RotationTuple rotation,
      Vector3i mutablePos,
      boolean reverse,
      int blockId,
      int blockRotation,
      int width
   ) {
      Rotation yaw = rotation.yaw();
      mutablePos.assign(reverse ? Vector3i.SOUTH : Vector3i.NORTH).scale(width);
      yaw.rotateY(mutablePos, mutablePos);
      mutablePos.add(checkCoordinate.x, checkCoordinate.y, checkCoordinate.z);
      ObjectIntPair<StairConnectedBlockRuleSet.StairType> backStair = StairConnectedBlockRuleSet.getStairData(
         world, mutablePos, currentRuleSet.getMaterialName()
      );
      if (backStair == null) {
         return null;
      } else {
         boolean backConnection = reverse
            ? isTopperConnectionCompatible(rotation, backStair, Rotation.None)
            : isValleyConnectionCompatible(rotation, backStair, Rotation.None, false);
         if (!backConnection) {
            return null;
         } else {
            mutablePos.assign(reverse ? Vector3i.EAST : Vector3i.WEST).scale(width);
            yaw.rotateY(mutablePos, mutablePos);
            mutablePos.add(checkCoordinate.x, checkCoordinate.y, checkCoordinate.z);
            ObjectIntPair<StairConnectedBlockRuleSet.StairType> leftStair = StairConnectedBlockRuleSet.getStairData(
               world, mutablePos, currentRuleSet.getMaterialName()
            );
            mutablePos.assign(reverse ? Vector3i.WEST : Vector3i.EAST).scale(width);
            yaw.rotateY(mutablePos, mutablePos);
            mutablePos.add(checkCoordinate.x, checkCoordinate.y, checkCoordinate.z);
            ObjectIntPair<StairConnectedBlockRuleSet.StairType> rightStair = StairConnectedBlockRuleSet.getStairData(
               world, mutablePos, currentRuleSet.getMaterialName()
            );
            boolean leftConnection = reverse
               ? isTopperConnectionCompatible(rotation, leftStair, Rotation.Ninety)
               : isValleyConnectionCompatible(rotation, leftStair, Rotation.Ninety, false);
            boolean rightConnection = reverse
               ? isTopperConnectionCompatible(rotation, rightStair, Rotation.TwoSeventy)
               : isValleyConnectionCompatible(rotation, rightStair, Rotation.TwoSeventy, false);
            if (leftConnection == rightConnection) {
               return null;
            } else {
               StairConnectedBlockRuleSet.StairConnection connection = leftConnection
                  ? StairConnectedBlockRuleSet.StairConnection.CORNER_LEFT
                  : StairConnectedBlockRuleSet.StairConnection.CORNER_RIGHT;
               return !isWidthFulfilled(world, placementCoordinate, mutablePos, connection, yaw, blockId, blockRotation, width) ? null : connection;
            }
         }
      }
   }

   private static boolean isTopperConnectionCompatible(
      RotationTuple rotation, ObjectIntPair<StairConnectedBlockRuleSet.StairType> otherStair, Rotation yawOffset
   ) {
      return isValleyConnectionCompatible(rotation, otherStair, yawOffset, true);
   }

   private static boolean canBeTopper(
      World world, Vector3i coordinate, StairLikeConnectedBlockRuleSet currentRuleSet, RotationTuple rotation, Vector3i mutablePos
   ) {
      Rotation yaw = rotation.yaw();
      Vector3i[] directions = new Vector3i[]{Vector3i.NORTH, Vector3i.SOUTH, Vector3i.EAST, Vector3i.WEST};
      Rotation[] yawOffsets = new Rotation[]{Rotation.OneEighty, Rotation.None, Rotation.Ninety, Rotation.TwoSeventy};

      for (int i = 0; i < directions.length; i++) {
         mutablePos.assign(directions[i]);
         yaw.rotateY(mutablePos, mutablePos);
         mutablePos.add(coordinate.x, coordinate.y, coordinate.z);
         ObjectIntPair<StairConnectedBlockRuleSet.StairType> stair = StairConnectedBlockRuleSet.getStairData(
            world, mutablePos, currentRuleSet.getMaterialName()
         );
         if (stair == null || !isTopperConnectionCompatible(rotation, stair, yawOffsets[i])) {
            return false;
         }
      }

      return true;
   }

   private static boolean isValleyConnectionCompatible(
      RotationTuple rotation, ObjectIntPair<StairConnectedBlockRuleSet.StairType> otherStair, Rotation yawOffset, boolean inverted
   ) {
      Rotation targetYaw = rotation.yaw().add(yawOffset);
      if (otherStair == null) {
         return false;
      } else {
         RotationTuple stairRotation = RotationTuple.get(otherStair.rightInt());
         StairConnectedBlockRuleSet.StairType otherStairType = (StairConnectedBlockRuleSet.StairType)otherStair.first();
         if (stairRotation.pitch() != rotation.pitch()) {
            return false;
         } else if (inverted && otherStairType.isCorner()) {
            return false;
         } else {
            return !inverted && otherStairType.isInvertedCorner()
               ? false
               : stairRotation.yaw() == targetYaw
                  || otherStairType == StairConnectedBlockRuleSet.StairConnection.CORNER_RIGHT.getStairType(inverted)
                     && stairRotation.yaw() == targetYaw.add(Rotation.Ninety)
                  || otherStairType == StairConnectedBlockRuleSet.StairConnection.CORNER_LEFT.getStairType(inverted)
                     && stairRotation.yaw() == targetYaw.add(Rotation.TwoSeventy);
         }
      }
   }

   @Override
   public boolean onlyUpdateOnPlacement() {
      return false;
   }

   @Override
   public Optional<ConnectedBlocksUtil.ConnectedBlockResult> getConnectedBlockType(
      World world, Vector3i coordinate, BlockType blockType, int rotation, Vector3i placementNormal, boolean isPlacement
   ) {
      WorldChunk chunk = world.getChunkIfLoaded(ChunkUtil.indexChunkFromBlock(coordinate.x, coordinate.z));
      if (chunk == null) {
         return Optional.empty();
      } else {
         int belowBlockId = chunk.getBlock(coordinate.x, coordinate.y - 1, coordinate.z);
         BlockType belowBlockType = BlockType.getAssetMap().getAsset(belowBlockId);
         int belowBlockRotation = chunk.getRotationIndex(coordinate.x, coordinate.y - 1, coordinate.z);
         boolean hollow = true;
         if (belowBlockType != null) {
            Map<BlockFace, BlockFaceSupport[]> supporting = belowBlockType.getSupporting(belowBlockRotation);
            if (supporting != null) {
               BlockFaceSupport[] support = supporting.get(BlockFace.UP);
               hollow = support == null;
            }
         }

         int blockId = BlockType.getAssetMap().getIndex(blockType.getId());
         StairConnectedBlockRuleSet.StairType stairType = getConnectedBlockStairType(world, coordinate, this, blockId, rotation, this.width);
         if (this.topper != null && stairType == StairConnectedBlockRuleSet.StairType.STRAIGHT) {
            Vector3i belowCoordinate = new Vector3i(coordinate).add(0, -1, 0);
            RotationTuple currentRotation = RotationTuple.get(rotation);
            currentRotation = RotationTuple.of(Rotation.None, currentRotation.pitch(), currentRotation.roll());
            Vector3i mutablePos = new Vector3i();
            boolean topper = canBeTopper(world, belowCoordinate, this, currentRotation, mutablePos);
            if (topper) {
               BlockType topperBlockType = BlockType.getAssetMap().getAsset(this.topper.blockTypeKey);
               if (topperBlockType != null) {
                  return Optional.of(new ConnectedBlocksUtil.ConnectedBlockResult(topperBlockType.getId(), rotation));
               }
            }
         }

         if (this.hollow != null && hollow) {
            BlockType hollowBlockType = this.hollow.getStairBlockType(stairType);
            if (hollowBlockType != null) {
               return Optional.of(new ConnectedBlocksUtil.ConnectedBlockResult(hollowBlockType.getId(), rotation));
            }
         }

         BlockType regularBlockType = this.regular.getStairBlockType(stairType);
         if (regularBlockType != null) {
            ConnectedBlocksUtil.ConnectedBlockResult result = new ConnectedBlocksUtil.ConnectedBlockResult(regularBlockType.getId(), rotation);
            if (this.regular != null && this.width > 0) {
               StairConnectedBlockRuleSet.StairType existingStairType = this.regular.getStairType(BlockType.getAssetMap().getIndex(blockType.getId()));
               if (existingStairType != null && existingStairType != StairConnectedBlockRuleSet.StairType.STRAIGHT) {
                  int previousWidth = existingStairType.isLeft() ? -(this.width - 1) : (existingStairType.isRight() ? this.width - 1 : 0);
                  int newWidth = stairType.isLeft() ? -(this.width - 1) : (stairType.isRight() ? this.width - 1 : 0);
                  if (newWidth != previousWidth) {
                     Vector3i mutablePos = new Vector3i();
                     Rotation currentYaw = RotationTuple.get(rotation).yaw();
                     mutablePos.assign(Vector3i.EAST).scale(previousWidth);
                     currentYaw.rotateY(mutablePos, mutablePos);
                     result.addAdditionalBlock(mutablePos, regularBlockType.getId(), rotation);
                  }
               }
            }

            return Optional.of(result);
         } else {
            return Optional.empty();
         }
      }
   }

   @Override
   public void updateCachedBlockTypes(BlockType baseBlockType, BlockTypeAssetMap<String, BlockType> assetMap) {
      if (this.regular != null) {
         this.regular.updateCachedBlockTypes(baseBlockType, assetMap);
      }

      if (this.hollow != null) {
         this.hollow.updateCachedBlockTypes(baseBlockType, assetMap);
      }

      if (this.topper != null) {
         this.topper.resolve(baseBlockType, assetMap);
      }
   }

   @Override
   public StairConnectedBlockRuleSet.StairType getStairType(int blockId) {
      StairConnectedBlockRuleSet.StairType regularStairType = this.regular.getStairType(blockId);
      if (regularStairType != null) {
         return regularStairType;
      } else {
         return this.hollow != null ? this.hollow.getStairType(blockId) : null;
      }
   }

   @Nullable
   @Override
   public String getMaterialName() {
      return this.materialName;
   }

   @Nullable
   @Override
   public com.hypixel.hytale.protocol.ConnectedBlockRuleSet toPacket(BlockTypeAssetMap<String, BlockType> assetMap) {
      com.hypixel.hytale.protocol.ConnectedBlockRuleSet packet = new com.hypixel.hytale.protocol.ConnectedBlockRuleSet();
      packet.type = ConnectedBlockRuleSetType.Roof;
      com.hypixel.hytale.protocol.RoofConnectedBlockRuleSet roofPacket = new com.hypixel.hytale.protocol.RoofConnectedBlockRuleSet();
      if (this.regular != null) {
         roofPacket.regular = this.regular.toProtocol(assetMap);
      }

      if (this.hollow != null) {
         roofPacket.hollow = this.hollow.toProtocol(assetMap);
      }

      if (this.topper != null) {
         roofPacket.topperBlockId = assetMap.getIndex(this.topper.blockTypeKey);
      } else {
         roofPacket.topperBlockId = -1;
      }

      roofPacket.width = this.width;
      roofPacket.materialName = this.materialName;
      packet.roof = roofPacket;
      return packet;
   }
}
