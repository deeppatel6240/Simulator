package com.adopt.common;

public class utils
{
	public static String VERSION = "${env.SYSLOG_VERSION}";

	public static void self_force_exit() {
		System.exit(1);
	}
}
