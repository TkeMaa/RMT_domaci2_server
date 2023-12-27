package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server {

	public static void main(String[] args) {
		
		ServerSocket serverSoket = null;
		Socket soketZaKomunikaciju = null;
		
		try {			
			
			serverSoket= new ServerSocket(5000);
			
			while (true) {
				
				System.out.println("Cekam na konekciju...");
				soketZaKomunikaciju = serverSoket.accept();
				System.out.println("Veza uspostavljena...");

				ClientHandler klijent = new ClientHandler(soketZaKomunikaciju);
				
				klijent.start();
				
			}
			
		} catch (SocketException e) {
			
		} catch (IOException e) {
			System.out.println("Doslo je do greske prilikom pokretanja servera!");
		}
	}
}
