package com.hypixel.hytale.builtin.tagset;

import com.hypixel.hytale.common.util.StringUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TagSetLookupTable<T extends TagSet> {
   @Nonnull
   private Int2ObjectMap<IntSet> tagMatcher = new Int2ObjectOpenHashMap();

   public TagSetLookupTable(@Nonnull Map<String, T> tagSetMap, @Nonnull Object2IntMap<String> tagSetIndexMap, @Nonnull Object2IntMap<String> tagIndexMap) {
      this.createTagMap(tagSetMap, tagSetIndexMap, tagIndexMap);
   }

   private void createTagMap(@Nonnull Map<String, T> tagSetMap, @Nonnull Object2IntMap<String> tagSetIndexMap, @Nonnull Object2IntMap<String> tagIndexMap) {
      IntArrayList path = new IntArrayList();
      tagSetMap.forEach((key, entry) -> {
         int id = tagSetIndexMap.getOrDefault(key, -1);
         if (id < 0 || !this.tagMatcher.containsKey(id)) {
            try {
               this.createTagSet((T)entry, tagSetMap, tagSetIndexMap, tagIndexMap, path);
            } catch (IllegalStateException var9) {
               throw new IllegalStateException(key + ": ", var9);
            }

            path.clear();
         }
      });
   }

   @Nonnull
   private IntSet createTagSet(
      @Nonnull T tagSet,
      @Nonnull Map<String, T> tagSetMap,
      @Nonnull Object2IntMap<String> tagSetIndexMap,
      @Nonnull Object2IntMap<String> tagIndexMap,
      @Nonnull IntArrayList path
   ) {
      IntOpenHashSet set = new IntOpenHashSet();
      int index = tagSetIndexMap.getInt(tagSet.getId());
      if (path.contains(index)) {
         throw new IllegalStateException("Cyclic reference to set detected: " + tagSet.getId());
      } else {
         path.add(index);
         this.tagMatcher.put(index, set);
         if (!tagIndexMap.isEmpty()) {
            String[] includedTagSets = tagSet.getIncludedTagSets();
            if (includedTagSets != null) {
               for (String tag : includedTagSets) {
                  this.consumeSet(tag, tagSetMap, tagSetIndexMap, tagIndexMap, path, set::addAll);
               }
            }

            String[] excludedTagSets = tagSet.getExcludedTagSets();
            if (excludedTagSets != null) {
               for (String tag : excludedTagSets) {
                  this.consumeSet(tag, tagSetMap, tagSetIndexMap, tagIndexMap, path, set::removeAll);
               }
            }

            String[] includedTags = tagSet.getIncludedTags();
            if (includedTags != null) {
               for (String tag : includedTags) {
                  this.consumeTag(tag, tagSet, tagIndexMap, set::add);
               }
            }

            String[] excludedTags = tagSet.getExcludedTags();
            if (excludedTags != null) {
               for (String tag : excludedTags) {
                  this.consumeTag(tag, tagSet, tagIndexMap, set::remove);
               }
            }
         }

         return set;
      }
   }

   private void consumeSet(
      String tag,
      @Nonnull Map<String, T> tagSetMap,
      @Nonnull Object2IntMap<String> tagSetIndexMap,
      @Nonnull Object2IntMap<String> tagIndexMap,
      @Nonnull IntArrayList path,
      @Nonnull Consumer<IntSet> predicate
   ) {
      IntSet s = this.getOrCreateTagSet(tag, tagSetMap, tagSetIndexMap, tagIndexMap, path);
      if (s != null) {
         predicate.accept(s);
      }
   }

   private void consumeTag(@Nonnull String tag, @Nonnull T tagSet, @Nonnull Object2IntMap<String> tagIndexMap, @Nonnull IntConsumer predicate) {
      if (StringUtil.isGlobPattern(tag)) {
         ObjectIterator<Entry<String>> it = Object2IntMaps.fastIterator(tagIndexMap);

         while (it.hasNext()) {
            Entry<String> entry = (Entry<String>)it.next();
            if (StringUtil.isGlobMatching(tag, (String)entry.getKey())) {
               predicate.accept(entry.getIntValue());
            }
         }
      } else {
         int index = tagIndexMap.getOrDefault(tag, -1);
         if (index >= 0) {
            predicate.accept(index);
         } else {
            TagSetPlugin.get()
               .getLogger()
               .at(Level.WARNING)
               .log("Tag Set '%s' references '%s' which is not a pattern and does not otherwise exist", tagSet.getId(), tag);
         }
      }
   }

   @Nullable
   private IntSet getOrCreateTagSet(
      String identifier,
      @Nonnull Map<String, T> tagSetMap,
      @Nonnull Object2IntMap<String> tagSetIndexMap,
      @Nonnull Object2IntMap<String> tagIndexMap,
      @Nonnull IntArrayList path
   ) {
      int tagSetIndex = tagSetIndexMap.getOrDefault(identifier, -1);
      IntSet intSet = null;
      if (tagSetIndex >= 0 && this.tagMatcher.containsKey(tagSetIndex)) {
         if (path.contains(tagSetIndex)) {
            throw new IllegalStateException("Cyclic reference to set detected: " + identifier);
         }

         path.add(tagSetIndex);
         intSet = (IntSet)this.tagMatcher.get(tagSetIndex);
      } else {
         T set = tagSetMap.get(identifier);
         if (set != null) {
            intSet = this.createTagSet(set, tagSetMap, tagSetIndexMap, tagIndexMap, path);
         } else {
            TagSetPlugin.get()
               .getLogger()
               .at(Level.WARNING)
               .log("Creating tag sets: Tag Set '%s' does not exist, but is being referenced as a tag", identifier);
         }
      }

      path.removeInt(path.size() - 1);
      return intSet;
   }

   @Nonnull
   public Int2ObjectMap<IntSet> getFlattenedSet() {
      return this.tagMatcher;
   }
}
