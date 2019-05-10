package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import com.sun.prism.paint.Paint;

import headless.Headless;

public class Server {

	public static void start(Headless main) {

		try {
			System.out.println("Server Startet on " + InetAddress.getLocalHost());
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
					main.connected();
					
					InputStreamReader ir = new InputStreamReader(sock.getInputStream());
					BufferedReader bf = new BufferedReader(ir);
					while(main.stop == 0) {
						
						String MESSAGE = bf.readLine();
						
						String[] args = MESSAGE.split(" ", 3);
						
						
						System.out.println(args[2]);
							args[2] = args[2].replaceAll(" qt", "");
							
							System.out.println(args[2]);
							String[] temp = args[2].split(" "); 
				String[] temp2 = args[2].split(",");
							
							System.out.println(temp[1]);
							System.out.println(temp[2]);
							main.draw(temp[1], temp[2], temp2);
						
						
                        //page
						//pencilmode
						//color
						//stokewith
						//moveto 2 ints
						//qt quadto 4 ints
						
						}
					ssock.close();

				} catch (IOException e) {

					e.printStackTrace();
				}

			}
		}).start();

	}

}
