package index.alchemy.enchantment;

import index.alchemy.core.Init;
import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.PREINITIALIZED)
public class AlchemyEnchantmentLoader {
	
	public static final Enchantment
			siphon_life = new EnchantmentSiphonLife();
	
	public static void init() {}

}