package index.alchemy.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import index.alchemy.api.ILocationProvider;
import index.alchemy.api.IMaterialConsumer;
import index.alchemy.core.AlchemyConstants;
import index.project.version.annotation.Beta;
import index.project.version.annotation.Omega;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.IFuelHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.server.FMLServerHandler;
import net.minecraftforge.oredict.OreDictionary;

import static java.lang.Math.*;

import java.io.File;

@Omega
public class Always {
	
	public static int maxHeight = 256;
	
	private static boolean isClient = Tool.forName("net.minecraft.client.Minecraft", false) != null;
	
	public static final Map<Thread, Side> SIDE_MAPPING = new HashMap<Thread, Side>();
	
	public static final boolean isAlchemyModLoaded() {
		return Loader.isModLoaded(AlchemyConstants.MOD_ID);
	}
	
	@SideOnly(Side.CLIENT)
	public static final boolean isPlaying() {
		return Minecraft.getMinecraft().thePlayer != null;
	}
	
	@SideOnly(Side.CLIENT)
	public static final long getClientWorldTime() {
		return Minecraft.getMinecraft().theWorld.getWorldTime();
	}
	
	public static final boolean runOnClient() {
		return isClient;
	}
	
	public static final void markSide(Side side) {
		SIDE_MAPPING.put(Thread.currentThread(), side);
	}
	
	public static final Side getSide() {
		Side side = SIDE_MAPPING.get(Thread.currentThread());
		if (side == null)
			SIDE_MAPPING.put(Thread.currentThread(), side = FMLCommonHandler.instance().getEffectiveSide());
		return side;
	}
	
	@Nullable
	public static final UUID getUUIDFromPlayerName(String name) {
		return UsernameCache.getMap()
				.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey))
				.get(name);
	}
	
	@Nullable
	public static final File getWorldDirectory() {
		if (DimensionManager.getWorld(0) != null)
			return DimensionManager.getWorld(0).getSaveHandler().getWorldDirectory();
		else if (FMLServerHandler.instance().getServer() != null) {
			MinecraftServer server = FMLServerHandler.instance().getServer();
			return server.getActiveAnvilConverter().getSaveLoader(server.getFolderName(), false).getWorldDirectory();
		} else
			return null;
	}
	
	public static final boolean isServer() {
		return getSide().isServer();
	}
	
	public static final boolean isClient() {
		return getSide().isClient();
	}
	
	public static final ItemStack getEnchantmentBook(Enchantment enchantment) {
		ItemStack book = new ItemStack(Items.BOOK);
		Items.ENCHANTED_BOOK.addEnchantment(book, new EnchantmentData(enchantment, 1));
		return book;
	}
	
	public static final double calculateTheStraightLineDistance(double x, double y, double z) {
		return x * x + y * y + z * z;
	}
	
	@Nullable
	@SideOnly(Side.CLIENT)
	public static final Entity findEntityFormClientWorld(int id) {
		World world = Minecraft.getMinecraft().theWorld;
		if (world != null)
			return world.getEntityByID(id);
		return null;
	}
	
	public static final Biome getCurrentBiome(EntityPlayer player) {
		return getCurrentBiome(player.worldObj, (int) player.posX, (int) player.posZ);
	}
	
	public static final Biome getCurrentBiome(World world, int x, int z) {
		return world.getBiomeForCoordsBody(new BlockPos(x, 0, z));
	}
	
	public static final IFuelHandler getFuelHandler(ItemStack item, int time) {
		return new IFuelHandler() {
			@Override
			public int getBurnTime(ItemStack fuel) {
				return ItemStack.areItemsEqual(fuel, item) ? time : 0;
			}
		};
	}
	
	public static final List<IFuelHandler> getFuelHandlers(String material_str, int time) {
		List<IFuelHandler> result = new LinkedList<IFuelHandler>();
		for (ItemStack material : OreDictionary.getOres(material_str))
			result.add(getFuelHandler(material, time));
		return result;
	}
	
	public static final ILocationProvider generateLocationProvider(Entity entity, double offsetY) {
		return new ILocationProvider() {
			@Override
			public Vec3d getLocation() {
				AxisAlignedBB aabb = entity.getEntityBoundingBox();
				return entity.getPositionVector()
						.addVector((aabb.maxX - aabb.minX) / 2, offsetY * (aabb.maxY - aabb.minY) / 2, (aabb.maxZ - aabb.minZ) / 2);
			}
		};
	}
	
	public static final ILocationProvider generateLocationProvider(BlockPos pos) {
		Vec3d vec3d = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
		return new ILocationProvider() {
			@Override
			public Vec3d getLocation() {
				return vec3d;
			}
		};
	}
	
	@Beta
	public static final List<IMaterialConsumer> generateMaterialConsumers(Object... args) {
		Tool.checkNull(args);
		List<IMaterialConsumer> result = new LinkedList<>();
		for (int i = 0, len = args.length + 1; i < len; i++) {
			Object last = i > 0 ? args[i - 1] : null, obj = i == args.length ? null : args[i];
			if (last != null && !(last instanceof ItemStack || last instanceof Number)) {
				if (last instanceof Item)
					result.add(generateMaterialConsumer(new ItemStack((Item) last, obj instanceof Number ?
							((Number) obj).intValue() : 1)));
				else if (last instanceof Block)
					result.add(generateMaterialConsumer(new ItemStack((Block) last, obj instanceof Number ?
							((Number) obj).intValue() : 1)));
				else if (last instanceof String)
					result.add(generateMaterialConsumer((String) last, obj instanceof Number ?
							((Number) obj).intValue() : 1));
				else
					throw new IllegalArgumentException("Type mismatch, type: " + last.getClass().getName() + " , index: " + (i - 1));
			}
			if (obj != null && obj instanceof ItemStack)
				result.add(generateMaterialConsumer((ItemStack) obj));
		}
		return result;
	}
	
	public static final IMaterialConsumer generateMaterialConsumer(ItemStack material) {
		return new IMaterialConsumer() {
			@Override
			public boolean treatmentMaterial(List<ItemStack> items) {
				int need = material.stackSize;
				for (Iterator<ItemStack> iterator = items.iterator(); iterator.hasNext();) {
					ItemStack item = iterator.next();
					if (item.isItemEqualIgnoreDurability(material)) {
						int change = min(need, item.stackSize);
						need -= change;
						item.stackSize -= change;
						if (item.stackSize == 0)
							iterator.remove();
						if (need < 1)
							return true;
					}
				}
				return false;
			}
		};
	}
	
	public static final IMaterialConsumer generateMaterialConsumer(String material_str, int size) {
		return new IMaterialConsumer() {
			@Override
			public boolean treatmentMaterial(List<ItemStack> items) {
				int need = size;
				for (Iterator<ItemStack> iterator = items.iterator(); iterator.hasNext();) {
					ItemStack item = iterator.next();
					for (ItemStack material : OreDictionary.getOres(material_str))
						if (item.isItemEqualIgnoreDurability(material)) {
							int change = min(need, item.stackSize);
							need -= change;
							item.stackSize -= change;
							if (item.stackSize == 0)
								iterator.remove();
							if (need < 1)
								return true;
							break;
						}
				}
				return false;
			}
		};
	}
	
}