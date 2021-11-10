package io.github.cottonmc.jankson;

import com.mojang.serialization.Codec;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonNull;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.provider.nbt.LootNbtProviderType;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.loot.provider.score.LootScoreProviderType;
import net.minecraft.particle.ParticleType;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.StatType;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.structure.rule.PosRuleTestType;
import net.minecraft.structure.rule.RuleTestType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.floatprovider.FloatProviderType;
import net.minecraft.util.math.intprovider.IntProviderType;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSourceType;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.size.FeatureSizeType;
import net.minecraft.world.gen.foliage.FoliagePlacerType;
import net.minecraft.world.gen.heightprovider.HeightProviderType;
import net.minecraft.world.gen.stateprovider.BlockStateProviderType;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;
import net.minecraft.world.gen.trunk.TrunkPlacerType;
import net.minecraft.world.poi.PointOfInterestType;

public class JanksonFactory {
	public static Jankson.Builder builder() {
		Jankson.Builder builder = Jankson.builder();
		
		builder
			.registerDeserializer(String.class, ItemStack.class, BlockAndItemSerializers::getItemStackPrimitive)
			.registerDeserializer(JsonObject.class, ItemStack.class, BlockAndItemSerializers::getItemStack)
			.registerSerializer(ItemStack.class, BlockAndItemSerializers::saveItemStack);
		
		builder
			.registerDeserializer(String.class, Block.class, BlockAndItemSerializers::getBlockPrimitive)
			.registerSerializer(Block.class, BlockAndItemSerializers::saveBlock);
				
		builder
			.registerDeserializer(String.class, BlockState.class, BlockAndItemSerializers::getBlockStatePrimitive)
			.registerDeserializer(JsonObject.class, BlockState.class, BlockAndItemSerializers::getBlockState)
			.registerSerializer(BlockState.class, BlockAndItemSerializers::saveBlockState);
		
		builder
			.registerDeserializer(String.class, Identifier.class,         (s,m)->new Identifier(s))
			.registerSerializer(Identifier.class, (i,m)->new JsonPrimitive(i.toString()))
			;
		
		//All the things you could potentially specify with just a registry ID
		register(builder, Activity.class,           Registry.ACTIVITY);
		register(builder, Biome.class,              BuiltinRegistries.BIOME);
		// register(builder, BiomeSourceType.class,    Registry.BIOME_SOURCE_TYPE);
		register(builder, BlockEntityType.class,    Registry.BLOCK_ENTITY_TYPE);
		register(builder, Carver.class,             Registry.CARVER);
		// register(builder, ChunkGeneratorType.class, Registry.CHUNK_GENERATOR);
		register(builder, ChunkStatus.class,        Registry.CHUNK_STATUS);
		register(builder, ScreenHandlerType.class,      Registry.SCREEN_HANDLER);
		register(builder, Decorator.class,          Registry.DECORATOR);
		// register(builder, DimensionType.class,      Registry.DIMENSION);
		
		builder
			.registerDeserializer(String.class, Activity.class,					(s,m)->Registry.ACTIVITY 				.get(new Identifier(s)))
			.registerDeserializer(String.class, Block.class,					(s,m)->Registry.BLOCK 					.get(new Identifier(s)))
			.registerDeserializer(String.class, BlockEntityType.class,			(s,m)->Registry.BLOCK_ENTITY_TYPE 		.get(new Identifier(s)))
			.registerDeserializer(String.class, BlockPredicateType.class,		(s,m)->Registry.BLOCK_PREDICATE_TYPE 	.get(new Identifier(s)))
			.registerDeserializer(String.class, BlockStateProviderType.class,	(s,m)->Registry.BLOCK_STATE_PROVIDER_TYPE.get(new Identifier(s)))
			.registerDeserializer(String.class, Carver.class,					(s,m)->Registry.CARVER 					.get(new Identifier(s)))
			.registerDeserializer(String.class, ChunkStatus.class,				(s,m)->Registry.CHUNK_STATUS 			.get(new Identifier(s)))
			.registerDeserializer(String.class, Codec.class,					(s,m)->Registry.BIOME_SOURCE 			.get(new Identifier(s)))
			.registerDeserializer(String.class, Codec.class,					(s,m)->Registry.CHUNK_GENERATOR 		.get(new Identifier(s)))
			.registerDeserializer(String.class, Codec.class,					(s,m)->Registry.MATERIAL_CONDITION		.get(new Identifier(s)))
			.registerDeserializer(String.class, Codec.class,					(s,m)->Registry.MATERIAL_RULE 			.get(new Identifier(s)))
			.registerDeserializer(String.class, Decorator.class,				(s,m)->Registry.DECORATOR 				.get(new Identifier(s)))
			.registerDeserializer(String.class, Enchantment.class,				(s,m)->Registry.ENCHANTMENT 			.get(new Identifier(s)))
			.registerDeserializer(String.class, EntityAttribute.class,			(s,m)->Registry.ATTRIBUTE 				.get(new Identifier(s)))
			.registerDeserializer(String.class, EntityType.class,				(s,m)->Registry.ENTITY_TYPE 			.get(new Identifier(s)))
			.registerDeserializer(String.class, Feature.class,					(s,m)->Registry.FEATURE 				.get(new Identifier(s)))
			.registerDeserializer(String.class, FeatureSizeType.class,			(s,m)->Registry.FEATURE_SIZE_TYPE 		.get(new Identifier(s)))
			.registerDeserializer(String.class, FloatProviderType.class,		(s,m)->Registry.FLOAT_PROVIDER_TYPE 	.get(new Identifier(s)))
			.registerDeserializer(String.class, Fluid.class,					(s,m)->Registry.FLUID 					.get(new Identifier(s)))
			.registerDeserializer(String.class, FoliagePlacerType.class,		(s,m)->Registry.FOLIAGE_PLACER_TYPE 	.get(new Identifier(s)))
			.registerDeserializer(String.class, GameEvent.class,				(s,m)->Registry.GAME_EVENT 				.get(new Identifier(s)))
			.registerDeserializer(String.class, HeightProviderType.class,		(s,m)->Registry.HEIGHT_PROVIDER_TYPE 	.get(new Identifier(s)))
			.registerDeserializer(String.class, Identifier.class,				(s,m)->Registry.CUSTOM_STAT 			.get(new Identifier(s)))
			.registerDeserializer(String.class, IntProviderType.class,			(s,m)->Registry.INT_PROVIDER_TYPE 		.get(new Identifier(s)))
			.registerDeserializer(String.class, Item.class,						(s,m)->Registry.ITEM 					.get(new Identifier(s))) //TODO: Support tags?
			.registerDeserializer(String.class, LootConditionType.class,		(s,m)->Registry.LOOT_CONDITION_TYPE 	.get(new Identifier(s)))
			.registerDeserializer(String.class, LootFunctionType.class,			(s,m)->Registry.LOOT_FUNCTION_TYPE 		.get(new Identifier(s)))
			.registerDeserializer(String.class, LootNbtProviderType.class,		(s,m)->Registry.LOOT_NBT_PROVIDER_TYPE 	.get(new Identifier(s)))
			.registerDeserializer(String.class, LootNumberProviderType.class,	(s,m)->Registry.LOOT_NUMBER_PROVIDER_TYPE.get(new Identifier(s)))
			.registerDeserializer(String.class, LootPoolEntryType.class,		(s,m)->Registry.LOOT_POOL_ENTRY_TYPE 	.get(new Identifier(s)))
			.registerDeserializer(String.class, LootScoreProviderType.class,	(s,m)->Registry.LOOT_SCORE_PROVIDER_TYPE.get(new Identifier(s)))
			.registerDeserializer(String.class, MemoryModuleType.class,			(s,m)->Registry.MEMORY_MODULE_TYPE 		.get(new Identifier(s)))
			.registerDeserializer(String.class, PaintingMotive.class,			(s,m)->Registry.PAINTING_MOTIVE 		.get(new Identifier(s))) //MOTIF. It's spelled "MOTIF".
			.registerDeserializer(String.class, ParticleType.class,				(s,m)->Registry.PARTICLE_TYPE 			.get(new Identifier(s)))
			.registerDeserializer(String.class, PointOfInterestType.class,		(s,m)->Registry.POINT_OF_INTEREST_TYPE 	.get(new Identifier(s)))
			.registerDeserializer(String.class, PositionSourceType.class,		(s,m)->Registry.POSITION_SOURCE_TYPE 	.get(new Identifier(s)))
			.registerDeserializer(String.class, PosRuleTestType.class,			(s,m)->Registry.POS_RULE_TEST 			.get(new Identifier(s)))
			.registerDeserializer(String.class, Potion.class,					(s,m)->Registry.POTION 					.get(new Identifier(s)))
			.registerDeserializer(String.class, RecipeSerializer.class,			(s,m)->Registry.RECIPE_SERIALIZER 		.get(new Identifier(s)))
			.registerDeserializer(String.class, RecipeType.class,				(s,m)->Registry.RECIPE_TYPE 			.get(new Identifier(s)))
			.registerDeserializer(String.class, RuleTestType.class,				(s,m)->Registry.RULE_TEST 				.get(new Identifier(s)))
			.registerDeserializer(String.class, Schedule.class,					(s,m)->Registry.SCHEDULE 				.get(new Identifier(s)))
			.registerDeserializer(String.class, ScreenHandlerType.class,		(s,m)->Registry.SCREEN_HANDLER 			.get(new Identifier(s)))
			.registerDeserializer(String.class, SensorType.class,				(s,m)->Registry.SENSOR_TYPE 			.get(new Identifier(s)))
			.registerDeserializer(String.class, SoundEvent.class,				(s,m)->Registry.SOUND_EVENT 			.get(new Identifier(s)))
			.registerDeserializer(String.class, StatType.class,					(s,m)->Registry.STAT_TYPE 				.get(new Identifier(s)))
			.registerDeserializer(String.class, StatusEffect.class,				(s,m)->Registry.STATUS_EFFECT 			.get(new Identifier(s)))
			.registerDeserializer(String.class, StructureFeature.class,			(s,m)->Registry.STRUCTURE_FEATURE 		.get(new Identifier(s)))
			.registerDeserializer(String.class, StructurePieceType.class,		(s,m)->Registry.STRUCTURE_PIECE 		.get(new Identifier(s)))
			.registerDeserializer(String.class, StructurePoolElementType.class,	(s,m)->Registry.STRUCTURE_POOL_ELEMENT 	.get(new Identifier(s)))
			.registerDeserializer(String.class, StructureProcessorType.class,	(s,m)->Registry.STRUCTURE_PROCESSOR 	.get(new Identifier(s)))
			.registerDeserializer(String.class, TreeDecoratorType.class,		(s,m)->Registry.TREE_DECORATOR_TYPE 	.get(new Identifier(s)))
			.registerDeserializer(String.class, TrunkPlacerType.class,			(s,m)->Registry.TRUNK_PLACER_TYPE 		.get(new Identifier(s)))
			.registerDeserializer(String.class, VillagerProfession.class,		(s,m)->Registry.VILLAGER_PROFESSION 	.get(new Identifier(s)))
			.registerDeserializer(String.class, VillagerType.class,				(s,m)->Registry.VILLAGER_TYPE 			.get(new Identifier(s)))
			.registerDeserializer(String.class, Registry.class, 				(s,m)->Registry.REGISTRIES				.get(new Identifier(s))) //You know you want to do it
			;
		
		builder
			.registerSerializer(Activity.class, (o,m)->lookupSerialize(o, Registry.ACTIVITY))
			
			;
		
		
		
		return builder;
	}
	
	private static <T> void register(Jankson.Builder builder, Class<T> clazz, Registry<? extends T> registry) {
		builder.registerDeserializer(String.class, clazz, (s,m)->lookupDeserialize(s, registry));
		builder.registerSerializer(clazz, (o,m)->lookupSerialize(o, registry));
	}
	
	private static <T> T lookupDeserialize(String s, Registry<T> registry) {
		return registry.get(new Identifier(s));
	}
	
	private static <T, U extends T> JsonElement lookupSerialize(T t, Registry<U> registry) {
		@SuppressWarnings("unchecked") //Widening cast happening because of generic type parameters in the registry class
		Identifier id = registry.getId((U)t);
		if (id==null) return JsonNull.INSTANCE;
		return new JsonPrimitive(id.toString());
	}
	
	
	public static Jankson createJankson() {
		return builder().build();
	}
	
}
