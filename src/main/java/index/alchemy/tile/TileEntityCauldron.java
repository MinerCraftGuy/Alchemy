package index.alchemy.tile;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import index.alchemy.animation.StdCycle;
import index.alchemy.api.IFXUpdate;
import index.alchemy.api.annotation.FX;
import index.alchemy.client.color.ColorHelper;
import index.alchemy.client.fx.update.FXARGBIteratorUpdate;
import index.alchemy.client.fx.update.FXMotionUpdate;
import index.alchemy.client.fx.update.FXPosUpdate;
import index.alchemy.client.fx.update.FXScaleUpdate;
import index.alchemy.util.Always;
import index.alchemy.util.NBTHelper;
import index.alchemy.util.Tool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

@FX.UpdateProvider
public class TileEntityCauldron extends AlchemyTileEntity implements ITickable {
	
	public static final String FX_KEY_GATHER = "cauldron_gather";
	
	@FX.UpdateMethod(FX_KEY_GATHER)
	public static List<IFXUpdate> getFXUpdateGather(int[] args) {
		List<IFXUpdate> result = new LinkedList<IFXUpdate>();
		int i = 1, 
			max_age = Tool.getSafe(args, i++, 1),
			scale = Tool.getSafe(args, i++, 1);
		result.add(new FXPosUpdate(0, 0, -5));
		result.add(new FXMotionUpdate(
				new StdCycle().setLoop(true).setRotation(true).setLenght(max_age / 3).setMin(-0.5F).setMax(0.5F),
				new StdCycle().setLenght(max_age).setMax(-0.3F),
				new StdCycle().setLoop(true).setRotation(true).setLenght(max_age / 3).setNow(max_age / 6).setMin(-0.5F).setMax(0.5F)));
		result.add(new FXARGBIteratorUpdate(ColorHelper.ahsbStep(Color.RED, new Color(0x66, 0xCC, 0xFF, 0x22), max_age, true, true, false)));
		result.add(new FXScaleUpdate(new StdCycle().setMin(scale / 1000F).setMax(scale / 100F)));
		return result;
	}
	
	public static enum State { NULL, ALCHEMY, OVER }
	
	public static final int CONTAINER_MAX_ITEM = 6;
	
	protected static final String 
			NBT_KEY_CONTAINER = "container",
			NBT_KEY_STATE = "state",
			NBT_KEY_TIME = "time",
			NBT_KEY_LIQUID = "liquid",
			NBT_KEY_ALCHEMY = "alchemy";
	
	protected final LinkedList<ItemStack> container = new LinkedList<ItemStack>();
	protected volatile boolean flag;
	
	protected State state = State.NULL;
	protected int time;
	
	protected FluidTank tank = new FluidTank(Fluid.BUCKET_VOLUME) {
		
		{
			setTileEntity(TileEntityCauldron.this);
		}
		
		public Fluid getFluidType() {
			return getFluid() != null ? getFluid().getFluid() : null;
		}
		
		public void setFluid(FluidStack stack) {
			Fluid fluid = getFluidType();
			super.setFluid(stack);
			if (getFluidType() != fluid)
				update();
		};
		
		@Override
		public int fillInternal(FluidStack resource, boolean doFill) {
			Fluid fluid = getFluidType();
			int result = super.fillInternal(resource, doFill);
			if (getFluidType() != fluid)
				update();
			return result;
		}
		
		public FluidStack drainInternal(int maxDrain, boolean doDrain) {
			Fluid fluid = getFluidType();
			FluidStack result = super.drainInternal(maxDrain, doDrain);
			if (getFluidType() != fluid)
				update();
			return result;
		};
		
		public void update() {
			if (worldObj != null && pos != null) {
				worldObj.checkLight(pos);
				worldObj.updateComparatorOutputLevel(pos, Blocks.CAULDRON);
			}
		}
		
	};
	
	
	/*  alchemy: 
	 *  	Magic Solvent volume	 -> alchemy & 4
	 *  	Glow Stone volume		 -> alchemy >> 4 & 4
	 *  	Red Stone volume 		 -> alchemy >> 8 & 4
	 *  	Dragon's Breath volume	 -> alchemy >> 12 & 4
	 */
	private int alchemy;
	
	public LinkedList<ItemStack> getContainer() {
		return container;
	}
	
	public State getState() {
		return state;
	}
	
	public int getTime() {
		return time;
	}
	
	public void setTime(int time) {
		this.time = time;
	}
	
	public int getLevel() {
		FluidStack stack = tank.getFluid();
		if (stack != null)
			if (stack.getFluid() == FluidRegistry.WATER)
				return stack.amount / 333;
			else
				return -1;
		return 0;
	}
	
	public void setLevel(int level) {
		if (level == 0)
			tank.setFluid(null);
		else if (getLevel() > -1) {
			if (tank.getFluid() == null)
				tank.setFluid(new FluidStack(FluidRegistry.WATER, 0));
			tank.getFluid().amount = (int) (level / 3F * 1000);
		}
	}
	
	@Nullable
	public IBlockState getLiquid() {
		return tank.getFluid() == null ? null : tank.getFluid().getFluid().getBlock().getDefaultState();
	}
	
	public FluidTank getTank() {
		return tank;
	}
	
	public void setAlchemy(int alchemy) {
		this.alchemy = alchemy;
	}
	
	public int getAlchemy() {
		return alchemy;
	}

	@Override
	public void update() {
		if (Always.isServer()) {
			List<Entity> entitys = worldObj.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos));
			for (Entity entity : entitys) {
				if (entity instanceof EntityItem) {
					ItemStack item = ((EntityItem) entity).getEntityItem();
					for (ItemStack c_item : container) {
						if (c_item.getItem() == ((EntityItem) entity).getEntityItem().getItem() && c_item.stackSize < c_item.getMaxStackSize()) {
							flag = true;
							int change = Math.min(c_item.getMaxStackSize() - c_item.stackSize, item.stackSize);
							c_item.stackSize += change;
							item.stackSize -= change;
							if (item.stackSize <= 0)
								break;
						}
					}
					if (item.stackSize > 0 && container.size() < CONTAINER_MAX_ITEM) {
						flag = true;
						container.add(item.copy());
						item.stackSize = 0;
					}
				}
			}
		
			if (flag) {
				updateState();
				flag = false;
			}
		}
	}
	
	public void updateState() {
		updateTracker();
	}
	
	public void onBlockBreak() {
		for (ItemStack item : container)
			InventoryHelper.spawnItemStack(worldObj, pos.getX(), pos.getY(), pos.getZ(), item);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		container.clear();
		container.addAll(Arrays.asList(NBTHelper.getItemStacksFormNBTList(nbt.getTagList(NBT_KEY_CONTAINER, NBT.TAG_COMPOUND))));
		state = State.values()[nbt.getInteger(NBT_KEY_STATE)];
		time = nbt.getInteger(NBT_KEY_TIME);
		alchemy = nbt.getInteger(NBT_KEY_ALCHEMY);
		tank.readFromNBT(nbt);
		super.readFromNBT(nbt);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setTag(NBT_KEY_CONTAINER, NBTHelper.getNBTListFormItemStacks(container.toArray(new ItemStack[container.size()])));
		nbt.setInteger(NBT_KEY_STATE, state.ordinal());
		nbt.setInteger(NBT_KEY_TIME, time);
		nbt.setInteger(NBT_KEY_ALCHEMY, alchemy);
		tank.writeToNBT(nbt);
		return super.writeToNBT(nbt);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing == EnumFacing.UP ||
				super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing == EnumFacing.UP)
			return (T) tank;
		return super.getCapability(capability, facing);
	}

}
