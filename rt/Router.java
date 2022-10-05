package edu.ut.cs.sdn.vnet.rt;

import edu.ut.cs.sdn.vnet.Device;
import edu.ut.cs.sdn.vnet.DumpFile;
import edu.ut.cs.sdn.vnet.Iface;

import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;

import java.util.Collection;

/**
 * @author Aaron Gember-Jacobson and Anubhavnidhi Abhashkumar
 */
public class Router extends Device
{	
	/** Routing table for the router */
	private RouteTable routeTable;
	
	/** ARP cache for the router */
	private ArpCache arpCache;
	
	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Router(String host, DumpFile logfile)
	{
		super(host,logfile);
		this.routeTable = new RouteTable();
		this.arpCache = new ArpCache();
	}
	
	/**
	 * @return routing table for the router
	 */
	public RouteTable getRouteTable()
	{ return this.routeTable; }
	
	/**
	 * Load a new routing table from a file.
	 * @param routeTableFile the name of the file containing the routing table
	 */
	public void loadRouteTable(String routeTableFile)
	{
		if (!routeTable.load(routeTableFile, this))
		{
			System.err.println("Error setting up routing table from file "
					+ routeTableFile);
			System.exit(1);
		}
		
		System.out.println("Loaded static route table");
		System.out.println("-------------------------------------------------");
		System.out.print(this.routeTable.toString());
		System.out.println("-------------------------------------------------");
	}
	
	/**
	 * Load a new ARP cache from a file.
	 * @param arpCacheFile the name of the file containing the ARP cache
	 */
	public void loadArpCache(String arpCacheFile)
	{
		if (!arpCache.load(arpCacheFile))
		{
			System.err.println("Error setting up ARP cache from file "
					+ arpCacheFile);
			System.exit(1);
		}
		
		System.out.println("Loaded static ARP cache");
		System.out.println("----------------------------------");
		System.out.print(this.arpCache.toString());
		System.out.println("----------------------------------");
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
		
		/********************************************************************/
		/* TODO: Handle packets                                             */
		
		// Check if Ethernet frame contains IPv4 packet
		if (etherPacket.getEtherType() == etherPacket.TYPE_IPv4)
		{
			// Get the IPv4 header
			IPv4 header = (IPv4) etherPacket.getPayload();

			// Naive approach to verifying checksum (serialize, deserialize, check with previous checksum)
			short initialChecksum = header.getChecksum();
			header.resetChecksum();
			byte[] serialize = header.serialize();
			header = (IPv4) header.deserialize(serialize, 0, serialize.length);
			short checksum = header.getChecksum();

			if (initialChecksum == checksum)
			{
				// Verifies TTL, decrements TTL, calculates new checksum
				if (header.getTtl() > 0)
				{
					byte newTtl = header.getTtl();
					newTtl--;
					header.setTtl(newTtl);
					header.resetChecksum();
					serialize = header.serialize();
					header = (IPv4) header.deserialize(serialize, 0 , serialize.length);
				}
				else // Sanity check, if TTL = 0 then packet should be dropped
				{
					System.out.println("TTL = 0, PACKET DROPPED");
				}
			}
			else // Sanity check, if checksum is not the same then packet should be dropped
			{
				System.out.println("VERIFICATION OF CHECKSUM FAILED, PACKET DROPPED");
				// System.out.println("OLD, NEW CHECKSUM: " + initialChecksum + ", " + checksum);
			}
			
			System.out.println("Header length: " + header.getHeaderLength() * 4);
			System.out.println("Checksum value ones complement: " + Integer.toBinaryString(header.getChecksum()));
			System.out.println("Checksum value ones complement: " + header.getChecksum());
			System.out.println("Checksum value: " + /*Integer.toBinaryString(*/(~(header.getChecksum()) & 0xffff))/*)*/;
			System.out.println("Checksum value: " + Integer.toBinaryString(~header.getChecksum() & 0xffff));	
	
			RouteEntry entry = routeTable.lookup(header.getDestinationAddress());
			// System.out.println(Integer.toBinaryString(entry.getDestinationAddress()));
			
		}
		else // Sanity check, TODO: Remove after, as we do nothing (drop packet) if doesn't contain IPv4 packet
		{
			System.out.println(etherPacket.getEtherType());
			System.out.println(etherPacket.TYPE_IPv4);
		}
		
		Collection<Iface> interfaces = this.getInterfaces().values();
		/*for (Iface i : interfaces)
		{
			System.out.println("Interface " + i.getName() + " IP: " + Integer.toBinaryString(i.getIpAddress()));
			System.out.println("Interface " + i.getName() + " mask: " + Integer.toBinaryString(i.getSubnetMask()));
		}*/		
		/********************************************************************/
	}
}
