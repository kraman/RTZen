package edu.uci.ece.zen.services.naming;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CosNaming.NamingContextExtPackage.*;
import org.omg.PortableServer.POA;

/**

Implementation of NamingContextExt.

@author Krishna Raman
@author Trevor Harmon

@version 1.1

*/
public class NamingContextExtImpl extends NamingContextExtPOA {

    private Hashtable bindings;
    private boolean destroyed;

    private POA poa;

	/**
	Ideally, we would use the org.omg.CORBA.CosNaming.Binding class as the Binding key, but
	for some reason its hashCode() and/or equals() methods don't work. So, in order to
	use Hashtable, we create our own (identical) Binding class with properly working
	hashCode() and equals() functions.
	*/
	class BindingKey
	{
		NameComponent[] binding_name;
		BindingType binding_type;

		BindingKey(NameComponent[] binding_name, BindingType binding_type)
		{
			this.binding_name = binding_name;
			this.binding_type = binding_type;
		}

		public int hashCode()
		{
			// Return a hashcode based on a string representation of the data
			//
			// NOTE: Changing this function can easily break the code that uses it
			// if you're not careful.
			String s = binding_name[0].id + "." + binding_name[0].kind + binding_type.value();

			for (int i = 1; i < binding_name.length; i++)
				s += "/" + binding_name[i].id + "." + binding_name[i].kind + binding_type.value();

			return s.hashCode();
		}

		public boolean equals(java.lang.Object o)
		{
			if (o != null && (o instanceof BindingKey))
			{
				BindingKey other = (BindingKey) o;

				if (binding_type.value() != other.binding_type.value() ||
					binding_name.length != other.binding_name.length)
					return false;

				for (int i = 0; i < binding_name.length; i++)
				{
					if (!binding_name[i].id.equals(other.binding_name[i].id) ||
						!binding_name[i].kind.equals(other.binding_name[i].kind))
					{
						return false;
					}
				}

				return true;
			}

			return false;
		}

		Binding toBinding()
		{
			return new Binding(binding_name, binding_type);
		}
	}

    NamingContextExtImpl() {
		bindings = new Hashtable();
        destroyed = false;
    }

    void init(POA _poa) {
        poa = _poa;
    }

    // Returns a new NameComponent array containing only the first element of the given array
	NameComponent[] getBaseName(NameComponent n[]) {
        NameComponent retN[] = new NameComponent[1];

        retN[0] = n[0];
        return retN;
    }

    // Returns a new NameComponent array containing all the elements of the given array except the last one
    NameComponent[] getContextName(NameComponent n[]) {
        NameComponent retN[] = new NameComponent[ n.length - 1 ];

		// Copy all elements, except the first one, from n to retN
        System.arraycopy(n, 1, retN, 0, n.length - 1);
        return retN;
    }

    public void bind(org.omg.CosNaming.NameComponent n[], org.omg.CORBA.Object obj) throws
                org.omg.CosNaming.NamingContextPackage.NotFound,
                org.omg.CosNaming.NamingContextPackage.CannotProceed,
                org.omg.CosNaming.NamingContextPackage.InvalidName,
                org.omg.CosNaming.NamingContextPackage.AlreadyBound {

        if (destroyed) {
            throw new CannotProceed("Cannot proceed because context has been destroyed.",
                    NamingContextExtHelper.narrow(_this_object()), n);
        }

        if (n == null || n.length == 0) {
            throw new InvalidName();
        }

        if (n.length > 1) {
            // go to the context and then bind the name there
            NameComponent baseName[] = getBaseName(n);
            NameComponent ctxName[] = getContextName(n);

            NamingContextHelper.narrow(resolve(baseName)).bind(ctxName, obj);
        } else {
            BindingKey binding = new BindingKey(n, BindingType.nobject);
            BindingKey bindingContext = new BindingKey(n, BindingType.ncontext);

            if (bindings.containsKey(binding) || bindings.containsKey(bindingContext)) {
                throw new AlreadyBound();
            }

            bindings.put(binding, obj);
        }
    }

    public void rebind(org.omg.CosNaming.NameComponent n[], org.omg.CORBA.Object obj) throws
                org.omg.CosNaming.NamingContextPackage.NotFound,
                org.omg.CosNaming.NamingContextPackage.CannotProceed,
                org.omg.CosNaming.NamingContextPackage.InvalidName {

        if (destroyed) {
            throw new CannotProceed("Cannot proceed because context has been destroyed.",
                    NamingContextExtHelper.narrow(_this_object()), n);
        }

        if (n == null || n.length == 0) {
            throw new InvalidName();
        }

        if (n.length > 1) {
            // go to the context and then bind the name there
            NameComponent baseName[] = getBaseName(n);
            NameComponent ctxName[] = getContextName(n);

            NamingContextHelper.narrow(resolve(baseName)).rebind(ctxName, obj);
        } else {
            BindingKey bindingContext = new BindingKey(n, BindingType.ncontext);

			if (bindings.containsKey(bindingContext))  // If the given name already exists as a context type...
			{
				throw new NotFound(NotFoundReason.not_object, n);
			}

            BindingKey binding = new BindingKey(n, BindingType.nobject);
            bindings.put(binding, obj);
        }
	}

    public void bind_context(org.omg.CosNaming.NameComponent n[], org.omg.CosNaming.NamingContext nc) throws
                org.omg.CosNaming.NamingContextPackage.NotFound,
                org.omg.CosNaming.NamingContextPackage.CannotProceed,
                org.omg.CosNaming.NamingContextPackage.InvalidName,
                org.omg.CosNaming.NamingContextPackage.AlreadyBound {
        if (destroyed) {
            throw new CannotProceed("Cannot proceed because context has been destroyed.",
                    NamingContextExtHelper.narrow(_this_object()), n);
        }

        if (n == null || n.length == 0) {
            throw new InvalidName();
        }

		if (nc == null)
		{
			throw new BAD_PARAM("NamingContext is null");
		}

        if (n.length > 1) {
            NameComponent baseName[] = getBaseName(n);
            NameComponent ctxName[] = getContextName(n);

            NamingContextHelper.narrow(resolve(baseName)).bind_context(ctxName,
                    nc);
        } else {
            BindingKey binding = new BindingKey(n, BindingType.ncontext);
            BindingKey bindingObject = new BindingKey(n, BindingType.nobject);

			// Make sure the name is not already bound either as a context or an object
            if (bindings.containsKey(binding) || bindings.containsKey(bindingObject)) {
                throw new AlreadyBound();
            }

            bindings.put(binding, nc);
        }
    }

    public void rebind_context(org.omg.CosNaming.NameComponent n[], org.omg.CosNaming.NamingContext nc) throws
                org.omg.CosNaming.NamingContextPackage.NotFound,
                org.omg.CosNaming.NamingContextPackage.CannotProceed,
                org.omg.CosNaming.NamingContextPackage.InvalidName {
        if (destroyed) {
            throw new CannotProceed("Cannot proceed because context has been destroyed.",
                    NamingContextExtHelper.narrow(_this_object()), n);
        }

        if (n == null || n.length == 0) {
            throw new InvalidName();
        }

		if (nc == null)
		{
			throw new BAD_PARAM("NamingContext is null");
		}

        if (n.length > 1) {
            NameComponent baseName[] = getBaseName(n);
            NameComponent ctxName[] = getContextName(n);

            NamingContextHelper.narrow(resolve(baseName)).rebind_context(ctxName,
                    nc);
        } else {
            BindingKey bindingObject = new BindingKey(n, BindingType.nobject);

            if (bindings.containsKey(bindingObject)) {
				throw new NotFound(NotFoundReason.not_context, n);
            }

            BindingKey binding = new BindingKey(n, BindingType.ncontext);
            bindings.put(binding, nc);
        }
    }

    public org.omg.CORBA.Object resolve(org.omg.CosNaming.NameComponent n[]) throws
                org.omg.CosNaming.NamingContextPackage.NotFound,
                org.omg.CosNaming.NamingContextPackage.CannotProceed,
                org.omg.CosNaming.NamingContextPackage.InvalidName {
        if (destroyed) {
            throw new CannotProceed("Cannot proceed because context has been destroyed.",
                    NamingContextExtHelper.narrow(_this_object()), n);
        }

        if (n == null || n.length == 0) {
            throw new InvalidName();
        }

        if (n.length > 1) {
            // go to the context and then bind the name there
            NameComponent baseName[] = getBaseName(n);
            NameComponent ctxName[] = getContextName(n);

            return NamingContextHelper.narrow(resolve(baseName)).resolve(ctxName);
        } else {
			BindingKey binding;

			binding = new BindingKey(n, BindingType.nobject);

            if (bindings.containsKey(binding)) {
                return (org.omg.CORBA.Object) bindings.get(binding);
            }

			binding = new BindingKey(n, BindingType.ncontext);

            if (bindings.containsKey(binding)) {
                return (org.omg.CORBA.Object) bindings.get(binding);
            }

            throw new NotFound(NotFoundReason.missing_node, n);
        }
    }

    public void unbind(org.omg.CosNaming.NameComponent n[]) throws
                org.omg.CosNaming.NamingContextPackage.NotFound,
                org.omg.CosNaming.NamingContextPackage.CannotProceed,
                org.omg.CosNaming.NamingContextPackage.InvalidName {

        if (destroyed) {
            throw new CannotProceed("Cannot proceed because context has been destroyed.",
                    NamingContextExtHelper.narrow(_this_object()), n);
        }

        if (n == null || n.length == 0) {
            throw new InvalidName();
        }

        if (n.length > 1) {
            // go to the context and then bind the name there
            NameComponent baseName[] = getBaseName(n);
            NameComponent ctxName[] = getContextName(n);

            NamingContextHelper.narrow(resolve(baseName)).unbind(ctxName);
        } else {
			BindingKey bindingContext = new BindingKey(n, BindingType.ncontext);

            if (bindings.containsKey(bindingContext)) {
                throw new InvalidName("Unbinding of context " + n[0] + " not allowed - only objects can be unbound");
            }

			BindingKey binding = new BindingKey(n, BindingType.nobject);

            if (!bindings.containsKey(binding)) {
                throw new NotFound(NotFoundReason.missing_node, n);
            }

            bindings.remove(binding);
        }
    }

    public org.omg.CosNaming.NamingContext new_context() {
        if (destroyed) {
            return null;
        }

        NamingContextExtImpl nc = new NamingContextExtImpl();
		nc.init(poa);

		try
		{
			org.omg.CORBA.Object obj = poa.servant_to_reference(nc);
			return NamingContextExtHelper.narrow(obj);
		}
		catch (org.omg.CORBA.UserException e)
		{
			return null;
		}
    }

    public org.omg.CosNaming.NamingContext bind_new_context(org.omg.CosNaming.NameComponent n[]) throws
                org.omg.CosNaming.NamingContextPackage.NotFound,
                org.omg.CosNaming.NamingContextPackage.AlreadyBound,
                org.omg.CosNaming.NamingContextPackage.CannotProceed,
                org.omg.CosNaming.NamingContextPackage.InvalidName {
        if (destroyed) {
            throw new CannotProceed("Cannot proceed because context has been destroyed.",
                    NamingContextExtHelper.narrow(_this_object()), n);
        }

        if (n == null || n.length == 0) {
            throw new InvalidName();
        }

        NamingContextExt ctx = NamingContextExtHelper.narrow(new_context());

        if (ctx == null) {
            throw new CannotProceed();
        }
        bind_context(n, ctx);
        return ctx;
    }

    public void destroy() throws
                org.omg.CosNaming.NamingContextPackage.NotEmpty {

		if (!destroyed)
		{
			if (bindings.isEmpty()) {
				bindings = null;
				destroyed = true;
			} else {
				throw new NotEmpty();
			}
		}
	}


	private BindingIterator createBindingIterator(Enumeration e)
	{
		try
		{
			BindingIteratorImpl bindingIterator = new BindingIteratorImpl(e);
			org.omg.CORBA.Object obj = poa.servant_to_reference(bindingIterator);
			return BindingIteratorHelper.narrow(obj);
		}
		catch (org.omg.CORBA.UserException ex)
		{
			// TODO: Need a better way of handling this exception
			return null;
		}
	}

	/**
	Creates an array of Bindings from the current table of bindings.

	@param max The maximum number of elements to retrieve
	@param bi A binding iterator that will hold any elements beyond
	the maximum, if non-null. (Null values are permitted.)
	*/
	private Binding[] createBindingArray(int max, org.omg.CosNaming.BindingIteratorHolder bi)
	{
		Binding[] bindingArray = new Binding[max];
		Enumeration e = bindings.keys();
		int i = 0;

		// Walk through the list and add each item to the array.
		// Stop if we run out of elements or hit the maximum.
		while ( e.hasMoreElements() && i < max )
		{
			BindingKey key = (BindingKey) e.nextElement();
			bindingArray[i] = key.toBinding();
			i++;
		}

		// If there are more elements and more are requested, create an iterator from
		// the remaining elements
		if (bi != null && e.hasMoreElements() )
			bi.value = createBindingIterator(e);

		return bindingArray;
	}

    public void list(int how_many, org.omg.CosNaming.BindingListHolder bl, org.omg.CosNaming.BindingIteratorHolder bi)
	{
		if (destroyed)
            throw new OBJECT_NOT_EXIST("Cannot proceed because context has been destroyed.");

		if (how_many <= 0)  // Do <= instead of == just in case we get passed a negative number
		{
			bl.value = new Binding[0];
			bi.value = createBindingIterator(bindings.keys());
		}
		else if (how_many >= bindings.size())  // Special case required by CORBA naming spec
		{
			bl.value = createBindingArray(bindings.size(), null);
			bi.value = null;
		}
		else
		{
			bl.value = createBindingArray(how_many, bi);
		}
	}

    private void escape(String name, StringBuffer sb) {
        for (int i = 0; i < name.length(); i++) {
            switch (name.charAt(i)) {
            case '.':
                sb.append("\\.");
                break;

            case '/':
                sb.append("\\/");
                break;

            case '\\':
                sb.append("\\\\");
                break;

            default:
                sb.append(name.charAt(i));
                break;
            }
        }
    }

    public java.lang.String to_string(org.omg.CosNaming.NameComponent n[]) throws
                org.omg.CosNaming.NamingContextPackage.InvalidName {
        if (n == null || n.length == 0) {
            throw new InvalidName();
        }

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < n.length; i++) {
            if (i != 0) {
                sb.append("/");
            }

            NameComponent cur = n[i];

			escape(cur.id, sb);
            sb.append(".");
            escape(cur.kind, sb);
        }

        return sb.toString();
    }

	// PRECONDITIONS: String must not be empty or null
    // Note: Escape characters that are not escaping the separator or another escape character are ignored.
	private String[] parse(String s, char separator, char escape) throws InvalidName
	{
		StringBuffer buf = new StringBuffer(s);

		// Walk through each character in the array...
		for (int i = 0; i < buf.length(); i++)
		{
            // If the character
			if (buf.charAt(i) == escape &&        // If the character is the escape character...
                i+1 < buf.length() &&             // ...and it is not at the end of the string...
                (buf.charAt(i+1) == escape ||     // ...and it is followed by an escape character...
                 buf.charAt(i+1) == separator))   // ...or a separator character...
			{
                // ...then delete it
                buf.deleteCharAt(i);
			}
			// If the character is a separator (and is not being escaped), then
			// change it to a non-character marker (0 in this case)
			else if (buf.charAt(i) == separator)
			{
				buf.setCharAt(i, '\0');
			}
		}

        // Throw an error if the string begins or ends with a separator ('\0' after unescaping)
        if (buf.toString().startsWith("\0") || buf.toString().endsWith("\0"))
            throw new InvalidName("Name is invalid for naming service: " + s);

		int start = 0;
		Vector strings = new Vector();

		// Split the string into its individual components, using our non-character
		// marker as the token
		for (int end = 0; end < buf.length(); end++)
		{
			if (buf.charAt(end) == '\0')
			{
				strings.add( buf.substring(start, end) );
				start = end+1;
			}
		}

        strings.add( buf.substring(start) );

		return (String[]) strings.toArray(new String[0]);
	}

	/**
	Tested okay on all stringified names shown in CORBA spec, such as:
	  a/b/c
	  a.b/c.d/.
	  a/./c.d/.e
	  a/x\/y\/z/b
	  a\.b.c\.d/e.f
	  a/b\\/c
	*/
    public org.omg.CosNaming.NameComponent[] to_name(java.lang.String sn) throws InvalidName {
		if ( sn == null || sn.length() == 0 )
			throw new InvalidName("Cannot convert empty strings");

		String[] names = parse(sn, '/', '\\');

		Vector nameComponents = new Vector();

        for (int i = 0; i < names.length; i++)
		{
            String id = "", kind = "";

            // The CORBA Naming Specification says (for some strange reason beyond any sane person's
            // comprehension) that the names "." and ".kind" are valid, but "id." is not. That
            // forces us to do the following special case handling.

            if (!names[i].equals("."))
            {
                // If the name is ".kind", then id = "" and kind = "kind"
                if (names[i].startsWith("."))
                {
                    kind = names[i].substring(1);

                    // We know that the name starts with the separator character, so we can't call parse()
                    // directly (because it will think it's an error), but we still need to unescape the name.
                    kind = kind.replaceAll("\\\\\\.", ".");     // Replace all "\." with "."
                    kind = kind.replaceAll("\\\\\\\\", "\\");   // Replace all "\\" with "\"
                }
                else
                {
                    String[] nameComponent = parse(names[i], '.', '\\');

                    if (nameComponent.length > 0)
                        id = nameComponent[0];

                    if (nameComponent.length > 1)
                        kind = nameComponent[1];
                }
            }

			nameComponents.add(new NameComponent(id, kind));
		}

		return (NameComponent[]) nameComponents.toArray(new NameComponent[0]);
    }

    public java.lang.String to_url(java.lang.String addr, java.lang.String sn) throws
                org.omg.CosNaming.NamingContextExtPackage.InvalidAddress,
                org.omg.CosNaming.NamingContextPackage.InvalidName {
        if (addr == null || addr.length() == 0)
		{
			throw new InvalidAddress("Specified address is empty");
		}

		// TODO: Throw InvalidAddress if addr is malformed
		// TODO: Throw InvalidName if sn is malformed

		try
		{
			return "corbaloc:" +
					URLEncoder.encode(addr, "UTF-8") +
					"/" +
					URLEncoder.encode(sn, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			String unencodedURL = "corbaloc:" + addr + "/" + sn;
//			ZenProperties.logger.log(
//			                Logger.SEVERE,
//			                "edu.uci.ece.zen.services.naming.NamingContextExImpl",
//			                "<to_url>",
//			                "UTF-8 encoding is not supported by the current runtime system. URL will be returned without encoding: " + unencodedURL
//			                );
			//Logger.error("UTF-8 encoding is not supported by the current runtime system. URL will be returned without encoding: " + unencodedURL);

			return unencodedURL;
		}
	}

    public org.omg.CORBA.Object resolve_str(java.lang.String sn) throws
                org.omg.CosNaming.NamingContextPackage.NotFound,
                org.omg.CosNaming.NamingContextPackage.CannotProceed,
                org.omg.CosNaming.NamingContextPackage.InvalidName,
                org.omg.CosNaming.NamingContextPackage.AlreadyBound {
        return resolve(to_name(sn));
    }

    public org.omg.CosNaming.NamingContext resolve_context(org.omg.CosNaming.NameComponent n[]) throws
                org.omg.CosNaming.NamingContextPackage.NotFound,
                org.omg.CosNaming.NamingContextPackage.CannotProceed,
                org.omg.CosNaming.NamingContextPackage.InvalidName {
        return NamingContextHelper.narrow(resolve(n));
    }
}

