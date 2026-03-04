package com.hypixel.hytale.server.npc.asset.builder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface BuilderContext {
   BuilderContext getOwner();

   String getLabel();

   default void setCurrentStateName(String name) {
   }

   @Nullable
   default Builder<?> getParent() {
      BuilderContext owner = this.getOwner();
      return owner instanceof Builder ? (Builder)owner : (owner != null ? owner.getParent() : null);
   }

   default void getBreadCrumbs(@Nonnull StringBuilder stringBuilder) {
      BuilderContext owner = this.getOwner();
      if (owner != null) {
         owner.getBreadCrumbs(stringBuilder);
      }

      String label = this.getLabel();
      if (label != null && !label.isEmpty()) {
         if (!stringBuilder.isEmpty()) {
            stringBuilder.append('|');
         }

         stringBuilder.append(label);
      }
   }

   @Nonnull
   default String getBreadCrumbs() {
      StringBuilder stringBuilder = new StringBuilder(80);
      this.getBreadCrumbs(stringBuilder);
      return stringBuilder.toString();
   }
}
