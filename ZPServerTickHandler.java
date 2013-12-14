package ZPCombatMod;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;

public class ZPServerTickHandler implements ITickHandler {

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		if (type.contains(TickType.SERVER))
		{
			// onPreServerTick
			
			synchronized(ZPCombat.threadSafePlayerListPerWorld)
			{
				//Yes, we're creating new collections instead of reusing the old ones for now!
				ZPCombat.threadSafePlayerListPerWorld.clear();
				
				for (WorldServer curWorld : DimensionManager.getWorlds())
				{
					ZPCombat.threadSafePlayerListPerWorld.put(curWorld, new ArrayList(curWorld.playerEntities));
				}
			}
		}
		else if (type.contains(TickType.PLAYER))
		{
			// onPlayerPreTick
			
			synchronized(ZPCombat.combatEventsServer)
			{
				EntityPlayer curPlayer = ((EntityPlayer)tickData[0]);
				
				List<ZPCombatEvent> curCEventList = ZPCombat.combatEventsServer.get(curPlayer);
				
				if (curCEventList != null)
				{
					for (ZPCombatEvent curCombatEvent : curCEventList)
						curCombatEvent.applyToPlayer(curPlayer, Side.SERVER);
					
					//curPlayer.moveEntityWithHeading(0, 0);
					//curPlayer.moveEntityWithHeading(0, 0);
					
					curCEventList.clear();
				}
			}
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		if (type.contains(TickType.SERVER))
		{
			// onPostServerTick
		}
		else if (type.contains(TickType.PLAYER))
		{
			// onPlayerPostTick
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.SERVER, TickType.PLAYER);
	}

	@Override
	public String getLabel() {
		return "ZPServerTickHandler";
	}

}
