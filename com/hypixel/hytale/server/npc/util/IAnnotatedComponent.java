package com.hypixel.hytale.server.npc.util;

import com.hypixel.hytale.server.npc.role.Role;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IAnnotatedComponent {
   void getInfo(Role var1, ComponentInfo var2);

   void setContext(IAnnotatedComponent var1, int var2);

   @Nullable
   IAnnotatedComponent getParent();

   int getIndex();

   default String getLabel() {
      int index = this.getIndex();
      return index >= 0 ? String.format("[%s]%s", index, this.getClass().getSimpleName()) : this.getClass().getSimpleName();
   }

   default void getBreadCrumbs(@Nonnull StringBuilder sb) {
      IAnnotatedComponent parent = this.getParent();
      if (parent != null) {
         parent.getBreadCrumbs(sb);
      }

      String label = this.getLabel();
      if (label != null && !label.isEmpty()) {
         if (!sb.isEmpty()) {
            sb.append('|');
         }

         sb.append(label);
      }
   }

   @Nonnull
   default String getBreadCrumbs() {
      StringBuilder sb = new StringBuilder();
      this.getBreadCrumbs(sb);
      return sb.toString();
   }
}
