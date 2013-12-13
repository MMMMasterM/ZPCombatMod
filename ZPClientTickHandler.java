package ZPCombatMod;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class ZPClientTickHandler implements ITickHandler {

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
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

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		// onPlayerPostTick
		
		EntityPlayer curPlayer = ((EntityPlayer)tickData[0]);
		if (curPlayer == Minecraft.getMinecraft().thePlayer)
		{
			ZPCombat.thisPlayerWasSprinting = ((EntityPlayer)tickData[0]).isSprinting();
		}
		else
		{
			boolean oldNoClip = curPlayer.noClip;
			curPlayer.noClip = false;
			curPlayer.moveEntityWithHeading(0, 0);
			curPlayer.noClip = oldNoClip;
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.PLAYER);
	}

	@Override
	public String getLabel() {
		return "ZPClientTickHandler";
	}

}
