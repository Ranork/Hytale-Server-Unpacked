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

public class AssetEditorSetupSchemas implements Packet, ToClientPacket {
   public static final int PACKET_ID = 305;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public SchemaFile[] schemas;

   @Override
   public int getId() {
      return 305;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorSetupSchemas() {
   }

   public AssetEditorSetupSchemas(@Nullable SchemaFile[] schemas) {
      this.schemas = schemas;
   }

   public AssetEditorSetupSchemas(@Nonnull AssetEditorSetupSchemas other) {
      this.schemas = other.schemas;
   }

   @Nonnull
   public static AssetEditorSetupSchemas deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("AssetEditorSetupSchemas", 1, buf.readableBytes() - offset);
      } else {
         AssetEditorSetupSchemas obj = new AssetEditorSetupSchemas();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int schemasCount = VarInt.peek(buf, pos);
            if (schemasCount < 0) {
               throw ProtocolException.invalidVarInt("Schemas");
            }

            int schemasVarLen = VarInt.size(schemasCount);
            if (schemasCount > 4096000) {
               throw ProtocolException.arrayTooLong("Schemas", schemasCount, 4096000);
            }

            if (pos + schemasVarLen + schemasCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Schemas", pos + schemasVarLen + schemasCount * 1, buf.readableBytes());
            }

            pos += schemasVarLen;
            obj.schemas = new SchemaFile[schemasCount];

            for (int i = 0; i < schemasCount; i++) {
               obj.schemas[i] = SchemaFile.deserialize(buf, pos);
               pos += SchemaFile.computeBytesConsumed(buf, pos);
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
            pos += SchemaFile.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static SchemaFile[] getSchemas(MemorySegment mem) {
      return getSchemas(mem, 0);
   }

   @Nullable
   public static SchemaFile[] getSchemas(MemorySegment mem, int offset) {
      if (!hasSchemas(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Schemas", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Schemas", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Schemas", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               SchemaFile[] data = new SchemaFile[len];

               for (int i = 0; i < len; i++) {
                  data[i] = SchemaFile.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasSchemas(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static AssetEditorSetupSchemas toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorSetupSchemas toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorSetupSchemas", offset + 1, (int)mem.byteSize());
      } else {
         SchemaFile[] schemas = null;
         if (hasSchemas(mem, offset)) {
            int off = offset + 1;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Schemas", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Schemas", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Schemas", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            schemas = new SchemaFile[len];

            for (int i = 0; i < len; i++) {
               schemas[i] = SchemaFile.toObject(mem, off);
               off += schemas[i].computeSize();
            }
         }

         return new AssetEditorSetupSchemas(schemas);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.schemas != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.schemas != null) {
         if (this.schemas.length > 4096000) {
            throw ProtocolException.arrayTooLong("Schemas", this.schemas.length, 4096000);
         }

         VarInt.write(buf, this.schemas.length);

         for (SchemaFile item : this.schemas) {
            item.serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.schemas != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.schemas != null) {
         if (this.schemas.length > 4096000) {
            throw ProtocolException.arrayTooLong("Schemas", this.schemas.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.schemas.length);
         int schemasValueOffset = 0;

         for (int i = 0; i < this.schemas.length; i++) {
            schemasValueOffset += this.schemas[i].serialize(mem, varOffset + schemasValueOffset);
         }

         varOffset += schemasValueOffset;
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.schemas != null) {
         int schemasSize = 0;

         for (SchemaFile elem : this.schemas) {
            schemasSize += elem.computeSize();
         }

         size += VarInt.size(this.schemas.length) + schemasSize;
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
            int schemasCount = VarInt.peek(buffer, pos);
            if (schemasCount < 0) {
               return ValidationResult.error("Invalid array count for Schemas");
            }

            if (schemasCount > 4096000) {
               return ValidationResult.error("Schemas exceeds max length 4096000");
            }

            pos += VarInt.size(schemasCount);

            for (int i = 0; i < schemasCount; i++) {
               ValidationResult structResult = SchemaFile.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid SchemaFile in Schemas[" + i + "]: " + structResult.error());
               }

               pos += SchemaFile.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorSetupSchemas clone() {
      AssetEditorSetupSchemas copy = new AssetEditorSetupSchemas();
      copy.schemas = this.schemas != null ? Arrays.stream(this.schemas).map(e -> e.clone()).toArray(SchemaFile[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof AssetEditorSetupSchemas other ? Arrays.equals((Object[])this.schemas, (Object[])other.schemas) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.schemas);
   }
}
