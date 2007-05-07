// ActivePutter.java
// $Id: ActivePutter.java,v 1.6 2007/02/21 00:07:50 sjardine Exp $
//
// Copyright 2000, Joe Phillips <jaiger@innovationsw.com>
// Copyright 2001, 2002 Innovation Software Group, LLC - http://www.innovationsw.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Library General Public
// License as published by the Free Software Foundation; either
// version 2 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Library General Public License for more details.
//
// You should have received a copy of the GNU Library General Public
// License along with this library; if not, write to the Free
// Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// TODO:
// - do compressed streams
//

package gnu.inet.ftp;

// system includes
import java.io.*;
import java.net.*;
import java.util.zip.*;

// home-grown imports
import gnu.inet.logging.Logger;
import gnu.inet.logging.LoggingFactory;

/**
 * This class implements an FTP-style data connection server thread for PUTing 
 * in a non-passive files/data.
 * <P>
 * This class is used internally to the FtpClient class.
 **/
public class ActivePutter extends Putter {
   // private data
   private final static Logger log = LoggingFactory.getLogger(ActivePutter.class);  
   private InetAddress address;
   private int port;
   private ServerSocket server;
   private int timeout;

   // public constructors
   //

   /**
    * Create a new ActivePutter thread given the InputStream data source.
    * @exception IOException io error with the ServerSocket
    * @param in data source
    */ 
   public ActivePutter(InputStream in)
     throws IOException
   {
      super();
    
      // create server socket
      this.server= new ServerSocket(0);
      this.timeout= 30*1000;	// 30s timeout
      // store the port that the server is listening on 
      this.port= server.getLocalPort();
      this.address= this.server.getInetAddress();

      this.istream= in;
   }// end of default constructor

   //
   // public methods
   //

   /**
    * get the port this ActivePutter is listening on
    * @return port number
    */
   public synchronized int getPort(){
      return port;
   }// getPort

   /**
    * get address that this Putter is listening on
    * @return server socket IP address
    */
   public InetAddress getInetAddress(){
      return address;
   }// getInetAddress

   /**
    * set connection timeout in milliseconds.
    * must be called before start()/run()
    * @param milliseconds the number of milliseconds the server socket should wait for a connection before timing-out.  the default timeout is 30s
    */
   public void setTimeout(int milliseconds){
      timeout= milliseconds;
   }// setTimeout

   /**
    * implements thread behavior.  Put data to server using given parameters.
    */
   public void run(){
      boolean signalClosure= false;
      Socket sock= null;
      OutputStream ostream;
      long amount= 0;
      int buffer_size= 0;
      byte buffer[]= new byte[BUFFER_SIZE];
      //this.cancelled= false;	// reset cancelled flag
 
      try{
         // wait for connection
         server.setSoTimeout(timeout);	// can only wait so long
         if (cancelled)
            throw new InterruptedIOException("Transfer cancelled"); //small race condition here
         sock= server.accept();
         signalConnectionOpened(
            new ConnectionEvent(sock.getInetAddress(), sock.getPort())
         );
         signalTransferStarted();

         try{

            // handle different type settings
            switch(type){
               case FtpClient.TYPE_ASCII:
                  ostream= new AsciiOutputStream(sock.getOutputStream());
                  break;
               default:
                  ostream= sock.getOutputStream();
                  break;
            }// switch

            // handle different mode settings
            switch(mode){
               case FtpClient.MODE_ZLIB:
                  ostream= new DeflaterOutputStream(ostream);
                  break;
               case FtpClient.MODE_STREAM:
               default:
                  break;
            }// switch

            int len;
            while((len= istream.read(buffer)) != -1){
               ostream.write(buffer, 0, len);
               amount+= len;
               buffer_size+= len;
               if(buffer_size >= BUFFER_SIZE){
                  buffer_size= buffer_size%BUFFER_SIZE;
                  signalTransfered(amount);
               }
               yield();
            }

            ostream.close();
            sock.close();
         }catch(InterruptedIOException iioe){
            if(!cancelled){
                log.error(iioe.getMessage(),iioe);
            }
         }catch(Exception e){
             log.error(e.getMessage(),e);
         }finally{
            signalTransferCompleted();
         }
      }catch(Exception ee){
         signalConnectionFailed(ee);
         log.error(ee.getMessage(),ee);
      }
      if(signalClosure == true){
         signalConnectionClosed(
            new ConnectionEvent(sock.getInetAddress(), sock.getPort())
         );
      }
   }// run

}// ActivePutter 

// ActivePutter.java
