package com.adopt.traffic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

import com.adopt.config.*;
import com.adopt.common.*;


public class tgenerator
{
	private DatagramSocket lsock = null;
	private load_setup lsetup = null;	
	private ArrayList<DatagramPacket> pLst = new ArrayList<DatagramPacket>();
	private boolean traffic_keep_up = true;

	public tgenerator() {
		lsetup = load_setup.getInstance();	
	}
	
	private boolean socket_init() {
		try {
			this.lsock = new DatagramSocket(lsetup.local_port, InetAddress.getLocalHost());
			return true;
		} catch(Exception e) {
			System.err.println("error in local socket init, message["+e.getMessage()+"].");
			e.printStackTrace();
		}
		return false;
	}

	private boolean data_load() {
		try {
			File file = new File(lsetup.syslog);
			String fList[] = null;
			// load all from IN directory
			if(file.exists() && file.isDirectory()) {
				File[] fileList = file.listFiles();
				fList = new String[fileList.length];
				int count =0;
				for(File f:fileList) {
					fList[count++] = f.getAbsolutePath();
				}
			// load file
			} else {
				fList = new String[1];
				fList[0] = lsetup.syslog;
			}


			// dump records to memory
			
			for(int r = 0 ; r< fList.length ; r++){
					File fd = new File(fList[r]);
					FileInputStream fileInputStream = null;
					BufferedReader bufferedReader = null;
					try{
						if(lsetup.syslog_with_newline == true){
							//System.out.println("path::"+fd.getPath());
							bufferedReader = new BufferedReader(new FileReader(fd));
							String cdr = null;
							while((cdr = bufferedReader.readLine()) != null){
								byte[] rb = cdr.getBytes();
								DatagramPacket rp = new DatagramPacket(rb,rb.length,InetAddress.getByName(lsetup.receiver_host),lsetup.receiver_port);
								pLst.add(rp);
								if(lsetup.traffic_with_all_records == false)
									break;
							}
						}else{
							
							fileInputStream = new FileInputStream(fd);
							byte rb[] = new byte[fileInputStream.available()];
							fileInputStream.read(rb);
							if(rb!=null){
								DatagramPacket rp = new DatagramPacket(rb,rb.length,InetAddress.getByName(lsetup.receiver_host),lsetup.receiver_port);
								pLst.add(rp);
							}
						}
					}catch (Exception e) {
						System.err.println("error in dumping data records to memory, message["+e.getMessage()+"].");
						e.printStackTrace();
					}finally{
						
						if(fileInputStream != null) {
							try {
								fileInputStream.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						if(bufferedReader != null) {
							try{
								
								bufferedReader.close();
							}catch(IOException ioe){
								
							}
						}
						
						if(lsetup.traffic_with_all_records == false && 
								(pLst.size() == 1 && pLst.get(0) != null)) break;
					}
				
			}
			
		} catch(Exception e) {
			System.err.println("error in loading load-data from syslog, message["+e.getMessage()+"].");
			e.printStackTrace();
		}	
		if(pLst.size() < 1) return false;
		return true;
	}

	
	public void init() {
		try
		{
			// init local socket
			if(socket_init() == false) {
				System.err.println("unable to init local socket.");
				utils.self_force_exit();
			}			
			
			// load records for generating traffic
			if(data_load() == false) {
				System.err.println("unable to pick even single record from syslog to generate traffic.");
				utils.self_force_exit();
			}

			// traffic - init
			Thread tjob = new Thread(new traffic_sender());
			tjob.start();	
		}
		catch(Exception ex)
		{
			System.out.println("error in traffic generator init, message["+ex.getMessage()+"].");
		}
	}


	public class traffic_sender implements Runnable
	{
		private volatile long g_pid = 0;
		public traffic_sender() {
			super();
		}

		public void run() {
			
			DatagramPacket d_packet = pLst.get(0);
			System.out.println("the top packet content to use [" + new String(d_packet.getData(), 0, d_packet.getLength())+"].\r\n");

			// puase till user pass signal to start
		        System.out.println("SYSLOG Traffic Generator ["+utils.VERSION+"] is UP. Type [Y] whenever you are ready to go or [N] to exit =>");  	
			Scanner sc= new Scanner(System.in);
                        String in= sc.nextLine();
			if(in.equals("N")) {
				System.out.println("Instance is exiting..");
				utils.self_force_exit();
			}

			System.out.println("started sendig traffic to the peer ["+lsetup.receiver_host+" : "+lsetup.receiver_port+"]. \r\nPress [Ctrl+C] to stop anytime.");
			
			while(traffic_keep_up) {
				try {
					if(lsetup.traffic_with_all_records == false) {
						System.out.println("data::"+new String(d_packet.getData(), 0, d_packet.getLength()));
						System.out.println("Dest IP+Port::"+ d_packet.getSocketAddress());
						System.out.println("Data length::"+ d_packet.getLength());
						System.out.println("Source IP+Port::"+ lsock.getLocalSocketAddress());
						lsock.send(d_packet);	
						g_pid++;
					} else {
						for(int pid = 0; pid < pLst.size() ; pid++) {
							lsock.send(pLst.get(pid));	
							g_pid++;
						}
					}

					System.out.println("total packet sent to peer => p-"+g_pid);
				
				} catch(Exception e) {
					System.err.println("error detected in pushing traffic. keep pushing.");
					e.printStackTrace();
				}	
			}
		}
	}
}
