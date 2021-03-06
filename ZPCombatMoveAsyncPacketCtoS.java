package ZPCombatMod;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet;

public class ZPCombatMoveAsyncPacketCtoS extends Packet {

	public byte eventID;
	
	public ZPCombatEvent zpcEvent;
	
	public ZPCombatMoveAsyncPacketCtoS() { }
	
	public ZPCombatMoveAsyncPacketCtoS(ZPCombatEvent eventToSend)
	{
		this.eventID = eventToSend.combatEventID;
		
		this.zpcEvent = eventToSend;
	}
	
	@Override
	public void readPacketData(DataInput datainput) throws IOException {
		this.eventID = datainput.readByte();
		
		this.zpcEvent = new ZPCombatEvent(this.eventID, datainput);
	}

	@Override
	public void writePacketData(DataOutput dataoutput) throws IOException {
		dataoutput.writeByte(eventID);
		
		this.zpcEvent.writeDataToPacket(dataoutput);
	}

	@Override
	public void processPacket(NetHandler nethandler) {
		ZPCombatMoveAsyncPacketCtoS.handleServerSide(this, nethandler);
	}

	@Override
	public int getPacketSize() {
		return 1 + ZPCombatEvent.getDataSize(this.eventID);
	}

	@Override
	public boolean canProcessAsync()
    {
        return true;
    }
	
	public ZPCombatMoveAsyncPacketStoC copyForRedirection(EntityPlayer senderPlayer)
	{
		return new ZPCombatMoveAsyncPacketStoC(senderPlayer.entityId, this.zpcEvent);
	}
	
	
	
	
	
	
	
	public static final int blocksDistanceThresholdForPlayer = 512;//taken from EntityTracker
	
	public static void handleServerSide(ZPCombatMoveAsyncPacketCtoS packet, NetHandler netHandler)
	{
		EntityPlayer senderPlayer = netHandler.getPlayer();
		//Entity tracker is not thread-safe so doing a workaround here
		synchronized(ZPCombat.threadSafePlayerListPerWorld)
		{
			NetServerHandler netServerHandler = (NetServerHandler)netHandler;
			List playerList = (List)ZPCombat.threadSafePlayerListPerWorld.get(senderPlayer.worldObj);
			
			if (playerList != null)
			{
				for (Object playerObj : playerList)
				{
					EntityPlayerMP curPlayer = (EntityPlayerMP)playerObj;
					
					if (curPlayer == senderPlayer)
						continue;
					
					//int is close enough here
					int dX = Math.abs((int)curPlayer.posX - (int)senderPlayer.posX);
					int dZ = Math.abs((int)curPlayer.posZ - (int)senderPlayer.posZ);
					
					if (dX > blocksDistanceThresholdForPlayer || dZ > blocksDistanceThresholdForPlayer)
						continue;
					
					curPlayer.playerNetServerHandler.sendPacketToPlayer(packet.copyForRedirection(senderPlayer));
				}
			}
		}
		
		ZPCombatEvent.addCombatEventToList(senderPlayer, packet.zpcEvent, Side.SERVER);
	}
}
