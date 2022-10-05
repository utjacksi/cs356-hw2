package edu.ut.cs.sdn.vnet.sw;

import net.floodlightcontroller.packet.Ethernet;
import edu.ut.cs.sdn.vnet.Device;
import edu.ut.cs.sdn.vnet.DumpFile;
import edu.ut.cs.sdn.vnet.Iface;

import net.floodlightcontroller.packet.MACAddress;

import java.util.Hashtable;
import java.util.Collection;

// Value in the key, value pair of a switch MAC address hashtable
class SwitchData
{
	Iface outIface;
	long start;	

	public SwitchData(Iface outIface, long start)
	{
		this.outIface = outIface;
		this.start = start;
	}
}

/**
 * @author Aaron Gember-Jacobson
 */
public class Switch extends Device
{	
	static final int NANOS_PER_SECOND = 1_000_000_000;
	Hashtable<MACAddress, SwitchData> addressTable;

	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */

	public Switch(String host, DumpFile logfile)
	{
		super(host,logfile);
		addressTable = new Hashtable<MACAddress, SwitchData>();
	}

	/**
	 * Handle an Ethernet packet received on a specific interface.
	 * @param etherPacket the Ethernet packet that was received
	 * @param inIface the interface on which the packet was received
	 */
	public void handlePacket(Ethernet etherPacket, Iface inIface)
	{
		System.out.println("*** -> Received packet: " +
                etherPacket.toString().replace("\n", "\n\t"));
			
		// Check address table for destination MAC address
		// If found, check timeout < 15
		// If timeout < 15, update timeout and send packet through specified interface
		// If timeout >= 15 or if not found, broadcast
		// Check source address entry in address table
		// If found, check timeout
		// If timeout < 15, update timeout
		// If timeout >= timeout, update entry and timeout
	
		MACAddress destinationMACAddr = etherPacket.getDestinationMAC();
		SwitchData destinationEntry = addressTable.get(destinationMACAddr);
		
		if (destinationEntry != null && ((double)(System.nanoTime() - destinationEntry.start)/NANOS_PER_SECOND < 15))
		{
			destinationEntry.start = System.nanoTime();
			sendPacket(etherPacket, destinationEntry.outIface);
		}
		else
		{
			addressTable.remove(destinationMACAddr);
			broadcastPacket(etherPacket, inIface);
		}

		MACAddress sourceMACAddr = etherPacket.getSourceMAC();
		SwitchData sourceEntry = addressTable.get(sourceMACAddr);
		
		if (sourceEntry != null && ((double)(System.nanoTime() - sourceEntry.start)/NANOS_PER_SECOND < 15))
		{
			sourceEntry.start = System.nanoTime();
		}
		else
		{
			addressTable.remove(sourceMACAddr);
			addressTable.put(sourceMACAddr, new SwitchData(inIface, System.nanoTime()));	
		}

		/********************************************************************/
		/* TODO: Handle packets                                             */
		
		/********************************************************************/
	}


	public void broadcastPacket(Ethernet etherPacket, Iface inIface)
	{
		Collection<Iface> Ifaces = interfaces.values();

		for (Iface i : Ifaces)
		{
			if (i != inIface)
			{
				sendPacket(etherPacket, i);
			}
		}		
	}
}
