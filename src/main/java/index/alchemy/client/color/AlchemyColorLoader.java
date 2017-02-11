package index.alchemy.client.color;

import java.util.List;

import com.google.common.collect.Lists;

import index.alchemy.api.IColorBlock;
import index.alchemy.api.IColorItem;
import index.alchemy.api.annotation.Init;
import index.alchemy.core.AlchemyInitHook;
import index.alchemy.core.AlchemyModLoader;
import index.project.version.annotation.Omega;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Omega
@SideOnly(Side.CLIENT)
@Init(state = ModState.POSTINITIALIZED)
public class AlchemyColorLoader {
	
	protected static final List item_color = Lists.newLinkedList(), block_color = Lists.newLinkedList();
	
	public static <T extends Item & IColorItem> void addItemColor(T t) {
		AlchemyModLoader.checkState();
		item_color.add(t);
	}
	
	public static <T extends Block & IColorBlock> void addBlockColor(T t) {
		AlchemyModLoader.checkState();
		block_color.add(t);
	}
	
	private static <T extends Item & IColorItem> void registerItemColor() {
		ItemColors colors = Minecraft.getMinecraft().getItemColors();
		for (T t : (List<T>) item_color) {
			colors.registerItemColorHandler(t.getItemColor(), t);
			AlchemyInitHook.push_event(t);
		}
	}
	
	private static <T extends Block & IColorBlock> void registerBlockColor() {
		BlockColors colors = Minecraft.getMinecraft().getBlockColors();
		for (T t : (List<T>) block_color) {
			colors.registerBlockColorHandler(t.getBlockColor(), t);
			AlchemyInitHook.push_event(t);
		}
	}
	
	public static void init() {
		AlchemyModLoader.checkInvokePermissions();
		AlchemyModLoader.checkState();
		registerItemColor();
		registerBlockColor();
	}
	
}
