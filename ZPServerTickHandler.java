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

public class ZPServerTickHandler implements ITickHandler {

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		if (type.equals(TickType.SERVER))
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
		else if (type.equals(TickType.PLAYER))
		{
			// onPlayerPreTick
			
			synchronized(ZPCombat.combatEvents)
			{
				EntityPlayer curPlayer = ((EntityPlayer)tickData[0]);
				
				List<ZPCombatEvent> curCEventList = ZPCombat.combatEvents.get(curPlayer);
				
				if (curCEventList != null)
				{
					for (ZPCombatEvent curCombatEvent : curCEventList)
					{
						switch (curCombatEvent.combatEventID)
						{
							case ZPCombatEvent.combatEvtID_JumpUp:
								curPlayer.motionY += 0.5d;
								//((EntityOtherPlayerMP)curPlayer).
								break;
							case ZPCombatEvent.combatEvtID_JumpFront:
								double rot = ZPCombatEvent.getRotationFromDirection(curCombatEvent.direction);
								double magX = -Math.sin(rot * Math.PI / 180.0d);
								double magZ = Math.cos(rot * Math.PI / 180.0d);
								curPlayer.motionX += magX;
								curPlayer.motionZ += magZ;
								break;
						}
					}
					
					curCEventList.clear();
				}
			}
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		if (type.equals(TickType.SERVER))
		{
			// onPostServerTick
		}
		else if (type.equals(TickType.PLAYER))
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
