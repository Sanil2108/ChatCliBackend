package com.sanilk.chatCliBackend;

import com.sanilk.chatCliBackend.Client.Message;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

@WebServlet("/Servlet1")
public class Servlet1 extends HttpServlet {
	private static final long serialVersionUID = 1L;
	ClientHandler clientHandler;
	JDBCController controller;

	private static final String[] REQUEST_TYPES={
		//Send is for sending a new message, receive checks if there is a new message or not, Check sends the number of messages from each sender
			"SEND",
			"RECEIVE",
			"CHECK",
			"SIGN_UP",
			"AUTHENTICATE",
			"SEND_LOG",
			"GET_LOG"
	};

	File logFile;
	String appLogs;

	private final static String ADMIN_USER_NAME="sanilk21";
	private final static String ADMIN_PASSWORD="root";
       
    public Servlet1() {
        super();
    }
    

	final String JDBC_DRIVER="com.mysql.jdbc.Driver";
    
    @Override
    public void init() throws ServletException {
    	logFile =new File("log");
    	clientHandler=ClientHandler.getInstance();
    	clientHandler.registerServlet(this);
    	
    	getServletContext().setAttribute("clientHandler", clientHandler);
    	
    	try{
			Class.forName(JDBC_DRIVER);
		}catch(Exception e){
			e.printStackTrace();
		}

		appLogs="";
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DataOutputStream dos=new DataOutputStream(response.getOutputStream());
		dos.write("in doGet()".getBytes());
		dos.flush();
		dos.close();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		controller=new JDBCController(this);
		
		DataInputStream din=new DataInputStream(request.getInputStream());
		String contents = din.readUTF();


		
		String senderNick="";
		String receiverNick="";
		String message="";
		String request_type="";
		String password="";
		
		//They mark the locations of ':'
		int k1=-1;
		int k2=-1;
		int k3=-1;
		int k4=-1;
		
		char[] requestContents=contents.toCharArray();
		for(int i=0;i<requestContents.length;i++){
			if(requestContents[i] == ':'){
				
				
				//Now that i think about it I could have just used the split() of String
				//If k1 == -1 then that means that this is the first ':' encountered
				//silly way to do it but if it looks stupid but it works, it ain't stupid
				if(k1==-1){
					k1=i;
				}else if(k2==-1){
					k2=i;
				}else if(k3==-1){
					k3=i;
				}else if(k4==-1){
					k4=i;
				}

				
				continue;
			}
			
			//password should be before the message. otherwise if the message contains ':', password would be wrongly interpreted
			if(k1==-1){
				senderNick+=requestContents[i];
			}else if(k2==-1){
				request_type+=requestContents[i];
			}else if(k3==-1){
				receiverNick+=requestContents[i];
			}else if(k4==-1){
				password+=requestContents[i];
			}else{
				message+=requestContents[i];
			}
		}
		
		System.out.println("HIT");
		
		//At this point, appropriate value must be set in senderNick, message, request_type and receiverNick

		//All formats are from the point of view of sender.
		//Formats
		//SEND - senderNick:SEND:receiverNick:password:message
		//RECEIVER - senderNick:RECEIVE:receiverNick:password:
		//CHECK - senderNick:CHECK::password:message (message contains string of usernames seperated by ';')
		//SIGN_UP - senderNick:SIGN_UP::password:
		//AUTHENTICATE - senderNick:AUTHENTICATE::password:
		//SEND_LOG - senderNick:SEND_LOG:::message
		//GET_LOG - senderNick:GET_LOG::password::
		if(senderNick == "" || request_type=="" || (request_type.equals(REQUEST_TYPES[0]) && (message=="" || receiverNick=="")) || (request_type.equals(REQUEST_TYPES[1]) && receiverNick.equals("")) || 
				(request_type.equals(REQUEST_TYPES[2]) && message.equals("")) || (request_type.equals(REQUEST_TYPES[3]) && (password.equals("") || password==null)) || (request_type.equals(REQUEST_TYPES[4]) && password.equals(""))){
			throw new RuntimeException("Something is set null");
		}
		
		ClientHandler clientHandler=(ClientHandler)getServletContext().getAttribute("clientHandler");

		System.out.println(String.format("\nSender : %s\nRequest type : %s\nReceiver Nick : %s\nMessage : %s	\nPassword : %s", senderNick, request_type, receiverNick, message, password));
		
		if(!request_type.equals(REQUEST_TYPES[3]) && !request_type.equals(REQUEST_TYPES[4]) &&
				!request_type.equals(REQUEST_TYPES[5]) && !request_type.equals(REQUEST_TYPES[6])){
			if(!clientHandler.isClientAuthentic(senderNick, password)){
				DataOutputStream dos=new DataOutputStream(response.getOutputStream());
				dos.writeUTF("Authentication failed. The client is either not registered or the password is wrong. ");
				return;
			}
		}
		
		if(request_type.equals(REQUEST_TYPES[0])){
			sendMessage(message, senderNick, receiverNick, controller);
		}else if(request_type.equals(REQUEST_TYPES[1])){
			DataOutputStream dos = new DataOutputStream(response.getOutputStream());
//			dos.writeUTF(String.format("\nSender : %s\nRequest type : %s\nMessage : %s\n\nOld messages : %s\n\n", senderNick, request_type, message, checkForNewMessages(senderNick)));
			String newMessages=checkForNewMessages(senderNick, receiverNick, controller);
			if(newMessages!=null && newMessages!=""){
				dos.writeUTF(newMessages);
			}
		}else if(request_type.equals(REQUEST_TYPES[2])){
			DataOutputStream dos=new DataOutputStream(response.getOutputStream());
			HashMap<String, Integer> info=getAllMessagesInfo(senderNick, message);
			String finalMessage="";
			Set<String> keys=info.keySet();
			for(String string:keys){
				finalMessage+=string+":"+info.get(string)+";";
			}
			System.out.println(finalMessage);
			if(finalMessage!= null && finalMessage!=""){
				dos.writeUTF(finalMessage);
			}
		}else if(request_type.equals(REQUEST_TYPES[3])){
			DataOutputStream dos=new DataOutputStream(response.getOutputStream());
			if(clientHandler.clientExists(senderNick)){
				dos.writeUTF("Client name already exists");
			}else{
				System.out.println("Registering new client by username - "+senderNick+" and password - "+password);
				clientHandler.registerClient(senderNick, password, controller);
			}
		}else if(request_type.equals(REQUEST_TYPES[4])){
			DataOutputStream dos=new DataOutputStream(response.getOutputStream());
			if(clientHandler.isClientAuthentic(senderNick, password)){
				System.out.println("Client is authentic");
				dos.writeUTF("T");
			}else{
				System.out.println("Client is not authentic");
				dos.writeUTF("F");
			}
			dos.flush();
			dos.close();
			
		}else if(request_type.equals(REQUEST_TYPES[5])){
			appLogs=appLogs+message;
		}else if(request_type.equals(REQUEST_TYPES[6])){
			if(senderNick.equals(ADMIN_USER_NAME) && password.equals(ADMIN_PASSWORD)){
				DataOutputStream dos=new DataOutputStream(response.getOutputStream());
				dos.writeUTF(appLogs);
				dos.flush();
				dos.close();
			}
		}

	}
	
	public HashMap<String, Integer> getAllMessagesInfo(String senderNick, String message){
		String[] senders=message.split(";");
		ClientHandler clientHandler=(ClientHandler)getServletContext().getAttribute("clientHandler");
		return clientHandler.getAllInfo(senderNick, senders);
	}
	
	public void sendMessage(String message, String senderNick, String receiverNick, JDBCController controller){
		((ClientHandler)getServletContext().getAttribute("clientHandler")).sendToClient(message, senderNick, receiverNick, controller);
	}
	
	public String checkForNewMessages(String nick, String senderNick, JDBCController controller){
		ClientHandler clientHandler=((ClientHandler)getServletContext().getAttribute("clientHandler"));
		String newMessages="";
		ArrayList<Message> newMessagesList=clientHandler.checkClient(nick, senderNick, controller);
		if(newMessagesList!=null && newMessagesList.size()>0){
			for(int i=0;i<newMessagesList.size();i++){
				newMessages+=newMessagesList.get(i);
			}
			clientHandler.clearMessages(nick, senderNick);
			return newMessages;
		}
		return null;
	}
	
}