package com.hypixel.hytale.codec.builder;

import com.hypixel.hytale.codec.util.RawJsonReader;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StringTreeMap<V> {
   public static final int STRING_PART_SIZE = 4;
   private Long2ObjectMap<StringTreeMap<V>> map;
   @Nullable
   private String key;
   @Nullable
   private V value;

   public StringTreeMap() {
   }

   public StringTreeMap(@Nonnull StringTreeMap<V> parent) {
      if (parent.map != null) {
         this.map = new Long2ObjectOpenHashMap(parent.map.size());
         ObjectIterator var2 = parent.map.long2ObjectEntrySet().iterator();

         while (var2.hasNext()) {
            Entry<StringTreeMap<V>> entry = (Entry<StringTreeMap<V>>)var2.next();
            this.map.put(entry.getLongKey(), new StringTreeMap((StringTreeMap<V>)entry.getValue()));
         }
      }

      this.key = parent.key;
      this.value = parent.value;
   }

   public StringTreeMap(@Nonnull Map<String, V> entries) {
      this.putAll(entries);
   }

   @Nullable
   public String getKey() {
      return this.key;
   }

   @Nullable
   public V getValue() {
      return this.value;
   }

   public void putAll(@Nonnull Map<String, V> entries) {
      for (java.util.Map.Entry<String, V> entry : entries.entrySet()) {
         this.put(entry.getKey(), entry.getValue());
      }
   }

   public void put(@Nonnull String key, V values) {
      this.put0(key, values, 0, key.length());
   }

   private void put0(@Nonnull String key, V fields, int start, int end) {
      if (this.map != null || this.key != null && !this.key.equals(key)) {
         if (start < end) {
            long part = readStringPartAsLong(key, start, end);
            if (this.map == null) {
               this.map = new Long2ObjectOpenHashMap();
            }

            ((StringTreeMap)this.map.computeIfAbsent(part, k -> new StringTreeMap())).put0(key, fields, start + 4, end);
            if (this.key != null && this.key.length() > start) {
               String oldKey = this.key;
               V oldFields = this.value;
               this.key = null;
               this.value = null;
               this.put0(oldKey, oldFields, start, oldKey.length());
            }
         } else {
            if (this.key != null && this.key.length() > start) {
               String oldKey = this.key;
               V oldFields = this.value;
               this.key = key;
               this.value = fields;
               this.put0(oldKey, oldFields, start, oldKey.length());
            } else {
               if (this.key != null && !this.key.equals(key)) {
                  throw new IllegalStateException("Keys don't match: " + this.key + " != " + key);
               }

               this.key = key;
               this.value = fields;
            }
         }
      } else {
         this.key = key;
         this.value = fields;
      }
   }

   public void remove(@Nonnull String key) {
      if (this.map == null) {
         if (this.key != null) {
            if (this.key.equals(key)) {
               this.key = null;
               this.value = null;
            }
         }
      } else {
         this.remove0(key, 0, key.length());
      }
   }

   protected void remove0(@Nonnull String key, int start, int end) {
      long part = readStringPartAsLong(key, start, end);
      StringTreeMap<V> entry = (StringTreeMap<V>)this.map.get(part);
      if (entry != null) {
         int newStart = start + 4;
         if (newStart >= end) {
            this.map.remove(part);
         } else {
            if (entry.map == null) {
               if (entry.key == null) {
                  throw new IllegalStateException("Incorrectly built tree!");
               }

               if (entry.key.equals(key)) {
                  this.map.remove(part);
                  return;
               }
            }

            entry.remove0(key, newStart, end);
         }
      }
   }

   @Nullable
   public StringTreeMap<V> findEntry(@Nonnull RawJsonReader reader) throws IOException {
      reader.expect('"');
      int end = reader.findOffset('"');
      return this.findEntry0(reader, null, end);
   }

   public StringTreeMap<V> findEntryOrDefault(@Nonnull RawJsonReader reader, StringTreeMap<V> def) throws IOException {
      reader.expect('"');
      int end = reader.findOffset('"');
      return this.findEntry0(reader, def, end);
   }

   protected StringTreeMap<V> findEntry0(@Nonnull RawJsonReader reader, StringTreeMap<V> def, int end) throws IOException {
      if (this.map == null) {
         if (this.key == null) {
            reader.skipRemainingString();
            return def;
         } else {
            return this.consumeEntryKey(reader, def, end, this);
         }
      } else {
         long part = reader.readStringPartAsLong(Math.min(end, 4));
         int newEnd = Math.max(end - 4, 0);
         StringTreeMap<V> entry = (StringTreeMap<V>)this.map.get(part);
         if (entry == null) {
            if (newEnd != 0) {
               reader.skipRemainingString();
            } else {
               reader.expect('"');
            }

            return def;
         } else if (newEnd == 0) {
            reader.expect('"');
            return entry;
         } else if (entry.map == null) {
            if (entry.key == null) {
               throw new IllegalStateException("Incorrectly built tree!");
            } else {
               return this.consumeEntryKey(reader, def, newEnd, entry);
            }
         } else {
            return entry.findEntry0(reader, def, newEnd);
         }
      }
   }

   private StringTreeMap<V> consumeEntryKey(@Nonnull RawJsonReader reader, StringTreeMap<V> def, int end, @Nonnull StringTreeMap<V> entry) throws IOException {
      int keyLength = entry.key.length();
      if (keyLength < end) {
         reader.skipRemainingString();
         return def;
      } else if (!reader.tryConsume(entry.key, keyLength - end)) {
         reader.skipRemainingString();
         return def;
      } else if (!reader.tryConsume('"')) {
         reader.skipRemainingString();
         return def;
      } else {
         return entry;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "StringTreeMap{map=" + this.map + ", key='" + this.key + "', value=" + this.value + "}";
   }

   public static long readStringPartAsLong(@Nonnull String key, int start, int end) {
      int length = end - start;
      char c1 = key.charAt(start);
      if (length == 1) {
         return c1;
      } else {
         char c2 = key.charAt(start + 1);
         long value = c1 | (long)c2 << 16;
         if (length == 2) {
            return value;
         } else {
            char c3 = key.charAt(start + 2);
            value |= (long)c3 << 32;
            if (length == 3) {
               return value;
            } else {
               char c4 = key.charAt(start + 3);
               return value | (long)c4 << 48;
            }
         }
      }
   }
}
