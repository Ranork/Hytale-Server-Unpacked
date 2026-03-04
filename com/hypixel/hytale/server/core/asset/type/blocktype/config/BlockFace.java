package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockNeighbor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum BlockFace {
   UP(BlockFace.FaceConnectionType.FLIP, BlockNeighbor.Up, Vector3i.UP),
   DOWN(BlockFace.FaceConnectionType.FLIP, BlockNeighbor.Down, Vector3i.DOWN),
   NORTH(BlockFace.FaceConnectionType.FLIP, BlockNeighbor.North, Vector3i.NORTH),
   EAST(BlockFace.FaceConnectionType.FLIP, BlockNeighbor.East, Vector3i.EAST),
   SOUTH(BlockFace.FaceConnectionType.FLIP, BlockNeighbor.South, Vector3i.SOUTH),
   WEST(BlockFace.FaceConnectionType.FLIP, BlockNeighbor.West, Vector3i.WEST),
   UP_NORTH(BlockFace.FaceConnectionType.ROTATE_X, BlockNeighbor.UpNorth, UP, NORTH),
   UP_SOUTH(BlockFace.FaceConnectionType.ROTATE_X, BlockNeighbor.UpSouth, UP, SOUTH),
   UP_EAST(BlockFace.FaceConnectionType.ROTATE_Z, BlockNeighbor.UpEast, UP, EAST),
   UP_WEST(BlockFace.FaceConnectionType.ROTATE_Z, BlockNeighbor.UpWest, UP, WEST),
   DOWN_NORTH(BlockFace.FaceConnectionType.ROTATE_X, BlockNeighbor.DownNorth, DOWN, NORTH),
   DOWN_SOUTH(BlockFace.FaceConnectionType.ROTATE_X, BlockNeighbor.DownSouth, DOWN, SOUTH),
   DOWN_EAST(BlockFace.FaceConnectionType.ROTATE_Z, BlockNeighbor.DownEast, DOWN, EAST),
   DOWN_WEST(BlockFace.FaceConnectionType.ROTATE_Z, BlockNeighbor.DownWest, DOWN, WEST),
   NORTH_EAST(BlockFace.FaceConnectionType.ROTATE_Y, BlockNeighbor.NorthEast, NORTH, EAST),
   SOUTH_EAST(BlockFace.FaceConnectionType.ROTATE_Y, BlockNeighbor.SouthEast, SOUTH, EAST),
   SOUTH_WEST(BlockFace.FaceConnectionType.ROTATE_Y, BlockNeighbor.SouthWest, SOUTH, WEST),
   NORTH_WEST(BlockFace.FaceConnectionType.ROTATE_Y, BlockNeighbor.NorthWest, NORTH, WEST),
   UP_NORTH_EAST(BlockFace.FaceConnectionType.ROTATE_ALL, BlockNeighbor.UpNorthEast, UP, NORTH, EAST),
   UP_SOUTH_EAST(BlockFace.FaceConnectionType.ROTATE_ALL, BlockNeighbor.UpSouthEast, UP, SOUTH, EAST),
   UP_SOUTH_WEST(BlockFace.FaceConnectionType.ROTATE_ALL, BlockNeighbor.UpSouthWest, UP, SOUTH, WEST),
   UP_NORTH_WEST(BlockFace.FaceConnectionType.ROTATE_ALL, BlockNeighbor.UpNorthWest, UP, NORTH, WEST),
   DOWN_NORTH_EAST(BlockFace.FaceConnectionType.ROTATE_ALL, BlockNeighbor.DownNorthEast, DOWN, NORTH, EAST),
   DOWN_SOUTH_EAST(BlockFace.FaceConnectionType.ROTATE_ALL, BlockNeighbor.DownSouthEast, DOWN, SOUTH, EAST),
   DOWN_SOUTH_WEST(BlockFace.FaceConnectionType.ROTATE_ALL, BlockNeighbor.DownSouthWest, DOWN, SOUTH, WEST),
   DOWN_NORTH_WEST(BlockFace.FaceConnectionType.ROTATE_ALL, BlockNeighbor.DownNorthWest, DOWN, NORTH, WEST);

   public static final EnumCodec<BlockFace> CODEC = new EnumCodec<>(BlockFace.class);
   public static final BlockFace[] VALUES = values();
   @Nonnull
   private static final Map<Vector3i, BlockFace> DIRECTION_MAP = new Object2ObjectOpenHashMap();
   private final BlockFace.FaceConnectionType faceConnectionType;
   @Nonnull
   private final BlockFace[] components;
   private final Vector3i direction;
   private final BlockNeighbor blockNeighbor;
   private BlockFace[] connectingFaces;
   private Vector3i[] connectingFaceOffsets;

   private BlockFace(BlockFace.FaceConnectionType faceConnectionType, BlockNeighbor blockNeighbor, Vector3i direction) {
      this.faceConnectionType = faceConnectionType;
      this.direction = direction;
      this.blockNeighbor = blockNeighbor;
      this.components = new BlockFace[0];
   }

   private BlockFace(BlockFace.FaceConnectionType faceConnectionType, BlockNeighbor blockNeighbor, @Nonnull BlockFace... components) {
      this.faceConnectionType = faceConnectionType;
      this.components = components;

      for (BlockFace component : components) {
         if (component.components.length > 0) {
            throw new IllegalArgumentException("Only the base BlockFace's can be used as components to make other block faces");
         }
      }

      this.direction = new Vector3i();

      for (BlockFace componentx : components) {
         this.direction.add(componentx.direction);
      }

      this.blockNeighbor = blockNeighbor;
   }

   public BlockFace.FaceConnectionType getFaceConnectionType() {
      return this.faceConnectionType;
   }

   @Nonnull
   public BlockFace[] getComponents() {
      return this.components;
   }

   public Vector3i getDirection() {
      return this.direction;
   }

   public BlockFace[] getConnectingFaces() {
      return this.connectingFaces;
   }

   public Vector3i[] getConnectingFaceOffsets() {
      return this.connectingFaceOffsets;
   }

   @Nonnull
   private BlockFace[] getConnectingFaces0() {
      switch (this.faceConnectionType) {
         case FLIP:
            return new BlockFace[]{flip(this)};
         case ROTATE_X: {
            BlockFace[] blockFaces = new BlockFace[3];
            blockFaces[0] = rotate(this, Rotation.Ninety, Rotation.None, Rotation.None);
            blockFaces[1] = rotate(this, Rotation.OneEighty, Rotation.None, Rotation.None);
            blockFaces[2] = rotate(this, Rotation.TwoSeventy, Rotation.None, Rotation.None);
            return blockFaces;
         }
         case ROTATE_Y: {
            BlockFace[] blockFaces = new BlockFace[3];
            blockFaces[0] = rotate(this, Rotation.None, Rotation.Ninety, Rotation.None);
            blockFaces[1] = rotate(this, Rotation.None, Rotation.OneEighty, Rotation.None);
            blockFaces[2] = rotate(this, Rotation.None, Rotation.TwoSeventy, Rotation.None);
            return blockFaces;
         }
         case ROTATE_Z: {
            BlockFace[] blockFaces = new BlockFace[3];
            blockFaces[0] = rotate(this, Rotation.None, Rotation.None, Rotation.Ninety);
            blockFaces[1] = rotate(this, Rotation.None, Rotation.None, Rotation.OneEighty);
            blockFaces[2] = rotate(this, Rotation.None, Rotation.None, Rotation.TwoSeventy);
            return blockFaces;
         }
         case ROTATE_ALL:
            return new BlockFace[]{
               rotate(this, Rotation.Ninety, Rotation.None, Rotation.None),
               rotate(this, Rotation.OneEighty, Rotation.None, Rotation.None),
               rotate(this, Rotation.None, Rotation.Ninety, Rotation.None),
               rotate(this, Rotation.None, Rotation.OneEighty, Rotation.None),
               rotate(this, Rotation.None, Rotation.TwoSeventy, Rotation.None),
               rotate(this, Rotation.None, Rotation.None, Rotation.OneEighty),
               flip(this)
            };
         default:
            throw new IllegalArgumentException("Unknown FaceConnectionType " + this.faceConnectionType);
      }
   }

   @Nonnull
   private Vector3i directionTo(@Nonnull BlockFace connectingFace) {
      Vector3i vector3i = new Vector3i();
      if (this.direction.getX() == -connectingFace.direction.getX()) {
         vector3i.setX(this.direction.getX());
      }

      if (this.direction.getY() == -connectingFace.direction.getY()) {
         vector3i.setY(this.direction.getY());
      }

      if (this.direction.getZ() == -connectingFace.direction.getZ()) {
         vector3i.setZ(this.direction.getZ());
      }

      return vector3i;
   }

   public static BlockFace lookup(Vector3i direction) {
      return DIRECTION_MAP.get(direction);
   }

   public static BlockFace rotate(@Nonnull BlockFace blockFace, @Nonnull Rotation rotationYaw, @Nonnull Rotation rotationPitch) {
      Vector3i rotate = Rotation.rotate(blockFace.direction, rotationYaw, rotationPitch);
      return lookup(rotate);
   }

   public static BlockFace rotate(@Nonnull BlockFace blockFace, @Nonnull Rotation rotationX, @Nonnull Rotation rotationY, @Nonnull Rotation rotationZ) {
      Vector3i rotate = Rotation.rotate(blockFace.direction, rotationX, rotationY, rotationZ);
      return lookup(rotate);
   }

   public static BlockFace flip(@Nonnull BlockFace blockFace) {
      Vector3i flipped = blockFace.direction.clone().scale(-1);
      return lookup(flipped);
   }

   public BlockNeighbor toProtocolBlockNeighbor() {
      return this.blockNeighbor;
   }

   @Nullable
   public static BlockFace fromProtocolFace(@Nonnull com.hypixel.hytale.protocol.BlockFace face) {
      return switch (face) {
         case Up -> UP;
         case Down -> DOWN;
         case North -> NORTH;
         case South -> SOUTH;
         case East -> EAST;
         case West -> WEST;
         case None -> null;
      };
   }

   @Nonnull
   public static com.hypixel.hytale.protocol.BlockFace toProtocolFace(@Nullable BlockFace face) {
      if (face == null) {
         return com.hypixel.hytale.protocol.BlockFace.None;
      } else {
         return switch (face) {
            case UP -> com.hypixel.hytale.protocol.BlockFace.Up;
            case DOWN -> com.hypixel.hytale.protocol.BlockFace.Down;
            case NORTH -> com.hypixel.hytale.protocol.BlockFace.North;
            case EAST -> com.hypixel.hytale.protocol.BlockFace.East;
            case SOUTH -> com.hypixel.hytale.protocol.BlockFace.South;
            case WEST -> com.hypixel.hytale.protocol.BlockFace.West;
            default -> throw new IllegalArgumentException("Invalid BlockFace");
         };
      }
   }

   static {
      for (BlockFace blockFace : VALUES) {
         DIRECTION_MAP.put(blockFace.direction, blockFace);
      }

      for (BlockFace blockFace : VALUES) {
         blockFace.connectingFaces = blockFace.getConnectingFaces0();
         blockFace.connectingFaceOffsets = new Vector3i[blockFace.connectingFaces.length];

         for (int i = 0; i < blockFace.connectingFaces.length; i++) {
            blockFace.connectingFaceOffsets[i] = blockFace.directionTo(blockFace.connectingFaces[i]);
         }
      }
   }

   static enum FaceConnectionType {
      FLIP,
      ROTATE_X,
      ROTATE_Y,
      ROTATE_Z,
      ROTATE_ALL;
   }
}
