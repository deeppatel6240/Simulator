package com.adopt.config;

import java.util.Properties;
import java.io.*;
import org.apache.commons.validator.routines.*;

class setup_defaults
{
	public static String RECEIVER_HOST_DEFAULT = "127.0.0.1";
	public static int RECEIVER_PORT_DEFAULT = 8835;
	public static int LOCAL_PORT_DEFAULT = 9090;
	public static String SYSLOG_DEFAULT = "data";
	public static boolean SYSLOG_WITH_NEWLINE_DEFAULT = true;
	public static boolean TRAFFIC_WITH_ALL_RECORDS_DEFAULT = false;
}

class parameter_validator
{
	public static boolean is_port(String str)
	{
		int port = -1;
		try
                {
	            port = Integer.parseInt(str);

                    if (port > 1024 && port < 65536)
                        return true;
                }
                catch (Exception ex){
                }
		return false;
	}

	public static boolean is_folder(String str)
	{
		File d = new File(str);
		if(d.exists() && d.isDirectory()) return true;
		return false;
	}

	public static boolean is_boolean(String str)
	{
		if(str.equals("true") || str.equals("false")) return true;
		return false;
	}

}

class parameter_parse extends Properties {
    Properties prop = null;

    public parameter_parse(Properties prop) {
	this.prop = prop;
    }

    //@Override
    public String getProperty(String param) {
	if(this.prop == null) return null;
        String value = this.prop.getProperty(param);
        if (value == null || value == ""){
           System.err.println("the parameter ["+param+"] not found/configured");
	   return null;
        }
        return value;
    }
}

public class load_setup {

    private static load_setup instance = null;
    public static final String SETUP_CONFIG= "conf/setup.properties";
    private parameter_parse parse = null;
    private InetAddressValidator inet_validator = null;    
    // load setup parameters 
    public String receiver_host = null;
    public int receiver_port = 0;
    public int local_port = 0;
    public String syslog = null; 
    public boolean syslog_with_newline = false; 
    public boolean traffic_with_all_records = false;

    // new
    public load_setup() {	
	this.receiver_host = setup_defaults.RECEIVER_HOST_DEFAULT;
	this.receiver_port = setup_defaults.RECEIVER_PORT_DEFAULT;
	this.local_port = setup_defaults.LOCAL_PORT_DEFAULT;
	this.syslog = setup_defaults.SYSLOG_DEFAULT;
	this.syslog_with_newline = setup_defaults.SYSLOG_WITH_NEWLINE_DEFAULT;
	this.traffic_with_all_records = setup_defaults.TRAFFIC_WITH_ALL_RECORDS_DEFAULT;

	this.inet_validator = InetAddressValidator.getInstance();
    }

    public boolean load_setup_read() {
	boolean pass_through = false;
	Properties config = null;
	InputStream ins = null;
	String param_value = null;
	try
	{
		System.out.println("loading load setup = "+this.SETUP_CONFIG);

		File fd = new File(this.SETUP_CONFIG);

		if(fd.exists() && !fd.isDirectory()) { 
			ins = new FileInputStream(this.SETUP_CONFIG);
			config = new Properties();	
	
			// load
			config.load(ins);
			this.parse = new parameter_parse(config);
		
			// read and validate against	

			// udp receiver host
			param_value = this.parse.getProperty("receiver.host");	
			if(param_value == null) {
				System.err.println("receiver host is not set. going with default host="+setup_defaults.RECEIVER_HOST_DEFAULT);	
			} else {
				if(this.inet_validator.isValid(param_value) == true) {
					this.receiver_host = param_value;
				} else {
					System.err.println("receiver host["+param_value+"] is not valid. going with default host="+setup_defaults.RECEIVER_HOST_DEFAULT);	
				}
			}


			// udp receiver port
			param_value = this.parse.getProperty("receiver.port");	
			if(param_value == null) {
				System.out.println("receiver port is not set. going with default port="+setup_defaults.RECEIVER_PORT_DEFAULT);	
			} else {
				if(parameter_validator.is_port(param_value) == false) {
					System.out.println("receiver port["+param_value+"] is not valid. going with default port="+setup_defaults.RECEIVER_PORT_DEFAULT);	
				} else {
					this.receiver_port = Integer.parseInt(param_value); 
				}
			}

			// udp local port
			param_value = this.parse.getProperty("local.port");	
			if(param_value == null) {
				System.out.println("local port is not set. going with default port="+setup_defaults.LOCAL_PORT_DEFAULT);	
			} else {
				if(parameter_validator.is_port(param_value) == false) {
					System.out.println("local port["+param_value+"] is not valid. going with default port="+setup_defaults.LOCAL_PORT_DEFAULT);	
				} else {
					this.local_port = Integer.parseInt(param_value); 
				}
			}

			// syslog dir
			param_value = this.parse.getProperty("syslog.dir");	
			if(param_value == null) {
				System.out.println("syslog directory not set. going with default syslog dir="+setup_defaults.SYSLOG_DEFAULT);	
			} else {
				if(parameter_validator.is_folder(param_value) == true) {
					this.syslog = param_value;	
				} else {
					System.out.println("syslog dir["+param_value+"] is not valid. going with default syslog dir="+setup_defaults.SYSLOG_DEFAULT);	
				}
			}

			// indicator on syslog has newline seperator
			param_value = this.parse.getProperty("syslog.has.newline");	
			if(param_value == null) {
				System.out.println("systelog newline indicator not set. going with default indicator="+setup_defaults.SYSLOG_WITH_NEWLINE_DEFAULT);	
			} else {
				if(parameter_validator.is_boolean(param_value) == true) {
					this.syslog_with_newline = Boolean.parseBoolean(param_value);
				} else {
					System.out.println("syslog new indicator ["+param_value+"] is not valid. going with default syslog newline indicator="+setup_defaults.SYSLOG_WITH_NEWLINE_DEFAULT);	
				}
			}

			// indicator on whethere traffic should go with all loaded records
			param_value = this.parse.getProperty("traffic.whole.records");	
			if(param_value == null) {
				System.out.println("traffic data indicator not set. going with default indicator="+setup_defaults.TRAFFIC_WITH_ALL_RECORDS_DEFAULT);	
			} else {
				if(parameter_validator.is_boolean(param_value) == true) {
					this.traffic_with_all_records = Boolean.parseBoolean(param_value);
				} else {
					System.out.println("traffic data indicator ["+param_value+"] is not valid. going with default traffic data indicator="+setup_defaults.TRAFFIC_WITH_ALL_RECORDS_DEFAULT);	
				}
			}

		} else {
			System.out.println("the load setup file not found/invalid.");
		}
		
		pass_through = true;

		load_setup_log();
	}
	catch(Exception ex)
	{
		System.out.println("error in loading setup, message ["+ex.getMessage()+"].");
		ex.printStackTrace();
	}
	return pass_through;
    }


    private void load_setup_log()
    {
	System.out.println("*************** LOAD SETUP *******************");
	System.out.println("receiver.host = "+this.receiver_host);
	System.out.println("receiver.port = "+this.receiver_port);
	System.out.println("local.port = "+this.local_port);
	System.out.println("syslog.dir = "+this.syslog);
	System.out.println("syslog.has.newline = "+this.syslog_with_newline);
	System.out.println("traffic.whole.records = "+this.traffic_with_all_records);
	System.out.println("**************************************************");
    }
    
    public static load_setup getInstance() {
        if (load_setup.instance == null) {
            load_setup.instance = new load_setup();
        }

        return load_setup.instance;
    }

}
