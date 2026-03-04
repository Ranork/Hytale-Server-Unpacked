package com.hypixel.hytale.codec.util;

import ch.randelshofer.fastdoubleparser.JavaDoubleParser;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.unsafe.UnsafeUtil;
import io.sentry.Sentry;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonNull;
import org.bson.BsonString;
import org.bson.BsonValue;
import sun.misc.Unsafe;

public class RawJsonReader implements AutoCloseable {
   public static final ThreadLocal<char[]> READ_BUFFER = ThreadLocal.withInitial(() -> new char[131072]);
   public static final int DEFAULT_CHAR_BUFFER_SIZE = 32768;
   public static final int MIN_CHAR_BUFFER_READ = 16384;
   public static final int BUFFER_GROWTH = 1048576;
   private static final int UNMARKED = -1;
   private int streamIndex;
   @Nullable
   private Reader in;
   @Nullable
   private char[] buffer;
   private int bufferIndex;
   private int bufferSize;
   private int markIndex = -1;
   private int markLine = -1;
   private int markLineStart = -1;
   private StringBuilder tempSb;
   private int line;
   private int lineStart;
   public static final int ERROR_LINES_BUFFER = 10;

   public RawJsonReader(@Nonnull char[] preFilledBuffer) {
      if (preFilledBuffer == null) {
         throw new IllegalArgumentException("buffer can't be null!");
      } else {
         this.in = null;
         this.buffer = preFilledBuffer;
         this.bufferIndex = 0;
         this.streamIndex = 0;
         this.bufferSize = preFilledBuffer.length;
      }
   }

   public RawJsonReader(Reader in, @Nonnull char[] buffer) {
      if (buffer == null) {
         throw new IllegalArgumentException("buffer can't be null!");
      } else {
         this.in = in;
         this.buffer = buffer;
         this.bufferIndex = 0;
         this.streamIndex = 0;
         this.bufferSize = 0;
      }
   }

   public char[] getBuffer() {
      return this.buffer;
   }

   public int getBufferIndex() {
      return this.bufferIndex;
   }

   public int getBufferSize() {
      return this.bufferSize;
   }

   public int getLine() {
      return this.line + 1;
   }

   public int getColumn() {
      return this.bufferIndex - this.lineStart + 1;
   }

   private boolean ensure() throws IOException {
      return this.ensure(1);
   }

   private boolean ensure(int n) throws IOException {
      boolean filled;
      for (filled = false; this.bufferIndex + n > this.bufferSize; filled = true) {
         if (!this.fill()) {
            throw this.unexpectedEOF();
         }
      }

      return filled;
   }

   private boolean fill() throws IOException {
      int dst;
      int len;
      if (this.markIndex <= -1) {
         this.streamIndex = this.streamIndex + this.bufferIndex;
         dst = 0;
         len = this.buffer.length;
      } else {
         int spaceInBuffer = this.buffer.length - this.bufferIndex;
         if (spaceInBuffer > 16384) {
            dst = this.bufferIndex;
            len = spaceInBuffer;
         } else {
            int delta = this.bufferIndex - this.markIndex;
            if (this.markIndex > 16384) {
               System.arraycopy(this.buffer, this.markIndex, this.buffer, 0, delta);
            } else {
               int newSize = this.bufferIndex + 1048576;
               System.err.println("Reallocate: " + this.buffer.length + " to " + newSize);
               char[] ncb = new char[newSize];
               System.arraycopy(this.buffer, this.markIndex, ncb, 0, delta);
               this.buffer = ncb;
            }

            this.streamIndex = this.streamIndex + this.markIndex;
            this.markIndex = 0;
            dst = delta;
            this.bufferIndex = this.bufferSize = delta;
            len = this.buffer.length - delta;
         }
      }

      if (this.in == null) {
         return false;
      } else {
         int n = this.in.read(this.buffer, dst, len);
         if (n > 0) {
            this.bufferSize = dst + n;
            this.bufferIndex = dst;
            return true;
         } else {
            return false;
         }
      }
   }

   public int peek() throws IOException {
      return this.peek(0);
   }

   public int peek(int n) throws IOException {
      if (this.bufferIndex + n >= this.bufferSize) {
         this.fill();
         if (this.bufferIndex + n >= this.bufferSize) {
            return -1;
         }
      }

      return this.buffer[this.bufferIndex + n];
   }

   public int read() throws IOException {
      if (this.bufferIndex >= this.bufferSize) {
         this.fill();
         if (this.bufferIndex >= this.bufferSize) {
            return -1;
         }
      }

      char c = this.buffer[this.bufferIndex++];
      if (c == '\n') {
         this.line++;
         this.lineStart = this.bufferIndex;
      }

      return c;
   }

   public long skip(long skip) throws IOException {
      if (skip < 0L) {
         int negativeBufferIndex = -this.bufferIndex;
         if (skip < negativeBufferIndex) {
            this.bufferIndex = 0;
            return negativeBufferIndex;
         } else {
            this.bufferIndex = (int)(this.bufferIndex + skip);
            return skip;
         }
      } else {
         long haveSkipped = 0L;

         while (haveSkipped < skip) {
            long charsInBuffer = this.bufferSize - this.bufferIndex;
            long charsToSkip = skip - haveSkipped;
            if (charsToSkip <= charsInBuffer) {
               this.bufferIndex = (int)(this.bufferIndex + charsToSkip);
               return skip;
            }

            haveSkipped += charsInBuffer;
            this.bufferIndex = this.bufferSize;
            this.fill();
            if (this.bufferIndex >= this.bufferSize) {
               break;
            }
         }

         return haveSkipped;
      }
   }

   public int findOffset(char value) throws IOException {
      return this.findOffset(0, value);
   }

   public int findOffset(int start, char value) throws IOException {
      while (true) {
         this.ensure();
         char c = this.buffer[this.bufferIndex + start];
         if (c == value) {
            return start;
         }

         start++;
      }
   }

   public void skipOrThrow(long n) throws IOException {
      long skipped = this.skip(n);
      if (skipped != n) {
         throw new IOException("Failed to skip " + n + " char's!");
      }
   }

   public boolean ready() throws IOException {
      return this.buffer != null && this.bufferIndex < this.bufferSize || this.in.ready();
   }

   public boolean markSupported() {
      return true;
   }

   public void mark(int readAheadLimit) throws IOException {
      this.mark();
   }

   public boolean isMarked() {
      return this.markIndex >= 0;
   }

   public void mark() throws IOException {
      if (this.markIndex >= 0) {
         throw new IOException("mark can't be used while already marked!");
      } else {
         this.markIndex = this.bufferIndex;
         this.markLine = this.line;
         this.markLineStart = this.lineStart;
      }
   }

   public void unmark() {
      this.markIndex = -1;
      this.markLine = -1;
      this.markLineStart = -1;
   }

   public int getMarkDistance() {
      return this.bufferIndex - this.markIndex;
   }

   public char[] cloneMark() {
      return Arrays.copyOfRange(this.buffer, this.markIndex, this.bufferIndex);
   }

   public void reset() throws IOException {
      if (this.markIndex < 0) {
         throw new IOException("Stream not marked");
      } else {
         this.bufferIndex = this.markIndex;
         this.markIndex = -1;
         this.line = this.markLine;
         this.lineStart = this.markLineStart;
         this.markLine = -1;
      }
   }

   @Override
   public void close() throws IOException {
      if (this.buffer != null) {
         try {
            if (this.in != null) {
               this.in.close();
            }
         } finally {
            this.in = null;
            this.buffer = null;
         }
      }
   }

   public char[] closeAndTakeBuffer() throws IOException {
      char[] buffer = this.buffer;
      this.close();
      return buffer;
   }

   public boolean peekFor(char consume) throws IOException {
      this.ensure();
      return this.buffer[this.bufferIndex] == consume;
   }

   public boolean tryConsume(char consume) throws IOException {
      this.ensure();
      if (this.buffer[this.bufferIndex] == consume) {
         this.bufferIndex++;
         if (consume == '\n') {
            this.line++;
            this.lineStart = this.bufferIndex;
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean tryConsumeString(@Nonnull String str) throws IOException {
      this.mark();
      if (this.tryConsume('"') && this.tryConsume(str) && this.tryConsume('"')) {
         this.unmark();
         return true;
      } else {
         this.reset();
         return false;
      }
   }

   public boolean tryConsume(@Nonnull String str) throws IOException {
      return this.tryConsume(str, 0);
   }

   public boolean tryConsume(@Nonnull String str, int start) throws IOException {
      while (start < str.length()) {
         this.ensure();

         while (start < str.length() && this.bufferIndex < this.bufferSize) {
            char c = this.buffer[this.bufferIndex];
            if (c != str.charAt(start++)) {
               return false;
            }

            this.bufferIndex++;
            if (c == '\n') {
               this.line++;
               this.lineStart = this.bufferIndex;
            }
         }
      }

      return true;
   }

   public int tryConsumeSome(@Nonnull String str, int start) throws IOException {
      while (start < str.length()) {
         this.ensure();

         while (start < str.length() && this.bufferIndex < this.bufferSize) {
            char c = this.buffer[this.bufferIndex];
            if (c != str.charAt(start)) {
               return start;
            }

            start++;
            this.bufferIndex++;
            if (c == '\n') {
               this.line++;
               this.lineStart = this.bufferIndex;
            }
         }
      }

      return start;
   }

   public void expect(char expect) throws IOException {
      this.ensure();
      char read = this.buffer[this.bufferIndex++];
      if (read != expect) {
         throw this.expecting(read, expect);
      } else {
         if (expect == '\n') {
            this.line++;
            this.lineStart = this.bufferIndex;
         }
      }
   }

   public void expect(@Nonnull String str, int start) throws IOException {
      this.ensure(str.length() - start);

      while (start < str.length()) {
         char c = this.buffer[this.bufferIndex];
         if (c != str.charAt(start++)) {
            throw this.expecting(c, str, start);
         }

         this.bufferIndex++;
         if (c == '\n') {
            this.line++;
            this.lineStart = this.bufferIndex;
         }
      }
   }

   public boolean tryConsumeOrExpect(char consume, char expect) throws IOException {
      this.ensure();
      char read = this.buffer[this.bufferIndex];
      if (read == consume) {
         this.bufferIndex++;
         if (consume == '\n') {
            this.line++;
            this.lineStart = this.bufferIndex;
         }

         return true;
      } else if (read == expect) {
         this.bufferIndex++;
         if (expect == '\n') {
            this.line++;
            this.lineStart = this.bufferIndex;
         }

         return false;
      } else {
         throw this.expecting(read, expect);
      }
   }

   public void consumeWhiteSpace() throws IOException {
      while (true) {
         if (this.bufferIndex >= this.bufferSize) {
            this.fill();
            if (this.bufferIndex >= this.bufferSize) {
               return;
            }
         }

         while (this.bufferIndex < this.bufferSize) {
            char ch = this.buffer[this.bufferIndex];
            switch (ch) {
               case '\t':
               case '\r':
               case ' ':
                  this.bufferIndex++;
                  break;
               case '\n':
                  this.bufferIndex++;
                  this.line++;
                  this.lineStart = this.bufferIndex;
                  break;
               default:
                  if (!Character.isWhitespace(ch)) {
                     return;
                  }

                  this.bufferIndex++;
            }
         }
      }
   }

   public void consumeIgnoreCase(@Nonnull String str, int start) throws IOException {
      this.ensure(str.length() - start);

      while (start < str.length()) {
         char c = this.buffer[this.bufferIndex];
         if (!equalsIgnoreCase(c, str.charAt(start++))) {
            throw this.expecting(c, str, start);
         }

         this.bufferIndex++;
         if (c == '\n') {
            this.line++;
            this.lineStart = this.bufferIndex;
         }
      }
   }

   @Nonnull
   public String readString() throws IOException {
      this.expect('"');
      return this.readRemainingString();
   }

   @Nonnull
   public String readRemainingString() throws IOException {
      if (this.tempSb == null) {
         this.tempSb = new StringBuilder(1024);
      }

      while (true) {
         this.ensure();

         while (this.bufferIndex < this.bufferSize) {
            char read = this.buffer[this.bufferIndex++];
            switch (read) {
               case '"':
                  String string = this.tempSb.toString();
                  this.tempSb.setLength(0);
                  return string;
               case '\\':
                  this.ensure();
                  read = this.buffer[this.bufferIndex++];
                  switch (read) {
                     case '"':
                     case '/':
                     case '\\':
                        this.tempSb.append(read);
                        continue;
                     case 'b':
                        this.tempSb.append('\b');
                        continue;
                     case 'f':
                        this.tempSb.append('\f');
                        continue;
                     case 'n':
                        this.tempSb.append('\n');
                        continue;
                     case 'r':
                        this.tempSb.append('\r');
                        continue;
                     case 't':
                        this.tempSb.append('\t');
                        continue;
                     case 'u':
                        this.ensure(4);
                        read = this.buffer[this.bufferIndex++];
                        int digit = Character.digit(read, 16);
                        if (digit == -1) {
                           throw this.expectingWhile(read, "HEX Digit 0-F", "reading string");
                        }

                        int hex = digit << 12;
                        read = this.buffer[this.bufferIndex++];
                        digit = Character.digit(read, 16);
                        if (digit == -1) {
                           throw this.expectingWhile(read, "HEX Digit 0-F", "reading string");
                        }

                        hex |= digit << 8;
                        read = this.buffer[this.bufferIndex++];
                        digit = Character.digit(read, 16);
                        if (digit == -1) {
                           throw this.expectingWhile(read, "HEX Digit 0-F", "reading string");
                        }

                        hex |= digit << 4;
                        read = this.buffer[this.bufferIndex++];
                        digit = Character.digit(read, 16);
                        if (digit == -1) {
                           throw this.expectingWhile(read, "HEX Digit 0-F", "reading string");
                        }

                        hex |= digit;
                        this.tempSb.appendCodePoint(hex);
                        continue;
                     default:
                        throw this.expecting(read, "escape char");
                  }
               default:
                  if (Character.isISOControl(read)) {
                     throw this.unexpectedChar(read);
                  }

                  this.tempSb.append(read);
            }
         }
      }
   }

   public void skipString() throws IOException {
      this.expect('"');
      this.skipRemainingString();
   }

   public void skipRemainingString() throws IOException {
      while (true) {
         this.ensure();

         while (this.bufferIndex < this.bufferSize) {
            char read = this.buffer[this.bufferIndex++];
            switch (read) {
               case '"':
                  return;
               case '\\':
                  this.ensure();
                  read = this.buffer[this.bufferIndex++];
                  switch (read) {
                     case '"':
                     case '/':
                     case '\\':
                     case 'b':
                     case 'f':
                     case 'n':
                     case 'r':
                     case 't':
                        continue;
                     case 'u':
                        this.ensure(4);
                        read = this.buffer[this.bufferIndex++];
                        int digit = Character.digit(read, 16);
                        if (digit == -1) {
                           throw this.expectingWhile(read, "HEX Digit 0-F", "skipping string");
                        }

                        read = this.buffer[this.bufferIndex++];
                        digit = Character.digit(read, 16);
                        if (digit == -1) {
                           throw this.expectingWhile(read, "HEX Digit 0-F", "skipping string");
                        }

                        read = this.buffer[this.bufferIndex++];
                        digit = Character.digit(read, 16);
                        if (digit == -1) {
                           throw this.expectingWhile(read, "HEX Digit 0-F", "skipping string");
                        }

                        read = this.buffer[this.bufferIndex++];
                        digit = Character.digit(read, 16);
                        if (digit == -1) {
                           throw this.expectingWhile(read, "HEX Digit 0-F", "skipping string");
                        }
                        continue;
                     default:
                        throw this.expecting(read, "escape char");
                  }
               default:
                  if (Character.isISOControl(read)) {
                     throw this.unexpectedChar(read);
                  }
            }
         }
      }
   }

   public long readStringPartAsLong(int count) throws IOException {
      assert count > 0 && count <= 4;

      return UnsafeUtil.UNSAFE != null && this.bufferIndex + count <= this.bufferSize
         ? this.readStringPartAsLongUnsafe(count)
         : this.readStringPartAsLongSlow(count);
   }

   protected long readStringPartAsLongSlow(int count) throws IOException {
      this.ensure(count);
      char c1 = this.buffer[this.bufferIndex++];
      if (count == 1) {
         return c1;
      } else {
         char c2 = this.buffer[this.bufferIndex++];
         long value = c1 | (long)c2 << 16;
         if (count == 2) {
            return value;
         } else {
            char c3 = this.buffer[this.bufferIndex++];
            value |= (long)c3 << 32;
            if (count == 3) {
               return value;
            } else {
               char c4 = this.buffer[this.bufferIndex++];
               return value | (long)c4 << 48;
            }
         }
      }
   }

   protected long readStringPartAsLongUnsafe(int count) throws IOException {
      this.ensure(count);
      int offset = Unsafe.ARRAY_CHAR_BASE_OFFSET + Unsafe.ARRAY_CHAR_INDEX_SCALE * this.bufferIndex;
      long value = UnsafeUtil.UNSAFE.getLong(this.buffer, offset);
      this.bufferIndex += count;
      long mask = count == 4 ? -1L : (1L << count * 16) - 1L;
      return value & mask;
   }

   public boolean readBooleanValue() throws IOException {
      this.ensure(4);
      char read = this.buffer[this.bufferIndex++];

      return switch (read) {
         case 'F', 'f' -> {
            this.consumeIgnoreCase("false", 1);
            yield false;
         }
         case 'T', 't' -> {
            this.consumeIgnoreCase("true", 1);
            yield true;
         }
         default -> throw this.expecting(read, "true' or 'false");
      };
   }

   public void skipBooleanValue() throws IOException {
      this.readBooleanValue();
   }

   @Nullable
   public Void readNullValue() throws IOException {
      this.consumeIgnoreCase("null", 0);
      return null;
   }

   public void skipNullValue() throws IOException {
      this.consumeIgnoreCase("null", 0);
   }

   public double readDoubleValue() throws IOException {
      int start = this.bufferIndex;

      while (true) {
         if (this.bufferIndex >= this.bufferSize) {
            this.fill();
            if (this.bufferIndex >= this.bufferSize) {
               return JavaDoubleParser.parseDouble(this.buffer, start, this.bufferIndex - start);
            }
         }

         while (this.bufferIndex < this.bufferSize) {
            char read = this.buffer[this.bufferIndex];
            switch (read) {
               case '+':
               case '-':
               case '.':
               case 'E':
               case 'e':
                  this.bufferIndex++;
                  break;
               default:
                  if (!Character.isDigit(read)) {
                     return JavaDoubleParser.parseDouble(this.buffer, start, this.bufferIndex - start);
                  }

                  this.bufferIndex++;
            }
         }
      }
   }

   public void skipDoubleValue() throws IOException {
      while (true) {
         if (this.bufferIndex >= this.bufferSize) {
            this.fill();
            if (this.bufferIndex >= this.bufferSize) {
               return;
            }
         }

         while (this.bufferIndex < this.bufferSize) {
            char read = this.buffer[this.bufferIndex];
            switch (read) {
               case '+':
               case '-':
               case '.':
               case 'E':
               case 'e':
                  this.bufferIndex++;
                  break;
               default:
                  if (!Character.isDigit(read)) {
                     return;
                  }

                  this.bufferIndex++;
            }
         }
      }
   }

   public float readFloatValue() throws IOException {
      return (float)this.readDoubleValue();
   }

   public void skipFloatValue() throws IOException {
      this.skipDoubleValue();
   }

   public long readLongValue() throws IOException {
      return this.readLongValue(10);
   }

   public long readLongValue(int radix) throws IOException {
      if (this.tempSb == null) {
         this.tempSb = new StringBuilder(1024);
      }

      while (true) {
         if (this.bufferIndex >= this.bufferSize) {
            this.fill();
            if (this.bufferIndex >= this.bufferSize) {
               long value = Long.parseLong(this.tempSb, 0, this.tempSb.length(), radix);
               this.tempSb.setLength(0);
               return value;
            }
         }

         while (this.bufferIndex < this.bufferSize) {
            char read = this.buffer[this.bufferIndex];
            switch (read) {
               case '+':
               case '-':
               case '.':
               case 'E':
               case 'e':
                  this.tempSb.append(read);
                  this.bufferIndex++;
                  break;
               default:
                  if (Character.digit(read, radix) < 0) {
                     long value = Long.parseLong(this.tempSb, 0, this.tempSb.length(), radix);
                     this.tempSb.setLength(0);
                     return value;
                  }

                  this.tempSb.append(read);
                  this.bufferIndex++;
            }
         }
      }
   }

   public void skipLongValue() throws IOException {
      this.skipLongValue(10);
   }

   public void skipLongValue(int radix) throws IOException {
      while (true) {
         if (this.bufferIndex >= this.bufferSize) {
            this.fill();
            if (this.bufferIndex >= this.bufferSize) {
               return;
            }
         }

         while (this.bufferIndex < this.bufferSize) {
            char read = this.buffer[this.bufferIndex];
            switch (read) {
               case '+':
               case '-':
               case '.':
               case 'E':
               case 'e':
                  this.bufferIndex++;
                  break;
               default:
                  if (Character.digit(read, radix) < 0) {
                     return;
                  }

                  this.bufferIndex++;
            }
         }
      }
   }

   public int readIntValue() throws IOException {
      return this.readIntValue(10);
   }

   public int readIntValue(int radix) throws IOException {
      return (int)this.readLongValue(radix);
   }

   public byte readByteValue() throws IOException {
      return this.readByteValue(10);
   }

   public byte readByteValue(int radix) throws IOException {
      return (byte)this.readLongValue(radix);
   }

   public void skipIntValue() throws IOException {
      this.skipLongValue();
   }

   public void skipIntValue(int radix) throws IOException {
      this.skipLongValue(radix);
   }

   public void skipObject() throws IOException {
      this.expect('{');
      this.skipObjectContinued();
   }

   public void skipObjectContinued() throws IOException {
      int count = 1;

      while (true) {
         this.ensure();

         while (this.bufferIndex < this.bufferSize) {
            char read = this.buffer[this.bufferIndex++];
            switch (read) {
               case '\n':
                  this.line++;
                  this.lineStart = this.bufferIndex;
                  break;
               case '{':
                  count++;
                  break;
               case '}':
                  if (--count == 0) {
                     return;
                  }
            }
         }
      }
   }

   public void skipArray() throws IOException {
      this.expect('[');
      this.skipArrayContinued();
   }

   public void skipArrayContinued() throws IOException {
      int count = 1;

      while (true) {
         this.ensure();

         while (this.bufferIndex < this.bufferSize) {
            char read = this.buffer[this.bufferIndex++];
            switch (read) {
               case '\n':
                  this.line++;
                  this.lineStart = this.bufferIndex;
                  break;
               case '[':
                  count++;
                  break;
               case ']':
                  if (--count == 0) {
                     return;
                  }
            }
         }
      }
   }

   public void skipValue() throws IOException {
      this.ensure();
      char read = this.buffer[this.bufferIndex];
      switch (read) {
         case '"':
            this.skipString();
            break;
         case '+':
         case '-':
            this.skipDoubleValue();
            break;
         case 'F':
         case 'f':
            this.consumeIgnoreCase("false", 0);
            break;
         case 'N':
         case 'n':
            this.skipNullValue();
            break;
         case 'T':
         case 't':
            this.consumeIgnoreCase("true", 0);
            break;
         case '[':
            this.skipArray();
            break;
         case '{':
            this.skipObject();
            break;
         default:
            if (!Character.isDigit(read)) {
               throw this.unexpectedChar(read);
            }

            this.skipDoubleValue();
      }
   }

   @Nonnull
   private IOException unexpectedEOF() {
      return new IOException("Unexpected EOF!");
   }

   @Nonnull
   private IOException unexpectedChar(char read) {
      return new IOException("Unexpected character: " + Integer.toHexString(read) + ", '" + read + "'!");
   }

   @Nonnull
   private IOException expecting(char read, char expect) {
      return new IOException("Unexpected character: " + Integer.toHexString(read) + ", '" + read + "' expected '" + expect + "'!");
   }

   @Nonnull
   private IOException expecting(char read, String expected) {
      return new IOException("Unexpected character: " + Integer.toHexString(read) + ", '" + read + "' expected '" + expected + "'!");
   }

   @Nonnull
   private IOException expectingWhile(char read, String expected, String reason) {
      return new IOException("Unexpected character: " + Integer.toHexString(read) + ", '" + read + "' expected '" + expected + "' while " + reason + "!");
   }

   @Nonnull
   private IOException expecting(char read, @Nonnull String expected, int index) {
      return new IOException(
         "Unexpected character: "
            + Integer.toHexString(read)
            + ", '"
            + read
            + "' when consuming string '"
            + expected
            + "' expected '"
            + expected.substring(index - 1)
            + "'!"
      );
   }

   @Nonnull
   @Override
   public String toString() {
      if (this.buffer == null) {
         return "Closed RawJsonReader";
      } else {
         StringBuilder s = new StringBuilder("Index: ")
            .append(this.streamIndex + this.bufferIndex)
            .append(", StreamIndex: ")
            .append(this.streamIndex)
            .append(", BufferIndex: ")
            .append(this.bufferIndex)
            .append(", BufferSize: ")
            .append(this.bufferSize)
            .append(", Line: ")
            .append(this.line)
            .append(", MarkIndex: ")
            .append(this.markIndex)
            .append(", MarkLine: ")
            .append(this.markLine)
            .append('\n');
         int lineStart = this.findLineStart(this.bufferIndex);

         int lineNumber;
         for (lineNumber = this.line; lineStart > 0 && lineNumber > this.line - 10; lineNumber--) {
            lineStart = this.findLineStart(lineStart);
         }

         while (lineNumber < this.line) {
            lineStart = this.appendLine(s, lineStart, lineNumber);
            lineNumber++;
         }

         for (int var4 = this.appendProblemLine(s, lineStart, this.line); var4 < this.bufferSize && lineNumber < this.line + 10; lineNumber++) {
            var4 = this.appendLine(s, var4, lineNumber);
         }

         return this.in == null ? "Buffer RawJsonReader: " + s : "Streamed RawJsonReader: " + s;
      }
   }

   private int findLineStart(int index) {
      index--;

      while (index > 0 && this.buffer[index] != '\n') {
         index--;
      }

      return index;
   }

   private int appendLine(@Nonnull StringBuilder sb, int index, int lineNumber) {
      int lineStart = index + 1;
      index++;

      while (index < this.bufferSize && this.buffer[index] != '\n') {
         index++;
      }

      sb.append("L").append(String.format("%3s", lineNumber)).append('|').append(this.buffer, lineStart, index - lineStart).append('\n');
      return index;
   }

   private int appendProblemLine(@Nonnull StringBuilder sb, int index, int lineNumber) {
      int lineStart = ++index;

      while (index < this.bufferSize && this.buffer[index] != '\n') {
         index++;
      }

      sb.append("L").append(String.format("%3s", lineNumber)).append('>').append(this.buffer, lineStart, index - lineStart).append('\n');
      sb.append("    |");
      sb.append("-".repeat(Math.max(0, this.bufferIndex - lineStart - 1)));
      sb.append('^').append('\n');
      return index;
   }

   @Nonnull
   public static RawJsonReader fromRawString(String str) {
      return fromJsonString("\"" + str + "\"");
   }

   @Nonnull
   public static RawJsonReader fromJsonString(@Nonnull String str) {
      return fromBuffer(str.toCharArray());
   }

   @Nonnull
   public static RawJsonReader fromPath(@Nonnull Path path, @Nonnull char[] buffer) throws IOException {
      return new RawJsonReader(new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8), buffer);
   }

   @Nonnull
   public static RawJsonReader fromBuffer(@Nonnull char[] buffer) {
      return new RawJsonReader(buffer);
   }

   public static boolean equalsIgnoreCase(char c1, char c2) {
      if (c1 == c2) {
         return true;
      } else {
         char u1 = Character.toUpperCase(c1);
         char u2 = Character.toUpperCase(c2);
         return u1 == u2 || Character.toLowerCase(u1) == Character.toLowerCase(u2);
      }
   }

   @Deprecated
   public static BsonDocument readBsonDocument(@Nonnull RawJsonReader reader) throws IOException {
      reader.expect('{');
      StringBuilder sb = new StringBuilder("{");
      readBsonDocument0(reader, sb);
      return BsonDocument.parse(sb.toString());
   }

   private static void readBsonDocument0(@Nonnull RawJsonReader reader, @Nonnull StringBuilder sb) throws IOException {
      int count = 1;

      int read;
      while ((read = reader.read()) != -1) {
         sb.append((char)read);
         switch (read) {
            case 10:
               reader.line++;
               reader.lineStart = reader.bufferIndex;
               break;
            case 91:
               readBsonArray0(reader, sb);
               break;
            case 123:
               count++;
               break;
            case 125:
               if (--count == 0) {
                  return;
               }
         }
      }

      throw reader.unexpectedEOF();
   }

   @Deprecated
   public static BsonArray readBsonArray(@Nonnull RawJsonReader reader) throws IOException {
      reader.expect('[');
      StringBuilder sb = new StringBuilder("[");
      readBsonArray0(reader, sb);
      return BsonArray.parse(sb.toString());
   }

   private static void readBsonArray0(@Nonnull RawJsonReader reader, @Nonnull StringBuilder sb) throws IOException {
      int count = 1;

      int read;
      while ((read = reader.read()) != -1) {
         sb.append((char)read);
         switch (read) {
            case 10:
               reader.line++;
               reader.lineStart = reader.bufferIndex;
               break;
            case 91:
               count++;
               break;
            case 93:
               if (--count == 0) {
                  return;
               }
               break;
            case 123:
               readBsonDocument0(reader, sb);
         }
      }

      throw reader.unexpectedEOF();
   }

   @Deprecated
   public static BsonValue readBsonValue(@Nonnull RawJsonReader reader) throws IOException {
      int read = reader.peek();
      if (read == -1) {
         throw reader.unexpectedEOF();
      } else {
         return (BsonValue)(switch (read) {
            case 34 -> new BsonString(reader.readString());
            case 43, 45 -> new BsonDouble(reader.readDoubleValue());
            case 70, 84, 102, 116 -> reader.readBooleanValue() ? BsonBoolean.TRUE : BsonBoolean.FALSE;
            case 78, 110 -> {
               reader.skipNullValue();
               yield BsonNull.VALUE;
            }
            case 91 -> readBsonArray(reader);
            case 123 -> readBsonDocument(reader);
            default -> {
               if (!Character.isDigit(read)) {
                  throw reader.unexpectedChar((char)read);
               }

               yield new BsonDouble(reader.readDoubleValue());
            }
         });
      }
   }

   public static boolean seekToKey(@Nonnull RawJsonReader reader, @Nonnull String search) throws IOException {
      reader.consumeWhiteSpace();
      reader.expect('{');
      reader.consumeWhiteSpace();
      if (reader.tryConsume('}')) {
         return false;
      } else {
         while (true) {
            reader.expect('"');
            if (reader.tryConsume(search) && reader.tryConsume('"')) {
               reader.consumeWhiteSpace();
               reader.expect(':');
               reader.consumeWhiteSpace();
               return true;
            }

            reader.skipRemainingString();
            reader.consumeWhiteSpace();
            reader.expect(':');
            reader.consumeWhiteSpace();
            reader.skipValue();
            reader.consumeWhiteSpace();
            if (reader.tryConsumeOrExpect('}', ',')) {
               return false;
            }

            reader.consumeWhiteSpace();
         }
      }
   }

   @Nullable
   public static String seekToKeyFromObjectStart(@Nonnull RawJsonReader reader, @Nonnull String search1, @Nonnull String search2) throws IOException {
      reader.consumeWhiteSpace();
      reader.expect('{');
      reader.consumeWhiteSpace();
      if (reader.tryConsume('}')) {
         return null;
      } else {
         while (true) {
            reader.expect('"');
            int search1Index = reader.tryConsumeSome(search1, 0);
            if (search1Index == search1.length() && reader.tryConsume('"')) {
               reader.consumeWhiteSpace();
               reader.expect(':');
               reader.consumeWhiteSpace();
               return search1;
            }

            if (reader.tryConsume(search2, search1Index) && reader.tryConsume('"')) {
               reader.consumeWhiteSpace();
               reader.expect(':');
               reader.consumeWhiteSpace();
               return search2;
            }

            reader.skipRemainingString();
            reader.consumeWhiteSpace();
            reader.expect(':');
            reader.consumeWhiteSpace();
            reader.skipValue();
            reader.consumeWhiteSpace();
            if (reader.tryConsumeOrExpect('}', ',')) {
               return null;
            }

            reader.consumeWhiteSpace();
         }
      }
   }

   @Nullable
   public static String seekToKeyFromObjectContinued(@Nonnull RawJsonReader reader, @Nonnull String search1, @Nonnull String search2) throws IOException {
      reader.consumeWhiteSpace();
      if (reader.tryConsumeOrExpect('}', ',')) {
         return null;
      } else {
         reader.consumeWhiteSpace();

         while (true) {
            reader.expect('"');
            int search1Index = reader.tryConsumeSome(search1, 0);
            if (search1Index == search1.length() && reader.tryConsume('"')) {
               reader.consumeWhiteSpace();
               reader.expect(':');
               reader.consumeWhiteSpace();
               return search1;
            }

            if (reader.tryConsume(search2, search1Index) && reader.tryConsume('"')) {
               reader.consumeWhiteSpace();
               reader.expect(':');
               reader.consumeWhiteSpace();
               return search2;
            }

            reader.skipRemainingString();
            reader.consumeWhiteSpace();
            reader.expect(':');
            reader.consumeWhiteSpace();
            reader.skipValue();
            reader.consumeWhiteSpace();
            if (reader.tryConsumeOrExpect('}', ',')) {
               return null;
            }

            reader.consumeWhiteSpace();
         }
      }
   }

   public static void validateBsonDocument(@Nonnull RawJsonReader reader) throws IOException {
      reader.expect('{');
      reader.consumeWhiteSpace();
      if (!reader.tryConsume('}')) {
         while (true) {
            reader.skipString();
            reader.consumeWhiteSpace();
            reader.expect(':');
            reader.consumeWhiteSpace();
            validateBsonValue(reader);
            reader.consumeWhiteSpace();
            if (reader.tryConsumeOrExpect('}', ',')) {
               return;
            }

            reader.consumeWhiteSpace();
         }
      }
   }

   public static void validateBsonArray(@Nonnull RawJsonReader reader) throws IOException {
      reader.expect('[');
      reader.consumeWhiteSpace();
      if (!reader.tryConsume(']')) {
         while (true) {
            validateBsonValue(reader);
            reader.consumeWhiteSpace();
            if (reader.tryConsumeOrExpect(']', ',')) {
               return;
            }

            reader.consumeWhiteSpace();
         }
      }
   }

   public static void validateBsonValue(@Nonnull RawJsonReader reader) throws IOException {
      int read = reader.peek();
      if (read == -1) {
         throw reader.unexpectedEOF();
      } else {
         switch (read) {
            case 34:
               reader.skipString();
               break;
            case 43:
            case 45:
               reader.readDoubleValue();
               break;
            case 70:
            case 84:
            case 102:
            case 116:
               reader.readBooleanValue();
               break;
            case 78:
            case 110:
               reader.skipNullValue();
               break;
            case 91:
               validateBsonArray(reader);
               break;
            case 123:
               validateBsonDocument(reader);
               break;
            default:
               if (Character.isDigit(read)) {
                  reader.readDoubleValue();
                  return;
               }

               throw reader.unexpectedChar((char)read);
         }
      }
   }

   @Nullable
   public static <T> T readSync(@Nonnull Path path, @Nonnull Codec<T> codec, @Nonnull HytaleLogger logger) throws IOException {
      char[] buffer = READ_BUFFER.get();
      RawJsonReader reader = fromPath(path, buffer);

      Object var7;
      try {
         ExtraInfo extraInfo = ExtraInfo.THREAD_LOCAL.get();
         T value = codec.decodeJson(reader, extraInfo);
         extraInfo.getValidationResults().logOrThrowValidatorExceptions(logger);
         var7 = value;
      } finally {
         char[] newBuffer = reader.closeAndTakeBuffer();
         if (newBuffer.length > buffer.length) {
            READ_BUFFER.set(newBuffer);
         }
      }

      return (T)var7;
   }

   @Nullable
   public static <T> T readSyncWithBak(@Nonnull Path path, @Nonnull Codec<T> codec, @Nonnull HytaleLogger logger) {
      try {
         return readSync(path, codec, logger);
      } catch (IOException var8) {
         Path backupPath = path.resolveSibling(path.getFileName() + ".bak");
         if (var8 instanceof NoSuchFileException && !Files.exists(backupPath)) {
            return null;
         } else {
            if (Sentry.isEnabled()) {
               Sentry.captureException(var8);
            }

            ((HytaleLogger.Api)logger.at(Level.SEVERE).withCause(var8)).log("Failed to load from primary file %s, trying backup file", path);

            try {
               T value = readSync(backupPath, codec, logger);
               logger.at(Level.WARNING).log("Loaded from backup file %s after primary file %s failed to load", backupPath, path);
               return value;
            } catch (NoSuchFileException var6) {
               return null;
            } catch (IOException var7) {
               ((HytaleLogger.Api)logger.at(Level.WARNING).withCause(var8)).log("Failed to load from both %s and backup file %s", path, backupPath);
               return null;
            }
         }
      }
   }
}
