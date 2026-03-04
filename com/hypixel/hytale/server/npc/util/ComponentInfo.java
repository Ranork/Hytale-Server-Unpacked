package com.hypixel.hytale.server.npc.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class ComponentInfo {
   private final String name;
   private final int index;
   private final int nestingDepth;
   private final List<String> fields = new ObjectArrayList();

   public ComponentInfo(String name, int index, int nestingDepth) {
      this.name = name;
      this.index = index;
      this.nestingDepth = nestingDepth;
   }

   public void addField(String field) {
      this.fields.add(field);
   }

   @Nonnull
   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(" ".repeat(this.nestingDepth));
      if (this.index > -1) {
         sb.append("[").append(this.index).append("] ");
      }

      sb.append(this.name);
      String fieldIndent = " ".repeat(this.nestingDepth + 1);

      for (String field : this.fields) {
         sb.append('\n').append(fieldIndent).append(field);
      }

      return sb.toString();
   }

   public String getName() {
      return this.name;
   }

   public int getIndex() {
      return this.index;
   }

   @Nonnull
   public List<String> getFields() {
      return this.fields;
   }
}
