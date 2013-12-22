import java.io.*;
import java.net.*;
import java.util.*;

class HttpRequest implements Runnable
{
	Socket client_socket; //this is the socket on the client side
	
	HttpRequest(Socket client_socket)
	{
		//now initialise the HttpRequest function with the client side
		this.client_socket = client_socket;
	}
	
	public void run()
	{
		try
		{
			//calls the processRequest which is another function that implements the IO
			processRequest();
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	
	public void processRequest() throws Exception
	{
		//Get a reference to the socket's input and output
		String CRLF = "\r\n";
		String requestLine;
		BufferedReader br = null;
		DataOutputStream os = null;//funtion is like DataInputStream with an extra argument 
	
		try
		{
			br = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
			os = new DataOutputStream(client_socket.getOutputStream());
		} 
		catch (Exception e)
		{
			System.out.println("failed: IS and OS");
			System.exit(0);
		}
		
		//read the requestLine so could get the filename
		requestLine = br.readLine();
		System.out.println();
		System.out.println(requestLine);
		
		String headerLine = null;
		
		while((headerLine = br.readLine()).length () != 0 )
		{
			System.out.println(headerLine);
		}
		//file
		
		StringTokenizer tokens = new StringTokenizer(requestLine);
		
		tokens.nextToken();
		
		String fileName = tokens.nextToken();
		String filename = "." + fileName; 
		System.out.println("Filename is : "+filename);

		//Open  the requested File read the File and print it on the browser
		FileInputStream fis = null;
		boolean fileExists = true ;
		
		try
		{
			fis = new FileInputStream(fileName);
		} 
		catch (FileNotFoundException e)
		{
			fileExists  = false;
		}
		
		//Construct the response message
		String statusLine = null;
		String contentTypeLine = null;
		String entityBody =  null;
		
		if(fileExists)
		{
				statusLine = "HTTP/1.0 200 Ok"+CRLF;
				contentTypeLine = "Content-type: " + contentType(fileName)+CRLF;
		}
		else
		{
				statusLine = "HTTP/1.0 404 Not Found"+CRLF;
				contentTypeLine = "Content-type: "+"text/html"+CRLF;
				entityBody = "<HTML>"+ "<HEAD> <TITLE>Not Found </TITLE></HEAD>"+"<BODY>Not Found</BODY></HTML>";
		}
		
		os.writeBytes(statusLine);
		os.writeBytes(contentTypeLine);
		os.writeBytes(CRLF);
		
		if(fileExists)
		{
			sendBytes(fis,os);
			fis.close();
		}
		else
		{
			os.writeBytes(entityBody);
		}
		os.close();
		br.close();
		client_socket.close();

	}
	
	private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception
	{
			byte [] buffer = new byte[1024];
				int bytes = 0;
				while((bytes = fis.read(buffer))!= -1 )
				{
					os.write(buffer,0,bytes);
				}
	}
	
	public static String contentType(String fileName)
	{
		if(fileName.endsWith(".htm") || fileName.endsWith(".html"))
			return "text/html";
		if(fileName.endsWith(".jpg"))
			return "jpg";
		if(fileName.endsWith(".gif") )
			return "image/gif";
		return "application/octet-stream";
	}

}
//basically this is is done to extend the program if needed and could be used in main
class WebServer 
{
	ServerSocket server_socket;
	
	public void listen_socket()
	{
		int port= 7001;
		
		try
		{
			server_socket = new ServerSocket(port);
		}
		catch(Exception e)
		{
			System.out.println("Unable to connect to port : " +port);
			System.exit(0);//returns with a 0 if Unable to connect to port
		}
		while(true)
		{
			//implement threads for each HttpRequest
			HttpRequest t;//makes a new HttpRequest element
			
			try
			{
				t = new HttpRequest(server_socket.accept());//this is done to get connected to the server 
				
				Thread thread = new Thread(t);//creates a new thread
				
				thread.start();//starts the thread
			}
			catch(Exception e)
			{
				System.out.println("Error");
			}

		}
	}
	public void serverclose()
	{
		try
		{
			server_socket.close();
		}
		catch(Exception e)
		{
			System.out.println("Unable to close");

		}
	}

	public static void main(String argv[])
	{
		WebServer w1 = new WebServer();
		
		w1.listen_socket();
	}
}
