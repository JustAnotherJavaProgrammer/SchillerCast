package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;



public class Server {

	public static void start() {

		
		try {
			System.out.println("Server Startet on "+InetAddress.getLocalHost());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					ServerSocket ssock = new ServerSocket(4444);
					Socket sock = ssock.accept();
					InputStreamReader ir = new InputStreamReader(sock.getInputStream());
					BufferedReader bf = new BufferedReader(ir);

					String MESSAGE = bf.readLine();
					System.out.println(MESSAGE);
					ssock.close();
				} catch (IOException e) {

					e.printStackTrace();
				}

			}
		});

	}

}
