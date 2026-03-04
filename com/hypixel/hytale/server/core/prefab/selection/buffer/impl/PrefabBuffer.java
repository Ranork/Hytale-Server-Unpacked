package com.hypixel.hytale.server.core.prefab.selection.buffer.impl;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.modules.prefabspawner.PrefabSpawnerState;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.core.prefab.PrefabWeights;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferCall;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.io.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
   @Nullable
   private ByteBuf buf;

   private PrefabBuffer(
      @Nonnull ByteBuf buf,
      @Nonnull Vector3i anchor,
      @Nonnull Vector3i min,
      @Nonnull Vector3i max,
      @Nonnull Int2ObjectMap<PrefabBufferColumn> columns,
      @Nonnull PrefabBuffer.ChildPrefab[] childPrefabs
   ) {
      this.buf = buf;
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
      return this.anchor.getX();
   }

   public int getAnchorY() {
      return this.anchor.getY();
   }

   public int getAnchorZ() {
      return this.anchor.getZ();
   }

   @Nonnull
   public PrefabBuffer.PrefabBufferAccessor newAccess() {
      this.checkReleased();
      return new PrefabBuffer.PrefabBufferAccessor(this);
   }

   public void release() {
      this.checkReleased();
      this.buf.release();
      this.buf = null;
   }

   private void checkReleased() {
      if (this.buf == null) {
         throw new IllegalStateException("PrefabBuffer has already been released!");
      }
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
      private final ByteBuf buf = Unpooled.buffer();
      @Nonnull
      private final Vector3i min = new Vector3i(Vector3i.MAX);
      @Nonnull
      private final Vector3i max = new Vector3i(Vector3i.MIN);
      @Nonnull
      private final Int2ObjectMap<PrefabBufferColumn> columns = new Int2ObjectOpenHashMap();
      @Nonnull
      private final List<PrefabBuffer.ChildPrefab> childPrefabs = new ObjectArrayList(0);
      private Vector3i anchor = Vector3i.ZERO;

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
         } else {
            int columnIndex = MathUtil.packInt((short)x, (short)z);
            if (this.columns.containsKey(columnIndex)) {
               throw new IllegalStateException("Column is already set! Given: " + x + ", " + z);
            } else {
               int blockCount = entries.length;
               Int2ObjectOpenHashMap<Holder<ChunkStore>> holderMap = new Int2ObjectOpenHashMap();
               if (blockCount != 0 || entityHolders != null && entityHolders.length != 0) {
                  int readerIndex = this.buf.writerIndex();
                  this.buf.writeInt(blockCount);
                  if (blockCount > 0) {
                     int offset = entries[0].y;
                     if (offset < this.min.y) {
                        this.min.y = offset;
                     }

                     this.buf.writeInt(offset - 1);
                     offset = Integer.MIN_VALUE;

                     for (int i = 0; i < blockCount; i++) {
                        PrefabBufferBlockEntry entry = entries[i];
                        int y = entry.y;
                        int blockId = entry.blockId;
                        float chance = entry.chance;
                        Holder<ChunkStore> holder = entry.state;
                        int fluidId = entry.fluidId;
                        byte fluidLevel = entry.fluidLevel;
                        if (y <= offset) {
                           throw new IllegalArgumentException("Y Values are not sequential. " + offset + " -> " + y);
                        }

                        int offsetx = i == 0 ? 0 : y - offset;
                        if (offsetx > 65535) {
                           throw new IllegalArgumentException("Offset is larger than 65535. Given: " + offsetx);
                        }

                        boolean hasChance = chance < 1.0F;
                        int blockBytes = MathUtil.byteCount(blockId);
                        int offsetBytes = offsetx == 1 ? 0 : MathUtil.byteCount(offsetx);
                        int fluidBytes = MathUtil.byteCount(fluidId);
                        int mask = PrefabBuffer.BlockMaskConstants.getBlockMask(
                           blockBytes, fluidBytes, hasChance, offsetBytes, holder, entry.supportValue, entry.rotation, entry.filler
                        );
                        this.buf.writeShort(mask);
                        ByteBufUtil.writeNumber(this.buf, blockBytes, blockId);
                        ByteBufUtil.writeNumber(this.buf, offsetBytes, offsetx);
                        if (hasChance) {
                           this.buf.writeFloat(chance);
                        }

                        if (entry.rotation != 0) {
                           this.buf.writeByte(entry.rotation);
                        }

                        if (entry.filler != 0) {
                           this.buf.writeShort(entry.filler);
                        }

                        if (fluidId != 0) {
                           ByteBufUtil.writeNumber(this.buf, fluidBytes, fluidId);
                           this.buf.writeByte(fluidLevel);
                        }

                        if (holder != null) {
                           holderMap.put(y, holder);
                           this.handleBlockComponents(entry.rotation, x, y, z, holder);
                        }

                        offset = y;
                     }

                     if (offset > this.max.y) {
                        this.max.y = offset;
                     }
                  }

                  if (x < this.min.x) {
                     this.min.x = x;
                  }

                  if (x > this.max.x) {
                     this.max.x = x;
                  }

                  if (z < this.min.z) {
                     this.min.z = z;
                  }

                  if (z > this.max.z) {
                     this.max.z = z;
                  }

                  if (holderMap.isEmpty()) {
                     holderMap = null;
                  }

                  PrefabBufferColumn column = new PrefabBufferColumn(readerIndex, entityHolders, holderMap);
                  this.columns.put(columnIndex, column);
               }
            }
         }
      }

      private void handleBlockComponents(int blockRotation, int x, int y, int z, @Nonnull Holder<ChunkStore> holder) {
         ComponentType<ChunkStore, PrefabSpawnerState> componentType = BlockStateModule.get().getComponentType(PrefabSpawnerState.class);
         PrefabSpawnerState spawnerState = holder.getComponent(componentType);
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

      @Nonnull
      public PrefabBuffer build() {
         ByteBuf buffer = Unpooled.copiedBuffer(this.buf);
         this.buf.release();
         PrefabBuffer.ChildPrefab[] childPrefabArray = this.childPrefabs.toArray(PrefabBuffer.ChildPrefab[]::new);
         if (this.columns.isEmpty()) {
            this.min.assign(0);
            this.max.assign(0);
         }

         return new PrefabBuffer(buffer, this.anchor, this.min, this.max, this.columns, childPrefabArray);
      }
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
      @Nullable
      private ByteBuf buffer;

      private PrefabBufferAccessor(@Nonnull PrefabBuffer prefabBuffer) {
         this.buffer = prefabBuffer.buf.retainedDuplicate();
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
         this.prefabBuffer.checkReleased();
         return Math.min(
            rotation.getX(this.prefabBuffer.min.getX(), this.prefabBuffer.min.getZ()),
            rotation.getX(this.prefabBuffer.max.getX(), this.prefabBuffer.max.getZ())
         );
      }

      @Override
      public int getMinY() {
         this.prefabBuffer.checkReleased();
         return this.prefabBuffer.min.getY();
      }

      @Override
      public int getMinZ(@Nonnull PrefabRotation rotation) {
         this.prefabBuffer.checkReleased();
         return Math.min(
            rotation.getZ(this.prefabBuffer.min.getX(), this.prefabBuffer.min.getZ()),
            rotation.getZ(this.prefabBuffer.max.getX(), this.prefabBuffer.max.getZ())
         );
      }

      @Override
      public int getMaxX(@Nonnull PrefabRotation rotation) {
         this.prefabBuffer.checkReleased();
         return Math.max(
            rotation.getX(this.prefabBuffer.min.getX(), this.prefabBuffer.min.getZ()),
            rotation.getX(this.prefabBuffer.max.getX(), this.prefabBuffer.max.getZ())
         );
      }

      @Override
      public int getMaxY() {
         this.prefabBuffer.checkReleased();
         return this.prefabBuffer.max.getY();
      }

      @Override
      public int getMaxZ(@Nonnull PrefabRotation rotation) {
         this.prefabBuffer.checkReleased();
         return Math.max(
            rotation.getZ(this.prefabBuffer.min.getX(), this.prefabBuffer.min.getZ()),
            rotation.getZ(this.prefabBuffer.max.getX(), this.prefabBuffer.max.getZ())
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
         this.prefabBuffer.checkReleased();
         int rotatedX = rotation.getX(x, z);
         int rotatedZ = rotation.getZ(x, z);
         int columnIndex = MathUtil.packInt(rotatedX, rotatedZ);
         PrefabBufferColumn columnData = (PrefabBufferColumn)this.prefabBuffer.columns.get(columnIndex);
         if (columnData != null) {
            this.buffer.readerIndex(columnData.getReaderIndex());
            int blockCount = this.buffer.readInt();
            if (blockCount > 0) {
               return this.buffer.readInt() + 1;
            }
         }

         return -1;
      }

      @Override
      public int getMaxYAt(@Nonnull PrefabRotation rotation, int x, int z) {
         this.prefabBuffer.checkReleased();
         int rotatedX = rotation.getX(x, z);
         int rotatedZ = rotation.getZ(x, z);
         int columnIndex = MathUtil.packInt(rotatedX, rotatedZ);
         PrefabBufferColumn column = (PrefabBufferColumn)this.prefabBuffer.columns.get(columnIndex);
         if (column == null) {
            return -1;
         } else {
            this.buffer.readerIndex(column.getReaderIndex());
            int blockCount = this.buffer.readInt();
            if (blockCount > 0) {
               int y = this.buffer.readInt();

               for (int i = 0; i < blockCount; i++) {
                  int mask = this.buffer.readUnsignedShort();
                  if (PrefabBuffer.BlockMaskConstants.getOffsetBytes(mask) > 0) {
                     this.buffer.skipBytes(PrefabBuffer.BlockMaskConstants.getBlockBytes(mask));
                     y += ByteBufUtil.readNumber(this.buffer, PrefabBuffer.BlockMaskConstants.getOffsetBytes(mask));
                     if (PrefabBuffer.BlockMaskConstants.hasChance(mask)) {
                        this.buffer.skipBytes(4);
                     }

                     if (PrefabBuffer.BlockMaskConstants.hasRotation(mask)) {
                        this.buffer.skipBytes(1);
                     }

                     if (PrefabBuffer.BlockMaskConstants.hasFiller(mask)) {
                        this.buffer.skipBytes(2);
                     }

                     this.buffer.skipBytes(PrefabBuffer.BlockMaskConstants.getFluidBytes(mask));
                  } else {
                     this.buffer.skipBytes(PrefabBuffer.BlockMaskConstants.getSkipBytes(mask));
                     y++;
                  }
               }

               return y;
            } else {
               return -1;
            }
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
         this.prefabBuffer.checkReleased();
         this.prefabBuffer.columns.int2ObjectEntrySet().forEach(entry -> {
            int columnIndex = entry.getIntKey();
            int cx = MathUtil.unpackLeft(columnIndex);
            int cz = MathUtil.unpackRight(columnIndex);
            int xx = t.rotation.getX(cx, cz);
            int zx = t.rotation.getZ(cx, cz);
            PrefabBufferColumn column = (PrefabBufferColumn)entry.getValue();
            this.buffer.readerIndex(column.getReaderIndex());
            int blockCount = this.buffer.readInt();
            if (columnPredicate.test(xx, zx, blockCount, t)) {
               BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
               if (blockCount > 0) {
                  int y = this.buffer.readInt();

                  for (int i = 0; i < blockCount; i++) {
                     int mask = this.buffer.readUnsignedShort();
                     int blockBytes = PrefabBuffer.BlockMaskConstants.getBlockBytes(mask);
                     int blockId = ByteBufUtil.readNumber(this.buffer, blockBytes);
                     int offsetBytes = PrefabBuffer.BlockMaskConstants.getOffsetBytes(mask);
                     y += offsetBytes == 0 ? 1 : ByteBufUtil.readNumber(this.buffer, offsetBytes);
                     if (PrefabBuffer.BlockMaskConstants.hasChance(mask)) {
                        float chance = this.buffer.readFloat();
                        if (chance < t.random.nextFloat()) {
                           this.buffer.skipBytes(2);
                           this.buffer.skipBytes(PrefabBuffer.BlockMaskConstants.getFluidBytes(mask));
                           continue;
                        }
                     }

                     Holder<ChunkStore> holder = PrefabBuffer.BlockMaskConstants.hasComponents(mask) ? (Holder)column.getBlockComponents().get(y) : null;
                     int supportValue = PrefabBuffer.BlockMaskConstants.getSupportValue(mask);
                     int rotation = 0;
                     if (PrefabBuffer.BlockMaskConstants.hasRotation(mask)) {
                        rotation = this.buffer.readUnsignedByte();
                     }

                     rotation = t.rotation.getRotation(rotation);
                     int filler = 0;
                     if (PrefabBuffer.BlockMaskConstants.hasFiller(mask)) {
                        filler = t.rotation.getFiller(this.buffer.readUnsignedShort());
                     }

                     int fluidBytes = PrefabBuffer.BlockMaskConstants.getFluidBytes(mask);
                     int fluidId = 0;
                     int fluidLevel = 0;
                     if (fluidBytes != 0) {
                        fluidId = ByteBufUtil.readNumber(this.buffer, fluidBytes - 1);
                        fluidLevel = this.buffer.readByte();
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
      public <T> void forEachRaw(
         @Nonnull IPrefabBuffer.ColumnPredicate<T> columnPredicate,
         @Nonnull IPrefabBuffer.RawBlockConsumer<T> blockConsumer,
         @Nonnull IPrefabBuffer.FluidConsumer<T> fluidConsumer,
         @Nullable IPrefabBuffer.EntityConsumer<T> entityConsumer,
         @Nullable T t
      ) {
         this.prefabBuffer.checkReleased();
         this.prefabBuffer.columns.int2ObjectEntrySet().forEach(entry -> {
            int columnIndex = entry.getIntKey();
            int x = MathUtil.unpackLeft(columnIndex);
            int z = MathUtil.unpackRight(columnIndex);
            PrefabBufferColumn column = (PrefabBufferColumn)entry.getValue();
            this.buffer.readerIndex(column.getReaderIndex());
            int blockCount = this.buffer.readInt();
            if (columnPredicate.test(x, z, blockCount, t)) {
               if (blockCount > 0) {
                  int y = this.buffer.readInt();

                  for (int i = 0; i < blockCount; i++) {
                     int mask = this.buffer.readUnsignedShort();
                     int blockBytes = PrefabBuffer.BlockMaskConstants.getBlockBytes(mask);
                     int blockId = ByteBufUtil.readNumber(this.buffer, blockBytes);
                     int offsetBytes = PrefabBuffer.BlockMaskConstants.getOffsetBytes(mask);
                     y += offsetBytes == 0 ? 1 : ByteBufUtil.readNumber(this.buffer, offsetBytes);
                     float chance = PrefabBuffer.BlockMaskConstants.hasChance(mask) ? this.buffer.readFloat() : 1.0F;
                     Holder<ChunkStore> holder = PrefabBuffer.BlockMaskConstants.hasComponents(mask) ? (Holder)column.getBlockComponents().get(y) : null;
                     int supportValue = PrefabBuffer.BlockMaskConstants.getSupportValue(mask);
                     int rotation = 0;
                     if (PrefabBuffer.BlockMaskConstants.hasRotation(mask)) {
                        rotation = this.buffer.readUnsignedByte();
                     }

                     int filler = 0;
                     if (PrefabBuffer.BlockMaskConstants.hasFiller(mask)) {
                        filler = this.buffer.readUnsignedShort();
                     }

                     int position = this.buffer.readerIndex();
                     blockConsumer.accept(x, y, z, mask, blockId, chance, holder, supportValue, rotation, filler, t);
                     this.buffer.readerIndex(position);
                     int fluidBytes = PrefabBuffer.BlockMaskConstants.getFluidBytes(mask);
                     if (fluidBytes != 0) {
                        int fluidId = ByteBufUtil.readNumber(this.buffer, fluidBytes - 1);
                        byte fluidLevel = this.buffer.readByte();
                        position = this.buffer.readerIndex();
                        fluidConsumer.accept(x, y, z, fluidId, fluidLevel, t);
                        this.buffer.readerIndex(position);
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
         this.prefabBuffer.checkReleased();
         ObjectIterator var6 = this.prefabBuffer.columns.int2ObjectEntrySet().iterator();

         while (var6.hasNext()) {
            Entry<PrefabBufferColumn> entry = (Entry<PrefabBufferColumn>)var6.next();
            int columnIndex = entry.getIntKey();
            int x = MathUtil.unpackLeft(columnIndex);
            int z = MathUtil.unpackRight(columnIndex);
            PrefabBufferColumn column = (PrefabBufferColumn)entry.getValue();
            this.buffer.readerIndex(column.getReaderIndex());
            int blockCount = this.buffer.readInt();
            if (!columnPredicate.test(x, z, blockCount, t)) {
               return false;
            }

            if (blockCount > 0) {
               int y = this.buffer.readInt();

               for (int i = 0; i < blockCount; i++) {
                  int mask = this.buffer.readUnsignedShort();
                  int blockBytes = PrefabBuffer.BlockMaskConstants.getBlockBytes(mask);
                  int blockId = ByteBufUtil.readNumber(this.buffer, blockBytes);
                  int offsetBytes = PrefabBuffer.BlockMaskConstants.getOffsetBytes(mask);
                  y += offsetBytes == 0 ? 1 : ByteBufUtil.readNumber(this.buffer, offsetBytes);
                  float chance = PrefabBuffer.BlockMaskConstants.hasChance(mask) ? this.buffer.readFloat() : 1.0F;
                  Holder<ChunkStore> holder = PrefabBuffer.BlockMaskConstants.hasComponents(mask) ? (Holder)column.getBlockComponents().get(y) : null;
                  short rotation = PrefabBuffer.BlockMaskConstants.hasRotation(mask) ? this.buffer.readUnsignedByte() : 0;
                  int filler = PrefabBuffer.BlockMaskConstants.hasFiller(mask) ? this.buffer.readUnsignedShort() : 0;
                  int supportValue = PrefabBuffer.BlockMaskConstants.getSupportValue(mask);
                  int position = this.buffer.readerIndex();
                  if (!blockPredicate.test(x, y, z, blockId, chance, holder, supportValue, rotation, filler, t)) {
                     return false;
                  }

                  this.buffer.readerIndex(position);
                  int fluidBytes = PrefabBuffer.BlockMaskConstants.getFluidBytes(mask);
                  if (fluidBytes != 0) {
                     int fluidId = ByteBufUtil.readNumber(this.buffer, fluidBytes - 1);
                     byte fluidLevel = this.buffer.readByte();
                     position = this.buffer.readerIndex();
                     if (!fluidPredicate.test(x, y, z, fluidId, fluidLevel, t)) {
                        return false;
                     }

                     this.buffer.readerIndex(position);
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
      public void release() {
         this.buffer.release();
         this.buffer = null;
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
            this.prefabBuffer.checkReleased();
            BlockTypeAssetMap assetMap = BlockType.getAssetMap();
            IntIterator columnIterator = columnIndexes.iterator();

            while (columnIterator.hasNext()) {
               int columnIndex = columnIterator.nextInt();
               int cx = MathUtil.unpackLeft(columnIndex);
               int cz = MathUtil.unpackRight(columnIndex);
               int x = t.rotation.getX(cx, cz);
               int z = t.rotation.getZ(cx, cz);
               PrefabBufferColumn firstColumn = (PrefabBufferColumn)this.prefabBuffer.columns.get(columnIndex);
               PrefabBufferColumn secondColumn = (PrefabBufferColumn)secondPrefabColumns.get(columnIndex);
               if (firstColumn != null) {
                  this.buffer.readerIndex(firstColumn.getReaderIndex());
               }

               if (secondColumn != null) {
                  secondPrefab.buffer.readerIndex(secondColumn.getReaderIndex());
               }

               int firstColumnBlockCount = firstColumn != null ? this.buffer.readInt() : 0;
               int secondColumnBlockCount = secondColumn != null ? secondPrefab.buffer.readInt() : 0;
               if (firstColumnBlockCount != 0 || secondColumnBlockCount != 0) {
                  int firstColumnY = firstColumnBlockCount > 0 ? this.buffer.readInt() : Integer.MAX_VALUE;
                  int secondColumnY = secondColumnBlockCount > 0 ? secondPrefab.buffer.readInt() : Integer.MAX_VALUE;
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
                     int oldFirstColumnReaderIndex = firstColumnBlocksRead < firstColumnBlockCount ? this.buffer.readerIndex() : -1;
                     int oldSecondColumnReaderIndex = secondColumnBlocksRead < secondColumnBlockCount ? secondPrefab.buffer.readerIndex() : -1;
                     if (firstColumnBlocksRead < firstColumnBlockCount) {
                        int mask = this.buffer.readUnsignedShort();
                        int blockBytes = PrefabBuffer.BlockMaskConstants.getBlockBytes(mask);
                        firstColumnBlockId = ByteBufUtil.readNumber(this.buffer, blockBytes);
                        int offsetBytes = PrefabBuffer.BlockMaskConstants.getOffsetBytes(mask);
                        firstColumnY += offsetBytes == 0 ? 1 : ByteBufUtil.readNumber(this.buffer, offsetBytes);
                        firstColumnChance = PrefabBuffer.BlockMaskConstants.hasChance(mask) ? this.buffer.readFloat() : 1.0F;
                        firstColumnRotation = t.rotation.getRotation(PrefabBuffer.BlockMaskConstants.hasRotation(mask) ? this.buffer.readUnsignedByte() : 0);
                        firstColumnFiller = PrefabBuffer.BlockMaskConstants.hasFiller(mask) ? t.rotation.getFiller(this.buffer.readUnsignedShort()) : 0;
                        firstColumnComponents = PrefabBuffer.BlockMaskConstants.hasComponents(mask)
                           ? (Holder)firstColumn.getBlockComponents().get(firstColumnY)
                           : null;
                        this.buffer.skipBytes(PrefabBuffer.BlockMaskConstants.getFluidBytes(mask));
                     }

                     if (secondColumnBlocksRead < secondColumnBlockCount) {
                        int mask = secondPrefab.buffer.readUnsignedShort();
                        int blockBytes = PrefabBuffer.BlockMaskConstants.getBlockBytes(mask);
                        secondColumnBlockId = ByteBufUtil.readNumber(secondPrefab.buffer, blockBytes);
                        int offsetBytes = PrefabBuffer.BlockMaskConstants.getOffsetBytes(mask);
                        secondColumnY += offsetBytes == 0 ? 1 : ByteBufUtil.readNumber(secondPrefab.buffer, offsetBytes);
                        secondColumnChance = PrefabBuffer.BlockMaskConstants.hasChance(mask) ? secondPrefab.buffer.readFloat() : 1.0F;
                        secondColumnRotation = t.rotation
                           .getRotation(PrefabBuffer.BlockMaskConstants.hasRotation(mask) ? secondPrefab.buffer.readUnsignedByte() : 0);
                        secondColumnFiller = PrefabBuffer.BlockMaskConstants.hasFiller(mask)
                           ? t.rotation.getFiller(secondPrefab.buffer.readUnsignedShort())
                           : 0;
                        secondColumnComponents = PrefabBuffer.BlockMaskConstants.hasComponents(mask)
                           ? (Holder)secondColumn.getBlockComponents().get(secondColumnY)
                           : null;
                        secondPrefab.buffer.skipBytes(PrefabBuffer.BlockMaskConstants.getFluidBytes(mask));
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
                        if (oldFirstColumnReaderIndex != -1) {
                           this.buffer.readerIndex(oldFirstColumnReaderIndex);
                        }

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
                        if (oldSecondColumnReaderIndex != -1) {
                           secondPrefab.buffer.readerIndex(oldSecondColumnReaderIndex);
                        }

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
         this.prefabBuffer.checkReleased();
         PrefabBufferColumn column = (PrefabBufferColumn)this.prefabBuffer.columns.get(MathUtil.packInt(x, z));
         if (column == null) {
            return 0;
         } else {
            this.buffer.readerIndex(column.getReaderIndex());
            int blockCount = this.buffer.readInt();
            if (blockCount <= 0) {
               return 0;
            } else {
               int blockY = this.buffer.readInt();

               for (int i = 0; i < blockCount; i++) {
                  int mask = this.buffer.readUnsignedShort();
                  int blockBytes = PrefabBuffer.BlockMaskConstants.getBlockBytes(mask);
                  int blockId = ByteBufUtil.readNumber(this.buffer, blockBytes);
                  int offsetBytes = PrefabBuffer.BlockMaskConstants.getOffsetBytes(mask);
                  blockY += offsetBytes == 0 ? 1 : ByteBufUtil.readNumber(this.buffer, offsetBytes);
                  if (blockY > y) {
                     return 0;
                  }

                  if (PrefabBuffer.BlockMaskConstants.hasChance(mask)) {
                     this.buffer.readFloat();
                  }

                  if (PrefabBuffer.BlockMaskConstants.hasRotation(mask)) {
                     this.buffer.readUnsignedByte();
                  }

                  if (PrefabBuffer.BlockMaskConstants.hasFiller(mask)) {
                     this.buffer.readUnsignedShort();
                  }

                  int fluidBytes = PrefabBuffer.BlockMaskConstants.getFluidBytes(mask);
                  this.buffer.skipBytes(fluidBytes);
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
         this.prefabBuffer.checkReleased();
         PrefabBufferColumn column = (PrefabBufferColumn)this.prefabBuffer.columns.get(MathUtil.packInt(x, z));
         if (column == null) {
            return 0;
         } else {
            this.buffer.readerIndex(column.getReaderIndex());
            int blockCount = this.buffer.readInt();
            if (blockCount <= 0) {
               return 0;
            } else {
               int blockY = this.buffer.readInt();

               for (int i = 0; i < blockCount; i++) {
                  int mask = this.buffer.readUnsignedShort();
                  int blockBytes = PrefabBuffer.BlockMaskConstants.getBlockBytes(mask);
                  int blockId = ByteBufUtil.readNumber(this.buffer, blockBytes);
                  int offsetBytes = PrefabBuffer.BlockMaskConstants.getOffsetBytes(mask);
                  blockY += offsetBytes == 0 ? 1 : ByteBufUtil.readNumber(this.buffer, offsetBytes);
                  if (blockY > y) {
                     return 0;
                  }

                  if (PrefabBuffer.BlockMaskConstants.hasChance(mask)) {
                     this.buffer.readFloat();
                  }

                  if (PrefabBuffer.BlockMaskConstants.hasRotation(mask)) {
                     this.buffer.readUnsignedByte();
                  }

                  int filler = 0;
                  if (PrefabBuffer.BlockMaskConstants.hasFiller(mask)) {
                     filler = this.buffer.readUnsignedShort();
                  }

                  int fluidBytes = PrefabBuffer.BlockMaskConstants.getFluidBytes(mask);
                  this.buffer.skipBytes(fluidBytes);
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
         this.prefabBuffer.checkReleased();
         PrefabBufferColumn column = (PrefabBufferColumn)this.prefabBuffer.columns.get(MathUtil.packInt(x, z));
         if (column == null) {
            return 0;
         } else {
            this.buffer.readerIndex(column.getReaderIndex());
            int blockCount = this.buffer.readInt();
            if (blockCount <= 0) {
               return 0;
            } else {
               int blockY = this.buffer.readInt();

               for (int i = 0; i < blockCount; i++) {
                  int mask = this.buffer.readUnsignedShort();
                  int blockBytes = PrefabBuffer.BlockMaskConstants.getBlockBytes(mask);
                  int blockId = ByteBufUtil.readNumber(this.buffer, blockBytes);
                  int offsetBytes = PrefabBuffer.BlockMaskConstants.getOffsetBytes(mask);
                  blockY += offsetBytes == 0 ? 1 : ByteBufUtil.readNumber(this.buffer, offsetBytes);
                  if (blockY > y) {
                     return 0;
                  }

                  if (PrefabBuffer.BlockMaskConstants.hasChance(mask)) {
                     this.buffer.readFloat();
                  }

                  int rotation = 0;
                  if (PrefabBuffer.BlockMaskConstants.hasRotation(mask)) {
                     rotation = this.buffer.readUnsignedByte();
                  }

                  if (PrefabBuffer.BlockMaskConstants.hasFiller(mask)) {
                     this.buffer.readUnsignedShort();
                  }

                  int fluidBytes = PrefabBuffer.BlockMaskConstants.getFluidBytes(mask);
                  this.buffer.skipBytes(fluidBytes);
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
