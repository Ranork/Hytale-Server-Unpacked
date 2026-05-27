package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

public class ServerLocKeyValidator implements Validator<String> {
   public static final ServerLocKeyValidator INSTANCE = new ServerLocKeyValidator();
   private static final String SERVER_LOC_KEY_PREFIX = "server.";
   private static final Pattern SERVER_LOC_KEY_PATTERN = Pattern.compile("^server.*");

   public void accept(@Nonnull String s, @Nonnull ValidationResults results) {
      if (!s.startsWith("server.")) {
         results.fail("Description must be a localization key starting with 'server.', got: " + s);
      }
   }

   @Override
   public void updateSchema(SchemaContext context, Schema target) {
      StringSchema s = (StringSchema)target;
      s.setPattern(SERVER_LOC_KEY_PATTERN);
   }
}
