package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class ClientHandler extends Thread {
	
	Socket soketZaKomunikaciju = null;
	
	PrintStream klijentOutput = null;
	BufferedReader klijentInput = null;
	
	public ClientHandler(Socket soketZaKomunikaciju) {
		this.soketZaKomunikaciju = soketZaKomunikaciju;
	}
	
	@Override
	public void run() {
		
		try {
			
			klijentOutput = new PrintStream(soketZaKomunikaciju.getOutputStream());
			klijentInput = new BufferedReader(new InputStreamReader(soketZaKomunikaciju.getInputStream()));
			
			klijentOutput.println("Veza uspesno uspostavljena");
			klijentOutput.println();
			
			int opcija;
			
			while (true) {
				
				klijentOutput.println("Izaberite opciju(1-5): ");
				klijentOutput.println("1. Uplata humanitarne pomoci");
				klijentOutput.println("2. Registruj se");
				klijentOutput.println("3. Prijavi se");
				klijentOutput.println("4. Pregled ukupno sakupljenih sredstava");
				klijentOutput.println("5. Pregled transakcija");
				klijentOutput.println("6. Izlaz");
								
				opcija = Integer.parseInt(klijentInput.readLine());				
				
				switch (opcija) {
					case 1: 
						klijentOutput.println("Odabrali ste uplatu humanitarne pomoci");
						break;				
					case 2:
						klijentOutput.println("Odabrali ste registraciju");
						break;
					case 3:
						klijentOutput.println("Odabrali ste prijavljivanje");
						break;
					case 4:
						klijentOutput.println("Odabrali ste pregled ukupno sakpljenih sredstava");
						break;
					case 5:
						klijentOutput.println("Odabrali ste pregled transakcija");
						break;
					case 6:
						klijentOutput.println("***izlaz***");
						soketZaKomunikaciju.close();
						return;
					default:
						klijentOutput.println("Niste odabrali postojecu opciju!\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
