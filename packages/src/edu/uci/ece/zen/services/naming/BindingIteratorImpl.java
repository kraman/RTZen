/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.services.naming;

import java.util.Enumeration;
import java.util.Vector;

import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CosNaming.Binding;
import org.omg.CosNaming.BindingHolder;
import org.omg.CosNaming.BindingIteratorPOA;
import org.omg.CosNaming.BindingListHolder;
import org.omg.CosNaming.BindingType;
import org.omg.CosNaming.NameComponent;

/**

Implementation of BindingIterator.

@author Trevor Harmon

@version 1.0

*/
public class BindingIteratorImpl extends BindingIteratorPOA
{
	private boolean destroyed;
	private Enumeration e;

	BindingIteratorImpl(Enumeration e)
	{
		destroyed = false;
		this.e = e;
	}

	public boolean next_one(BindingHolder b)
	{
		if (destroyed)
			throw new OBJECT_NOT_EXIST("Iterator was destroyed");

		if (!e.hasMoreElements())
		{
			// Even though we have no value to return, we must provide one
			// because some (all?) IDL compilers generate code that attempts
            // to access the fields of b.value, but if b.value is null, then
            // a NullPointerException is thrown. So, we initialize b.value
			// with a dummy (empty) value. This is okay because the CORBA naming
			// spec says that if next_one returns false, then the Binding
			// value is undefined.
			b.value = new Binding(new NameComponent[0], BindingType.nobject);

			return false;
		}

		NamingContextExtImpl.BindingKey key =
			(NamingContextExtImpl.BindingKey) e.nextElement();

		b.value = key.toBinding();

		return true;
	}

	public boolean next_n(int how_many, BindingListHolder b)
	{
		if (destroyed)
			throw new OBJECT_NOT_EXIST("Iterator was destroyed");

		if (how_many == 0)
			throw new BAD_PARAM("Request for zero items not allowed");

		if (!e.hasMoreElements())
		{
			b.value = new Binding[0]; // Required by CORBA naming spec
			return false;
		}

		Vector bindings = new Vector();

		while (how_many > 0 && e.hasMoreElements())
		{
			NamingContextExtImpl.BindingKey key =
				(NamingContextExtImpl.BindingKey) e.nextElement();
			bindings.add(key.toBinding());
			how_many--;
		}

		b.value = (Binding[]) bindings.toArray(new Binding[0]);

		return true;
	}

	public void destroy()
	{
		destroyed = true;
		e = null;
	}
}
