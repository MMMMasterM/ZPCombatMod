package ZPCombatMod;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet;

public class ZPCombatMoveAsyncPacketCtoS extends Packet {

	byte eventID;
	
	byte[] packetMetaData;
	
	public ZPCombatMoveAsyncPacketCtoS() { }
	
	public ZPCombatMoveAsyncPacketCtoS(ZPCombatEvent eventToSend)
	{
		this.eventID = eventToSend.combatEventID;
		
		this.packetMetaData = new byte[ZPCombatEvent.getMetaDataSize(this.eventID)];
		
		switch (eventToSend.combatEventID)
		{
			case ZPCombatEvent.combatEvtID_JumpFront:
				this.packetMetaData[0] = eventToSend.direction;
				break;
		}
	}
	
	@Override
	public void readPacketData(DataInput datainput) throws IOException {
		this.eventID = datainput.readByte();
		
		this.packetMetaData = new byte[ZPCombatEvent.getMetaDataSize(this.eventID)];
		datainput.readFully(this.packetMetaData);
	}

	@Override
	public void writePacketData(DataOutput dataoutput) throws IOException {
		dataoutput.writeByte(eventID);
		
		dataoutput.write(this.packetMetaData);
	}

	@Override
	public void processPacket(NetHandler nethandler) {
		ZPCombatMoveAsyncPacketCtoS.handleServerSide(this, nethandler);
	}

	@Override
	public int getPacketSize() {
		return 1 + ZPCombatEvent.getMetaDataSize(this.eventID);
	}

	@Override
	public boolean canProcessAsync()
    {
        return true;
    }
	
	public ZPCombatMoveAsyncPacketStoC copyForRedirection(EntityPlayer senderPlayer)
	{
		return new ZPCombatMoveAsyncPacketStoC(senderPlayer.entityId, this);
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
		
		ZPCombatEvent.addCombatEventToList(senderPlayer, packet.eventID, packet.packetMetaData);
	}
}
