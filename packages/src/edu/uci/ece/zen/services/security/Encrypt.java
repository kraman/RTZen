package edu.uci.ece.zen.services.security;

import java.security.*;
import javax.crypto.*;
import java.util.*;

public class Encrypt
{
	private void printAllProviders()
	{
		Provider[] providers = Security.getProviders();

		for ( int i = 0; i < providers.length; i++ )
		{
			Provider provider = providers[i];

			System.out.println( "Provider name: " + provider.getName() );
			System.out.println( "Provider information: " + provider.getInfo() );
			System.out.println( "Provider version: " + provider.getVersion() );
			Set entries = provider.entrySet();

			Iterator iterator = entries.iterator();

			while ( iterator.hasNext() )
			{
				System.out.println( "Property entry: " + iterator.next() );
			}
		}
	}

	public static void main( String[] args )
	{
		Security.addProvider( new com.sun.crypto.provider.SunJCE() );
		try
		{

			KeyGenerator kg = KeyGenerator.getInstance( "DES" );
			Key key = kg.generateKey();
			Cipher cipher = Cipher.getInstance( "DES" );

			byte[] data = "Encrypt Me !".getBytes();
			System.out.println( "Original data : " + new String( data ) );

			cipher.init( Cipher.ENCRYPT_MODE, key );
			byte[] result = cipher.doFinal( data );
			System.out.println( "Encrypted data: " + new String( result ) );

			cipher.init( Cipher.DECRYPT_MODE, key );
			byte[] original = cipher.doFinal( result );
			System.out.println( "Decrypted data: " + new String( original ) );
		}

		catch ( Exception exception )
		{
			System.out.println( "Exception" );
		}
	}
}

