package com.hypixel.hytale.logger.util;

import com.hypixel.hytale.logger.HytaleLogger;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.Level;

public class LoggerPrintStream extends PrintStream {
   private final HytaleLogger logger;
   private final Level level;
   private final ByteArrayOutputStream bufferedOutput;
   private int last;

   public LoggerPrintStream(HytaleLogger logger, Level level) {
      super(new ByteArrayOutputStream());
      this.logger = logger;
      this.level = level;
      this.bufferedOutput = (ByteArrayOutputStream)super.out;
      this.last = -1;
   }

   @Override
   public void write(int b) {
      if (this.last == 13 && b == 10) {
         this.last = -1;
      } else {
         if (b != 10 && b != 13) {
            super.write(b);
         } else {
            try {
               this.logger.at(this.level).log(this.bufferedOutput.toString());
            } finally {
               this.bufferedOutput.reset();
            }
         }

         this.last = b;
      }
   }

   @Override
   public void write(byte[] buf, int off, int len) {
      if (len < 0) {
         throw new ArrayIndexOutOfBoundsException(len);
      } else {
         for (int i = 0; i < len; i++) {
            this.write(buf[off + i]);
         }
      }
   }

   public HytaleLogger getLogger() {
      return this.logger;
   }

   public Level getLevel() {
      return this.level;
   }
}
