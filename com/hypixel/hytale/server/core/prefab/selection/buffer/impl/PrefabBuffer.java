package com.hypixel.hytale.server.core.prefab.selection.buffer.impl;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.modules.prefabspawner.PrefabSpawnerBlock;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.core.prefab.PrefabWeights;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferCall;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.io.MemorySegmentUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3i;

public class PrefabBuffer {
   public static final float DEFAULT_CHANCE = 1.0F;
   @Nonnull
   private final Vector3i anchor;
   @Nonnull
   private final Vector3i min;
   @Nonnull
   private final Vector3i max;
   @Nonnull
   private final Int2ObjectMap<PrefabBufferColumn> columns;
   @Nonnull
   private final PrefabBuffer.ChildPrefab[] childPrefabs;

   private PrefabBuffer(
      @Nonnull Vector3i anchor,
      @Nonnull Vector3i min,
      @Nonnull Vector3i max,
      @Nonnull Int2ObjectMap<PrefabBufferColumn> columns,
      @Nonnull PrefabBuffer.ChildPrefab[] childPrefabs
   ) {
      this.anchor = anchor;
      this.min = min;
      this.max = max;
      this.columns = columns;
      this.childPrefabs = childPrefabs;
   }

   @Nonnull
   public static PrefabBuffer.Builder newBuilder() {
      return new PrefabBuffer.Builder();
   }

   public int getAnchorX() {
      return this.anchor.x();
   }

   public int getAnchorY() {
      return this.anchor.y();
   }

   public int getAnchorZ() {
      return this.anchor.z();
   }

   @Nonnull
   public PrefabBuffer.PrefabBufferAccessor newAccess() {
      return new PrefabBuffer.PrefabBufferAccessor(this);
   }

   public interface BlockMaskConstants {
      int ID_IS_BYTE = 1;
      int ID_IS_SHORT = 2;
      int ID_IS_INT = 3;
      int ID_MASK = 3;
      int HAS_CHANCE = 4;
      int OFFSET_IS_BYTE = 8;
      int OFFSET_IS_SHORT = 16;
      int OFFSET_IS_INT = 24;
      int OFFSET_MASK = 24;
      int HAS_COMPONENTS = 32;
      int FLUID_IS_BYTE = 64;
      int FLUID_IS_SHORT = 128;
      int FLUID_IS_INT = 192;
      int FLUID_MASK = 192;
      int SUPPORT_MASK = 3840;
      int SUPPORT_OFFSET = 8;
      int HAS_FILLER = 4096;
      int HAS_ROTATION = 8192;

      static int getBlockMask(
         int blockBytes, int fluidBytes, boolean chance, int offsetBytes, @Nullable Holder<ChunkStore> holder, byte supportValue, int rotation, int filler
      ) {
         int mask = 0;

         mask = switch (blockBytes) {
            case 0 -> {
            }
            case 1 -> 1;
            case 2 -> 2;
            default -> throw new IllegalArgumentException("Unsupported amount of bytes for blocks (0, 1, 2, 4). Given: " + blockBytes);
            case 4 -> 3;
         };
         if (chance) {
            mask |= 4;
         }
         mask = switch (offsetBytes) {
            case 0 -> {
            }
            case 1 -> 8;
            case 2 -> 16;
            default -> throw new IllegalArgumentException("Unsupported amount of bytes for offset (0, 1, 2, 4). Given: " + offsetBytes);
            case 4 -> 24;
         };
         if (holder != null) {
            mask |= 32;
         }
         mask = switch (fluidBytes) {
            case 0 -> {
            }
            case 1 -> 64;
            case 2 -> 128;
            default -> throw new IllegalArgumentException("Unsupported amount of bytes for fluids (0, 1, 2, 4). Given: " + fluidBytes);
            case 4 -> 192;
         } | supportValue << 8 & 3840;
         if (filler != 0) {
            mask |= 4096;
         }

         if (rotation != 0) {
            mask |= 8192;
         }

         return mask;
      }

      static int getSkipBytes(int mask) {
         int bytes = 0;
         bytes += getBlockBytes(mask);
         bytes += getOffsetBytes(mask);
         if (hasChance(mask)) {
            bytes += 4;
         }

         bytes += getFluidBytes(mask);
         if (hasFiller(mask)) {
            bytes += 2;
         }

         if (hasRotation(mask)) {
            bytes++;
         }

         return bytes;
      }

      static boolean hasChance(int mask) {
         return (mask & 4) == 4;
      }

      static boolean hasFiller(int mask) {
         return (mask & 4096) == 4096;
      }

      static boolean hasRotation(int mask) {
         return (mask & 8192) == 8192;
      }

      static int getBlockBytes(int mask) {
         return switch (mask & 3) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 4;
            default -> 0;
         };
      }

      static int getOffsetBytes(int mask) {
         return switch (mask & 24) {
            case 8 -> 1;
            case 16 -> 2;
            case 24 -> 4;
            default -> 0;
         };
      }

      static int getFluidBytes(int mask) {
         return switch (mask & 192) {
            case 64 -> 2;
            case 128 -> 3;
            case 192 -> 5;
            default -> 0;
         };
      }

      static int getSupportValue(int mask) {
         return (mask & 3840) >> 8;
      }

      static boolean hasComponents(int mask) {
         return (mask & 32) == 32;
      }
   }

   public static class Builder {
      @Nonnull
      private final Vector3i min = new Vector3i(Vector3iUtil.MAX);
      @Nonnull
      private final Vector3i max = new Vector3i(Vector3iUtil.MIN);
      @Nonnull
      private final Int2ObjectMap<PrefabBuffer.BuilderColumn> builderColumns = new Int2ObjectOpenHashMap();
      @Nonnull
      private final List<PrefabBuffer.ChildPrefab> childPrefabs = new ObjectArrayList(0);
      private Vector3i anchor = new Vector3i();
      private int requiredMemory;

      private Builder() {
      }

      public void setAnchor(@Nonnull Vector3i anchor) {
         this.anchor = anchor;
      }

      public void addColumn(int x, int z, @Nonnull PrefabBufferBlockEntry[] entries, @Nullable Holder<EntityStore>[] entityHolders) {
         if (x < -32768) {
            throw new IllegalArgumentException("x is smaller than -32768. Given: " + x);
         } else if (x > 32767) {
            throw new IllegalArgumentException("x is larger than 32767. Given: " + x);
         } else if (z < -32768) {
            throw new IllegalArgumentException("z is smaller than -32768. Given: " + z);
         } else if (z > 32767) {
            throw new IllegalArgumentException("z is larger than 32767. Given: " + z);
         } else if (entries.length != 0) {
            int columnIndex = MathUtil.packInt(x, z);
            if (this.builderColumns.put(columnIndex, new PrefabBuffer.BuilderColumn(x, z, entries, entityHolders)) != null) {
               throw new IllegalArgumentException("Duplicate column");
            } else {
               int size = 4;
               int lastY = Integer.MIN_VALUE;

               for (int i = 0; i < entries.length; i++) {
                  PrefabBufferBlockEntry entry = entries[i];
                  int y = entry.y;
                  int blockId = entry.blockId;
                  float chance = entry.chance;
                  Holder<ChunkStore> holder = entry.state;
                  int fluidId = entry.fluidId;
                  if (y <= lastY) {
                     throw new IllegalArgumentException("Y Values are not sequential. " + lastY + " -> " + y);
                  }

                  int offset = i == 0 ? 0 : y - lastY;
                  if (offset > 65535) {
                     throw new IllegalArgumentException("Offset is larger than 65535. Given: " + offset);
                  }

                  boolean hasChance = chance < 1.0F;
                  int blockBytes = MathUtil.byteCount(blockId);
                  int offsetBytes = offset == 1 ? 0 : MathUtil.byteCount(offset);
                  int fluidBytes = MathUtil.byteCount(fluidId);
                  int mask = PrefabBuffer.BlockMaskConstants.getBlockMask(
                     blockBytes, fluidBytes, hasChance, offsetBytes, holder, entry.supportValue, entry.rotation, entry.filler
                  );
                  size += 2 + PrefabBuffer.BlockMaskConstants.getSkipBytes(mask);
                  lastY = y;
               }

               this.requiredMemory += size;
            }
         }
      }

      private void handleBlockComponents(int blockRotation, int x, int y, int z, @Nonnull Holder<ChunkStore> holder) {
         ComponentType<ChunkStore, PrefabSpawnerBlock> componentType = PrefabSpawnerBlock.getComponentType();
         PrefabSpawnerBlock spawnerState = holder.getComponent(componentType);
         if (spawnerState != null) {
            String path = spawnerState.getPrefabPath();
            if (path == null) {
               HytaleLogger.getLogger().at(Level.WARNING).log("Prefab spawner at %d, %d, %d is missing prefab path!", x, y, z);
            } else {
               PrefabWeights weights = spawnerState.getPrefabWeights();
               PrefabRotation rotation = PrefabRotation.fromRotation(RotationTuple.get(blockRotation).yaw());
               this.addChildPrefab(
                  x, y, z, path, spawnerState.isFitHeightmap(), spawnerState.isInheritSeed(), spawnerState.isInheritHeightCondition(), weights, rotation
               );
            }
         }
      }

      public void addChildPrefab(
         int x,
         int y,
         int z,
         @Nonnull String path,
         boolean fitHeightmap,
         boolean inheritSeed,
         boolean inheritHeightCondition,
         @Nullable PrefabWeights weights,
         @Nonnull PrefabRotation rotation
      ) {
         this.childPrefabs.add(new PrefabBuffer.ChildPrefab(x, y, z, path, fitHeightmap, inheritSeed, inheritHeightCondition, weights, rotation));
      }

      @Nonnull
      public PrefabBufferBlockEntry newBlockEntry(int y) {
         return new PrefabBufferBlockEntry(y);
      }

      private int buildColumn(Int2ObjectMap<PrefabBufferColumn> columns, PrefabBuffer.BuilderColumn column, MemorySegment data, int offset) {
         PrefabBufferBlockEntry[] entries = column.entries;
         int blockCount = entries.length;
         Int2ObjectOpenHashMap<Holder<ChunkStore>> holderMap = new Int2ObjectOpenHashMap();
         if (blockCount != 0 || column.entityHolders != null && column.entityHolders.length != 0) {
            int writeOffset = offset;
            if (blockCount > 0) {
               int initialY = entries[0].y;
               if (initialY < this.min.y) {
                  this.min.y = initialY;
               }

               data.set(ValueLayout.JAVA_INT_UNALIGNED, (long)offset, initialY - 1);
               writeOffset = offset + 4;
               initialY = Integer.MIN_VALUE;

               for (int i = 0; i < blockCount; i++) {
                  PrefabBufferBlockEntry entry = entries[i];
                  int y = entry.y;
                  int blockId = entry.blockId;
                  float chance = entry.chance;
                  Holder<ChunkStore> holder = entry.state;
                  int fluidId = entry.fluidId;
                  byte fluidLevel = entry.fluidLevel;
                  int yOffset = i == 0 ? 0 : y - initialY;
                  boolean hasChance = chance < 1.0F;
                  int blockBytes = MathUtil.byteCount(blockId);
                  int offsetBytes = yOffset == 1 ? 0 : MathUtil.byteCount(yOffset);
                  int fluidBytes = MathUtil.byteCount(fluidId);
                  int mask = PrefabBuffer.BlockMaskConstants.getBlockMask(
                     blockBytes, fluidBytes, hasChance, offsetBytes, holder, entry.supportValue, entry.rotation, entry.filler
                  );
                  data.set(ValueLayout.JAVA_SHORT_UNALIGNED, (long)writeOffset, (short)mask);
                  writeOffset += 2;
                  MemorySegmentUtil.writeNumber(data, writeOffset, blockBytes, blockId);
                  writeOffset += blockBytes;
                  MemorySegmentUtil.writeNumber(data, writeOffset, offsetBytes, yOffset);
                  writeOffset += offsetBytes;
                  if (hasChance) {
                     data.set(ValueLayout.JAVA_FLOAT_UNALIGNED, (long)writeOffset, chance);
                     writeOffset += 4;
                  }

                  if (entry.rotation != 0) {
                     data.set(ValueLayout.JAVA_BYTE, (long)writeOffset, (byte)entry.rotation);
                     writeOffset++;
                  }

                  if (entry.filler != 0) {
                     data.set(ValueLayout.JAVA_SHORT_UNALIGNED, (long)writeOffset, (short)entry.filler);
                     writeOffset += 2;
                  }

                  if (fluidId != 0) {
                     MemorySegmentUtil.writeNumber(data, writeOffset, fluidBytes, fluidId);
                     writeOffset += fluidBytes;
                     data.set(ValueLayout.JAVA_BYTE, (long)writeOffset, fluidLevel);
                     writeOffset++;
                  }

                  if (holder != null) {
                     holderMap.put(y, holder);
                     this.handleBlockComponents(entry.rotation, column.x, y, column.z, holder);
                  }

                  initialY = y;
               }

               if (initialY > this.max.y) {
                  this.max.y = initialY;
               }
            }

            if (column.x < this.min.x) {
               this.min.x = column.x;
            }

            if (column.x > this.max.x) {
               this.max.x = column.x;
            }

            if (column.z < this.min.z) {
               this.min.z = column.z;
            }

            if (column.z > this.max.z) {
               this.max.z = column.z;
            }

            if (holderMap.isEmpty()) {
               holderMap = null;
            }

            int size = writeOffset - offset;
            PrefabBufferColumn newColumn = new PrefabBufferColumn(blockCount, data.asSlice(offset, size), column.entityHolders, holderMap);
            columns.put(MathUtil.packInt(column.x, column.z), newColumn);
            return size;
         } else {
            return 0;
         }
      }

      @Nonnull
      public PrefabBuffer build() {
         Int2ObjectOpenHashMap<PrefabBufferColumn> columns = new Int2ObjectOpenHashMap();
         MemorySegment memorySegment = MemorySegment.ofArray(new byte[this.requiredMemory]);
         int offset = 0;
         ObjectIterator childPrefabArray = this.builderColumns.values().iterator();

         while (childPrefabArray.hasNext()) {
            PrefabBuffer.BuilderColumn e = (PrefabBuffer.BuilderColumn)childPrefabArray.next();
            offset += this.buildColumn(columns, e, memorySegment, offset);
         }

         PrefabBuffer.ChildPrefab[] childPrefabArrayx = this.childPrefabs.toArray(PrefabBuffer.ChildPrefab[]::new);
         if (this.builderColumns.isEmpty()) {
            this.min.zero();
            this.max.zero();
         }

         return new PrefabBuffer(this.anchor, this.min, this.max, columns, childPrefabArrayx);
      }
   }

   private record BuilderColumn(int x, int z, @Nonnull PrefabBufferBlockEntry[] entries, @Nullable Holder<EntityStore>[] entityHolders) {
   }

   public static class ChildPrefab {
      private final int x;
      private final int y;
      private final int z;
      @Nonnull
      private final String path;
      private final boolean fitHeightmap;
      private final boolean inheritSeed;
      private final boolean inheritHeightCondition;
      @Nonnull
      private final PrefabWeights weights;
      @Nonnull
      private final PrefabRotation rotation;

      private ChildPrefab(
         int x,
         int y,
         int z,
         @Nonnull String path,
         boolean fitHeightmap,
         boolean inheritSeed,
         boolean inheritHeightCondition,
         @Nonnull PrefabWeights weights,
         @Nonnull PrefabRotation rotation
      ) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.path = path;
         this.fitHeightmap = fitHeightmap;
         this.inheritSeed = inheritSeed;
         this.inheritHeightCondition = inheritHeightCondition;
         this.weights = weights;
         this.rotation = rotation;
      }

      public int getX() {
         return this.x;
      }

      public int getY() {
         return this.y;
      }

      public int getZ() {
         return this.z;
      }

      @Nonnull
      public String getPath() {
         return this.path;
      }

      public boolean isFitHeightmap() {
         return this.fitHeightmap;
      }

      public boolean isInheritSeed() {
         return this.inheritSeed;
      }

      public boolean isInheritHeightCondition() {
         return this.inheritHeightCondition;
      }

      @Nonnull
      public PrefabWeights getWeights() {
         return this.weights;
      }

      @Nonnull
      public PrefabRotation getRotation() {
         return this.rotation;
      }
   }

   public static class PrefabBufferAccessor implements IPrefabBuffer {
      @Nonnull
      private final PrefabBuffer prefabBuffer;

      private PrefabBufferAccessor(@Nonnull PrefabBuffer prefabBuffer) {
         this.prefabBuffer = prefabBuffer;
      }

      @Override
      public int getAnchorX() {
         return this.prefabBuffer.getAnchorX();
      }

      @Override
      public int getAnchorY() {
         return this.prefabBuffer.getAnchorY();
      }

      @Override
      public int getAnchorZ() {
         return this.prefabBuffer.getAnchorZ();
      }

      @Override
      public int getMinX(@Nonnull PrefabRotation rotation) {
         return Math.min(
            rotation.getX(this.prefabBuffer.min.x(), this.prefabBuffer.min.z()), rotation.getX(this.prefabBuffer.max.x(), this.prefabBuffer.max.z())
         );
      }

      @Override
      public int getMinY() {
         return this.prefabBuffer.min.y();
      }

      @Override
      public int getMinZ(@Nonnull PrefabRotation rotation) {
         return Math.min(
            rotation.getZ(this.prefabBuffer.min.x(), this.prefabBuffer.min.z()), rotation.getZ(this.prefabBuffer.max.x(), this.prefabBuffer.max.z())
         );
      }

      @Override
      public int getMaxX(@Nonnull PrefabRotation rotation) {
         return Math.max(
            rotation.getX(this.prefabBuffer.min.x(), this.prefabBuffer.min.z()), rotation.getX(this.prefabBuffer.max.x(), this.prefabBuffer.max.z())
         );
      }

      @Override
      public int getMaxY() {
         return this.prefabBuffer.max.y();
      }

      @Override
      public int getMaxZ(@Nonnull PrefabRotation rotation) {
         return Math.max(
            rotation.getZ(this.prefabBuffer.min.x(), this.prefabBuffer.min.z()), rotation.getZ(this.prefabBuffer.max.x(), this.prefabBuffer.max.z())
         );
      }

      @Override
      public int getColumnCount() {
         return this.prefabBuffer.columns.size();
      }

      @Nonnull
      @Override
      public PrefabBuffer.ChildPrefab[] getChildPrefabs() {
         return this.prefabBuffer.childPrefabs;
      }

      @Override
      public int getMinYAt(@Nonnull PrefabRotation rotation, int x, int z) {
         int rotatedX = rotation.getX(x, z);
         int rotatedZ = rotation.getZ(x, z);
         int columnIndex = MathUtil.packInt(rotatedX, rotatedZ);
         PrefabBufferColumn columnData = (PrefabBufferColumn)this.prefabBuffer.columns.get(columnIndex);
         return columnData != null && columnData.getBlockCount() > 0 ? columnData.getMemorySegment().get(ValueLayout.JAVA_INT_UNALIGNED, 0L) + 1 : -1;
      }

      @Override
      public int getMaxYAt(@Nonnull PrefabRotation rotation, int x, int z) {
         int rotatedX = rotation.getX(x, z);
         int rotatedZ = rotation.getZ(x, z);
         int columnIndex = MathUtil.packInt(rotatedX, rotatedZ);
         PrefabBufferColumn column = (PrefabBufferColumn)this.prefabBuffer.columns.get(columnIndex);
         if (column == null) {
            return -1;
         } else if (column.getBlockCount() > 0) {
            int offset = 0;
            MemorySegment data = column.getMemorySegment();
            int y = data.get(ValueLayout.JAVA_INT_UNALIGNED, (long)offset);
            offset += 4;

            for (int i = 0; i < column.getBlockCount(); i++) {
               int mask = Short.toUnsignedInt(data.get(ValueLayout.JAVA_SHORT_UNALIGNED, (long)offset));
               offset += 2;
               if (PrefabBuffer.BlockMaskConstants.getOffsetBytes(mask) > 0) {
                  offset += PrefabBuffer.BlockMaskConstants.getBlockBytes(mask);
                  int offsetBytes = PrefabBuffer.BlockMaskConstants.getOffsetBytes(mask);
                  y += MemorySegmentUtil.readNumber(data, offset, offsetBytes);
                  offset += offsetBytes;
                  if (PrefabBuffer.BlockMaskConstants.hasChance(mask)) {
                     offset += 4;
                  }

                  if (PrefabBuffer.BlockMaskConstants.hasRotation(mask)) {
                     offset++;
                  }

                  if (PrefabBuffer.BlockMaskConstants.hasFiller(mask)) {
                     offset += 2;
                  }

                  offset += PrefabBuffer.BlockMaskConstants.getFluidBytes(mask);
               } else {
                  offset += PrefabBuffer.BlockMaskConstants.getSkipBytes(mask);
                  y++;
               }
            }

            return y;
         } else {
            return -1;
         }
      }

      @Override
      public <T extends PrefabBufferCall> void forEach(
         @Nonnull IPrefabBuffer.ColumnPredicate<T> columnPredicate,
         @Nonnull IPrefabBuffer.BlockConsumer<T> blockConsumer,
         @Nullable IPrefabBuffer.EntityConsumer<T> entityConsumer,
         @Nullable IPrefabBuffer.ChildConsumer<T> childConsumer,
         @Nonnull T t
      ) {
         this.prefabBuffer.columns.int2ObjectEntrySet().forEach(entry -> {
            int columnIndex = entry.getIntKey();
            int cx = MathUtil.unpackLeft(columnIndex);
            int cz = MathUtil.unpackRight(columnIndex);
            int xx = t.rotation.getX(cx, cz);
            int zx = t.rotation.getZ(cx, cz);
            PrefabBufferColumn column = (PrefabBufferColumn)entry.getValue();
            MemorySegment data = column.getMemorySegment();
            int blockCount = column.getBlockCount();
            if (columnPredicate.test(xx, zx, blockCount, t)) {
               if (blockCount > 0) {
                  int offset = 0;
                  int y = data.get(ValueLayout.JAVA_INT_UNALIGNED, (long)offset);
                  offset += 4;

                  for (int i = 0; i < blockCount; i++) {
                     int mask = Short.toUnsignedInt(data.get(ValueLayout.JAVA_SHORT_UNALIGNED, (long)offset));
                     offset += 2;
                     int blockBytes = PrefabBuffer.BlockMaskConstants.getBlockBytes(mask);
                     int blockId = MemorySegmentUtil.readNumber(data, offset, blockBytes);
                     offset += blockBytes;
                     int offsetBytes = PrefabBuffer.BlockMaskConstants.getOffsetBytes(mask);
                     y += offsetBytes == 0 ? 1 : MemorySegmentUtil.readNumber(data, offset, offsetBytes);
                     offset += offsetBytes;
                     if (PrefabBuffer.BlockMaskConstants.hasChance(mask)) {
                        float chance = data.get(ValueLayout.JAVA_FLOAT_UNALIGNED, (long)offset);
                        offset += 4;
                        if (chance < t.random.nextFloat()) {
                           offset += 2;
                           offset += PrefabBuffer.BlockMaskConstants.getFluidBytes(mask);
                           continue;
                        }
                     }

                     Holder<ChunkStore> holder = PrefabBuffer.BlockMaskConstants.hasComponents(mask) ? (Holder)column.getBlockComponents().get(y) : null;
                     int supportValue = PrefabBuffer.BlockMaskConstants.getSupportValue(mask);
                     int rotation = 0;
                     if (PrefabBuffer.BlockMaskConstants.hasRotation(mask)) {
                        rotation = Byte.toUnsignedInt(data.get(ValueLayout.JAVA_BYTE, (long)offset));
                        offset++;
                     }

                     rotation = t.rotation.getRotation(rotation);
                     int filler = 0;
                     if (PrefabBuffer.BlockMaskConstants.hasFiller(mask)) {
                        filler = t.rotation.getFiller(Short.toUnsignedInt(data.get(ValueLayout.JAVA_SHORT_UNALIGNED, (long)offset)));
                        offset += 2;
                     }

                     int fluidBytes = PrefabBuffer.BlockMaskConstants.getFluidBytes(mask);
                     int fluidId = 0;
                     int fluidLevel = 0;
                     if (fluidBytes != 0) {
                        fluidId = MemorySegmentUtil.readNumber(data, offset, fluidBytes - 1);
                        offset += fluidBytes - 1;
                        fluidLevel = data.get(ValueLayout.JAVA_BYTE, (long)offset);
                        offset++;
                     }

                     blockConsumer.accept(xx, y, zx, blockId, holder, supportValue, rotation, filler, t, fluidId, fluidLevel);
                  }
               }

               Holder<EntityStore>[] entityHolders = column.getEntityHolders();
               if (entityHolders != null && entityConsumer != null) {
                  entityConsumer.accept(xx, zx, entityHolders, t);
               }
            }
         });
         if (this.prefabBuffer.childPrefabs != null && childConsumer != null) {
            for (PrefabBuffer.ChildPrefab childPrefab : this.prefabBuffer.childPrefabs) {
               int x = t.rotation.getX(childPrefab.x, childPrefab.z);
               int z = t.rotation.getZ(childPrefab.x, childPrefab.z);
               childConsumer.accept(
                  x,
                  childPrefab.y,
                  z,
                  childPrefab.path,
                  childPrefab.fitHeightmap,
                  childPrefab.inheritSeed,
                  childPrefab.inheritHeightCondition,
                  childPrefab.weights,
                  childPrefab.rotation,
                  t
               );
            }
         }
      }

      @Override
      public <T> void forEachEntity(@Nonnull IPrefabBuffer.EntityConsumer<T> entityConsumer, @Nullable T t) {
         this.prefabBuffer.columns.int2ObjectEntrySet().forEach(entry -> {
            int columnIndex = entry.getIntKey();
            int x = MathUtil.unpackLeft(columnIndex);
            int z = MathUtil.unpackRight(columnIndex);
            PrefabBufferColumn column = (PrefabBufferColumn)entry.getValue();
            Holder<EntityStore>[] entityHolders = column.getEntityHolders();
            if (entityConsumer != null) {
               entityConsumer.accept(x, z, entityHolders, t);
            }
         });
      }

      @Override
      public <T> void forEachRaw(
         @Nonnull IPrefabBuffer.ColumnPredicate<T> columnPredicate,
         @Nonnull IPrefabBuffer.RawBlockConsumer<T> blockConsumer,
         @Nonnull IPrefabBuffer.FluidConsumer<T> fluidConsumer,
         @Nullable IPrefabBuffer.EntityConsumer<T> entityConsumer,
         @Nullable T t
      ) {
         this.prefabBuffer.columns.int2ObjectEntrySet().forEach(entry -> {
            int columnIndex = entry.getIntKey();
            int x = MathUtil.unpackLeft(columnIndex);
            int z = MathUtil.unpackRight(columnIndex);
            PrefabBufferColumn column = (PrefabBufferColumn)entry.getValue();
            MemorySegment data = column.getMemorySegment();
            int blockCount = column.getBlockCount();
            if (columnPredicate.test(x, z, blockCount, t)) {
               if (blockCount > 0) {
                  int offset = 0;
                  int y = data.get(ValueLayout.JAVA_INT_UNALIGNED, (long)offset);
                  offset += 4;

                  for (int i = 0; i < blockCount; i++) {
                     int mask = Short.toUnsignedInt(data.get(ValueLayout.JAVA_SHORT_UNALIGNED, (long)offset));
                     offset += 2;
                     int blockBytes = PrefabBuffer.BlockMaskConstants.getBlockBytes(mask);
                     int blockId = MemorySegmentUtil.readNumber(data, offset, blockBytes);
                     offset += blockBytes;
                     int offsetBytes = PrefabBuffer.BlockMaskConstants.getOffsetBytes(mask);
                     y += offsetBytes == 0 ? 1 : MemorySegmentUtil.readNumber(data, offset, offsetBytes);
                     offset += offsetBytes;
                     float chance = 1.0F;
                     if (PrefabBuffer.BlockMaskConstants.hasChance(mask)) {
                        chance = data.get(ValueLayout.JAVA_FLOAT_UNALIGNED, (long)offset);
                        offset += 4;
                     }

                     Holder<ChunkStore> holder = PrefabBuffer.BlockMaskConstants.hasComponents(mask) ? (Holder)column.getBlockComponents().get(y) : null;
                     int supportValue = PrefabBuffer.BlockMaskConstants.getSupportValue(mask);
                     int rotation = 0;
                     if (PrefabBuffer.BlockMaskConstants.hasRotation(mask)) {
                        rotation = Byte.toUnsignedInt(data.get(ValueLayout.JAVA_BYTE, (long)offset));
                        offset++;
                     }

                     int filler = 0;
                     if (PrefabBuffer.BlockMaskConstants.hasFiller(mask)) {
                        filler = Short.toUnsignedInt(data.get(ValueLayout.JAVA_SHORT_UNALIGNED, (long)offset));
                        offset += 2;
                     }

                     blockConsumer.accept(x, y, z, mask, blockId, chance, holder, supportValue, rotation, filler, t);
                     int fluidBytes = PrefabBuffer.BlockMaskConstants.getFluidBytes(mask);
                     if (fluidBytes != 0) {
                        int fluidId = MemorySegmentUtil.readNumber(data, offset, fluidBytes - 1);
                        offset += fluidBytes - 1;
                        byte fluidLevel = data.get(ValueLayout.JAVA_BYTE, (long)offset);
                        offset++;
                        fluidConsumer.accept(x, y, z, fluidId, fluidLevel, t);
                     }
                  }
               }

               Holder<EntityStore>[] entityHolders = column.getEntityHolders();
               if (entityConsumer != null) {
                  entityConsumer.accept(x, z, entityHolders, t);
               }
            }
         });
      }

      @Override
      public <T> boolean forEachRaw(
         @Nonnull IPrefabBuffer.ColumnPredicate<T> columnPredicate,
         @Nonnull IPrefabBuffer.RawBlockPredicate<T> blockPredicate,
         @Nonnull IPrefabBuffer.FluidPredicate<T> fluidPredicate,
         @Nullable IPrefabBuffer.EntityPredicate<T> entityPredicate,
         @Nullable T t
      ) {
         ObjectIterator var6 = this.prefabBuffer.columns.int2ObjectEntrySet().iterator();

         while (var6.hasNext()) {
            Entry<PrefabBufferColumn> entry = (Entry<PrefabBufferColumn>)var6.next();
            int columnIndex = entry.getIntKey();
            int x = MathUtil.unpackLeft(columnIndex);
            int z = MathUtil.unpackRight(columnIndex);
            PrefabBufferColumn column = (PrefabBufferColumn)entry.getValue();
            MemorySegment data = column.getMemorySegment();
            int blockCount = column.getBlockCount();
            if (!columnPredicate.test(x, z, blockCount, t)) {
               return false;
            }

            if (blockCount > 0) {
               int offset = 0;
               int y = data.get(ValueLayout.JAVA_INT_UNALIGNED, (long)offset);
               offset += 4;

               for (int i = 0; i < blockCount; i++) {
                  int mask = Short.toUnsignedInt(data.get(ValueLayout.JAVA_SHORT_UNALIGNED, (long)offset));
                  offset += 2;
                  int blockBytes = PrefabBuffer.BlockMaskConstants.getBlockBytes(mask);
                  int blockId = MemorySegmentUtil.readNumber(data, offset, blockBytes);
                  offset += blockBytes;
                  int offsetBytes = PrefabBuffer.BlockMaskConstants.getOffsetBytes(mask);
                  y += offsetBytes == 0 ? 1 : MemorySegmentUtil.readNumber(data, offset, offsetBytes);
                  offset += offsetBytes;
                  float chance = 1.0F;
                  if (PrefabBuffer.BlockMaskConstants.hasChance(mask)) {
                     chance = data.get(ValueLayout.JAVA_FLOAT_UNALIGNED, (long)offset);
                     offset += 4;
                  }

                  Holder<ChunkStore> holder = PrefabBuffer.BlockMaskConstants.hasComponents(mask) ? (Holder)column.getBlockComponents().get(y) : null;
                  int rotation = 0;
                  if (PrefabBuffer.BlockMaskConstants.hasRotation(mask)) {
                     rotation = Byte.toUnsignedInt(data.get(ValueLayout.JAVA_BYTE, (long)offset));
                     offset++;
                  }

                  int filler = 0;
                  if (PrefabBuffer.BlockMaskConstants.hasFiller(mask)) {
                     filler = Short.toUnsignedInt(data.get(ValueLayout.JAVA_SHORT_UNALIGNED, (long)offset));
                     offset += 2;
                  }

                  int supportValue = PrefabBuffer.BlockMaskConstants.getSupportValue(mask);
                  if (!blockPredicate.test(x, y, z, blockId, chance, holder, supportValue, rotation, filler, t)) {
                     return false;
                  }

                  int fluidBytes = PrefabBuffer.BlockMaskConstants.getFluidBytes(mask);
                  if (fluidBytes != 0) {
                     int fluidId = MemorySegmentUtil.readNumber(data, offset, fluidBytes - 1);
                     offset += fluidBytes - 1;
                     byte fluidLevel = data.get(ValueLayout.JAVA_BYTE, (long)offset);
                     offset++;
                     if (!fluidPredicate.test(x, y, z, fluidId, fluidLevel, t)) {
                        return false;
                     }
                  }
               }
            }

            Holder<EntityStore>[] entityHolders = column.getEntityHolders();
            if (entityPredicate != null && !entityPredicate.test(x, z, entityHolders, t)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public <T extends PrefabBufferCall> boolean compare(
         @Nonnull IPrefabBuffer.BlockComparingPrefabPredicate<T> blockComparingIterator, @Nonnull T t, @Nonnull IPrefabBuffer otherPrefab
      ) {
         if (!(otherPrefab instanceof PrefabBuffer.PrefabBufferAccessor secondPrefab)) {
            return IPrefabBuffer.super.compare(blockComparingIterator, t, otherPrefab);
         } else {
            Int2ObjectMap secondPrefabColumns = secondPrefab.prefabBuffer.columns;
            IntOpenHashSet columnIndexes = new IntOpenHashSet(this.prefabBuffer.columns.size() + secondPrefabColumns.size());
            columnIndexes.addAll(this.prefabBuffer.columns.keySet());
            columnIndexes.addAll(secondPrefabColumns.keySet());
            IntIterator columnIterator = columnIndexes.iterator();

            while (columnIterator.hasNext()) {
               int columnIndex = columnIterator.nextInt();
               int cx = MathUtil.unpackLeft(columnIndex);
               int cz = MathUtil.unpackRight(columnIndex);
               int x = t.rotation.getX(cx, cz);
               int z = t.rotation.getZ(cx, cz);
               PrefabBufferColumn firstColumn = (PrefabBufferColumn)this.prefabBuffer.columns.get(columnIndex);
               PrefabBufferColumn secondColumn = (PrefabBufferColumn)secondPrefabColumns.get(columnIndex);
               MemorySegment firstData = firstColumn != null ? firstColumn.getMemorySegment() : null;
               MemorySegment secondData = secondColumn != null ? secondColumn.getMemorySegment() : null;
               int firstColumnBlockCount = firstColumn != null ? firstColumn.getBlockCount() : 0;
               int secondColumnBlockCount = secondColumn != null ? secondColumn.getBlockCount() : 0;
               if (firstColumnBlockCount != 0 || secondColumnBlockCount != 0) {
                  int firstOffset = 0;
                  int firstColumnY;
                  if (firstColumnBlockCount > 0) {
                     firstColumnY = firstData.get(ValueLayout.JAVA_INT_UNALIGNED, (long)firstOffset);
                     firstOffset += 4;
                  } else {
                     firstColumnY = Integer.MAX_VALUE;
                  }

                  int secondOffset = 0;
                  int secondColumnY;
                  if (secondColumnBlockCount > 0) {
                     secondColumnY = secondData.get(ValueLayout.JAVA_INT_UNALIGNED, (long)secondOffset);
                     secondOffset += 4;
                  } else {
                     secondColumnY = Integer.MAX_VALUE;
                  }

                  int firstColumnBlockId = Integer.MIN_VALUE;
                  float firstColumnChance = 1.0F;
                  int firstColumnRotation = 0;
                  int firstColumnFiller = 0;
                  Holder<ChunkStore> firstColumnComponents = null;
                  int secondColumnBlockId = Integer.MIN_VALUE;
                  float secondColumnChance = 1.0F;
                  int secondColumnRotation = 0;
                  int secondColumnFiller = 0;
                  Holder<ChunkStore> secondColumnComponents = null;
                  int firstColumnBlocksRead = 0;
                  int secondColumnBlocksRead = 0;

                  while (firstColumnBlocksRead < firstColumnBlockCount || secondColumnBlocksRead < secondColumnBlockCount) {
                     int oldFirstColumnY = firstColumnY;
                     int oldSecondColumnY = secondColumnY;
                     int oldFirstOffset = firstOffset;
                     int oldSecondOffset = secondOffset;
                     if (firstColumnBlocksRead < firstColumnBlockCount) {
                        int mask = Short.toUnsignedInt(firstData.get(ValueLayout.JAVA_SHORT_UNALIGNED, (long)firstOffset));
                        firstOffset += 2;
                        int blockBytes = PrefabBuffer.BlockMaskConstants.getBlockBytes(mask);
                        firstColumnBlockId = MemorySegmentUtil.readNumber(firstData, firstOffset, blockBytes);
                        firstOffset += blockBytes;
                        int offsetBytes = PrefabBuffer.BlockMaskConstants.getOffsetBytes(mask);
                        firstColumnY += offsetBytes == 0 ? 1 : MemorySegmentUtil.readNumber(firstData, firstOffset, offsetBytes);
                        firstOffset += offsetBytes;
                        if (PrefabBuffer.BlockMaskConstants.hasChance(mask)) {
                           firstColumnChance = firstData.get(ValueLayout.JAVA_FLOAT_UNALIGNED, (long)firstOffset);
                           firstOffset += 4;
                        } else {
                           firstColumnChance = 1.0F;
                        }

                        if (PrefabBuffer.BlockMaskConstants.hasRotation(mask)) {
                           firstColumnRotation = t.rotation.getRotation(Byte.toUnsignedInt(firstData.get(ValueLayout.JAVA_BYTE, (long)firstOffset)));
                           firstOffset++;
                        } else {
                           firstColumnRotation = t.rotation.getRotation(0);
                        }

                        if (PrefabBuffer.BlockMaskConstants.hasFiller(mask)) {
                           firstColumnFiller = t.rotation.getFiller(Short.toUnsignedInt(firstData.get(ValueLayout.JAVA_SHORT_UNALIGNED, (long)firstOffset)));
                           firstOffset += 2;
                        } else {
                           firstColumnFiller = 0;
                        }

                        firstColumnComponents = PrefabBuffer.BlockMaskConstants.hasComponents(mask)
                           ? (Holder)firstColumn.getBlockComponents().get(firstColumnY)
                           : null;
                        firstOffset += PrefabBuffer.BlockMaskConstants.getFluidBytes(mask);
                     }

                     if (secondColumnBlocksRead < secondColumnBlockCount) {
                        int maskx = Short.toUnsignedInt(secondData.get(ValueLayout.JAVA_SHORT_UNALIGNED, (long)secondOffset));
                        secondOffset += 2;
                        int blockBytesx = PrefabBuffer.BlockMaskConstants.getBlockBytes(maskx);
                        secondColumnBlockId = MemorySegmentUtil.readNumber(secondData, secondOffset, blockBytesx);
                        secondOffset += blockBytesx;
                        int offsetBytesx = PrefabBuffer.BlockMaskConstants.getOffsetBytes(maskx);
                        secondColumnY += offsetBytesx == 0 ? 1 : MemorySegmentUtil.readNumber(secondData, secondOffset, offsetBytesx);
                        secondOffset += offsetBytesx;
                        if (PrefabBuffer.BlockMaskConstants.hasChance(maskx)) {
                           secondColumnChance = secondData.get(ValueLayout.JAVA_FLOAT_UNALIGNED, (long)secondOffset);
                           secondOffset += 4;
                        } else {
                           secondColumnChance = 1.0F;
                        }

                        if (PrefabBuffer.BlockMaskConstants.hasRotation(maskx)) {
                           secondColumnRotation = t.rotation.getRotation(Byte.toUnsignedInt(secondData.get(ValueLayout.JAVA_BYTE, (long)secondOffset)));
                           secondOffset++;
                        } else {
                           secondColumnRotation = t.rotation.getRotation(0);
                        }

                        if (PrefabBuffer.BlockMaskConstants.hasFiller(maskx)) {
                           secondColumnFiller = t.rotation.getFiller(Short.toUnsignedInt(secondData.get(ValueLayout.JAVA_SHORT_UNALIGNED, (long)secondOffset)));
                           secondOffset += 2;
                        } else {
                           secondColumnFiller = 0;
                        }

                        secondColumnComponents = PrefabBuffer.BlockMaskConstants.hasComponents(maskx)
                           ? (Holder)secondColumn.getBlockComponents().get(secondColumnY)
                           : null;
                        secondOffset += PrefabBuffer.BlockMaskConstants.getFluidBytes(maskx);
                     }

                     if (firstColumnY == secondColumnY) {
                        firstColumnBlocksRead++;
                        secondColumnBlocksRead++;
                        boolean test = blockComparingIterator.test(
                           x,
                           firstColumnY,
                           z,
                           firstColumnBlockId,
                           firstColumnComponents,
                           firstColumnChance,
                           firstColumnRotation,
                           firstColumnFiller,
                           secondColumnBlockId,
                           secondColumnComponents,
                           secondColumnChance,
                           secondColumnRotation,
                           secondColumnFiller,
                           t
                        );
                        if (!test) {
                           return false;
                        }
                     } else if ((firstColumnY >= secondColumnY || firstColumnBlocksRead >= firstColumnBlockCount)
                        && secondColumnBlocksRead < secondColumnBlockCount) {
                        secondColumnBlocksRead++;
                        firstColumnY = oldFirstColumnY;
                        firstOffset = oldFirstOffset;
                        boolean test = blockComparingIterator.test(
                           x,
                           secondColumnY,
                           z,
                           Integer.MIN_VALUE,
                           null,
                           1.0F,
                           0,
                           0,
                           secondColumnBlockId,
                           secondColumnComponents,
                           secondColumnChance,
                           secondColumnRotation,
                           secondColumnFiller,
                           t
                        );
                        if (!test) {
                           return false;
                        }
                     } else {
                        firstColumnBlocksRead++;
                        secondColumnY = oldSecondColumnY;
                        secondOffset = oldSecondOffset;
                        boolean test = blockComparingIterator.test(
                           x,
                           firstColumnY,
                           z,
                           firstColumnBlockId,
                           firstColumnComponents,
                           firstColumnChance,
                           firstColumnRotation,
                           firstColumnFiller,
                           Integer.MIN_VALUE,
                           null,
                           1.0F,
                           0,
                           0,
                           t
                        );
                        if (!test) {
                           return false;
                        }
                     }
                  }
               }
            }

            return true;
         }
      }

      @Override
      public int getBlockId(int x, int y, int z) {
         PrefabBufferColumn column = (PrefabBufferColumn)this.prefabBuffer.columns.get(MathUtil.packInt(x, z));
         if (column == null) {
            return 0;
         } else {
            int blockCount = column.getBlockCount();
            if (blockCount <= 0) {
               return 0;
            } else {
               MemorySegment data = column.getMemorySegment();
               int offset = 0;
               int blockY = data.get(ValueLayout.JAVA_INT_UNALIGNED, (long)offset);
               offset += 4;

               for (int i = 0; i < blockCount; i++) {
                  int mask = Short.toUnsignedInt(data.get(ValueLayout.JAVA_SHORT_UNALIGNED, (long)offset));
                  offset += 2;
                  int blockBytes = PrefabBuffer.BlockMaskConstants.getBlockBytes(mask);
                  int blockId = MemorySegmentUtil.readNumber(data, offset, blockBytes);
                  offset += blockBytes;
                  int offsetBytes = PrefabBuffer.BlockMaskConstants.getOffsetBytes(mask);
                  blockY += offsetBytes == 0 ? 1 : MemorySegmentUtil.readNumber(data, offset, offsetBytes);
                  offset += offsetBytes;
                  if (blockY > y) {
                     return 0;
                  }

                  if (PrefabBuffer.BlockMaskConstants.hasChance(mask)) {
                     offset += 4;
                  }

                  if (PrefabBuffer.BlockMaskConstants.hasRotation(mask)) {
                     offset++;
                  }

                  if (PrefabBuffer.BlockMaskConstants.hasFiller(mask)) {
                     offset += 2;
                  }

                  offset += PrefabBuffer.BlockMaskConstants.getFluidBytes(mask);
                  if (blockY == y) {
                     if (PrefabBuffer.BlockMaskConstants.hasChance(mask)) {
                        throw new UnsupportedOperationException("Unable to access block with chance!");
                     }

                     return blockId;
                  }
               }

               return 0;
            }
         }
      }

      @Override
      public int getFiller(int x, int y, int z) {
         PrefabBufferColumn column = (PrefabBufferColumn)this.prefabBuffer.columns.get(MathUtil.packInt(x, z));
         if (column == null) {
            return 0;
         } else {
            int blockCount = column.getBlockCount();
            if (blockCount <= 0) {
               return 0;
            } else {
               MemorySegment data = column.getMemorySegment();
               int offset = 0;
               int blockY = data.get(ValueLayout.JAVA_INT_UNALIGNED, (long)offset);
               offset += 4;

               for (int i = 0; i < blockCount; i++) {
                  int mask = Short.toUnsignedInt(data.get(ValueLayout.JAVA_SHORT_UNALIGNED, (long)offset));
                  offset += 2;
                  int blockBytes = PrefabBuffer.BlockMaskConstants.getBlockBytes(mask);
                  MemorySegmentUtil.readNumber(data, offset, blockBytes);
                  offset += blockBytes;
                  int offsetBytes = PrefabBuffer.BlockMaskConstants.getOffsetBytes(mask);
                  blockY += offsetBytes == 0 ? 1 : MemorySegmentUtil.readNumber(data, offset, offsetBytes);
                  offset += offsetBytes;
                  if (blockY > y) {
                     return 0;
                  }

                  if (PrefabBuffer.BlockMaskConstants.hasChance(mask)) {
                     offset += 4;
                  }

                  if (PrefabBuffer.BlockMaskConstants.hasRotation(mask)) {
                     offset++;
                  }

                  int filler = 0;
                  if (PrefabBuffer.BlockMaskConstants.hasFiller(mask)) {
                     filler = Short.toUnsignedInt(data.get(ValueLayout.JAVA_SHORT_UNALIGNED, (long)offset));
                     offset += 2;
                  }

                  offset += PrefabBuffer.BlockMaskConstants.getFluidBytes(mask);
                  if (blockY == y) {
                     if (PrefabBuffer.BlockMaskConstants.hasChance(mask)) {
                        throw new UnsupportedOperationException("Unable to access block with chance!");
                     }

                     return filler;
                  }
               }

               return 0;
            }
         }
      }

      @Override
      public int getRotationIndex(int x, int y, int z) {
         PrefabBufferColumn column = (PrefabBufferColumn)this.prefabBuffer.columns.get(MathUtil.packInt(x, z));
         if (column == null) {
            return 0;
         } else {
            int blockCount = column.getBlockCount();
            if (blockCount <= 0) {
               return 0;
            } else {
               MemorySegment data = column.getMemorySegment();
               int offset = 0;
               int blockY = data.get(ValueLayout.JAVA_INT_UNALIGNED, (long)offset);
               offset += 4;

               for (int i = 0; i < blockCount; i++) {
                  int mask = Short.toUnsignedInt(data.get(ValueLayout.JAVA_SHORT_UNALIGNED, (long)offset));
                  offset += 2;
                  int blockBytes = PrefabBuffer.BlockMaskConstants.getBlockBytes(mask);
                  MemorySegmentUtil.readNumber(data, offset, blockBytes);
                  offset += blockBytes;
                  int offsetBytes = PrefabBuffer.BlockMaskConstants.getOffsetBytes(mask);
                  blockY += offsetBytes == 0 ? 1 : MemorySegmentUtil.readNumber(data, offset, offsetBytes);
                  offset += offsetBytes;
                  if (blockY > y) {
                     return 0;
                  }

                  if (PrefabBuffer.BlockMaskConstants.hasChance(mask)) {
                     offset += 4;
                  }

                  int rotation = 0;
                  if (PrefabBuffer.BlockMaskConstants.hasRotation(mask)) {
                     rotation = Byte.toUnsignedInt(data.get(ValueLayout.JAVA_BYTE, (long)offset));
                     offset++;
                  }

                  if (PrefabBuffer.BlockMaskConstants.hasFiller(mask)) {
                     offset += 2;
                  }

                  offset += PrefabBuffer.BlockMaskConstants.getFluidBytes(mask);
                  if (blockY == y) {
                     if (PrefabBuffer.BlockMaskConstants.hasChance(mask)) {
                        throw new UnsupportedOperationException("Unable to access block with chance!");
                     }

                     return rotation;
                  }
               }

               return 0;
            }
         }
      }
   }
}
