package ZPCombatMod;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;

public class ZPClientTickHandler implements ITickHandler {

	public static boolean thisPlayerWasOnGround = true;
	public static double thisPlayerOldY;
	
	public static boolean doBlink = false;
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		// onPlayerPreTick
		
		EntityPlayer curPlayer = ((EntityPlayer)tickData[0]);
		
		synchronized(ZPCombat.combatEventsClient)
		{
			
			List<ZPCombatEvent> curCEventList = ZPCombat.combatEventsClient.get(curPlayer);
			
			if (doBlink)
			{
				if (curCEventList == null)
				{
					curCEventList = new ArrayList<ZPCombatEvent>();
					
					ZPCombat.combatEventsClient.put(curPlayer, curCEventList);
				}
				
				doBlink = false;
				
				//Calculate destination:
				double cosPitch = Math.cos(curPlayer.rotationPitch * Math.PI / 180.0d);
				double blinkDirX = cosPitch * -Math.sin(curPlayer.rotationYaw * Math.PI / 180.0d);
				double blinkDirY = -Math.sin(curPlayer.rotationPitch * Math.PI / 180.0d);
				double blinkDirZ = cosPitch * Math.cos(curPlayer.rotationYaw * Math.PI / 180.0d);
				
				double blinkReach = 20.0d;
				Vec3 blinkDirection = curPlayer.worldObj.getWorldVec3Pool().getVecFromPool(blinkDirX, blinkDirY, blinkDirZ);//unit length
				
				Vec3 playerEyePos = curPlayer.worldObj.getWorldVec3Pool().getVecFromPool(curPlayer.posX, curPlayer.posY, curPlayer.posZ);//absolute coords
				Vec3 playerDestPos = playerEyePos.addVector(blinkDirection.xCoord * blinkReach, blinkDirection.yCoord * blinkReach, blinkDirection.zCoord * blinkReach);//absolute coords
				
				//call addVector(0, 0, 0) to duplicate it because clip() changes its content otherwise
				MovingObjectPosition raytraceResult = curPlayer.worldObj.clip(playerEyePos.addVector(0, 0, 0), playerDestPos, false);
				if (raytraceResult != null)
				{
					playerDestPos = blinkDirection.subtract(raytraceResult.hitVec);
				}
				
				Vec3 blinkVec = playerEyePos.subtract(playerDestPos);//blink range length
				
				Entity hitEntity = null;
	            List list = curPlayer.worldObj.getEntitiesWithinAABBExcludingEntity(curPlayer, curPlayer.boundingBox.addCoord(blinkVec.xCoord, blinkVec.yCoord, blinkVec.zCoord).expand(1.0D, 1.0D, 1.0D));
	            double d0 = 0.0D;
				
				for (int i = 0; i < list.size(); ++i)
	            {
	                Entity curEntity = (Entity)list.get(i);
	                
	                if (!(curEntity instanceof EntityLivingBase))
	                	continue;
	                
                    raytraceResult = curEntity.boundingBox.calculateIntercept(playerEyePos, playerDestPos);

                    if (raytraceResult != null)
                    {
                        double d1 = playerEyePos.distanceTo(raytraceResult.hitVec);

                        if (d1 < d0 || d0 == 0.0D)
                        {
                        	hitEntity = curEntity;
                            d0 = d1;
                            playerDestPos = blinkDirection.subtract(raytraceResult.hitVec);
                        }
                    }
	            }
				
				double targetYaw = curPlayer.rotationYaw;
				double targetPitch = curPlayer.rotationPitch;
				
				//Teleport behind target and turn around
				if (d0 != 0.0d)
				{
					playerDestPos = playerDestPos.addVector(blinkDirection.xCoord * 2.0d, blinkDirection.yCoord * 2.0d, blinkDirection.zCoord * 2.0d);
					targetYaw += 180.0f;
					if (targetYaw > 180.0f)
						targetYaw -= 360.0f;
					
					targetPitch = 0;//-targetPitch / 2.0f;
				}
				
				//Add+send event:
				ZPCombatEvent newEvent = new ZPCombatEvent(ZPCombatEvent.combatEvtID_Blink);
				newEvent.direction = ZPCombatEvent.getDirectionFromRotation(targetYaw);
				newEvent.pitch = ZPCombatEvent.getPitchFromRotation(targetPitch);
				newEvent.impactX = (float)playerDestPos.xCoord;
				newEvent.impactY = (float)playerDestPos.yCoord;// - curPlayer.height / 2.0f;
				newEvent.impactZ = (float)playerDestPos.zCoord;
				curCEventList.add(newEvent);
				((EntityClientPlayerMP)curPlayer).sendQueue.addToSendQueue(new ZPCombatMoveAsyncPacketCtoS(newEvent));
			}
			
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
			
			EntityRenderer entityRenderer = Minecraft.getMinecraft().entityRenderer;
			
			ZPCEntityState curEntityState = (ZPCEntityState)curPlayer.getExtendedProperties("zpcState");
			if (curEntityState != null && !curEntityState.isCruising)
			{
				try {
					float camRoll = ZPCombat.camRollField.getFloat(entityRenderer);
					
					if (camRoll != 0.0f)
					{
						if (Math.abs(camRoll) < 5.0f)
							ZPCombat.camRollField.setFloat(entityRenderer, 0.0f);
						else
							ZPCombat.camRollField.setFloat(entityRenderer, camRoll * 0.5f);
					}
					
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
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
