package com.adopt.main;

import com.adopt.config.*;
import com.adopt.traffic.*;
import com.adopt.common.*;

public class main 
{
	public static void main(String args[]) {

		System.out.println("SYSLOG Traffic Generator["+utils.VERSION+"] instance init in progress..");

		// load setup - init
		load_setup lsetup = load_setup.getInstance();	
		if(lsetup.load_setup_read() == false) {
			System.out.println("error in loading load-setup. please see to console log and setup to re-run.");
			utils.self_force_exit();
		}
		
		// traffic generator - init
		new tgenerator().init();
		
	}
}
