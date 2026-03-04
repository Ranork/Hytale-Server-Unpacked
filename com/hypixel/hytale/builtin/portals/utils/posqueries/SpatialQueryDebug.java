package com.hypixel.hytale.builtin.portals.utils.posqueries;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import java.util.Stack;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class SpatialQueryDebug {
   @Nonnull
   private final StringBuilder builder = new StringBuilder();
   @Nonnull
   private String indent = "";
   @Nonnull
   private final Stack<String> scope = new Stack<>();

   public SpatialQueryDebug() {
      this.appendLine("SPATIAL QUERY DEBUG");
   }

   @Nonnull
   public SpatialQueryDebug appendLine(@Nonnull String string) {
      HytaleLogger.getLogger().at(Level.INFO).log(this.indent + "| " + string);
      return this;
   }

   @Nonnull
   public SpatialQueryDebug indent(@Nonnull String scopeReason) {
      HytaleLogger.getLogger().at(Level.INFO).log(this.indent + "⮑ " + scopeReason);
      this.indent = this.indent + "  ";
      this.scope.add(scopeReason);
      return this;
   }

   @Nonnull
   public SpatialQueryDebug unindent() {
      if (this.indent.length() >= 2) {
         this.indent = this.indent.substring(0, this.indent.length() - 2);
      }

      if (!this.scope.isEmpty()) {
         String scopeReason = this.scope.pop();
         HytaleLogger.getLogger().at(Level.INFO).log(this.indent + "⮐ (DONE) " + scopeReason);
      }

      return this;
   }

   @Nonnull
   public static String fmt(@Nonnull Vector3d point) {
      return "(" + String.format("%.1f", point.x) + ", " + String.format("%.1f", point.y) + ", " + String.format("%.1f", point.z) + ")";
   }

   @Nonnull
   @Override
   public String toString() {
      return this.builder.toString();
   }
}
