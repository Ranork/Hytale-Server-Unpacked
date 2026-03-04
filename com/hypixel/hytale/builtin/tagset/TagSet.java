package com.hypixel.hytale.builtin.tagset;

import com.hypixel.hytale.assetstore.JsonAsset;

public interface TagSet extends JsonAsset<String> {
   String[] getIncludedTagSets();

   String[] getExcludedTagSets();

   String[] getIncludedTags();

   String[] getExcludedTags();
}
