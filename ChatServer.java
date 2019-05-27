import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(10001);
			System.out.println("Waiting connection...");
			HashMap hm = new HashMap();
			while(true){
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);
				chatthread.start();
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
}

class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private boolean initFlag = false;
	public ChatThread(Socket sock, HashMap hm){
		this.sock = sock;
		this.hm = hm;
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			id = br.readLine();
			broadcast(id + " entered.");
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);
			}
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // construcor
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				if(line.equals("/quit"))
					break;
				if(line.indexOf("/to ") == 0){
					sendmsg(line);
				//"/userlist"를 입력받으면
				if(line.equals("/userlist"))
					//함수 호출
					send_userlist();
				//금지어들을 입력받으면
				if(line.equals('A') || line.equals("B") || line.equals("C") || line.equals("D") || line.equals("E"))
					//함수 호출
					warning();
				}else
					broadcast(id + " : " + line);
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
	public void sendmsg(String msg){
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		if(end != -1){
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end+1);
			Object obj = hm.get(to);
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg
	public void broadcast(String msg){
		synchronized(hm){
			Collection collection = hm.values();
			Iterator iter = collection.iterator();
			while(iter.hasNext()){
				PrintWriter pw = (PrintWriter)iter.next();
				pw.println(msg);
				pw.flush();
			}
		}
	} // broadcast
	public void warning(){
		//아이디에게 경고
		synchronized(hm){
			try{
				//id = br.readLine(); // 읽어들인 id
				if(id != null){
					PrintWriter pw = (PrintWriter)hm.get(id); // hm의 id에게 쓰기
					pw.println("The word is prohibited"); // 다음과 같이 출력
					pw.flush();
				} // if
			}
			catch(Exception ex){ // try에 대한 catch
				System.out.println(ex);
			}
		}
	} // warning
	public void send_userlist(){
		synchronized(hm){
			Collection collection = hm.values(); // hm의 value들의 모음
			Iterator iter = collection.iterator(); // 모음을 출력하는 iter
			while(iter.hasNext()){ 
				PrintWriter pw = (PrintWriter)iter.next();
				Set key = hm.keySet();
				String keyName = (String) hm.get(key); // 아이디를 keyName으로 
				pw.println(keyName);//아이디 쓰기
				pw.flush();
			}
			
			
		} // send_userlist()
	}
	
}
