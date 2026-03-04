package com.hypixel.hytale.server.npc.asset.builder.validators;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StateStringValidator extends StringValidator {
   private String[] stateParts;
   private final boolean allowEmptyMain;
   private final boolean mainStateOnly;
   private final boolean allowNull;

   private StateStringValidator(boolean allowEmptyMain, boolean mainStateOnly, boolean allowNull) {
      this.allowEmptyMain = allowEmptyMain;
      this.mainStateOnly = mainStateOnly;
      this.allowNull = allowNull;
   }

   @Override
   public boolean test(@Nullable String value) {
      if (value == null) {
         return this.allowNull;
      } else if (value.isEmpty()) {
         return false;
      } else {
         this.stateParts = value.split("\\.");
         if (this.stateParts.length > 2) {
            return false;
         } else if (this.stateParts.length > 1 && this.mainStateOnly) {
            return false;
         } else if (this.stateParts.length > 1 && this.allowEmptyMain) {
            String statePart = this.stateParts[1];
            return statePart != null && !statePart.isEmpty();
         } else {
            return this.stateParts.length == 0 ? false : this.stateParts[0] != null && !this.stateParts[0].isEmpty();
         }
      }
   }

   @Nonnull
   @Override
   public String errorMessage(String value) {
      return String.format(
         "%s is not a valid format for a state string. May only contain one . separator and must not be empty.%s%s",
         value,
         this.allowEmptyMain ? "" : " Main state must not be empty.",
         this.mainStateOnly ? " Sub state must not be set." : ""
      );
   }

   @Nonnull
   @Override
   public String errorMessage(String value, String name) {
      return String.format(
         "Parameter %s, %s is not a valid format for a state string. May only contain one . separator and must not be empty.%s%s",
         name,
         value,
         this.allowEmptyMain ? "" : " Main state must not be empty.",
         this.mainStateOnly ? " Sub state must not be set." : ""
      );
   }

   public boolean hasMainState() {
      if (this.stateParts.length <= 0) {
         return false;
      } else {
         String statePart = this.stateParts[0];
         return statePart != null && !statePart.isEmpty();
      }
   }

   public boolean hasSubState() {
      return this.stateParts.length > 1 && this.stateParts[1] != null && !this.stateParts[1].isEmpty();
   }

   public String getMainState() {
      return this.stateParts[0];
   }

   public String getSubState() {
      return this.stateParts[1];
   }

   @Nonnull
   public static StateStringValidator get() {
      return new StateStringValidator(true, false, false);
   }

   @Nonnull
   public static StateStringValidator mainStateOnly() {
      return new StateStringValidator(false, true, false);
   }

   @Nonnull
   public static StateStringValidator requireMainState() {
      return new StateStringValidator(false, false, false);
   }

   @Nonnull
   public static StateStringValidator requireMainStateOrNull() {
      return new StateStringValidator(false, false, true);
   }
}
