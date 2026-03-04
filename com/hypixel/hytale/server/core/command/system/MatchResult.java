package com.hypixel.hytale.server.core.command.system;

import com.hypixel.hytale.common.util.StringCompareUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MatchResult implements Comparable<MatchResult> {
   public static final MatchResult NONE = new MatchResult(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
   public static final MatchResult EXACT = new MatchResult(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
   public static final int NAME = 0;
   public static final int ALIAS = 1;
   public static final int USAGE_ARG = 3;
   public static final int DESCRIPTION = 4;
   public static final int USAGE_DESCRIPTION = 5;
   private final int term;
   private final int depth;
   private final int type;
   private final int match;

   @Nonnull
   public static MatchResult of(int termDepth, int depth, int type, @Nonnull String text, @Nonnull String search) {
      return new MatchResult(termDepth, depth, type, StringCompareUtil.getLevenshteinDistance(text, search));
   }

   public MatchResult(int term, int depth, int type, int match) {
      this.term = term;
      this.depth = depth;
      this.type = type;
      this.match = match;
   }

   public int getDepth() {
      return this.depth;
   }

   public int getType() {
      return this.type;
   }

   public int getMatch() {
      return this.match;
   }

   @Nonnull
   public MatchResult min(@Nonnull MatchResult other) {
      if (this.term < other.term) {
         return this;
      } else if (this.term > other.term) {
         return other;
      } else if (this.type < other.type) {
         return this;
      } else if (this.type > other.type) {
         return other;
      } else if (this.depth < other.depth) {
         return this;
      } else if (this.depth > other.depth) {
         return other;
      } else if (this.match < other.match) {
         return this;
      } else {
         return this.match > other.match ? other : this;
      }
   }

   public int compareTo(@Nonnull MatchResult o) {
      if (o.term != this.term) {
         return this.term - o.term;
      } else if (o.type != this.type) {
         return this.type - o.type;
      } else {
         return o.depth != this.depth ? this.depth - o.depth : this.match - o.match;
      }
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         MatchResult that = (MatchResult)o;
         return this.depth != that.depth ? false : this.match == that.match;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.depth;
      return 31 * result + this.match;
   }

   @Nonnull
   @Override
   public String toString() {
      return "MatchResult{depth=" + this.depth + ", match=" + this.match + "}";
   }
}
