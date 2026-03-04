package com.hypixel.hytale.server.core.universe.world.connectedblocks.builtin;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.ConnectedBlockRuleSetType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.connectedblocks.ConnectedBlockRuleSet;
import com.hypixel.hytale.server.core.universe.world.connectedblocks.ConnectedBlocksUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import java.util.Optional;
import javax.annotation.Nullable;

public class StairConnectedBlockRuleSet extends ConnectedBlockRuleSet implements StairLikeConnectedBlockRuleSet {
   public static final String DEFAULT_MATERIAL_NAME = "Stair";
   public static final BuilderCodec<StairConnectedBlockRuleSet> CODEC = BuilderCodec.builder(StairConnectedBlockRuleSet.class, StairConnectedBlockRuleSet::new)
      .append(new KeyedCodec<>("Straight", ConnectedBlockOutput.CODEC), (ruleSet, output) -> ruleSet.straight = output, ruleSet -> ruleSet.straight)
      .addValidator(Validators.nonNull())
      .add()
      .<ConnectedBlockOutput>append(
         new KeyedCodec<>("Corner_Left", ConnectedBlockOutput.CODEC), (ruleSet, output) -> ruleSet.cornerLeft = output, ruleSet -> ruleSet.cornerLeft
      )
      .addValidator(Validators.nonNull())
      .add()
      .<ConnectedBlockOutput>append(
         new KeyedCodec<>("Corner_Right", ConnectedBlockOutput.CODEC), (ruleSet, output) -> ruleSet.cornerRight = output, ruleSet -> ruleSet.cornerRight
      )
      .addValidator(Validators.nonNull())
      .add()
      .append(
         new KeyedCodec<>("Inverted_Corner_Left", ConnectedBlockOutput.CODEC),
         (ruleSet, output) -> ruleSet.invertedCornerLeft = output,
         ruleSet -> ruleSet.invertedCornerLeft
      )
      .add()
      .append(
         new KeyedCodec<>("Inverted_Corner_Right", ConnectedBlockOutput.CODEC),
         (ruleSet, output) -> ruleSet.invertedCornerRight = output,
         ruleSet -> ruleSet.invertedCornerRight
      )
      .add()
      .append(new KeyedCodec<>("MaterialName", Codec.STRING), (ruleSet, materialName) -> ruleSet.materialName = materialName, ruleSet -> ruleSet.materialName)
      .add()
      .build();
   protected Object2IntMap<StairConnectedBlockRuleSet.StairType> stairTypeToBlockId;
   private ConnectedBlockOutput straight;
   private ConnectedBlockOutput cornerLeft;
   private ConnectedBlockOutput cornerRight;
   private ConnectedBlockOutput invertedCornerLeft;
   private ConnectedBlockOutput invertedCornerRight;
   private String materialName = "Stair";
   private Int2ObjectMap<StairConnectedBlockRuleSet.StairType> blockIdToStairType;

   @Nullable
   protected static ObjectIntPair<StairConnectedBlockRuleSet.StairType> getStairData(World world, Vector3i coordinate, @Nullable String requiredMaterialName) {
      WorldChunk chunk = world.getChunkIfLoaded(ChunkUtil.indexChunkFromBlock(coordinate.x, coordinate.z));
      if (chunk == null) {
         return null;
      } else {
         int filler = chunk.getFiller(coordinate.x, coordinate.y, coordinate.z);
         if (filler != 0) {
            return null;
         } else {
            int blockId = chunk.getBlock(coordinate);
            BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
            if (blockType == null) {
               return null;
            } else if (blockType.getConnectedBlockRuleSet() instanceof StairLikeConnectedBlockRuleSet stairRuleSet) {
               String otherMaterialName = stairRuleSet.getMaterialName();
               if (requiredMaterialName != null && otherMaterialName != null && !requiredMaterialName.equals(otherMaterialName)) {
                  return null;
               } else {
                  StairConnectedBlockRuleSet.StairType stairType = stairRuleSet.getStairType(blockId);
                  if (stairType == null) {
                     return null;
                  } else {
                     int rotation = chunk.getRotationIndex(coordinate.x, coordinate.y, coordinate.z);
                     return new ObjectIntImmutablePair(stairType, rotation);
                  }
               }
            } else {
               return null;
            }
         }
      }
   }

   protected static StairConnectedBlockRuleSet.StairConnection getCornerConnection(
      World world,
      StairLikeConnectedBlockRuleSet currentRuleSet,
      Vector3i coordinate,
      Vector3i mutablePos,
      int rotation,
      Rotation currentYaw,
      boolean upsideDown,
      int width
   ) {
      StairConnectedBlockRuleSet.StairConnection backConnection = null;
      mutablePos.assign(Vector3i.NORTH).scale(width);
      currentYaw.rotateY(mutablePos, mutablePos);
      mutablePos.add(coordinate.x, coordinate.y, coordinate.z);
      ObjectIntPair<StairConnectedBlockRuleSet.StairType> backStair = getStairData(world, mutablePos, currentRuleSet.getMaterialName());
      if (backStair == null && width > 1) {
         mutablePos.assign(Vector3i.NORTH).scale(width + 1);
         currentYaw.rotateY(mutablePos, mutablePos);
         mutablePos.add(coordinate.x, coordinate.y, coordinate.z);
         backStair = getStairData(world, mutablePos, currentRuleSet.getMaterialName());
         if (backStair != null && backStair.first() == StairConnectedBlockRuleSet.StairType.STRAIGHT) {
            backStair = null;
         }
      }

      if (backStair != null) {
         StairConnectedBlockRuleSet.StairType otherStairType = (StairConnectedBlockRuleSet.StairType)backStair.left();
         RotationTuple otherStairRotation = RotationTuple.get(backStair.rightInt());
         Rotation otherYaw = otherStairRotation.yaw();
         boolean otherUpsideDown = otherStairRotation.pitch() != Rotation.None;
         if (otherUpsideDown) {
            otherYaw = otherYaw.flip();
         }

         if (canConnectTo(currentYaw, otherYaw, upsideDown, otherUpsideDown)) {
            mutablePos.assign(Vector3i.SOUTH);
            otherYaw.rotateY(mutablePos, mutablePos);
            mutablePos.add(coordinate.x, coordinate.y, coordinate.z);
            ObjectIntPair<StairConnectedBlockRuleSet.StairType> sidewaysStair = getStairData(world, mutablePos, currentRuleSet.getMaterialName());
            if (sidewaysStair == null || sidewaysStair.rightInt() != rotation) {
               backConnection = getConnection(currentYaw, otherYaw, otherStairType, false, upsideDown);
            }
         }
      }

      return backConnection;
   }

   protected static StairConnectedBlockRuleSet.StairConnection getInvertedCornerConnection(
      World world, StairLikeConnectedBlockRuleSet currentRuleSet, Vector3i coordinate, Vector3i mutablePos, Rotation currentYaw, boolean upsideDown
   ) {
      StairConnectedBlockRuleSet.StairConnection frontConnection = null;
      mutablePos.assign(Vector3i.SOUTH);
      currentYaw.rotateY(mutablePos, mutablePos);
      mutablePos.add(coordinate.x, coordinate.y, coordinate.z);
      ObjectIntPair<StairConnectedBlockRuleSet.StairType> frontStair = getStairData(world, mutablePos, currentRuleSet.getMaterialName());
      if (frontStair != null) {
         StairConnectedBlockRuleSet.StairType otherStairType = (StairConnectedBlockRuleSet.StairType)frontStair.left();
         RotationTuple otherStairRotation = RotationTuple.get(frontStair.rightInt());
         Rotation otherYaw = otherStairRotation.yaw();
         boolean otherUpsideDown = otherStairRotation.pitch() != Rotation.None;
         if (otherUpsideDown) {
            otherYaw = otherYaw.flip();
         }

         if (canConnectTo(currentYaw, otherYaw, upsideDown, otherUpsideDown)) {
            frontConnection = getConnection(currentYaw, otherYaw, otherStairType, true, upsideDown);
         }
      }

      return frontConnection;
   }

   private static boolean canConnectTo(Rotation currentYaw, Rotation otherYaw, boolean upsideDown, boolean otherUpsideDown) {
      return otherUpsideDown == upsideDown && otherYaw != currentYaw && otherYaw.add(Rotation.OneEighty) != currentYaw;
   }

   private static StairConnectedBlockRuleSet.StairConnection getConnection(
      Rotation currentYaw, Rotation otherYaw, StairConnectedBlockRuleSet.StairType otherStairType, boolean inverted, boolean upsideDown
   ) {
      if (otherYaw == currentYaw.add(Rotation.Ninety)
         && otherStairType != StairConnectedBlockRuleSet.StairType.invertedCorner(upsideDown ^ inverted)
         && otherStairType != StairConnectedBlockRuleSet.StairType.corner(upsideDown ^ !inverted)) {
         return StairConnectedBlockRuleSet.StairConnection.CORNER_LEFT;
      } else {
         return otherYaw == currentYaw.subtract(Rotation.Ninety)
               && otherStairType != StairConnectedBlockRuleSet.StairType.invertedCorner(upsideDown ^ !inverted)
               && otherStairType != StairConnectedBlockRuleSet.StairType.corner(upsideDown ^ inverted)
            ? StairConnectedBlockRuleSet.StairConnection.CORNER_RIGHT
            : null;
      }
   }

   @Override
   public boolean onlyUpdateOnPlacement() {
      return false;
   }

   @Override
   public void updateCachedBlockTypes(BlockType baseBlockType, BlockTypeAssetMap<String, BlockType> assetMap) {
      int baseIndex = assetMap.getIndex(baseBlockType.getId());
      Int2ObjectMap<StairConnectedBlockRuleSet.StairType> blockIdToStairType = new Int2ObjectOpenHashMap();
      Object2IntMap<StairConnectedBlockRuleSet.StairType> stairTypeToBlockId = new Object2IntOpenHashMap();
      stairTypeToBlockId.defaultReturnValue(baseIndex);
      ConnectedBlockOutput[] outputs = new ConnectedBlockOutput[]{
         this.straight, this.cornerLeft, this.cornerRight, this.invertedCornerLeft, this.invertedCornerRight
      };
      StairConnectedBlockRuleSet.StairType[] stairTypes = StairConnectedBlockRuleSet.StairType.VALUES;

      for (int i = 0; i < outputs.length; i++) {
         ConnectedBlockOutput output = outputs[i];
         if (output != null) {
            int index = output.resolve(baseBlockType, assetMap);
            if (index != -1) {
               blockIdToStairType.put(index, stairTypes[i]);
               stairTypeToBlockId.put(stairTypes[i], index);
            }
         }
      }

      this.blockIdToStairType = blockIdToStairType;
      this.stairTypeToBlockId = stairTypeToBlockId;
   }

   @Override
   public StairConnectedBlockRuleSet.StairType getStairType(int blockId) {
      return (StairConnectedBlockRuleSet.StairType)this.blockIdToStairType.get(blockId);
   }

   @Nullable
   @Override
   public String getMaterialName() {
      return this.materialName;
   }

   public BlockType getStairBlockType(StairConnectedBlockRuleSet.StairType stairType) {
      if (this.stairTypeToBlockId == null) {
         return null;
      } else {
         int resultingBlockTypeIndex = this.stairTypeToBlockId.getInt(stairType);
         return BlockType.getAssetMap().getAsset(resultingBlockTypeIndex);
      }
   }

   @Override
   public Optional<ConnectedBlocksUtil.ConnectedBlockResult> getConnectedBlockType(
      World world, Vector3i coordinate, BlockType currentBlockType, int rotation, Vector3i placementNormal, boolean isPlacement
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
      StairConnectedBlockRuleSet.StairConnection frontConnection = getInvertedCornerConnection(world, this, coordinate, mutablePos, currentYaw, upsideDown);
      if (frontConnection != null) {
         resultingStair = frontConnection.getStairType(true);
      }

      StairConnectedBlockRuleSet.StairConnection backConnection = getCornerConnection(world, this, coordinate, mutablePos, rotation, currentYaw, upsideDown, 1);
      if (backConnection != null) {
         resultingStair = backConnection.getStairType(false);
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

      int resultingBlockTypeIndex = this.stairTypeToBlockId.getInt(resultingStair);
      BlockType resultingBlockType = BlockType.getAssetMap().getAsset(resultingBlockTypeIndex);
      if (resultingBlockType == null) {
         return Optional.empty();
      } else {
         String resultingBlockTypeKey = resultingBlockType.getId();
         return Optional.of(new ConnectedBlocksUtil.ConnectedBlockResult(resultingBlockTypeKey, rotation));
      }
   }

   @Nullable
   @Override
   public com.hypixel.hytale.protocol.ConnectedBlockRuleSet toPacket(BlockTypeAssetMap<String, BlockType> assetMap) {
      com.hypixel.hytale.protocol.ConnectedBlockRuleSet packet = new com.hypixel.hytale.protocol.ConnectedBlockRuleSet();
      packet.type = ConnectedBlockRuleSetType.Stair;
      packet.stair = this.toProtocol(assetMap);
      return packet;
   }

   public com.hypixel.hytale.protocol.StairConnectedBlockRuleSet toProtocol(BlockTypeAssetMap<String, BlockType> assetMap) {
      com.hypixel.hytale.protocol.StairConnectedBlockRuleSet stairPacket = new com.hypixel.hytale.protocol.StairConnectedBlockRuleSet();
      stairPacket.straightBlockId = this.getBlockIdForStairType(StairConnectedBlockRuleSet.StairType.STRAIGHT, assetMap);
      stairPacket.cornerLeftBlockId = this.getBlockIdForStairType(StairConnectedBlockRuleSet.StairType.CORNER_LEFT, assetMap);
      stairPacket.cornerRightBlockId = this.getBlockIdForStairType(StairConnectedBlockRuleSet.StairType.CORNER_RIGHT, assetMap);
      stairPacket.invertedCornerLeftBlockId = this.getBlockIdForStairType(StairConnectedBlockRuleSet.StairType.INVERTED_CORNER_LEFT, assetMap);
      stairPacket.invertedCornerRightBlockId = this.getBlockIdForStairType(StairConnectedBlockRuleSet.StairType.INVERTED_CORNER_RIGHT, assetMap);
      stairPacket.materialName = this.materialName;
      return stairPacket;
   }

   private int getBlockIdForStairType(StairConnectedBlockRuleSet.StairType stairType, BlockTypeAssetMap<String, BlockType> assetMap) {
      BlockType blockType = this.getStairBlockType(stairType);
      return blockType == null ? -1 : assetMap.getIndex(blockType.getId());
   }

   protected static enum StairConnection {
      CORNER_LEFT,
      CORNER_RIGHT;

      public StairConnectedBlockRuleSet.StairType getStairType(boolean inverted) {
         return switch (this) {
            case CORNER_LEFT -> inverted ? StairConnectedBlockRuleSet.StairType.INVERTED_CORNER_LEFT : StairConnectedBlockRuleSet.StairType.CORNER_LEFT;
            case CORNER_RIGHT -> inverted ? StairConnectedBlockRuleSet.StairType.INVERTED_CORNER_RIGHT : StairConnectedBlockRuleSet.StairType.CORNER_RIGHT;
         };
      }
   }

   public static enum StairType {
      STRAIGHT,
      CORNER_LEFT,
      CORNER_RIGHT,
      INVERTED_CORNER_LEFT,
      INVERTED_CORNER_RIGHT;

      private static final StairConnectedBlockRuleSet.StairType[] VALUES = values();

      public static StairConnectedBlockRuleSet.StairType corner(boolean right) {
         return right ? CORNER_RIGHT : CORNER_LEFT;
      }

      public static StairConnectedBlockRuleSet.StairType invertedCorner(boolean right) {
         return right ? INVERTED_CORNER_RIGHT : INVERTED_CORNER_LEFT;
      }

      public boolean isCorner() {
         return this == CORNER_LEFT || this == CORNER_RIGHT;
      }

      public boolean isInvertedCorner() {
         return this == INVERTED_CORNER_LEFT || this == INVERTED_CORNER_RIGHT;
      }

      public boolean isLeft() {
         return this == CORNER_LEFT || this == INVERTED_CORNER_LEFT;
      }

      public boolean isRight() {
         return this == CORNER_RIGHT || this == INVERTED_CORNER_RIGHT;
      }
   }
}
