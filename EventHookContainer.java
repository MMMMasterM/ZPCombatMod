package ZPCombatMod;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class EventHookContainer {
	public static boolean isZPCFall = false;
	
	@ForgeSubscribe
	public void onLivingAttack(LivingAttackEvent event)
	{
		if (isZPCFall)
			return;
		
		if (event.entityLiving instanceof EntityPlayer)
		{
			if (event.source.damageType == "player")
			{
				ZPCombatEvent newEvent;
				
				if (event.entityLiving.onGround)
				{
					newEvent = new ZPCombatEvent(ZPCombatEvent.combatEvtID_PunchUp);
				}
				else
				{
					newEvent = new ZPCombatEvent(ZPCombatEvent.combatEvtID_PunchDown);
					newEvent.impactX = (float)event.entityLiving.posX;
					newEvent.impactY = (float)event.entityLiving.posY;
					newEvent.impactZ = (float)event.entityLiving.posZ;
				}
				
				synchronized(ZPCombat.combatEventsServer)
				{
					List<ZPCombatEvent> eventList = ZPCombat.combatEventsServer.get(event.entityLiving);
					
					if (eventList == null)
					{
						eventList = new ArrayList<ZPCombatEvent>();
						
						ZPCombat.combatEventsServer.put((EntityPlayer)event.entityLiving, eventList);
					}
					
					eventList.add(newEvent);
					((WorldServer)event.entityLiving.worldObj).getEntityTracker().sendPacketToAllAssociatedPlayers(event.source.getEntity(), new ZPCombatMoveAsyncPacketStoC(event.entityLiving.entityId, newEvent));
				}
				
				event.setCanceled(true);
			}
			else if (event.source.damageType == "fall")
			{
				event.setCanceled(true);
			}
		}
	}
	
	@ForgeSubscribe
	public void onRenderPlayer(RenderPlayerEvent.Pre event)
	{
		ZPCEntityState curEntityState = (ZPCEntityState)event.entityPlayer.getExtendedProperties("zpcState");
		if (curEntityState != null && curEntityState.isCruising)
		{
			event.entityPlayer.renderYawOffset = event.entityPlayer.rotationYaw;
		}
	}
}
