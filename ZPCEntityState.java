package ZPCombatMod;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class ZPCEntityState implements IExtendedEntityProperties {

	public boolean isCruising = false;
	public double dirX;
	public double dirY;
	public double dirZ;
	
	@Override
	public void saveNBTData(NBTTagCompound compound) {
	}

	@Override
	public void loadNBTData(NBTTagCompound compound) {
	}

	@Override
	public void init(Entity entity, World world) {
	}

}
