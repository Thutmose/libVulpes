package zmaster587.libVulpes.block;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import zmaster587.libVulpes.LibVulpes;
import zmaster587.libVulpes.util.IAdjBlockUpdate;

public class BlockTile extends RotatableBlock {

	protected Class<? extends TileEntity> tileClass;
	protected int guiId;
	public static final PropertyBool STATE = PropertyBool.create("state");

	public BlockTile(Class<? extends TileEntity> tileClass, int guiId) {
		super(Material.ROCK);
		this.tileClass = tileClass;
		this.guiId = guiId;
		this.setDefaultState(this.blockState.getBaseState().withProperty(STATE, false));
	}

    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {FACING, STATE});
    }
	
    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state) {
    	return state.getValue(FACING).getIndex() | (state.getValue(STATE).booleanValue() ? 8 : 0);
    }
    
	public void setBlockState(World world, IBlockState state, BlockPos pos, boolean newState) {
		world.setBlockState(pos, state.withProperty(STATE, newState), 2);
	}
    
	@Override
	public boolean hasTileEntity(IBlockState state) { return true; }

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		try {
			return tileClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
		if(!world.isRemote)
			player.openGui(LibVulpes.instance, guiId, world, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		super.onNeighborChange(world, pos,
				neighbor);
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof IAdjBlockUpdate)
			((IAdjBlockUpdate)tile).onAdjacentBlockUpdated();

	}

	@Override
	public boolean isBlockNormalCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		TileEntity tile = world.getTileEntity(pos);

		//This code could use some optimization -Dark
		if (tile instanceof IInventory)
		{
			IInventory inventory = (IInventory)tile;
			for (int i1 = 0; i1 < inventory.getSizeInventory(); ++i1)
			{
				ItemStack itemstack = inventory.getStackInSlot(i1);

				if (itemstack != null)
				{
					float f = world.rand.nextFloat() * 0.8F + 0.1F;
					float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
					EntityItem entityitem;

					for (float f2 = world.rand.nextFloat() * 0.8F + 0.1F; itemstack.stackSize > 0; world.spawnEntityInWorld(entityitem))
					{
						int j1 = world.rand.nextInt(21) + 10;

						if (j1 > itemstack.stackSize)
						{
							j1 = itemstack.stackSize;
						}

						itemstack.stackSize -= j1;
						entityitem = new EntityItem(world, (double)((float)pos.getX() + f), (double)((float)pos.getY() + f1), (double)((float)pos.getZ() + f2), new ItemStack(itemstack.getItem(), j1, itemstack.getItemDamage()));
						float f3 = 0.05F;
						entityitem.motionX = (double)((float)world.rand.nextGaussian() * f3);
						entityitem.motionY = (double)((float)world.rand.nextGaussian() * f3 + 0.2F);
						entityitem.motionZ = (double)((float)world.rand.nextGaussian() * f3);

						if (itemstack.hasTagCompound())
						{
							entityitem.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
						}
					}
				}
			}

		}

		super.breakBlock(world, pos, state);
	}
}
