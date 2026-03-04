package com.hypixel.hytale.builtin.instances;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import javax.annotation.Nonnull;

public class InstanceValidator implements Validator<String> {
   @Nonnull
   public static final InstanceValidator INSTANCE = new InstanceValidator();
   @Nonnull
   public static final String CUSTOM_ASSET_NAME = "Instance";

   public void accept(@Nonnull String s, @Nonnull ValidationResults results) {
      if (!InstancesPlugin.doesInstanceAssetExist(s)) {
         results.fail("Instance asset with name '" + s + "' does not exist");
      }
   }

   @Override
   public void updateSchema(SchemaContext context, @Nonnull Schema target) {
      target.setHytaleCustomAssetRef("Instance");
   }
}
