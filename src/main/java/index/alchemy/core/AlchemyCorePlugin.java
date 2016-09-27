package index.alchemy.core;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

import static index.alchemy.core.AlchemyConstants.*;

@Name(MOD_ID)
@MCVersion(MC_VERSION)
@TransformerExclusions(MOD_TRANSFORMER_PACKAGE)
public class AlchemyCorePlugin implements IFMLLoadingPlugin {

	@Override
	public String[] getASMTransformerClass() {
		return new String[] {
				
		};
	}

	@Override
	public String getModContainerClass() {
		return "index.alchemy.core.AlchemyModContainer";
	}

	@Override
	public String getSetupClass() {
		return getModContainerClass();
	}

	@Override
	public void injectData(Map<String, Object> data) { }

	@Override
	public String getAccessTransformerClass() {
		return "index.alchemy.core.asm.transformer.AlchemyTransformerManager";
	}

}
