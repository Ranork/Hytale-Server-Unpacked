package com.hypixel.hytale.protocol.io;

import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.ValueLayout.OfInt;
import java.lang.foreign.ValueLayout.OfShort;
import java.nio.ByteOrder;
import javax.annotation.Nonnull;

public final class VarInt {
   private static final OfShort BE_SHORT = ValueLayout.JAVA_SHORT_UNALIGNED.withOrder(ByteOrder.BIG_ENDIAN);
   private static final OfInt BE_INT = ValueLayout.JAVA_INT_UNALIGNED.withOrder(ByteOrder.BIG_ENDIAN);

   private VarInt() {
   }

   public static void write(@Nonnull ByteBuf buf, int value) {
      if (value < 0) {
         throw new IllegalArgumentException("VarInt cannot encode negative values: " + value);
      } else {
         if ((value & -128) == 0) {
            buf.writeByte(value);
         } else if ((value & -16384) == 0) {
            buf.writeShort((value & 127 | 128) << 8 | value >>> 7);
         } else if ((value & -2097152) == 0) {
            buf.writeMedium((value & 127 | 128) << 16 | (value >>> 7 & 127 | 128) << 8 | value >>> 14);
         } else if ((value & -268435456) == 0) {
            buf.writeInt((value & 127 | 128) << 24 | (value >>> 7 & 127 | 128) << 16 | (value >>> 14 & 127 | 128) << 8 | value >>> 21);
         } else {
            buf.writeInt((value & 127 | 128) << 24 | (value >>> 7 & 127 | 128) << 16 | (value >>> 14 & 127 | 128) << 8 | value >>> 21 & 127 | 128);
            buf.writeByte(value >>> 28);
         }
      }
   }

   public static int read(@Nonnull ByteBuf buf) {
      int b = buf.readByte();
      int value = b & 127;
      if ((b & 128) == 0) {
         return value;
      } else {
         int var3 = buf.readByte();
         value |= (var3 & 127) << 7;
         if ((var3 & 128) == 0) {
            return value;
         } else {
            var3 = buf.readByte();
            value |= (var3 & 127) << 14;
            if ((var3 & 128) == 0) {
               return value;
            } else {
               var3 = buf.readByte();
               value |= (var3 & 127) << 21;
               if ((var3 & 128) == 0) {
                  return value;
               } else {
                  var3 = buf.readByte();
                  value |= (var3 & 127) << 28;
                  if ((var3 & 128) == 0) {
                     return value;
                  } else {
                     throw new ProtocolException("VarInt exceeds maximum length (5 bytes)");
                  }
               }
            }
         }
      }
   }

   public static int peek(@Nonnull ByteBuf buf, int index) {
      int limit = buf.writerIndex();
      if (index >= limit) {
         return -1;
      } else {
         int b = buf.getByte(index);
         int value = b & 127;
         if ((b & 128) == 0) {
            return value;
         } else if (index + 1 >= limit) {
            return -1;
         } else {
            int var5 = buf.getByte(index + 1);
            value |= (var5 & 127) << 7;
            if ((var5 & 128) == 0) {
               return value;
            } else if (index + 2 >= limit) {
               return -1;
            } else {
               var5 = buf.getByte(index + 2);
               value |= (var5 & 127) << 14;
               if ((var5 & 128) == 0) {
                  return value;
               } else if (index + 3 >= limit) {
                  return -1;
               } else {
                  var5 = buf.getByte(index + 3);
                  value |= (var5 & 127) << 21;
                  if ((var5 & 128) == 0) {
                     return value;
                  } else if (index + 4 >= limit) {
                     return -1;
                  } else {
                     var5 = buf.getByte(index + 4);
                     value |= (var5 & 127) << 28;
                     return (var5 & 128) == 0 ? value : -1;
                  }
               }
            }
         }
      }
   }

   public static int length(@Nonnull ByteBuf buf, int index) {
      int limit = buf.writerIndex();
      if (index >= limit) {
         return -1;
      } else if ((buf.getByte(index) & 128) == 0) {
         return 1;
      } else if (index + 1 >= limit) {
         return -1;
      } else if ((buf.getByte(index + 1) & 128) == 0) {
         return 2;
      } else if (index + 2 >= limit) {
         return -1;
      } else if ((buf.getByte(index + 2) & 128) == 0) {
         return 3;
      } else if (index + 3 >= limit) {
         return -1;
      } else if ((buf.getByte(index + 3) & 128) == 0) {
         return 4;
      } else if (index + 4 >= limit) {
         return -1;
      } else {
         return (buf.getByte(index + 4) & 128) == 0 ? 5 : -1;
      }
   }

   public static int length(@Nonnull MemorySegment mem, int offset) {
      int limit = (int)mem.byteSize();
      if (offset >= limit) {
         return -1;
      } else if ((mem.get(PacketIO.PROTO_BYTE, (long)offset) & 128) == 0) {
         return 1;
      } else if (offset + 1 >= limit) {
         return -1;
      } else if ((mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)) & 128) == 0) {
         return 2;
      } else if (offset + 2 >= limit) {
         return -1;
      } else if ((mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2)) & 128) == 0) {
         return 3;
      } else if (offset + 3 >= limit) {
         return -1;
      } else if ((mem.get(PacketIO.PROTO_BYTE, (long)(offset + 3)) & 128) == 0) {
         return 4;
      } else if (offset + 4 >= limit) {
         return -1;
      } else {
         return (mem.get(PacketIO.PROTO_BYTE, (long)(offset + 4)) & 128) == 0 ? 5 : -1;
      }
   }

   public static int set(@Nonnull MemorySegment mem, int offset, int value) {
      if (value < 0) {
         throw new IllegalArgumentException("VarInt cannot encode negative values: " + value);
      } else if ((value & -128) == 0) {
         mem.set(PacketIO.PROTO_BYTE, (long)offset, (byte)value);
         return 1;
      } else if ((value & -16384) == 0) {
         mem.set(BE_SHORT, (long)offset, (short)((value & 127 | 128) << 8 | value >>> 7));
         return 2;
      } else if ((value & -2097152) == 0) {
         mem.set(BE_SHORT, (long)offset, (short)((value & 127 | 128) << 8 | value >>> 7 & 127 | 128));
         mem.set(PacketIO.PROTO_BYTE, (long)(offset + 2), (byte)(value >>> 14));
         return 3;
      } else if ((value & -268435456) == 0) {
         mem.set(BE_INT, (long)offset, (value & 127 | 128) << 24 | (value >>> 7 & 127 | 128) << 16 | (value >>> 14 & 127 | 128) << 8 | value >>> 21);
         return 4;
      } else {
         mem.set(BE_INT, (long)offset, (value & 127 | 128) << 24 | (value >>> 7 & 127 | 128) << 16 | (value >>> 14 & 127 | 128) << 8 | value >>> 21 & 127 | 128);
         mem.set(PacketIO.PROTO_BYTE, (long)(offset + 4), (byte)(value >>> 28));
         return 5;
      }
   }

   public static int get(@Nonnull MemorySegment mem, int offset) {
      int limit = (int)mem.byteSize();
      if (offset >= limit) {
         return -1;
      } else {
         int b = mem.get(PacketIO.PROTO_BYTE, (long)offset);
         int value = b & 127;
         if ((b & 128) == 0) {
            return value;
         } else if (offset + 1 >= limit) {
            return -1;
         } else {
            int var5 = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
            value |= (var5 & 127) << 7;
            if ((var5 & 128) == 0) {
               return value;
            } else if (offset + 2 >= limit) {
               return -1;
            } else {
               var5 = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
               value |= (var5 & 127) << 14;
               if ((var5 & 128) == 0) {
                  return value;
               } else if (offset + 3 >= limit) {
                  return -1;
               } else {
                  var5 = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 3));
                  value |= (var5 & 127) << 21;
                  if ((var5 & 128) == 0) {
                     return value;
                  } else if (offset + 4 >= limit) {
                     return -1;
                  } else {
                     var5 = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 4));
                     value |= (var5 & 127) << 28;
                     return (var5 & 128) == 0 ? value : -1;
                  }
               }
            }
         }
      }
   }

   public static long getWithLength(@Nonnull MemorySegment mem, int offset) {
      int limit = (int)mem.byteSize();
      if (offset >= limit) {
         return -1L;
      } else {
         int b = mem.get(PacketIO.PROTO_BYTE, (long)offset);
         int value = b & 127;
         if ((b & 128) == 0) {
            return 4294967296L | value;
         } else if (offset + 1 >= limit) {
            return -1L;
         } else {
            int var5 = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
            value |= (var5 & 127) << 7;
            if ((var5 & 128) == 0) {
               return 8589934592L | value;
            } else if (offset + 2 >= limit) {
               return -1L;
            } else {
               var5 = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
               value |= (var5 & 127) << 14;
               if ((var5 & 128) == 0) {
                  return 12884901888L | value;
               } else if (offset + 3 >= limit) {
                  return -1L;
               } else {
                  var5 = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 3));
                  value |= (var5 & 127) << 21;
                  if ((var5 & 128) == 0) {
                     return 17179869184L | value;
                  } else if (offset + 4 >= limit) {
                     return -1L;
                  } else {
                     var5 = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 4));
                     value |= (var5 & 127) << 28;
                     return (var5 & 128) == 0 ? 21474836480L | value : -1L;
                  }
               }
            }
         }
      }
   }

   public static int size(int value) {
      if ((value & -128) == 0) {
         return 1;
      } else if ((value & -16384) == 0) {
         return 2;
      } else if ((value & -2097152) == 0) {
         return 3;
      } else {
         return (value & -268435456) == 0 ? 4 : 5;
      }
   }
}
