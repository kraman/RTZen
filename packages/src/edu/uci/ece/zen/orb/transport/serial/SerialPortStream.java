package edu.uci.ece.zen.orb.transport.serial;

import java.util.*;
import java.io.*;
import edu.oswego.cs.dl.util.concurrent.*;

interface SerialPortStream
{
    InputStream getInputStream();
    OutputStream getOutputStream();
}

class LocalSerialPortStream implements SerialPortStream
{
    private IS inputStream = new IS();
    private OS outputStream = new OS();
    private int data;
    private Semaphore dataEmpty = new Semaphore(1);
    private Semaphore dataAvailable = new Semaphore(0);

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public OutputStream getOutputStream()
    {
        return outputStream;
    }

    class IS extends InputStream
    {
        // FIXME: When (if ever) must we return -1 here?
        public int read() throws IOException
        {
            try
            {
                System.out.println("LocalSerialPortStream: InputStream.read: blocking until data available");
                dataAvailable.acquire();

                int b = data;

                dataEmpty.release();

                byte[] ch = new byte[1];
                ch[0] = (byte) b;
                System.out.println("LocalSerialPortStream: InputStream.read: got lock, read byte: " + ((b&0xFF) >=32 && (b&0xFF) <=126 ? new String(ch) : ("0x" + Integer.toHexString(b&0xFF))));

                return b & 0xFF;
            }
            catch (InterruptedException e)
            {
                IOException ioex = new IOException();
                ioex.initCause(e);
                throw ioex;
            }
        }

        public int available() throws IOException
        {
            return (int) dataAvailable.permits();
        }
    }

    class OS extends OutputStream
    {
        public void write(int b) throws IOException
        {
            try
            {
                System.out.println("LocalSerialPortStream: OutputStream.write: trying to write to buffer");
                dataEmpty.acquire();

                data = b;

                dataAvailable.release();
                System.out.println("LocalSerialPortStream: OutputStream.write: wrote byte: " + (b & 0xFF));
            }
            catch (InterruptedException e)
            {
                IOException ioex = new IOException();
                ioex.initCause(e);
                throw ioex;
            }
        }
    }
}

class RemoteSerialPortStream implements SerialPortStream
{
    private IS inputStream;
    private OS outputStream;
    private SerialPort serialPort;
    private Socket socket;
    private int input;
    private Semaphore inputEmpty = new Semaphore(1);
    private Semaphore inputAvailable = new Semaphore(0);

    RemoteSerialPortStream(Socket socket, SerialPort serialPort)
    {
        this.socket = socket;
        this.serialPort = serialPort;

        inputStream = new IS();
        outputStream = new OS();
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public OutputStream getOutputStream()
    {
        return outputStream;
    }

    void addToInputStream(int b) throws InterruptedException, IOException
    {
//        System.out.println("RemoteSerialPortStream: addToInputStream: trying to write to byte");

        inputEmpty.acquire();

        input = b;

        inputAvailable.release();

        byte[] ch = new byte[1];
        ch[0] = (byte) b;
//        System.out.println("RemoteSerialPortStream: addToInputStream: done writing to input byte " + ((b&0xFF) >=32 && (b&0xFF) <=126 ? new String(ch) : ("0x" + Integer.toHexString(b&0xFF))));
    }

    class IS extends InputStream
    {
        // FIXME: When (if ever) must we return -1 here?
        public int read() throws IOException
        {
            try
            {
                inputAvailable.acquire();

                int b = input;

                inputEmpty.release();

                byte[] ch = new byte[1];
                ch[0] = (byte) b;
                System.out.println("RemoteSerialPortStream: InputStream.read: read byte: " + ((b&0xFF) >=32 && (b&0xFF) <=126 ? new String(ch) : ("0x" + Integer.toHexString(b&0xFF))));

                return b & 0xFF;
            }
            catch (InterruptedException e)
            {
                IOException ioex = new IOException();
                ioex.initCause(e);
                throw ioex;
            }
        }

   public int read(byte b[], int off, int len) throws IOException {
       System.out.println("RemoteSerialPortStream: InputStream.read(byte[], int, int): trying to read " + len + " bytes into buffer");
	if (b == null) {
	    throw new NullPointerException();
	} else if ((off < 0) || (off > b.length) || (len < 0) ||
		   ((off + len) > b.length) || ((off + len) < 0)) {
	    throw new IndexOutOfBoundsException();
	} else if (len == 0) {
	    return 0;
	}

	int c = read();
	if (c == -1) {
System.out.println("RemoteSerialPortStream: InputStream.read(byte[], int, int): c == -1, returning");
	    return -1;
	}
	b[off] = (byte)c;

	int i = 1;
	try {
	    for (; i < len ; i++) {
		c = read();
		if (c == -1) {
		    break;
		}
		if (b != null) {
		    b[off + i] = (byte)c;
		}
	    }
	} catch (IOException ee) {
System.out.println("RemoteSerialPortStream: InputStream.read(byte[], int, int): IOException!!!!");
	}
System.out.println("RemoteSerialPortStream: InputStream.read(byte[], int, int): returning " + i + " bytes");
	return i;
    }

        public int available() throws IOException
        {
            return (int) inputAvailable.permits();
        }
    }

    class OS extends OutputStream
    {
        private byte[] buffer = new byte[SerialPortSocketProtocol.SOCKET_DATA_HEADER_LENGTH + SerialPort.MAX_MESSAGE_LENGTH];
        private int bufferSize = SerialPortSocketProtocol.SOCKET_DATA_HEADER_LENGTH;

        public void write(int b) throws IOException
        {
            synchronized (buffer)
            {
                // It would probably be nice to block here until the output buffer becomes
                // free, but the InputStream.read() documentation doesn't require this, and
                // if we're assuming that SerialPort.MAX_MESSAGE_LENGTH holds true, then it's
                // not necessary anyway.
                if (bufferSize == buffer.length)
                {
                    throw new IOException("Serial port output buffer is full");
                }

                byte[] ch = new byte[1];
                ch[0] = (byte) b;
                System.out.println("RemoteSerialPortStream: OutputStream.write: writing byte: " + ((b&0xFF) >=32 && (b&0xFF) <=126 ? new String(ch) : ("0x" + Integer.toHexString(b&0xFF))));

                buffer[bufferSize++] = (byte) b;
            }
        }

        public void write(byte b[], int off, int len) throws IOException
        {
            super.write(b, off, len);
            flush();
        }

        public void flush() throws IOException
        {
            synchronized (buffer)
            {
                System.out.println("RemoteSerialPortStream: OutputStream.flush: flushing " + bufferSize + " bytes to serial port (includes socket data header)");
                SerialPortSocketProtocol.encodeSocketData(socket, buffer);
                serialPort.sendMessage(buffer, bufferSize);
                bufferSize = SerialPortSocketProtocol.SOCKET_DATA_HEADER_LENGTH;
            }
        }
    }
}
