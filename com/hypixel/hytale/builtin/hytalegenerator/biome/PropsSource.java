package com.hypixel.hytale.builtin.hytalegenerator.biome;

import com.hypixel.hytale.builtin.hytalegenerator.PropField;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.Assignments;
import java.util.List;

public interface PropsSource {
   List<PropField> getPropFields();

   List<Assignments> getAllPropDistributions();
}
