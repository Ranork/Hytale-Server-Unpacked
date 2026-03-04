package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

public class NonEmptyStringValidator implements Validator<String> {
   public static final NonEmptyStringValidator INSTANCE = new NonEmptyStringValidator();
   private static final Pattern NON_WHITESPACE_PATTERN = Pattern.compile("[^\\s]");

   protected NonEmptyStringValidator() {
   }

   public void accept(@Nonnull String string, @Nonnull ValidationResults results) {
      if (string.isBlank()) {
         results.fail("String can't be empty!");
      }
   }

   @Override
   public void updateSchema(SchemaContext context, Schema target) {
      StringSchema s = (StringSchema)target;
      s.setMinLength(1);
      s.setPattern(NON_WHITESPACE_PATTERN);
   }
}
