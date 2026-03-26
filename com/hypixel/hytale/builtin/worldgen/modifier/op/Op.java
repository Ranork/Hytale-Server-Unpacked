package com.hypixel.hytale.builtin.worldgen.modifier.op;

import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import javax.annotation.Nonnull;

public interface Op {
   Op[] EMPTY_ARRAY = new Op[0];
   String TYPE_KEY = "Operation";
   CodecMapCodec<Op> TYPE_CODEC = new CodecMapCodec<>("Operation", true);
   ArrayCodec<Op> ARRAY_CODEC = new ArrayCodec<>(TYPE_CODEC, Op[]::new);

   <T> void apply(@Nonnull ModifyEvent<T> var1) throws Error;
}
