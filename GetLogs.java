import java.net.*;
import java.io.*;
public class GetLogs{
	public static void main(String[] args) throws Exception{
		URL url=new URL("http://127.0.0.1:8080/Servlet1");
		HttpURLConnection conn=(HttpURLConnection)url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		
		DataOutputStream dos=new DataOutputStream(conn.getOutputStream());
		dos.writeUTF("sanilk21:GET_LOG::root::");
		dos.flush();
		dos.close();
		
		InputStream in=conn.getInputStream();
		int c;

		FileOutputStream out=new FileOutputStream("logs.txt");
		while((c=in.read())!=-1){
			//System.out.print((char)c);
			out.write((char)c);
		}
	}
}
