package cofh.core.world.feature;

import cofh.api.world.IFeatureGenerator;
import cofh.api.world.IFeatureParser;
import cofh.core.world.FeatureParser;
import cofh.lib.util.WeightedRandomBlock;
import cofh.lib.util.helpers.MathHelper;
import cofh.lib.world.feature.FeatureBase;
import cofh.lib.world.feature.FeatureBase.GenRestriction;
import cofh.lib.world.feature.FeatureGenUniform;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

public class UniformParser implements IFeatureParser {

	protected final List<WeightedRandomBlock> defaultMaterial;

	public UniformParser() {

		defaultMaterial = generateDefaultMaterial();
	}

	protected List<WeightedRandomBlock> generateDefaultMaterial() {

		return Arrays.asList(new WeightedRandomBlock(Blocks.STONE, -1));
	}

	@Override
	public IFeatureGenerator parseFeature(String featureName, JsonObject genObject, Logger log) {

		int numClusters = 0;
		if (genObject.has("clusterCount")) {
			numClusters = genObject.get("clusterCount").getAsInt();
		}
		if (numClusters <= 0) {
			log.error("Invalid cluster count specified in '%s'", featureName);
			return null;
		}
		boolean retrogen = false;
		if (genObject.has("retrogen")) {
			retrogen = genObject.get("retrogen").getAsBoolean();
		}
		GenRestriction biomeRes = GenRestriction.NONE;
		if (genObject.has("biomeRestriction")) {
			biomeRes = GenRestriction.get(genObject.get("biomeRestriction").getAsString());
		}
		GenRestriction dimRes = GenRestriction.NONE;
		if (genObject.has("dimensionRestriction")) {
			dimRes = GenRestriction.get(genObject.get("dimensionRestriction").getAsString());
		}

		WorldGenerator generator = FeatureParser.parseGenerator(getDefaultGenerator(), genObject, defaultMaterial);
		if (generator == null) {
			log.warn("Invalid generator for '%s'!", featureName);
			return null;
		}
		FeatureBase feature = getFeature(featureName, genObject, generator, numClusters, biomeRes, retrogen, dimRes, log);

		if (feature != null) {
			if (genObject.has("chunkChance")) {
				int rarity = MathHelper.clamp(genObject.get("chunkChance").getAsInt(), 1, 1000000);
				feature.setRarity(rarity);
			}
			addFeatureRestrictions(feature, genObject);
		}
		return feature;
	}

	protected FeatureBase getFeature(String featureName, JsonObject genObject, WorldGenerator gen, int numClusters, GenRestriction biomeRes, boolean retrogen, GenRestriction dimRes, Logger log) {

		if (!(genObject.has("minHeight") && genObject.has("maxHeight"))) {
			log.error("Height parameters for 'uniform' template not specified in \"" + featureName + "\"");
			return null;
		}

		int minHeight = genObject.get("minHeight").getAsInt();
		int maxHeight = genObject.get("maxHeight").getAsInt();

		if (minHeight >= maxHeight || minHeight < 0) {
			log.error("Invalid height parameters specified in \"" + featureName + "\"");
			return null;
		}
		return new FeatureGenUniform(featureName, gen, numClusters, minHeight, maxHeight, biomeRes, retrogen, dimRes);
	}

	protected String getDefaultGenerator() {

		return "cluster";
	}

	protected static boolean addFeatureRestrictions(FeatureBase feature, JsonObject genObject) {

		if (feature.biomeRestriction != GenRestriction.NONE) {
			feature.addBiomes(FeatureParser.parseBiomeRestrictions(genObject));
		}
		if (feature.dimensionRestriction != GenRestriction.NONE && genObject.has("dimensions")) {
			JsonArray restrictionList = genObject.getAsJsonArray("dimensions");
			for (int i = 0; i < restrictionList.size(); i++) {
				feature.addDimension(restrictionList.get(i).getAsInt());
			}
		}
		return true;
	}

}
