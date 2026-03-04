package com.hypixel.hytale.server.core.meta;

import com.hypixel.hytale.codec.ExtraInfo;
import java.util.function.BiConsumer;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public interface IMetaStoreImpl<K> extends IMetaStore<K> {
   IMetaRegistry<K> getRegistry();

   void decode(BsonDocument var1, ExtraInfo var2);

   BsonDocument encode(ExtraInfo var1);

   void forEachUnknownEntry(BiConsumer<String, BsonValue> var1);
}
