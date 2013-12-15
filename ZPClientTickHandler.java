package ZPCombatMod;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;

public class ZPClientTickHandler implements ITickHandler {

	public static boolean thisPlayerWasOnGround = true;
	public static double thisPlayerOldY;
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		// onPlayerPreTick
		
		EntityPlayer curPlayer = ((EntityPlayer)tickData[0]);
		
		synchronized(ZPCombat.combatEventsClient)
		{
			List<ZPCombatEvent> curCEventList = ZPCombat.combatEventsClient.get(curPlayer);
			
			if (curCEventList != null)
			{
				for (ZPCombatEvent curCombatEvent : curCEventList)
					curCombatEvent.applyToPlayer(curPlayer, Side.CLIENT);
				
				curCEventList.clear();
			}
		}
		
		if (curPlayer == Minecraft.getMinecraft().thePlayer)
		{
			//Send motion to server
			PacketDispatcher.sendPacketToServer(new PacketSyncVelUpdateCtoS(curPlayer.motionX, curPlayer.motionY, curPlayer.motionZ).makePacket());
			
			thisPlayerWasOnGround = curPlayer.onGround;
			thisPlayerOldY = curPlayer.boundingBox.minY;
		}
		else
		{
			EntityOtherPlayerMP curOtherPlayer = (EntityOtherPlayerMP)curPlayer;
			
			try 
			{
				int otherPlayerMPPosRotationIncrements = ZPCombat.otherPlayerMPPosRotationIncrementsField.getInt(curOtherPlayer);
				if (otherPlayerMPPosRotationIncrements > 0)
				{
					double dX = ZPCombat.otherPlayerMPXField.getDouble(curOtherPlayer) - curOtherPlayer.posX;
					double dY = ZPCombat.otherPlayerMPYField.getDouble(curOtherPlayer) - curOtherPlayer.posY;
					double dZ = ZPCombat.otherPlayerMPZField.getDouble(curOtherPlayer) - curOtherPlayer.posZ;
					
					//dirty workaround
					boolean isInAir = curOtherPlayer.motionY > -0.05d || curOtherPlayer.motionY < -0.08d;
					
					/*if (Math.min(Math.abs(dY), Math.abs(dY + curOtherPlayer.motionY)) < 0.5d && Math.abs(curOtherPlayer.motionY) > 0.08d)// && isInAir)
					{
						ZPCombat.otherPlayerMPYField.setDouble(curOtherPlayer, curOtherPlayer.posY);
					}*/
					
					double distSqXZ = dX * dX + dZ * dZ;
					
					if (!curOtherPlayer.onGround)///*distSqXZ + */dY * dY <= 5.0d)
					{
						//ZPCombat.otherPlayerMPXField.setDouble(curOtherPlayer, curOtherPlayer.posX);
						ZPCombat.otherPlayerMPYField.setDouble(curOtherPlayer, curOtherPlayer.posY);
						//ZPCombat.otherPlayerMPZField.setDouble(curOtherPlayer, curOtherPlayer.posZ);
						//ZPCombat.otherPlayerMPPosRotationIncrementsField.setInt(curOtherPlayer, 0);
					}
					else
					{
						if (dY != 0.0d)
							ZPCombat.otherPlayerMPPosRotationIncrementsField.setInt(curOtherPlayer, 1);
					}
				}
				
				
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		ZPCEntityState entityState = (ZPCEntityState)curPlayer.getExtendedProperties("zpcState");
		if (entityState != null && entityState.isCruising)
		{
			if (curPlayer == Minecraft.getMinecraft().thePlayer)
			{
				synchronized(ZPCombat.combatEventsClient)
				{
					List<ZPCombatEvent> eventList = ZPCombat.combatEventsClient.get(curPlayer);
					
					if (eventList == null)
					{
						eventList = new ArrayList<ZPCombatEvent>();
						
						ZPCombat.combatEventsClient.put(curPlayer, eventList);
					}
					
					ZPCombatEvent newEvent = new ZPCombatEvent(ZPCombatEvent.combatEvtID_Cruise);
					newEvent.direction = ZPCombatEvent.getDirectionFromRotation(curPlayer.rotationYaw);
					newEvent.pitch = ZPCombatEvent.getPitchFromRotation(curPlayer.rotationPitch);
					eventList.add(newEvent);
					((EntityClientPlayerMP)curPlayer).sendQueue.addToSendQueue(new ZPCombatMoveAsyncPacketCtoS(newEvent));
				}
			}
			
			ZPCombatEvent.updateCruising(curPlayer, entityState);
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		// onPlayerPostTick
		
		EntityPlayer curPlayer = ((EntityPlayer)tickData[0]);
		if (curPlayer == Minecraft.getMinecraft().thePlayer)
		{
			ZPCombat.thisPlayerWasSprinting = ((EntityPlayer)tickData[0]).isSprinting();
			
			if (!thisPlayerWasOnGround && curPlayer.onGround)
			{
				//Impact event
				//synchronized(ZPCombat.combatEventsClient)
				{
					EntityClientPlayerMP thisPlayer = (EntityClientPlayerMP)curPlayer;
					/*List<ZPCombatEvent> eventList = ZPCombat.combatEventsClient.get(thisPlayer);
					
					if (eventList == null)
					{
						eventList = new ArrayList<ZPCombatEvent>();
						
						ZPCombat.combatEventsClient.put(thisPlayer, eventList);
					}
					
					if (ZPCombat.thisPlayerWasSprinting)
					{
						ZPCombatEvent newEvent = new ZPCombatEvent(ZPCombatEvent.combatEvtID_JumpFront);
						newEvent.direction = ZPCombatEvent.getDirectionFromRotation(thisPlayer.rotationYaw);
						eventList.add(newEvent);
						thisPlayer.sendQueue.addToSendQueue(new ZPCombatMoveAsyncPacketCtoS(newEvent));
					}
					else
					{
						ZPCombatEvent newEvent = new ZPCombatEvent(ZPCombatEvent.combatEvtID_JumpUp);
						eventList.add(newEvent);
						thisPlayer.sendQueue.addToSendQueue(new ZPCombatMoveAsyncPacketCtoS(newEvent));
					}*/
					
					ZPCombatEvent newEvent = new ZPCombatEvent(ZPCombatEvent.combatEvtID_Impact);
					newEvent.impactX = (float)thisPlayer.posX;
					newEvent.impactY = (float)thisPlayer.boundingBox.minY;
					newEvent.impactZ = (float)thisPlayer.posZ;
					thisPlayer.sendQueue.addToSendQueue(new ZPCombatMoveAsyncPacketCtoS(newEvent));
				}
			}
			else if (thisPlayerWasOnGround && !curPlayer.onGround)
			{
				ZPCombatEvent newEvent = new ZPCombatEvent(ZPCombatEvent.combatEvtID_Liftoff);
				((EntityClientPlayerMP)curPlayer).sendQueue.addToSendQueue(new ZPCombatMoveAsyncPacketCtoS(newEvent));
			}
			/*else if (thisPlayerWasOnGround && curPlayer.onGround && thisPlayerOldY != curPlayer.boundingBox.minY)
			{
				EntityClientPlayerMP thisPlayer = (EntityClientPlayerMP)curPlayer;
				ZPCombatEvent newEvent = new ZPCombatEvent(ZPCombatEvent.combatEvtID_Impact);
				newEvent.impactX = (float)thisPlayer.posX;
				newEvent.impactY = (float)thisPlayer.boundingBox.minY;
				newEvent.impactZ = (float)thisPlayer.posZ;
				thisPlayer.sendQueue.addToSendQueue(new ZPCombatMoveAsyncPacketCtoS(newEvent));*/
				/*
				ZPCombatEvent newEvent = new ZPCombatEvent(ZPCombatEvent.combatEvtID_Step);
				newEvent.impactY = (float)curPlayer.boundingBox.minY;
				((EntityClientPlayerMP)curPlayer).sendQueue.addToSendQueue(new ZPCombatMoveAsyncPacketCtoS(newEvent));*/
			//}
		}
		else
		{
			boolean oldNoClip = curPlayer.noClip;
			curPlayer.noClip = false;
			//double oldMotionY = curPlayer.motionY;
			curPlayer.moveEntityWithHeading(0, 0);
			//curPlayer.motionY = oldMotionY;
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
