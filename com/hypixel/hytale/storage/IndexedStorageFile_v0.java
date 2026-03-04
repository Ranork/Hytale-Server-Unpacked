package com.hypixel.hytale.storage;

import com.github.luben.zstd.Zstd;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.logger.HytaleLogger;
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
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Deprecated
public class IndexedStorageFile_v0 implements Closeable {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   public static final StampedLock[] EMPTY_STAMPED_LOCKS = new StampedLock[0];
   public static final MetricsRegistry<IndexedStorageFile_v0> METRICS_REGISTRY = new MetricsRegistry<IndexedStorageFile_v0>()
      .register("Size", file -> {
         try {
            return file.size();
         } catch (IOException var2) {
            return -1L;
         }
      }, Codec.LONG)
      .register("CompressionLevel", file -> file.getCompressionLevel(), Codec.INTEGER)
      .register("ContiguousBlobs", file -> file.isContiguousBlobs(), Codec.BOOLEAN)
      .register("BlobCount", file -> file.getBlobCount(), Codec.INTEGER)
      .register("UsedBlobCount", file -> file.keys().size(), Codec.INTEGER)
      .register("SegmentSize", file -> file.segmentSize(), Codec.INTEGER)
      .register("SegmentCount", file -> file.segmentCount(), Codec.INTEGER);
   public static final String MAGIC_STRING = "HytaleIndexedStorage";
   public static final int VERSION = 0;
   public static final int DEFAULT_BLOB_COUNT = 1024;
   public static final int DEFAULT_SEGMENT_SIZE = 4096;
   public static final int DEFAULT_COMPRESSION_LEVEL = 3;
   public static final boolean DEFAULT_CONTIGUOUS_BLOBS = true;
   static final IndexedStorageFile_v0.OffsetHelper HOH = new IndexedStorageFile_v0.OffsetHelper();
   public static final int MAGIC_LENGTH = 20;
   public static final int MAGIC_OFFSET = HOH.next(20);
   public static final int VERSION_OFFSET = HOH.next(4);
   public static final int BLOB_COUNT_OFFSET = HOH.next(4);
   public static final int SEGMENT_SIZE_OFFSET = HOH.next(4);
   public static final int HEADER_LENGTH = HOH.length();
   static final IndexedStorageFile_v0.OffsetHelper SOH = new IndexedStorageFile_v0.OffsetHelper();
   public static final int NEXT_SEGMENT_OFFSET = SOH.next(4);
   public static final int SEGMENT_HEADER_LENGTH = SOH.length();
   static final IndexedStorageFile_v0.OffsetHelper BOH = new IndexedStorageFile_v0.OffsetHelper();
   public static final int SRC_LENGTH_OFFSET = BOH.next(4);
   public static final int COMPRESSED_LENGTH_OFFSET = BOH.next(4);
   public static final int BLOB_HEADER_LENGTH = BOH.length();
   public static final int INDEX_SIZE = 4;
   public static final int UNASSIGNED_INDEX = 0;
   public static final int END_BLOB_INDEX = Integer.MIN_VALUE;
   public static final int FIRST_SEGMENT_INDEX = 1;
   public static final FileAttribute<?>[] NO_ATTRIBUTES = new FileAttribute[0];
   static final byte[] MAGIC_BYTES = "HytaleIndexedStorage".getBytes(StandardCharsets.UTF_8);
   private static final ByteBuffer MAGIC_BUFFER = ByteBuffer.wrap(MAGIC_BYTES);
   private static final ThreadLocal<ByteBuffer> CACHED_TEMP_BUFFER = ThreadLocal.withInitial(() -> ByteBuffer.allocateDirect(HEADER_LENGTH));
   @Nonnull
   private final Path path;
   private final FileChannel fileChannel;
   private int compressionLevel = 3;
   private boolean contiguousBlobs = true;
   private int blobCount;
   private int segmentSize;
   private StampedLock[] indexLocks;
   @Nullable
   private MappedByteBuffer mappedBlobIndexes;
   private final StampedLock segmentLocksLock = new StampedLock();
   private StampedLock[] segmentLocks = EMPTY_STAMPED_LOCKS;
   private final StampedLock nextSegmentIndexesLock = new StampedLock();
   @Nonnull
   private int[] nextSegmentIndexes = new int[0];

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
   public static IndexedStorageFile_v0 open(@Nonnull Path path, OpenOption... options) throws IOException {
      return open(path, 1024, 4096, Set.of(options), NO_ATTRIBUTES);
   }

   @Nonnull
   public static IndexedStorageFile_v0 open(@Nonnull Path path, @Nonnull Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
      return open(path, 1024, 4096, options, attrs);
   }

   @Nonnull
   public static IndexedStorageFile_v0 open(@Nonnull Path path, int blobCount, int segmentSize, OpenOption... options) throws IOException {
      return open(path, blobCount, segmentSize, Set.of(options), NO_ATTRIBUTES);
   }

   @Nonnull
   public static IndexedStorageFile_v0 open(
      @Nonnull Path path, int blobCount, int segmentSize, @Nonnull Set<? extends OpenOption> options, FileAttribute<?>... attrs
   ) throws IOException {
      IndexedStorageFile_v0 storageFile = new IndexedStorageFile_v0(path, options, attrs);
      if (options.contains(StandardOpenOption.CREATE_NEW)) {
         storageFile.create(blobCount, segmentSize);
         return storageFile;
      } else {
         if (options.contains(StandardOpenOption.CREATE) && storageFile.fileChannel.size() == 0L) {
            storageFile.create(blobCount, segmentSize);
         } else {
            storageFile.open();
         }

         return storageFile;
      }
   }

   private IndexedStorageFile_v0(@Nonnull Path path, Set<? extends OpenOption> options, FileAttribute<?>[] attrs) throws IOException {
      this.path = path;
      this.fileChannel = FileChannel.open(path, options, attrs);
   }

   IndexedStorageFile_v0(@Nonnull Path path, FileChannel fileChannel) throws IOException {
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

   public void setCompressionLevel(int compressionLevel) {
      this.compressionLevel = compressionLevel;
   }

   public boolean isContiguousBlobs() {
      return this.contiguousBlobs;
   }

   public void setContiguousBlobs(boolean contiguousBlobs) {
      this.contiguousBlobs = contiguousBlobs;
   }

   @Nonnull
   protected IndexedStorageFile_v0 create(int blobCount, int segmentSize) throws IOException {
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
      header.putInt(VERSION_OFFSET, 0);
      header.putInt(BLOB_COUNT_OFFSET, blobCount);
      header.putInt(SEGMENT_SIZE_OFFSET, segmentSize);
      header.position(0);
      if (this.fileChannel.write(header, 0L) != HEADER_LENGTH) {
         throw new IllegalStateException();
      }
   }

   @Nonnull
   protected IndexedStorageFile_v0 open() throws IOException {
      if (this.fileChannel.size() == 0L) {
         throw new IOException("file channel is empty");
      } else {
         this.readHeader();
         this.memoryMapBlobIndexes();
         this.readNextIndexes();
         this.processTempIndexes();
         return this;
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
            int version = header.getInt(VERSION_OFFSET);
            if (version >= 0 && version <= 0) {
               this.blobCount = header.getInt(BLOB_COUNT_OFFSET);
               this.segmentSize = header.getInt(SEGMENT_SIZE_OFFSET);
            } else {
               throw new IOException("Invalid version! " + version);
            }
         }
      }
   }

   protected void memoryMapBlobIndexes() throws IOException {
      this.indexLocks = new StampedLock[this.blobCount];

      for (int i = 0; i < this.blobCount; i++) {
         this.indexLocks[i] = new StampedLock();
      }

      int indexesSize = this.indexesLength() * 2;
      this.mappedBlobIndexes = this.fileChannel.map(MapMode.READ_WRITE, HEADER_LENGTH, indexesSize);
   }

   protected void readNextIndexes() throws IOException {
      ByteBuffer tempIndexBuffer = getTempBuffer(4);
      long stamp = this.nextSegmentIndexesLock.writeLock();

      try {
         this.nextSegmentIndexes = new int[this.requiredSegments(this.fileChannel.size() - this.segmentsBase()) + 1];

         for (int segmentIndex = 1; segmentIndex < this.nextSegmentIndexes.length; segmentIndex++) {
            tempIndexBuffer.position(0);
            if (this.fileChannel.read(tempIndexBuffer, this.segmentPosition(segmentIndex)) != 4) {
               tempIndexBuffer.position(0);
               tempIndexBuffer.putInt(0, 0);
               this.fileChannel.write(tempIndexBuffer, this.segmentPosition(segmentIndex));
               break;
            }

            this.nextSegmentIndexes[segmentIndex] = tempIndexBuffer.getInt(NEXT_SEGMENT_OFFSET);
         }
      } finally {
         this.nextSegmentIndexesLock.unlockWrite(stamp);
      }
   }

   protected void processTempIndexes() throws IOException {
      int blobsCleared = 0;
      int segmentsCleared = 0;
      ByteBuffer tempIndexBuffer = getTempBuffer(4);
      int indexesLength = this.indexesLength();

      for (int blobIndex = 0; blobIndex < this.blobCount; blobIndex++) {
         int tempIndexPos = indexesLength + blobIndex * 4;
         int firstSegmentIndex = this.mappedBlobIndexes.getInt(tempIndexPos);
         if (firstSegmentIndex != 0) {
            blobsCleared++;
            segmentsCleared += this.clearSegments(firstSegmentIndex, tempIndexBuffer);
            this.mappedBlobIndexes.putInt(tempIndexPos, 0);
         }
      }

      if (blobsCleared != 0 || segmentsCleared != 0) {
         LOGGER.at(Level.WARNING).log("Detected failed write for %s! Cleaned %s blobs with %s segments!", this.fileChannel, blobsCleared, segmentsCleared);
      }
   }

   protected int clearSegments(int firstSegmentIndex, @Nonnull ByteBuffer tempIndexBuffer) throws IOException {
      int[] segments = new int[8];
      int count = 0;
      int nextSegmentIndex = firstSegmentIndex;

      while (nextSegmentIndex != 0 && nextSegmentIndex != Integer.MIN_VALUE) {
         if (count >= segments.length) {
            segments = Arrays.copyOf(segments, count * 2);
         }

         segments[count] = nextSegmentIndex;
         count++;
         long indexesStamp = this.nextSegmentIndexesLock.tryOptimisticRead();
         int segmentIndex = this.nextSegmentIndexes[nextSegmentIndex];
         if (!this.nextSegmentIndexesLock.validate(indexesStamp)) {
            indexesStamp = this.nextSegmentIndexesLock.readLock();

            try {
               segmentIndex = this.nextSegmentIndexes[nextSegmentIndex];
            } finally {
               this.nextSegmentIndexesLock.unlockRead(indexesStamp);
            }
         }

         nextSegmentIndex = segmentIndex;
      }

      tempIndexBuffer.putInt(0, 0);

      for (int i = count - 1; i >= 0; i--) {
         tempIndexBuffer.position(0);
         int segmentIndex = segments[i];
         StampedLock segmentLock = this.getSegmentLock(segmentIndex);
         long segmentStamp = segmentLock.writeLock();

         try {
            if (this.fileChannel.write(tempIndexBuffer, this.segmentPosition(segmentIndex)) != 4) {
               throw new IllegalStateException();
            }

            long indexesStamp = this.nextSegmentIndexesLock.writeLock();

            try {
               this.nextSegmentIndexes[segmentIndex] = 0;
            } finally {
               this.nextSegmentIndexesLock.unlockWrite(indexesStamp);
            }
         } finally {
            segmentLock.unlockWrite(segmentStamp);
         }
      }

      return count;
   }

   public long size() throws IOException {
      return this.fileChannel.size();
   }

   public int segmentSize() {
      long stamp = this.nextSegmentIndexesLock.tryOptimisticRead();
      int value = this.nextSegmentIndexes.length;
      if (this.nextSegmentIndexesLock.validate(stamp)) {
         return value;
      } else {
         stamp = this.nextSegmentIndexesLock.readLock();

         int var4;
         try {
            var4 = this.nextSegmentIndexes.length;
         } finally {
            this.nextSegmentIndexesLock.unlockRead(stamp);
         }

         return var4;
      }
   }

   public int segmentCount() {
      long stamp = this.nextSegmentIndexesLock.tryOptimisticRead();
      int count = 0;
      int[] temp = this.nextSegmentIndexes;

      for (int i = 0; i < temp.length; i++) {
         if (temp[i] != 0) {
            count++;
         }
      }

      if (this.nextSegmentIndexesLock.validate(stamp)) {
         return count;
      } else {
         stamp = this.nextSegmentIndexesLock.readLock();

         int var13;
         try {
            count = 0;
            temp = this.nextSegmentIndexes;

            for (int ix = 0; ix < temp.length; ix++) {
               if (temp[ix] != 0) {
                  count++;
               }
            }

            var13 = count;
         } finally {
            this.nextSegmentIndexesLock.unlockRead(stamp);
         }

         return var13;
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
                  src = this.readSegments(firstSegmentIndex, compressedLength, blobHeaderBuffer);
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

            src = this.readSegments(firstSegmentIndex, compressedLength, blobHeaderBuffer);
         } finally {
            this.indexLocks[blobIndex].unlockRead(stamp);
         }

         src.position(0);
         if (dest.isDirect()) {
            Zstd.decompress(dest, src);
         } else {
            ByteBuffer tempDest = allocateDirect(srcLength);
            Zstd.decompress(tempDest, src);
            tempDest.position(0);
            dest.put(tempDest);
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
         if (this.fileChannel.read(blobHeaderBuffer, this.blobHeaderPosition(firstSegmentIndex)) != BLOB_HEADER_LENGTH) {
            throw new IllegalStateException();
         } else {
            return blobHeaderBuffer;
         }
      }
   }

   @Nonnull
   protected ByteBuffer readSegments(int firstSegmentIndex, int compressedLength, @Nonnull ByteBuffer tempHeaderBuffer) throws IOException {
      tempHeaderBuffer.limit(SEGMENT_HEADER_LENGTH);
      ByteBuffer buffer = allocateDirect(compressedLength);
      int remainingBytes = compressedLength;
      int nextSegmentIndex = firstSegmentIndex;

      while (nextSegmentIndex != 0 && nextSegmentIndex != Integer.MIN_VALUE) {
         long segmentPosition = this.segmentPosition(nextSegmentIndex);
         long indexesStamp = this.nextSegmentIndexesLock.tryOptimisticRead();
         int segmentIndex = this.nextSegmentIndexes[nextSegmentIndex];
         if (!this.nextSegmentIndexesLock.validate(indexesStamp)) {
            indexesStamp = this.nextSegmentIndexesLock.readLock();

            try {
               segmentIndex = this.nextSegmentIndexes[nextSegmentIndex];
            } finally {
               this.nextSegmentIndexesLock.unlockRead(indexesStamp);
            }
         }

         nextSegmentIndex = segmentIndex;
         int dataOffset = SEGMENT_HEADER_LENGTH;
         if (buffer.position() == 0) {
            dataOffset += BLOB_HEADER_LENGTH;
         }

         int dataToRead = Math.min(this.segmentSize - dataOffset, remainingBytes);
         buffer.limit(buffer.position() + dataToRead);
         if (this.fileChannel.read(buffer, segmentPosition + dataOffset) != dataToRead) {
            throw new IllegalStateException();
         }

         remainingBytes -= dataToRead;
         if (remainingBytes == 0) {
            break;
         }
      }

      if (buffer.remaining() == 0 && nextSegmentIndex == Integer.MIN_VALUE) {
         return buffer;
      } else {
         throw new IOException("Failed to read segments: " + firstSegmentIndex + ", " + compressedLength + ", " + buffer + ", " + nextSegmentIndex);
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
            tempSrc.put(src);
            tempSrc.position(0);
            compressedLength = Zstd.compress(dest, tempSrc, this.compressionLevel);
         }

         dest.putInt(COMPRESSED_LENGTH_OFFSET, compressedLength);
         dest.limit(dest.position());
         dest.position(0);
         int indexPos = blobIndex * 4;
         int tempIndexPos = this.indexesLength() + indexPos;
         long stamp = this.indexLocks[blobIndex].writeLock();

         try {
            int firstSegmentIndex = this.writeSegments(blobIndex, dest);
            int oldFirstSegmentIndex = this.mappedBlobIndexes.getInt(indexPos);
            this.mappedBlobIndexes.putInt(indexPos, firstSegmentIndex);
            this.mappedBlobIndexes.putInt(tempIndexPos, oldFirstSegmentIndex);
            if (oldFirstSegmentIndex != 0) {
               ByteBuffer tempIndexBuffer = getTempBuffer(4);
               this.clearSegments(oldFirstSegmentIndex, tempIndexBuffer);
               this.mappedBlobIndexes.putInt(tempIndexPos, 0);
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
         int tempIndexPos = this.indexesLength() + indexPos;
         long stamp = this.indexLocks[blobIndex].writeLock();

         try {
            int oldFirstSegmentIndex = this.mappedBlobIndexes.getInt(indexPos);
            if (oldFirstSegmentIndex != 0) {
               this.mappedBlobIndexes.putInt(indexPos, 0);
               this.mappedBlobIndexes.putInt(tempIndexPos, oldFirstSegmentIndex);
               ByteBuffer tempIndexBuffer = getTempBuffer(4);
               this.clearSegments(oldFirstSegmentIndex, tempIndexBuffer);
               this.mappedBlobIndexes.putInt(tempIndexPos, 0);
            }
         } finally {
            this.indexLocks[blobIndex].unlockWrite(stamp);
         }
      } else {
         throw new IndexOutOfBoundsException("Index out of range: " + blobIndex + " blobCount: " + this.blobCount);
      }
   }

   protected int writeSegments(int blobIndex, @Nonnull ByteBuffer data) throws IOException {
      int dataRemaining = data.remaining();
      int indexPos = blobIndex * 4;
      int tempIndexPos = this.indexesLength() + indexPos;
      ByteBuffer tempIndexBuffer = getTempBuffer(4);
      if (this.contiguousBlobs) {
         int segmentsCount = this.requiredSegments(dataRemaining);
         IndexedStorageFile_v0.SegmentRangeLock segmentLock = this.findFreeSegment(segmentsCount);

         int var34;
         try {
            int firstSegmentIndex = segmentLock.segmentIndex;
            this.mappedBlobIndexes.putInt(tempIndexPos, firstSegmentIndex);
            int endSegmentIndex = firstSegmentIndex + segmentsCount;
            long indexesResizeStamp = this.nextSegmentIndexesLock.writeLock();

            try {
               if (endSegmentIndex >= this.nextSegmentIndexes.length) {
                  this.nextSegmentIndexes = Arrays.copyOf(this.nextSegmentIndexes, endSegmentIndex);
               }
            } finally {
               this.nextSegmentIndexesLock.unlockWrite(indexesResizeStamp);
            }

            for (int segmentIndex = firstSegmentIndex; segmentIndex < endSegmentIndex; segmentIndex++) {
               long segmentPosition = this.segmentPosition(segmentIndex);
               int next = segmentIndex + 1;
               int nextSegmentIndex = next < endSegmentIndex ? next : Integer.MIN_VALUE;
               tempIndexBuffer.position(0);
               tempIndexBuffer.putInt(0, nextSegmentIndex);
               if (this.fileChannel.write(tempIndexBuffer, segmentPosition) != SEGMENT_HEADER_LENGTH) {
                  throw new IllegalStateException();
               }

               long indexesStamp = this.nextSegmentIndexesLock.writeLock();

               try {
                  this.nextSegmentIndexes[segmentIndex] = nextSegmentIndex;
               } finally {
                  this.nextSegmentIndexesLock.unlockWrite(indexesStamp);
               }

               int dataToWrite = Math.min(this.segmentSize - SEGMENT_HEADER_LENGTH, dataRemaining);
               data.limit(data.position() + dataToWrite);
               if (this.fileChannel.write(data, segmentPosition + SEGMENT_HEADER_LENGTH) != dataToWrite) {
                  throw new IllegalStateException();
               }

               dataRemaining -= dataToWrite;
            }

            var34 = firstSegmentIndex;
         } finally {
            segmentLock.unlockWrite();
         }

         return var34;
      } else {
         throw new UnsupportedOperationException("Not implemented!");
      }
   }

   @Nonnull
   private IndexedStorageFile_v0.SegmentRangeLock findFreeSegment(int count) {
      int start = 0;
      int index = 1;
      int found = 0;

      while (true) {
         label75:
         while (found >= count) {
            IndexedStorageFile_v0.SegmentRangeLock segmentRangeLock = this.tryWriteLockSegmentRange(start, count);
            if (segmentRangeLock == null) {
               start++;
               found--;
            } else {
               for (int i = count - 1; i >= 0; i--) {
                  int segmentIndex = start + i;
                  int next = this.getNextIndex(segmentIndex);
                  if (next != 0) {
                     segmentRangeLock.unlockWrite();
                     index = segmentIndex + 1;
                     start = 0;
                     found = 0;
                     continue label75;
                  }
               }

               return segmentRangeLock;
            }
         }

         StampedLock segmentLock = this.getSegmentLock(index);
         long stamp = segmentLock.tryReadLock();
         if (stamp == 0L) {
            start = 0;
            found = 0;
            index++;
         } else {
            int next;
            try {
               next = this.getNextIndex(index);
            } finally {
               segmentLock.unlockRead(stamp);
            }

            if (next == 0) {
               if (start == 0) {
                  start = index;
               }

               found++;
            } else {
               start = 0;
               found = 0;
            }

            index++;
         }
      }
   }

   protected int getNextIndex(int segmentIndex) {
      long indexesStamp = this.nextSegmentIndexesLock.tryOptimisticRead();
      int nextSegmentIndex = segmentIndex < this.nextSegmentIndexes.length ? this.nextSegmentIndexes[segmentIndex] : 0;
      if (this.nextSegmentIndexesLock.validate(indexesStamp)) {
         return nextSegmentIndex;
      } else {
         indexesStamp = this.nextSegmentIndexesLock.readLock();

         int var5;
         try {
            var5 = segmentIndex < this.nextSegmentIndexes.length ? this.nextSegmentIndexes[segmentIndex] : 0;
         } finally {
            this.nextSegmentIndexesLock.unlockRead(indexesStamp);
         }

         return var5;
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

   @Nullable
   protected IndexedStorageFile_v0.SegmentRangeLock tryWriteLockSegmentRange(int start, int count) {
      long[] stamps = new long[count];

      for (int i = 0; i < count; i++) {
         StampedLock segmentLock = this.getSegmentLock(start + i);
         if (segmentLock.isWriteLocked()) {
            for (int i1 = 0; i1 < i; i1++) {
               this.getSegmentLock(start + i1).unlockWrite(stamps[i1]);
            }

            return null;
         }

         stamps[i] = segmentLock.writeLock();
      }

      return new IndexedStorageFile_v0.SegmentRangeLock(start, count, stamps);
   }

   protected int indexesLength() {
      return this.blobCount * 4;
   }

   protected long segmentsBase() {
      return HEADER_LENGTH + this.indexesLength() * 2L;
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

   protected long blobHeaderPosition(int segmentIndex) {
      return this.segmentPosition(segmentIndex) + SEGMENT_HEADER_LENGTH;
   }

   protected int requiredSegments(long dataLength) {
      int size = this.segmentSize - SEGMENT_HEADER_LENGTH;
      return (int)((dataLength + size - 1L) / size);
   }

   public FileLock lock() throws IOException {
      return this.fileChannel.lock();
   }

   public void force(boolean metaData) throws IOException {
      this.mappedBlobIndexes.force();
      this.fileChannel.force(metaData);
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
         + ", contiguousBlobs="
         + this.contiguousBlobs
         + ", blobCount="
         + this.blobCount
         + ", segmentSize="
         + this.segmentSize
         + ", mappedBlobIndexes="
         + this.mappedBlobIndexes
         + ", nextSegmentIndexes="
         + Arrays.toString(this.nextSegmentIndexes)
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

   protected class SegmentRangeLock {
      private final int segmentIndex;
      private final int count;
      private final long[] stamps;

      public SegmentRangeLock(int segmentIndex, int count, long[] stamps) {
         this.segmentIndex = segmentIndex;
         this.count = count;
         this.stamps = stamps;
      }

      protected void unlockRead() {
         for (int i = 0; i < this.count; i++) {
            IndexedStorageFile_v0.this.getSegmentLock(this.segmentIndex + i).unlockRead(this.stamps[i]);
            this.stamps[i] = 0L;
         }
      }

      protected void unlockWrite() {
         for (int i = 0; i < this.count; i++) {
            IndexedStorageFile_v0.this.getSegmentLock(this.segmentIndex + i).unlockWrite(this.stamps[i]);
            this.stamps[i] = 0L;
         }
      }
   }
}
