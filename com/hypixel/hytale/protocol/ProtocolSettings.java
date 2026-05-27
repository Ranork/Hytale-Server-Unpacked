package com.hypixel.hytale.protocol;

public final class ProtocolSettings {
   public static final int PROTOCOL_CRC = 1316766548;
   public static final int PROTOCOL_VERSION = 2;
   public static final int PROTOCOL_BUILD_NUMBER = 100;
   public static final int PACKET_COUNT = 326;
   public static final int STRUCT_COUNT = 386;
   public static final int ENUM_COUNT = 155;
   public static final int MAX_PACKET_SIZE = 1677721600;

   private ProtocolSettings() {
   }

   public static boolean validateCrc(int crc) {
      return 1316766548 == crc;
   }
}
