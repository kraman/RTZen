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
    private byte[] buffer = new byte[SerialPort.MAX_MESSAGE_LENGTH];
    private Semaphore bufferSize = new Semaphore(0);
    private Semaphore bufferLock = new Semaphore(1);

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
                bufferSize.acquire();
                System.out.println("LocalSerialPortStream: InputStream.read: data available, trying to lock");
                bufferLock.acquire();

                byte b = buffer[0];
                System.arraycopy(buffer, 1, buffer, 0, buffer.length - 1);

                bufferLock.release();
                System.out.println("LocalSerialPortStream: InputStream.read: got lock, read byte: " + Integer.toHexString(b));

                return b;
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
            return (int) bufferSize.permits();
        }
    }

    class OS extends OutputStream
    {
        public void write(int b) throws IOException
        {
            try
            {
                System.out.println("LocalSerialPortStream: OutputStream.write: trying to write to buffer");
                bufferLock.acquire();

                int size = (int) bufferSize.permits();

                if (size == buffer.length)
                {
                    // It would probably be nice to block here until the buffer becomes free, but
                    // we're assuming that SerialPort.MAX_MESSAGE_LENGTH holds true, so it shouldn't
                    // be necessary.
                    bufferLock.release();
                    throw new IOException("Buffer is full");
                }


                bufferSize.release();
                bufferLock.release();
                System.out.println("LocalSerialPortStream: OutputStream.write: wrote " + size + " bytes");
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
    private byte[] inputBuffer = new byte[SerialPort.MAX_MESSAGE_LENGTH];
    private Semaphore inputBufferSize = new Semaphore(0);
    private Semaphore inputBufferLock = new Semaphore(1);

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

    void addToInputStream(byte[] buffer, int offset, int length) throws InterruptedException, IOException
    {
        System.out.println("RemoteSerialPortStream: addToInputStream: trying to write to buffer");

        inputBufferLock.acquire();

        int bufferSize = (int) inputBufferSize.permits();

        if (bufferSize == inputBuffer.length)
        {
            // It would probably be nice to block here until the input buffer becomes free, but
            // we're assuming that SerialPort.MAX_MESSAGE_LENGTH holds true, so it shouldn't
            // be necessary.
            inputBufferLock.release();
            throw new IOException("Input buffer is full");
        }

        System.out.println("RemoteSerialPortStream: addToInputStream: writing to buffer");
        for (int i = 0; i < length; i++)
        {
            inputBuffer[bufferSize + i] = buffer[offset + i];
        }

        inputBufferSize.release(length);
        inputBufferLock.release();
        System.out.println("RemoteSerialPortStream: addToInputStream: done writing to buffer");
    }

    class IS extends InputStream
    {
        // FIXME: When (if ever) must we return -1 here?
        public int read() throws IOException
        {
            try
            {
//                System.out.println("RemoteSerialPortStream: InputStream.read: waiting until stream has data");
                inputBufferSize.acquire();
                inputBufferLock.acquire();

//                System.out.println("RemoteSerialPortStream: InputStream.read: data available, reading");

                byte b = inputBuffer[0];
                System.arraycopy(inputBuffer, 1, inputBuffer, 0, inputBuffer.length - 1);

                inputBufferLock.release();

                System.out.println("RemoteSerialPortStream: InputStream.read: read byte: " + Integer.toHexString(b&0xFF));

                return b & 0xFF;
            }
            catch (InterruptedException e)
            {
                IOException ioex = new IOException();
                ioex.initCause(e);
                throw ioex;
            }
        }

public int read(byte[] b, int off, int len) throws IOException
{
int bytesRead = super.read(b, off, len);
System.out.println("RemoteSerialPortStream: InputStream.read(byte[]): read " + bytesRead + " bytes");
return bytesRead;
}

        public int available() throws IOException
        {
            return (int) inputBufferSize.permits();
        }
    }

    class OS extends OutputStream
    {
        private byte[] buffer = new byte[SerialPortProtocol.SOCKET_DATA_HEADER_LENGTH + SerialPort.MAX_MESSAGE_LENGTH];
        private int bufferSize = SerialPortProtocol.SOCKET_DATA_HEADER_LENGTH;

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

                System.out.println("RemoteSerialPortStream: OutputStream.write: writing byte: " + Integer.toHexString(b & 0xFF));

                buffer[bufferSize++] = (byte)(b & 0xFF);
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
                SerialPortProtocol.encodeSocketData(socket, buffer);
                serialPort.sendMessage(buffer, bufferSize);
                bufferSize = SerialPortProtocol.SOCKET_DATA_HEADER_LENGTH;
            }
        }
    }
}
