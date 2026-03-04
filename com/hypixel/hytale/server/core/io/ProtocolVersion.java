package com.hypixel.hytale.server.core.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ProtocolVersion {
   private final int crc;

   public ProtocolVersion(int crc) {
      this.crc = crc;
   }

   public int getCrc() {
      return this.crc;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ProtocolVersion that = (ProtocolVersion)o;
         return this.crc == that.crc;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return 31 * this.crc;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ProtocolVersion{crc=" + this.crc + "}";
   }
}
