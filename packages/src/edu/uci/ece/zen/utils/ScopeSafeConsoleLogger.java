package edu.uci.ece.zen.utils;

class ScopeSafeConsoleLogger extends ConsoleLogger
{
	protected ScopeSafeConsoleLogger()
	{
		super(new org.ovmj.java.io.ScopeSafePrintStream(System.err));
	}
}
