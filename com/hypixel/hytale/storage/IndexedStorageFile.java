package com.hypixel.hytale.storage;

import com.github.luben.zstd.Zstd;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.metrics.MetricsRegistry;
import com.hypixel.hytale.unsafe.UnsafeUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IndexedStorageFile implements Closeable {
   public static final StampedLock[] EMPTY_STAMPED_LOCKS = new StampedLock[0];
   public static final MetricsRegistry<IndexedStorageFile> METRICS_REGISTRY = new MetricsRegistry<IndexedStorageFile>()
      .register("Size", file -> {
         try {
            return file.size();
         } catch (IOException var2) {
            return -1L;
         }
      }, Codec.LONG)
      .register("CompressionLevel", file -> file.getCompressionLevel(), Codec.INTEGER)
      .register("BlobCount", file -> file.getBlobCount(), Codec.INTEGER)
      .register("UsedBlobCount", file -> file.keys().size(), Codec.INTEGER)
      .register("SegmentSize", file -> file.segmentSize(), Codec.INTEGER)
      .register("SegmentCount", file -> file.segmentCount(), Codec.INTEGER);
   public static final String MAGIC_STRING = "HytaleIndexedStorage";
   public static final int VERSION = 1;
   public static final int DEFAULT_BLOB_COUNT = 1024;
   public static final int DEFAULT_SEGMENT_SIZE = 4096;
   public static final int DEFAULT_COMPRESSION_LEVEL = 3;
   static final IndexedStorageFile.OffsetHelper HOH = new IndexedStorageFile.OffsetHelper();
   public static final int MAGIC_LENGTH = 20;
   public static final int MAGIC_OFFSET = HOH.next(20);
   public static final int VERSION_OFFSET = HOH.next(4);
   public static final int BLOB_COUNT_OFFSET = HOH.next(4);
   public static final int SEGMENT_SIZE_OFFSET = HOH.next(4);
   public static final int HEADER_LENGTH = HOH.length();
   static final IndexedStorageFile.OffsetHelper BOH = new IndexedStorageFile.OffsetHelper();
   public static final int SRC_LENGTH_OFFSET = BOH.next(4);
   public static final int COMPRESSED_LENGTH_OFFSET = BOH.next(4);
   public static final int BLOB_HEADER_LENGTH = BOH.length();
   public static final int INDEX_SIZE = 4;
   public static final int UNASSIGNED_INDEX = 0;
   public static final int FIRST_SEGMENT_INDEX = 1;
   public static final FileAttribute<?>[] NO_ATTRIBUTES = new FileAttribute[0];
   static final byte[] MAGIC_BYTES = "HytaleIndexedStorage".getBytes(StandardCharsets.UTF_8);
   private static final ByteBuffer MAGIC_BUFFER = ByteBuffer.wrap(MAGIC_BYTES);
   private static final ThreadLocal<ByteBuffer> CACHED_TEMP_BUFFER = ThreadLocal.withInitial(() -> ByteBuffer.allocateDirect(HEADER_LENGTH));
   @Nonnull
   private final Path path;
   private final FileChannel fileChannel;
   private boolean flushOnWrite = false;
   private int compressionLevel = 3;
   private int version;
   private int blobCount;
   private int segmentSize;
   private StampedLock[] indexLocks;
   private MappedByteBuffer mappedBlobIndexes;
   private final StampedLock segmentLocksLock = new StampedLock();
   private StampedLock[] segmentLocks = EMPTY_STAMPED_LOCKS;
   private final StampedLock usedSegmentsLock = new StampedLock();
   private final BitSet usedSegments = new BitSet();

   @Nonnull
   private static ByteBuffer getTempBuffer(int length) {
      ByteBuffer buffer = CACHED_TEMP_BUFFER.get();
      buffer.position(0);
      buffer.limit(length);
      return buffer;
   }

   @Nonnull
   private static ByteBuffer allocateDirect(int length) {
      return ByteBuffer.allocateDirect(length);
   }

   @Nonnull
   public static IndexedStorageFile open(@Nonnull Path path, OpenOption... options) throws IOException {
      return open(path, 1024, 4096, Set.of(options), NO_ATTRIBUTES);
   }

   @Nonnull
   public static IndexedStorageFile open(@Nonnull Path path, @Nonnull Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
      return open(path, 1024, 4096, options, attrs);
   }

   @Nonnull
   public static IndexedStorageFile open(@Nonnull Path path, int blobCount, int segmentSize, OpenOption... options) throws IOException {
      return open(path, blobCount, segmentSize, Set.of(options), NO_ATTRIBUTES);
   }

   @Nonnull
   public static IndexedStorageFile open(
      @Nonnull Path path, int blobCount, int segmentSize, @Nonnull Set<? extends OpenOption> options, FileAttribute<?>... attrs
   ) throws IOException {
      IndexedStorageFile storageFile = new IndexedStorageFile(path, FileChannel.open(path, options, attrs));
      if (options.contains(StandardOpenOption.CREATE_NEW)) {
         storageFile.create(blobCount, segmentSize);
         return storageFile;
      } else {
         if (options.contains(StandardOpenOption.CREATE) && storageFile.fileChannel.size() == 0L) {
            storageFile.create(blobCount, segmentSize);
         } else {
            if (storageFile.fileChannel.size() == 0L) {
               throw new IOException("file channel is empty");
            }

            storageFile.readHeader();
            storageFile.memoryMapBlobIndexes();
            if (storageFile.version == 0) {
               storageFile = migrateV0(path, blobCount, segmentSize, options, attrs, storageFile);
            } else {
               storageFile.readUsedSegments();
            }
         }

         return storageFile;
      }
   }

   private static IndexedStorageFile migrateV0(
      Path path, int blobCount, int segmentSize, Set<? extends OpenOption> options, FileAttribute<?>[] attrs, IndexedStorageFile storageFile
   ) throws IOException {
      storageFile.close();
      Path tempFile = path.resolveSibling(path.getFileName().toString() + ".old");
      Path tempPath = Files.move(path, tempFile, StandardCopyOption.REPLACE_EXISTING);
      HashSet<OpenOption> newOptions = new HashSet<>(options);
      newOptions.add(StandardOpenOption.CREATE);
      storageFile = new IndexedStorageFile(path, FileChannel.open(path, newOptions, attrs));
      storageFile.create(blobCount, segmentSize);

      try (IndexedStorageFile_v0 oldStorageFile = new IndexedStorageFile_v0(tempPath, FileChannel.open(tempPath, options, attrs))) {
         oldStorageFile.open();

         for (int blobIndex = 0; blobIndex < blobCount; blobIndex++) {
            ByteBuffer blob = oldStorageFile.readBlob(blobIndex);
            if (blob != null) {
               storageFile.writeBlob(blobIndex, blob);
            }
         }
      } finally {
         Files.delete(tempFile);
      }

      return storageFile;
   }

   private IndexedStorageFile(@Nonnull Path path, @Nonnull FileChannel fileChannel) {
      this.path = path;
      this.fileChannel = fileChannel;
   }

   @Nonnull
   public Path getPath() {
      return this.path;
   }

   public int getBlobCount() {
      return this.blobCount;
   }

   public int getSegmentSize() {
      return this.segmentSize;
   }

   public int getCompressionLevel() {
      return this.compressionLevel;
   }

   public void setFlushOnWrite(boolean flushOnWrite) {
      this.flushOnWrite = flushOnWrite;
   }

   public void setCompressionLevel(int compressionLevel) {
      this.compressionLevel = compressionLevel;
   }

   @Nonnull
   protected IndexedStorageFile create(int blobCount, int segmentSize) throws IOException {
      if (blobCount <= 0) {
         throw new IllegalArgumentException("blobCount must be > 0");
      } else if (segmentSize <= 0) {
         throw new IllegalArgumentException("segmentSize must be > 0");
      } else {
         this.blobCount = blobCount;
         this.segmentSize = segmentSize;
         if (this.fileChannel.size() != 0L) {
            throw new IOException("file channel is not empty");
         } else {
            this.writeHeader(blobCount, segmentSize);
            this.memoryMapBlobIndexes();
            return this;
         }
      }
   }

   protected void writeHeader(int blobCount, int segmentSize) throws IOException {
      ByteBuffer header = getTempBuffer(HEADER_LENGTH);
      header.put(MAGIC_BYTES);
      header.putInt(VERSION_OFFSET, 1);
      header.putInt(BLOB_COUNT_OFFSET, blobCount);
      header.putInt(SEGMENT_SIZE_OFFSET, segmentSize);
      header.position(0);
      if (this.fileChannel.write(header, 0L) != HEADER_LENGTH) {
         throw new IllegalStateException();
      }
   }

   protected void readHeader() throws IOException {
      ByteBuffer header = getTempBuffer(HEADER_LENGTH);
      if (this.fileChannel.read(header, 0L) != HEADER_LENGTH) {
         throw new IllegalStateException();
      } else {
         header.position(0);
         header.limit(20);
         if (!MAGIC_BUFFER.equals(header)) {
            header.position(0);
            byte[] dst = new byte[20];
            header.get(dst);
            throw new IOException("Invalid MAGIC! " + header + ", " + Arrays.toString(dst) + " expected " + Arrays.toString(MAGIC_BYTES));
         } else {
            header.limit(HEADER_LENGTH);
            this.version = header.getInt(VERSION_OFFSET);
            if (this.version >= 0 && this.version <= 1) {
               this.blobCount = header.getInt(BLOB_COUNT_OFFSET);
               this.segmentSize = header.getInt(SEGMENT_SIZE_OFFSET);
            } else {
               throw new IOException("Invalid version! " + this.version);
            }
         }
      }
   }

   protected void memoryMapBlobIndexes() throws IOException {
      this.indexLocks = new StampedLock[this.blobCount];

      for (int i = 0; i < this.blobCount; i++) {
         this.indexLocks[i] = new StampedLock();
      }

      this.mappedBlobIndexes = this.fileChannel.map(MapMode.READ_WRITE, HEADER_LENGTH, this.blobCount * 4L);
   }

   protected void readUsedSegments() throws IOException {
      long stamp = this.usedSegmentsLock.writeLock();

      try {
         for (int blobIndex = 0; blobIndex < this.blobCount; blobIndex++) {
            int indexPos = blobIndex * 4;
            long segmentStamp = this.indexLocks[blobIndex].readLock();

            int firstSegmentIndex;
            int compressedLength;
            try {
               firstSegmentIndex = this.mappedBlobIndexes.getInt(indexPos);
               if (firstSegmentIndex == 0) {
                  compressedLength = 0;
               } else {
                  ByteBuffer blobHeaderBuffer = this.readBlobHeader(firstSegmentIndex);
                  compressedLength = blobHeaderBuffer.getInt(COMPRESSED_LENGTH_OFFSET);
               }
            } finally {
               this.indexLocks[blobIndex].unlockRead(segmentStamp);
            }

            if (compressedLength > 0) {
               int segmentsCount = this.requiredSegments(BLOB_HEADER_LENGTH + compressedLength);
               this.usedSegments.set(firstSegmentIndex, firstSegmentIndex + segmentsCount);
            }
         }
      } finally {
         this.usedSegmentsLock.unlockWrite(stamp);
      }
   }

   public long size() throws IOException {
      return this.fileChannel.size();
   }

   public int segmentSize() {
      try {
         return this.requiredSegments(this.fileChannel.size() - this.segmentsBase()) + 1;
      } catch (IOException var2) {
         return -1;
      }
   }

   public int segmentCount() {
      long stamp = this.usedSegmentsLock.tryOptimisticRead();
      int count = this.usedSegments.cardinality();
      if (this.usedSegmentsLock.validate(stamp)) {
         return count;
      } else {
         stamp = this.usedSegmentsLock.readLock();

         int var4;
         try {
            var4 = this.usedSegments.cardinality();
         } finally {
            this.usedSegmentsLock.unlockRead(stamp);
         }

         return var4;
      }
   }

   @Nonnull
   public IntList keys() {
      IntArrayList list = new IntArrayList(this.blobCount);

      for (int blobIndex = 0; blobIndex < this.blobCount; blobIndex++) {
         int indexPos = blobIndex * 4;
         StampedLock lock = this.indexLocks[blobIndex];
         long stamp = lock.tryOptimisticRead();
         int segmentIndex = this.mappedBlobIndexes.getInt(indexPos);
         if (lock.validate(stamp)) {
            if (segmentIndex != 0) {
               list.add(blobIndex);
            }
         } else {
            stamp = lock.readLock();

            try {
               if (this.mappedBlobIndexes.getInt(indexPos) != 0) {
                  list.add(blobIndex);
               }
            } finally {
               lock.unlockRead(stamp);
            }
         }
      }

      return list;
   }

   public int readBlobLength(int blobIndex) throws IOException {
      if (blobIndex >= 0 && blobIndex < this.blobCount) {
         int indexPos = blobIndex * 4;
         long stamp = this.indexLocks[blobIndex].readLock();

         byte blobHeaderBuffer;
         try {
            int firstSegmentIndex = this.mappedBlobIndexes.getInt(indexPos);
            if (firstSegmentIndex != 0) {
               ByteBuffer blobHeaderBufferx = this.readBlobHeader(firstSegmentIndex);
               return blobHeaderBufferx.getInt(SRC_LENGTH_OFFSET);
            }

            blobHeaderBuffer = 0;
         } finally {
            this.indexLocks[blobIndex].unlockRead(stamp);
         }

         return blobHeaderBuffer;
      } else {
         throw new IndexOutOfBoundsException("Index out of range: " + blobIndex + " blobCount: " + this.blobCount);
      }
   }

   public int readBlobCompressedLength(int blobIndex) throws IOException {
      if (blobIndex >= 0 && blobIndex < this.blobCount) {
         int indexPos = blobIndex * 4;
         long stamp = this.indexLocks[blobIndex].readLock();

         byte blobHeaderBuffer;
         try {
            int firstSegmentIndex = this.mappedBlobIndexes.getInt(indexPos);
            if (firstSegmentIndex != 0) {
               ByteBuffer blobHeaderBufferx = this.readBlobHeader(firstSegmentIndex);
               return blobHeaderBufferx.getInt(COMPRESSED_LENGTH_OFFSET);
            }

            blobHeaderBuffer = 0;
         } finally {
            this.indexLocks[blobIndex].unlockRead(stamp);
         }

         return blobHeaderBuffer;
      } else {
         throw new IndexOutOfBoundsException("Index out of range: " + blobIndex + " blobCount: " + this.blobCount);
      }
   }

   @Nullable
   public ByteBuffer readBlob(int blobIndex) throws IOException {
      if (blobIndex >= 0 && blobIndex < this.blobCount) {
         int indexPos = blobIndex * 4;
         long stamp = this.indexLocks[blobIndex].readLock();

         ByteBuffer src;
         int srcLength;
         label43: {
            ByteBuffer blobHeaderBuffer;
            try {
               int firstSegmentIndex = this.mappedBlobIndexes.getInt(indexPos);
               if (firstSegmentIndex != 0) {
                  blobHeaderBuffer = this.readBlobHeader(firstSegmentIndex);
                  srcLength = blobHeaderBuffer.getInt(SRC_LENGTH_OFFSET);
                  int compressedLength = blobHeaderBuffer.getInt(COMPRESSED_LENGTH_OFFSET);
                  src = this.readSegments(firstSegmentIndex, compressedLength);
                  break label43;
               }

               blobHeaderBuffer = null;
            } finally {
               this.indexLocks[blobIndex].unlockRead(stamp);
            }

            return blobHeaderBuffer;
         }

         src.position(0);
         return Zstd.decompress(src, srcLength);
      } else {
         throw new IndexOutOfBoundsException("Index out of range: " + blobIndex + " blobCount: " + this.blobCount);
      }
   }

   public void readBlob(int blobIndex, @Nonnull ByteBuffer dest) throws IOException {
      if (blobIndex >= 0 && blobIndex < this.blobCount) {
         int indexPos = blobIndex * 4;
         long stamp = this.indexLocks[blobIndex].readLock();

         ByteBuffer src;
         int srcLength;
         try {
            int firstSegmentIndex = this.mappedBlobIndexes.getInt(indexPos);
            if (firstSegmentIndex == 0) {
               return;
            }

            ByteBuffer blobHeaderBuffer = this.readBlobHeader(firstSegmentIndex);
            srcLength = blobHeaderBuffer.getInt(SRC_LENGTH_OFFSET);
            int compressedLength = blobHeaderBuffer.getInt(COMPRESSED_LENGTH_OFFSET);
            if (srcLength > dest.remaining()) {
               throw new IllegalArgumentException("dest buffer is not large enough! required dest.remaining() >= " + srcLength);
            }

            src = this.readSegments(firstSegmentIndex, compressedLength);
         } finally {
            this.indexLocks[blobIndex].unlockRead(stamp);
         }

         src.position(0);
         if (dest.isDirect()) {
            Zstd.decompress(dest, src);
         } else {
            ByteBuffer tempDest = allocateDirect(srcLength);

            try {
               Zstd.decompress(tempDest, src);
               tempDest.position(0);
               dest.put(tempDest);
            } finally {
               if (UnsafeUtil.UNSAFE != null) {
                  UnsafeUtil.UNSAFE.invokeCleaner(tempDest);
               }
            }
         }
      } else {
         throw new IndexOutOfBoundsException("Index out of range: " + blobIndex + " blobCount: " + this.blobCount);
      }
   }

   @Nonnull
   protected ByteBuffer readBlobHeader(int firstSegmentIndex) throws IOException {
      if (firstSegmentIndex == 0) {
         throw new IllegalArgumentException("Invalid segment index!");
      } else {
         ByteBuffer blobHeaderBuffer = getTempBuffer(BLOB_HEADER_LENGTH);
         if (this.fileChannel.read(blobHeaderBuffer, this.segmentPosition(firstSegmentIndex)) != BLOB_HEADER_LENGTH) {
            throw new IllegalStateException();
         } else {
            return blobHeaderBuffer;
         }
      }
   }

   @Nonnull
   protected ByteBuffer readSegments(int firstSegmentIndex, int compressedLength) throws IOException {
      ByteBuffer buffer = allocateDirect(compressedLength);
      long segmentPosition = this.segmentPosition(firstSegmentIndex);
      if (this.fileChannel.read(buffer, segmentPosition + BLOB_HEADER_LENGTH) != compressedLength) {
         throw new IllegalStateException();
      } else if (buffer.remaining() != 0) {
         throw new IOException("Failed to read segments: " + firstSegmentIndex + ", " + compressedLength + ", " + buffer);
      } else {
         return buffer;
      }
   }

   public void writeBlob(int blobIndex, @Nonnull ByteBuffer src) throws IOException {
      if (blobIndex >= 0 && blobIndex < this.blobCount) {
         int srcLength = src.remaining();
         int maxCompressedLength = (int)Zstd.compressBound(srcLength);
         ByteBuffer dest = allocateDirect(BLOB_HEADER_LENGTH + maxCompressedLength);
         dest.putInt(SRC_LENGTH_OFFSET, srcLength);
         dest.position(BLOB_HEADER_LENGTH);
         int compressedLength;
         if (src.isDirect()) {
            compressedLength = Zstd.compress(dest, src, this.compressionLevel);
         } else {
            ByteBuffer tempSrc = allocateDirect(srcLength);

            try {
               tempSrc.put(src);
               tempSrc.position(0);
               compressedLength = Zstd.compress(dest, tempSrc, this.compressionLevel);
            } finally {
               if (UnsafeUtil.UNSAFE != null) {
                  UnsafeUtil.UNSAFE.invokeCleaner(tempSrc);
               }
            }
         }

         dest.putInt(COMPRESSED_LENGTH_OFFSET, compressedLength);
         dest.limit(dest.position());
         dest.position(0);
         int indexPos = blobIndex * 4;
         long stamp = this.indexLocks[blobIndex].writeLock();

         try {
            int oldSegmentLength = 0;
            int oldFirstSegmentIndex = this.mappedBlobIndexes.getInt(indexPos);
            if (oldFirstSegmentIndex != 0) {
               ByteBuffer blobHeaderBuffer = this.readBlobHeader(oldFirstSegmentIndex);
               int oldCompressedLength = blobHeaderBuffer.getInt(COMPRESSED_LENGTH_OFFSET);
               oldSegmentLength = this.requiredSegments(BLOB_HEADER_LENGTH + oldCompressedLength);
            }

            int firstSegmentIndex = this.writeSegments(dest);
            if (this.flushOnWrite) {
               this.fileChannel.force(false);
            }

            this.mappedBlobIndexes.putInt(indexPos, firstSegmentIndex);
            if (this.flushOnWrite) {
               this.mappedBlobIndexes.force(indexPos, 4);
            }

            if (oldSegmentLength > 0) {
               long usedSegmentsStamp = this.usedSegmentsLock.writeLock();

               try {
                  this.usedSegments.clear(oldFirstSegmentIndex, oldFirstSegmentIndex + oldSegmentLength);
               } finally {
                  this.usedSegmentsLock.unlockWrite(usedSegmentsStamp);
               }
            }
         } finally {
            this.indexLocks[blobIndex].unlockWrite(stamp);
         }
      } else {
         throw new IndexOutOfBoundsException("Index out of range: " + blobIndex + " blobCount: " + this.blobCount);
      }
   }

   public void removeBlob(int blobIndex) throws IOException {
      if (blobIndex >= 0 && blobIndex < this.blobCount) {
         int indexPos = blobIndex * 4;
         long stamp = this.indexLocks[blobIndex].writeLock();

         try {
            int oldFirstSegmentIndex = this.mappedBlobIndexes.getInt(indexPos);
            if (oldFirstSegmentIndex != 0) {
               ByteBuffer blobHeaderBuffer = this.readBlobHeader(oldFirstSegmentIndex);
               int oldCompressedLength = blobHeaderBuffer.getInt(COMPRESSED_LENGTH_OFFSET);
               int oldSegmentLength = this.requiredSegments(BLOB_HEADER_LENGTH + oldCompressedLength);
               this.mappedBlobIndexes.putInt(indexPos, 0);
               if (this.flushOnWrite) {
                  this.mappedBlobIndexes.force(indexPos, 4);
               }

               long usedSegmentsStamp = this.usedSegmentsLock.writeLock();

               try {
                  this.usedSegments.clear(oldFirstSegmentIndex, oldFirstSegmentIndex + oldSegmentLength);
               } finally {
                  this.usedSegmentsLock.unlockWrite(usedSegmentsStamp);
               }
            }
         } finally {
            this.indexLocks[blobIndex].unlockWrite(stamp);
         }
      } else {
         throw new IndexOutOfBoundsException("Index out of range: " + blobIndex + " blobCount: " + this.blobCount);
      }
   }

   protected int writeSegments(@Nonnull ByteBuffer data) throws IOException {
      int dataRemaining = data.remaining();
      int segmentsCount = this.requiredSegments(dataRemaining);
      IndexedStorageFile.SegmentRangeWriteLock segmentLock = this.findFreeSegment(segmentsCount);

      int var8;
      try {
         int firstSegmentIndex = segmentLock.segmentIndex;
         if (this.fileChannel.write(data, this.segmentPosition(firstSegmentIndex)) != dataRemaining) {
            throw new IllegalStateException();
         }

         long stamp = this.usedSegmentsLock.writeLock();

         try {
            this.usedSegments.set(firstSegmentIndex, firstSegmentIndex + segmentsCount);
         } finally {
            this.usedSegmentsLock.unlockWrite(stamp);
         }

         var8 = firstSegmentIndex;
      } finally {
         segmentLock.unlock();
      }

      return var8;
   }

   @Nonnull
   private IndexedStorageFile.SegmentRangeWriteLock findFreeSegment(int count) {
      long[] stamps = new long[count];
      int index = 1;

      label98:
      while (true) {
         long indexesStamp = this.usedSegmentsLock.readLock();

         try {
            int start = 0;
            int found = 0;

            while (found < count) {
               int nextUsedIndex = this.usedSegments.nextSetBit(index);
               if (nextUsedIndex < 0) {
                  start = index;
                  break;
               }

               if (index == nextUsedIndex) {
                  start = this.usedSegments.nextClearBit(index);
                  nextUsedIndex = this.usedSegments.nextSetBit(start + 1);
                  if (nextUsedIndex < 0) {
                     break;
                  }

                  found = nextUsedIndex - start;
                  index = nextUsedIndex + 1;
               } else {
                  start = index;
                  found = nextUsedIndex - index;
                  index = nextUsedIndex + 1;
               }
            }

            for (int i = count - 1; i >= 0; i--) {
               stamps[i] = this.getSegmentLock(start + i).tryWriteLock();
               if (stamps[i] == 0L) {
                  for (int j = count - 1; j > i; j--) {
                     this.getSegmentLock(start + j).unlockWrite(stamps[j]);
                  }

                  index = start + i + 1;
                  continue label98;
               }
            }

            return new IndexedStorageFile.SegmentRangeWriteLock(start, count, stamps);
         } finally {
            this.usedSegmentsLock.unlockRead(indexesStamp);
         }
      }
   }

   protected StampedLock getSegmentLock(int segmentIndex) {
      if (segmentIndex < this.segmentLocks.length) {
         return this.segmentLocks[segmentIndex];
      } else {
         long stamp = this.segmentLocksLock.writeLock();

         StampedLock newLength;
         try {
            if (segmentIndex >= this.segmentLocks.length) {
               int newLengthx = segmentIndex + 1;
               StampedLock[] newArray = Arrays.copyOf(this.segmentLocks, newLengthx);

               for (int i = this.segmentLocks.length; i < newLengthx; i++) {
                  newArray[i] = new StampedLock();
               }

               this.segmentLocks = newArray;
               return this.segmentLocks[segmentIndex];
            }

            newLength = this.segmentLocks[segmentIndex];
         } finally {
            this.segmentLocksLock.unlockWrite(stamp);
         }

         return newLength;
      }
   }

   protected long segmentsBase() {
      return HEADER_LENGTH + this.blobCount * 4L;
   }

   protected long segmentOffset(int segmentIndex) {
      if (segmentIndex == 0) {
         throw new IllegalArgumentException("Invalid segment index!");
      } else {
         return (long)(segmentIndex - 1) * this.segmentSize;
      }
   }

   protected long segmentPosition(int segmentIndex) {
      return this.segmentOffset(segmentIndex) + this.segmentsBase();
   }

   protected int positionToSegment(long position) {
      long segmentOffset = position - this.segmentsBase();
      if (segmentOffset < 0L) {
         throw new IllegalArgumentException("position is before the segments start");
      } else {
         return (int)(segmentOffset / this.segmentSize) + 1;
      }
   }

   protected int requiredSegments(long dataLength) {
      return (int)((dataLength + this.segmentSize - 1L) / this.segmentSize);
   }

   public FileLock lock() throws IOException {
      return this.fileChannel.lock();
   }

   public void force(boolean metaData) throws IOException {
      this.fileChannel.force(metaData);
      this.mappedBlobIndexes.force();
   }

   @Override
   public void close() throws IOException {
      this.fileChannel.close();
      if (UnsafeUtil.UNSAFE != null) {
         UnsafeUtil.UNSAFE.invokeCleaner(this.mappedBlobIndexes);
      }

      this.mappedBlobIndexes = null;
   }

   @Nonnull
   @Override
   public String toString() {
      return "IndexedStorageFile{fileChannel="
         + this.fileChannel
         + ", compressionLevel="
         + this.compressionLevel
         + ", blobCount="
         + this.blobCount
         + ", segmentSize="
         + this.segmentSize
         + ", mappedBlobIndexes="
         + this.mappedBlobIndexes
         + ", usedSegments="
         + this.usedSegments
         + "}";
   }

   static {
      MAGIC_BUFFER.position(0);
   }

   static class OffsetHelper {
      private int index;

      public int next(int len) {
         int cur = this.index;
         this.index += len;
         return cur;
      }

      public int length() {
         return this.index;
      }
   }

   protected class SegmentRangeWriteLock {
      private final int segmentIndex;
      private final int count;
      private final long[] stamps;

      public SegmentRangeWriteLock(int segmentIndex, int count, long[] stamps) {
         if (segmentIndex == 0) {
            throw new IllegalArgumentException("Invalid segment index!");
         } else if (count == 0) {
            throw new IllegalArgumentException("Invalid count!");
         } else {
            this.segmentIndex = segmentIndex;
            this.count = count;
            this.stamps = stamps;
         }
      }

      protected void unlock() {
         for (int i = 0; i < this.count; i++) {
            IndexedStorageFile.this.getSegmentLock(this.segmentIndex + i).unlockWrite(this.stamps[i]);
            this.stamps[i] = 0L;
         }
      }
   }
}
