package com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace;

import com.hypixel.hytale.math.vector.Vector3i;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BooleanVoxelSpace implements VoxelSpace<Boolean> {
   protected final int sizeX;
   protected final int sizeY;
   protected final int sizeZ;
   @Nonnull
   protected final int[][] cells;
   protected VoxelCoordinate origin;
   private boolean alignedOriginZ;
   private int originZOffset;

   public BooleanVoxelSpace(int sizeX, int sizeY, int sizeZ, int originX, int originY, int originZ, boolean alignedOriginZ) {
      if (sizeX < 1 || sizeY < 1 || sizeZ < 1) {
         throw new IllegalArgumentException("invalid size " + sizeX + " " + sizeY + " " + sizeZ);
      } else if (alignedOriginZ && !isAlignedOriginZ(originZ)) {
         throw new IllegalArgumentException("unaligned originZ: " + originZ);
      } else {
         this.sizeX = sizeX;
         this.sizeY = sizeY;
         this.sizeZ = sizeZ;
         this.alignedOriginZ = alignedOriginZ;
         int primaryDepth = sizeX * sizeY;
         int secondaryDepth = (sizeZ - 1 >> 5) + 1;
         if (!alignedOriginZ) {
            secondaryDepth++;
         }

         this.cells = new int[primaryDepth][secondaryDepth];
         this.origin = new VoxelCoordinate(originX, originY, originZ);
         this.setOrigin(originX, originY, originZ);
      }
   }

   public BooleanVoxelSpace(int sizeX, int sizeY, int sizeZ, int originX, int originY, int originZ) {
      this(sizeX, sizeY, sizeZ, originX, originY, originZ, false);
   }

   public BooleanVoxelSpace(int sizeX, int sizeY, int sizeZ) {
      this(sizeX, sizeY, sizeZ, 0, 0, 0);
   }

   public BooleanVoxelSpace(int sizeX, int sizeY, int sizeZ, boolean forceAlignOriginZ) {
      this(sizeX, sizeY, sizeZ, 0, 0, 0, forceAlignOriginZ);
   }

   @Override
   public int sizeX() {
      return this.sizeX;
   }

   @Override
   public int sizeY() {
      return this.sizeY;
   }

   @Override
   public int sizeZ() {
      return this.sizeZ;
   }

   @Override
   public void pasteFrom(@Nonnull VoxelSpace<Boolean> source) {
      if (source == null) {
         throw new NullPointerException();
      } else {
         for (int x = source.minX(); x < source.maxX(); x++) {
            for (int y = source.minY(); y < source.maxY(); y++) {
               for (int z = source.minZ(); z < source.maxZ(); z++) {
                  this.set(source.getContent(x, y, z), x, y, z);
               }
            }
         }
      }
   }

   private int primaryAddressIndex(int x, int y) {
      return x * this.sizeY + y;
   }

   private int secondaryAddressIndex(int z) {
      z += this.originZOffset;
      return z >> 5;
   }

   private static int setBit(int bits, int index, boolean value) {
      int mask = 1 << index;
      if (!value) {
         bits &= ~mask;
      } else {
         bits |= mask;
      }

      return bits;
   }

   private static boolean getBit(int bits, int index) {
      return (bits >> index & 1) == 1;
   }

   public boolean set(@Nullable Boolean value, int x, int y, int z) {
      if (!this.isInsideSpace(x, y, z)) {
         return false;
      } else {
         if (value == null) {
            value = false;
         }

         int localX = x + this.origin.x;
         int localY = y + this.origin.y;
         int localZ = z + this.origin.z;
         int i = this.primaryAddressIndex(localX, localY);
         int j = this.secondaryAddressIndex(localZ);
         int bitIndex = localZ - j * 32 + this.originZOffset;
         int cell = setBit(this.cells[i][j], bitIndex, value);
         this.cells[i][j] = cell;
         return true;
      }
   }

   public boolean set(Boolean content, @Nonnull Vector3i position) {
      return this.set(content, position.x, position.y, position.z);
   }

   @Nonnull
   public Boolean getContent(int x, int y, int z) {
      if (!this.isInsideSpace(x, y, z)) {
         throw new IndexOutOfBoundsException(
            "Coordinates outside VoxelSpace: "
               + x
               + " "
               + y
               + " "
               + z
               + " constraints "
               + this.minX()
               + " -> "
               + this.maxX()
               + " "
               + this.minY()
               + " -> "
               + this.maxY()
               + " "
               + this.minZ()
               + " -> "
               + this.maxZ()
               + "\n"
               + this.toString()
         );
      } else {
         int localX = x + this.origin.x;
         int localY = y + this.origin.y;
         int localZ = z + this.origin.z;
         int i = this.primaryAddressIndex(localX, localY);
         int j = this.secondaryAddressIndex(localZ);
         int bitIndex = localZ - j * 32 + this.originZOffset;
         return getBit(this.cells[i][j], bitIndex);
      }
   }

   @Nonnull
   public Boolean getContent(@Nonnull Vector3i position) {
      return this.getContent(position.x, position.y, position.z);
   }

   private int globalJ(int globalZ) {
      return globalZ >> 5;
   }

   private int localJ(int globalJ) {
      return globalJ - this.globalJ(-this.origin.z);
   }

   public void deepCopyFrom(@Nonnull BooleanVoxelSpace other) {
      if (other.cells.length != 0) {
         if (other.cells[0].length != 0) {
            if (this.cells.length != 0) {
               if (this.cells[0].length != 0) {
                  int thisGlobalJ = this.globalJ(-this.origin.z);
                  int otherGlobalJ = other.globalJ(-other.origin.z);
                  int minGlobalJ = Math.max(otherGlobalJ, thisGlobalJ);
                  int minThisJ = this.localJ(minGlobalJ);
                  int minOtherJ = other.localJ(minGlobalJ);
                  int maxIterations = Math.min(other.cells[0].length - minOtherJ, this.cells[0].length - minThisJ);
                  int minX = Math.max(this.minX(), other.minX());
                  int minY = Math.max(this.minY(), other.minY());
                  int maxX = Math.min(this.maxX(), other.maxX());
                  int maxY = Math.min(this.maxY(), other.maxY());

                  for (int x = minX; x < maxX; x++) {
                     for (int y = minY; y < maxY; y++) {
                        int thisLocalX = x + this.origin.x;
                        int thisLocalY = y + this.origin.y;
                        int otherLocalX = x + other.origin.x;
                        int otherLocalY = y + other.origin.y;
                        int thisI = this.primaryAddressIndex(thisLocalX, thisLocalY);
                        int otherI = other.primaryAddressIndex(otherLocalX, otherLocalY);
                        int thisJ = minThisJ;
                        int otherJ = minOtherJ;

                        for (int c = 0; c < maxIterations; c++) {
                           this.cells[thisI][thisJ] = other.cells[otherI][otherJ];
                           otherJ++;
                           thisJ++;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void set(Boolean content) {
      for (int x = this.minX(); x < this.maxX(); x++) {
         for (int y = this.minY(); y < this.maxY(); y++) {
            for (int z = this.minZ(); z < this.maxZ(); z++) {
               this.set(content, x, y, z);
            }
         }
      }
   }

   @Override
   public void setOrigin(int x, int y, int z) {
      if (this.alignedOriginZ && z % 32 != 0) {
         throw new IllegalArgumentException("z isn't aligned to 32 bit integer grid: " + z);
      } else {
         this.origin.x = x;
         this.origin.y = y;
         this.origin.z = z;
         this.originZOffset = -this.origin.z - getAlignedZ(-this.origin.z);
      }
   }

   public boolean replace(Boolean replacement, int x, int y, int z, @Nonnull Predicate<Boolean> mask) {
      if (!this.isInsideSpace(x, y, z)) {
         throw new IllegalArgumentException("outside schematic");
      } else if (!mask.test(this.getContent(x, y, z))) {
         return false;
      } else {
         this.set(replacement, x, y, z);
         return true;
      }
   }

   @Nonnull
   VoxelCoordinate getOrigin() {
      return this.origin.clone();
   }

   @Override
   public int getOriginX() {
      return this.origin.x;
   }

   @Override
   public int getOriginY() {
      return this.origin.y;
   }

   @Override
   public int getOriginZ() {
      return this.origin.z;
   }

   @Nonnull
   @Override
   public String getName() {
      return "";
   }

   @Override
   public boolean isInsideSpace(int x, int y, int z) {
      return x + this.origin.x >= 0
         && x + this.origin.x < this.sizeX
         && y + this.origin.y >= 0
         && y + this.origin.y < this.sizeY
         && z + this.origin.z >= 0
         && z + this.origin.z < this.sizeZ;
   }

   @Override
   public boolean isInsideSpace(@Nonnull Vector3i position) {
      return this.isInsideSpace(position.x, position.y, position.z);
   }

   @Override
   public void forEach(@Nonnull VoxelConsumer<? super Boolean> action) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         for (int x = this.minX(); x < this.maxX(); x++) {
            for (int y = this.minY(); y < this.maxY(); y++) {
               for (int z = this.minZ(); z < this.maxZ(); z++) {
                  action.accept(this.getContent(x, y, z), x, y, z);
               }
            }
         }
      }
   }

   @Override
   public int minX() {
      return -this.origin.x;
   }

   @Override
   public int maxX() {
      return this.sizeX - this.origin.x;
   }

   @Override
   public int minY() {
      return -this.origin.y;
   }

   @Override
   public int maxY() {
      return this.sizeY - this.origin.y;
   }

   @Override
   public int minZ() {
      return -this.origin.z;
   }

   @Override
   public int maxZ() {
      return this.sizeZ - this.origin.z;
   }

   @Nonnull
   public BooleanVoxelSpace clone() {
      BooleanVoxelSpace clone = new BooleanVoxelSpace(this.sizeX, this.sizeY, this.sizeZ, this.origin.x, this.origin.y, this.origin.z);
      this.forEach(clone::set);
      return clone;
   }

   private int arrayIndex(int x, int y, int z) {
      return y + x * this.sizeY + z * this.sizeY * this.sizeX;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ArrayVoxelSpace{sizeX="
         + this.sizeX
         + ", sizeY="
         + this.sizeY
         + ", sizeZ="
         + this.sizeZ
         + ", minX="
         + this.minX()
         + ", minY="
         + this.minY()
         + ", minZ="
         + this.minZ()
         + ", maxX="
         + this.maxX()
         + ", maxY="
         + this.maxY()
         + ", maxZ="
         + this.maxZ()
         + ", origin="
         + this.origin
         + "}";
   }

   public static boolean isAlignedOriginZ(int z) {
      return z % 32 == 0;
   }

   public static int getAlignedZ(int z) {
      return z >> 5 << 5;
   }
}
