package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorModsDirectories implements Packet, ToClientPacket {
   public static final int PACKET_ID = 356;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String[] directories;

   @Override
   public int getId() {
      return 356;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorModsDirectories() {
   }

   public AssetEditorModsDirectories(@Nullable String[] directories) {
      this.directories = directories;
   }

   public AssetEditorModsDirectories(@Nonnull AssetEditorModsDirectories other) {
      this.directories = other.directories;
   }

   @Nonnull
   public static AssetEditorModsDirectories deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("AssetEditorModsDirectories", 1, buf.readableBytes() - offset);
      } else {
         AssetEditorModsDirectories obj = new AssetEditorModsDirectories();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int directoriesCount = VarInt.peek(buf, pos);
            if (directoriesCount < 0) {
               throw ProtocolException.invalidVarInt("Directories");
            }

            int directoriesVarLen = VarInt.size(directoriesCount);
            if (directoriesCount > 4096000) {
               throw ProtocolException.arrayTooLong("Directories", directoriesCount, 4096000);
            }

            if (pos + directoriesVarLen + directoriesCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Directories", pos + directoriesVarLen + directoriesCount * 1, buf.readableBytes());
            }

            pos += directoriesVarLen;
            obj.directories = new String[directoriesCount];

            for (int i = 0; i < directoriesCount; i++) {
               int strLen = VarInt.peek(buf, pos);
               if (strLen < 0) {
                  throw ProtocolException.invalidVarInt("directories[" + i + "]");
               }

               int strVarLen = VarInt.size(strLen);
               if (strLen > 4096000) {
                  throw ProtocolException.stringTooLong("directories[" + i + "]", strLen, 4096000);
               }

               if (pos + strVarLen + strLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("directories[" + i + "]", pos + strVarLen + strLen, buf.readableBytes());
               }

               obj.directories[i] = PacketIO.readVarString(buf, pos);
               pos += strVarLen + strLen;
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos);
            pos += VarInt.size(sl) + sl;
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static String[] getDirectories(MemorySegment mem) {
      return getDirectories(mem, 0);
   }

   @Nullable
   public static String[] getDirectories(MemorySegment mem, int offset) {
      if (!hasDirectories(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Directories", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Directories", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Directories", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("Directories", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   public static boolean hasDirectories(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static AssetEditorModsDirectories toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorModsDirectories toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorModsDirectories", offset + 1, (int)mem.byteSize());
      } else {
         String[] directories = null;
         if (hasDirectories(mem, offset)) {
            int off = offset + 1;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Directories", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Directories", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Directories", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            directories = new String[len];

            for (int i = 0; i < len; i++) {
               long sp = VarInt.getWithLength(mem, off);
               int n = (int)sp + (int)(sp >>> 32);
               directories[i] = PacketIO.readVarString("Directories", mem, off, 16384000, PacketIO.UTF8);
               off += n;
            }
         }

         return new AssetEditorModsDirectories(directories);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.directories != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.directories != null) {
         if (this.directories.length > 4096000) {
            throw ProtocolException.arrayTooLong("Directories", this.directories.length, 4096000);
         }

         VarInt.write(buf, this.directories.length);

         for (String item : this.directories) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.directories != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.directories != null) {
         if (this.directories.length > 4096000) {
            throw ProtocolException.arrayTooLong("Directories", this.directories.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.directories.length);
         int directoriesValueOffset = 0;

         for (int i = 0; i < this.directories.length; i++) {
            directoriesValueOffset += PacketIO.writeVarString(mem, varOffset + directoriesValueOffset, this.directories[i], 16384000);
         }

         varOffset += directoriesValueOffset;
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.directories != null) {
         int directoriesSize = 0;

         for (String elem : this.directories) {
            directoriesSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.directories.length) + directoriesSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int directoriesCount = VarInt.peek(buffer, pos);
            if (directoriesCount < 0) {
               return ValidationResult.error("Invalid array count for Directories");
            }

            if (directoriesCount > 4096000) {
               return ValidationResult.error("Directories exceeds max length 4096000");
            }

            pos += VarInt.size(directoriesCount);

            for (int i = 0; i < directoriesCount; i++) {
               int strLen = VarInt.peek(buffer, pos);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in Directories");
               }

               pos += VarInt.size(strLen);
               pos += strLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in Directories");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorModsDirectories clone() {
      AssetEditorModsDirectories copy = new AssetEditorModsDirectories();
      copy.directories = this.directories != null ? Arrays.copyOf(this.directories, this.directories.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof AssetEditorModsDirectories other ? Arrays.equals((Object[])this.directories, (Object[])other.directories) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.directories);
   }
}
