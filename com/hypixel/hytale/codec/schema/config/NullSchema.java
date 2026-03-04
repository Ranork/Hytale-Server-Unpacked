package com.hypixel.hytale.codec.schema.config;

import com.hypixel.hytale.codec.builder.BuilderCodec;

public class NullSchema extends Schema {
   public static final BuilderCodec<NullSchema> CODEC = BuilderCodec.builder(NullSchema.class, NullSchema::new, Schema.BASE_CODEC).build();
   public static final NullSchema INSTANCE = new NullSchema();
}
