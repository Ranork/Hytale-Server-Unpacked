package com.hypixel.hytale.server.core.universe.world.chunk.environment;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.modules.LegacyModule;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.io.ByteBufUtil;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.Int2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import javax.annotation.Nonnull;

public class EnvironmentChunk implements Component<ChunkStore> {
   public static final BuilderCodec<EnvironmentChunk> CODEC = BuilderCodec.builder(EnvironmentChunk.class, EnvironmentChunk::new)
      .addField(new KeyedCodec<>("Data", Codec.BYTE_ARRAY), EnvironmentChunk::deserialize, EnvironmentChunk::serialize)
      .build();
   private final EnvironmentColumn[] columns = new EnvironmentColumn[1024];
   private final Int2LongMap counts = new Int2LongOpenHashMap();

   public static ComponentType<ChunkStore, EnvironmentChunk> getComponentType() {
      return LegacyModule.get().getEnvironmentChunkComponentType();
   }

   public EnvironmentChunk() {
      this(0);
   }

   public EnvironmentChunk(int defaultId) {
      for (int i = 0; i < this.columns.length; i++) {
         this.columns[i] = new EnvironmentColumn(defaultId);
      }
   }

   @Nonnull
   @Override
   public Component<ChunkStore> clone() {
      EnvironmentChunk chunk = new EnvironmentChunk();

      for (int i = 0; i < this.columns.length; i++) {
         chunk.columns[i].copyFrom(this.columns[i]);
      }

      chunk.counts.putAll(this.counts);
      return chunk;
   }

   public int get(int x, int y, int z) {
      return this.columns[idx(x, z)].get(y);
   }

   public EnvironmentColumn get(int x, int z) {
      return this.columns[idx(x, z)];
   }

   public void setColumn(int x, int z, int environmentId) {
      EnvironmentColumn column = this.columns[idx(x, z)];
      column.set(environmentId);
      int minY = Integer.MIN_VALUE;

      int maxY;
      do {
         int id = column.get(minY);
         maxY = column.getMax(minY);
         int count = maxY - minY + 1;
         this.decrementBlockCount(id, count);
      } while (maxY < Integer.MAX_VALUE);

      this.createIfNotExist(environmentId);
      this.incrementBlockCount(environmentId, Integer.MAX_VALUE);
      column.set(environmentId);
   }

   public boolean set(int x, int y, int z, int environmentId) {
      EnvironmentColumn column = this.columns[idx(x, z)];
      int oldInternalId = column.get(y);
      if (environmentId != oldInternalId) {
         this.decrementBlockCount(oldInternalId, 1L);
         this.createIfNotExist(environmentId);
         this.incrementBlockCount(environmentId);
         column.set(y, environmentId);
         return true;
      } else {
         return false;
      }
   }

   public boolean contains(int environmentId) {
      return this.counts.containsKey(environmentId);
   }

   private void createIfNotExist(int environmentId) {
      if (!this.counts.containsKey(environmentId)) {
         this.counts.put(environmentId, 0L);
      }
   }

   private void incrementBlockCount(int internalId) {
      this.counts.mergeLong(internalId, 1L, Long::sum);
   }

   private void incrementBlockCount(int internalId, int count) {
      long oldCount = this.counts.get(internalId);
      this.counts.put(internalId, oldCount + count);
   }

   private boolean decrementBlockCount(int environmentId, long count) {
      long oldCount = this.counts.get(environmentId);
      if (oldCount <= count) {
         this.counts.remove(environmentId);
         return true;
      } else {
         this.counts.put(environmentId, oldCount - count);
         return false;
      }
   }

   private byte[] serialize() {
      ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();

      try {
         buf.writeInt(this.counts.size());
         ObjectIterator t = this.counts.int2LongEntrySet().iterator();

         while (t.hasNext()) {
            Entry entry = (Entry)t.next();
            int environmentId = entry.getIntKey();
            Environment environment = Environment.getAssetMap().getAsset(environmentId);
            String key = environment != null ? environment.getId() : Environment.UNKNOWN.getId();
            buf.writeInt(environmentId);
            ByteBufUtil.writeUTF(buf, key);
         }

         for (int i = 0; i < this.columns.length; i++) {
            this.columns[i].serialize(buf, (environmentIdx, buf0) -> buf0.writeInt(environmentIdx));
         }

         return ByteBufUtil.getBytesRelease(buf);
      } catch (Throwable var7) {
         buf.release();
         throw SneakyThrow.sneakyThrow(var7);
      }
   }

   private void deserialize(@Nonnull byte[] bytes) {
      ByteBuf buf = Unpooled.wrappedBuffer(bytes);
      this.counts.clear();
      int mappingCount = buf.readInt();
      Int2IntMap idMapping = new Int2IntOpenHashMap(mappingCount);

      for (int i = 0; i < mappingCount; i++) {
         int serialId = buf.readInt();
         String key = ByteBufUtil.readUTF(buf);
         int environmentId = Environment.getIndexOrUnknown(key, "Failed to find environment '%s' when deserializing environment chunk", key);
         idMapping.put(serialId, environmentId);
         this.counts.put(environmentId, 0L);
      }

      for (int i = 0; i < this.columns.length; i++) {
         EnvironmentColumn column = this.columns[i];
         column.deserialize(buf, buf0 -> idMapping.get(buf0.readInt()));

         for (int x = 0; x < column.size(); x++) {
            this.counts.mergeLong(column.getValue(x), 1L, Long::sum);
         }
      }
   }

   public byte[] serializeProtocol() {
      ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();

      for (int i = 0; i < this.columns.length; i++) {
         this.columns[i].serializeProtocol(buf);
      }

      return ByteBufUtil.getBytesRelease(buf);
   }

   public void trim() {
      for (int i = 0; i < this.columns.length; i++) {
         this.columns[i].trim();
      }
   }

   private static int idx(int x, int z) {
      return ChunkUtil.indexColumn(x, z);
   }

   public static class BulkWriter {
      private final EnvironmentChunk.BulkWriter.ColumnWriter[] columnWriters = new EnvironmentChunk.BulkWriter.ColumnWriter[1024];

      public BulkWriter() {
         for (int i = 0; i < this.columnWriters.length; i++) {
            this.columnWriters[i] = new EnvironmentChunk.BulkWriter.ColumnWriter();
         }
      }

      @Nonnull
      public EnvironmentChunk.BulkWriter.ColumnWriter getColumnWriter(int x, int z) {
         assert x >= 0 && x < 32;

         assert z >= 0 && z < 32;

         int idx = EnvironmentChunk.idx(x, z);
         return this.columnWriters[idx];
      }

      public void write(@Nonnull EnvironmentChunk environmentChunk) {
         environmentChunk.counts.clear();

         for (int x = 0; x < 32; x++) {
            for (int z = 0; z < 32; z++) {
               int idx = EnvironmentChunk.idx(x, z);

               assert this.columnWriters[idx] != null;

               this.columnWriters[idx].write(environmentChunk.columns[idx]);
               transferCounts(this.columnWriters[idx].columnCounts, environmentChunk.counts);
            }
         }
      }

      private static void transferCounts(@Nonnull Int2LongMap from, @Nonnull Int2LongMap into) {
         ObjectIterator var2 = from.int2LongEntrySet().iterator();

         while (var2.hasNext()) {
            Entry entry = (Entry)var2.next();
            long currentCount = into.getOrDefault(entry.getIntKey(), 0L);
            into.put(entry.getIntKey(), currentCount + entry.getLongValue());
         }
      }

      public static class ColumnWriter {
         private final IntArrayList maxYsReversed = new IntArrayList(2);
         private final IntArrayList valuesReversed = new IntArrayList(2);
         private final Int2LongMap columnCounts = new Int2LongOpenHashMap(2);

         public void write(@Nonnull EnvironmentColumn environmentColumn) {
            int[] maxYs = new int[this.maxYsReversed.size()];
            int[] values = new int[this.valuesReversed.size()];
            int index = 0;

            for (int reversedIndex = maxYs.length - 1; reversedIndex >= 0; index++) {
               maxYs[index] = this.maxYsReversed.getInt(reversedIndex);
               reversedIndex--;
            }

            index = 0;

            for (int reversedIndex = values.length - 1; reversedIndex >= 0; index++) {
               values[index] = this.valuesReversed.getInt(reversedIndex);
               reversedIndex--;
            }

            environmentColumn.resetTo(maxYs, values);
         }

         public void count(int environmentId, int count) {
            long currentCount = this.columnCounts.getOrDefault(environmentId, 0L);
            count = (int)(count + currentCount);
            this.columnCounts.put(environmentId, count);
         }

         public void intake(@Nonnull Int2IntFunction dataSource) {
            int maxYInclusive = 319;
            int previousEnvironment = 0;
            int environmentId = 0;
            int runCounter = 0;

            for (int y = 319; y >= 0; y--) {
               environmentId = dataSource.applyAsInt(y);
               if (y == 319) {
                  previousEnvironment = environmentId;
                  this.valuesReversed.add(environmentId);
                  runCounter++;
               } else if (environmentId == previousEnvironment) {
                  runCounter++;
               } else {
                  this.maxYsReversed.add(y);
                  this.valuesReversed.add(environmentId);
                  previousEnvironment = environmentId;
                  this.count(environmentId, runCounter);
                  runCounter = 1;
               }
            }

            this.count(environmentId, runCounter);
         }
      }
   }
}
