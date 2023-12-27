package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.GregorianCalendar;

public class ClientHandler extends Thread {
	
	Socket soketZaKomunikaciju = null;
	
	PrintStream klijentOutput = null;
	BufferedReader klijentInput = null;
	
	final File ukupnaSredstva = new File("ukupna_sredstva.txt");
	
	public ClientHandler(Socket soketZaKomunikaciju) {
		this.soketZaKomunikaciju = soketZaKomunikaciju;

	}
	
	@Override
	public void run() {
		
		try {
			
			klijentOutput = new PrintStream(soketZaKomunikaciju.getOutputStream());
			klijentInput = new BufferedReader(new InputStreamReader(soketZaKomunikaciju.getInputStream()));
			
			klijentOutput.println("Veza uspesno uspostavljena");
			
			int opcija;
			
			while (true) {
				
				meni();

				opcija = Integer.parseInt(klijentInput.readLine());				
				
				switch (opcija) {
					case 1: 
						klijentOutput.println("Odabrali ste uplatu humanitarne pomoci");
						uplata(klijentOutput, klijentInput);
						break;				
					case 2:
						klijentOutput.println("Odabrali ste registraciju");
						break;
					case 3:
						klijentOutput.println("Odabrali ste prijavljivanje");
						break;
					case 4:
						klijentOutput.println("Odabrali ste pregled ukupno sakpljenih sredstava");
						pregledSredstava();
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
		} catch (SocketException e) {
			
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	private void meni() {
		
		klijentOutput.println("Izaberite opciju(1-5): ");
		klijentOutput.println("1. Uplata humanitarne pomoci");
		klijentOutput.println("2. Registruj se");
		klijentOutput.println("3. Prijavi se");
		klijentOutput.println("4. Pregled ukupno sakupljenih sredstava");
		klijentOutput.println("5. Pregled transakcija");
		klijentOutput.println("6. Izlaz");
		
	}
	
	private void uplata(PrintStream klijentOutput, BufferedReader klijentInput) {
		
		boolean imeIsValid = false,
		 		prezimeIsValid = false,
		 		adresaIsValid = false,
		 		brKarticeIsValid = false,
		 		cvvIsValid = false,
		 		iznosIsValid = false;
		
		String ime = null, 
			   prezime = null, 
			   adresa = null,
			   brKartice = null, 
			   cvv = null, 
			   iznos = null;				
		
		while (!imeIsValid) {
			try {
				
				klijentOutput.println("Unesite ime: ");
				ime = klijentInput.readLine();
				if (ime.contains(" ") || ime.isBlank()) {
					klijentOutput.println("Polje ime ne sme biti prazno i ne sme sadrzati razmake!");
					continue;
				}
				imeIsValid = true;
				
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		
		while (!prezimeIsValid) {
			try {
				
				klijentOutput.println("Unesite prezime: ");
				prezime = klijentInput.readLine();
				if (prezime.contains(" ") || prezime.isBlank()) {
					klijentOutput.println("Polje prezime ne sme biti prazno i ne sme sadrzati razmake!");
					continue;
				}
				prezimeIsValid = true;	
				
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		
		while (!adresaIsValid) {
			try {
				
				klijentOutput.println("Unesite adresu: ");
				adresa = klijentInput.readLine();
				if (adresa.isBlank()) {
					klijentOutput.println("Polje adresa ne sme biti prazno!");
					continue;
				}
				adresaIsValid = true;	
				
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		
		while (!brKarticeIsValid) {
			try {
				
				klijentOutput.println("Unesite broj kartice u formatu XXXX-XXXX-XXXX-XXXX: ");
				brKartice = klijentInput.readLine();
				if (!brKartice.matches("\\d{4}-\\d{4}-\\d{4}-\\d{4}")) {
					klijentOutput.println("Neispravan format kartice!");
					continue;
				}
				if (!brKarticePostoji(brKartice)) {
					klijentOutput.println("Kartica ne postoji u bazi!");
					continue;
				}
				brKarticeIsValid = true;
				
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		
		while (!cvvIsValid) {
			try {
				
				klijentOutput.println("Unesite CVV broj(trocifren broj): ");
				cvv = klijentInput.readLine();
				if (!cvv.matches("\\d{3}")) {
					klijentOutput.println("Niste ispravno uneli CVV!");
					continue;
				}
				if (!cvvOdgovaraBrKartice(cvv, brKartice)) {
					klijentOutput.println("CVV se ne poklapa sa unetim brojem kartice!");
					continue;
				}
				cvvIsValid = true;
				
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		
		while (!iznosIsValid) {
			try {
				
				klijentOutput.println("Unesite iznos: ");
				iznos = klijentInput.readLine();
				if (Integer.parseInt(iznos) < 200) {
					klijentOutput.println("Minimalan iznos je 200 dinara!");
					continue;
				}
				iznosIsValid = true;
				
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		
		azurirajUkupno(iznos);
		
		try (PrintWriter out = 
				new PrintWriter(
						new BufferedWriter(
								new FileWriter(ime + prezime + (new GregorianCalendar()).getTimeInMillis() + ".txt")))) {
						
			out.print("Ime: " + ime + "\nPrezime: " + prezime + "\nAdresa: " + adresa + 
					"\nDatum i vreme uplate: " + (new GregorianCalendar()).getTime() + "\nIznos: " + iznos);

		} catch (IOException e) {
			e.printStackTrace();		
		}						
	
	}
	
	private boolean brKarticePostoji(String brKartice) {		
		try (BufferedReader in = new BufferedReader(new FileReader("BazaKarticeCVV.txt"))) {
			
			String pom;
			while ((pom = in.readLine()) != null) {
				if (pom.equals(brKartice)) {
					return true;
				}
			}
			return false;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;		
	}

	private boolean cvvOdgovaraBrKartice(String cvv, String brKartice) {
		try (BufferedReader in = new BufferedReader(new FileReader("BazaKarticeCVV.txt"))) {
			
			String pom;
			while ((pom = in.readLine()) != null) {
				if (pom.equals(brKartice)) {
					if (cvv.equals(in.readLine())) {
						return true;
					}
					return false;
				}
			}
			return false;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void pregledSredstava() {
		
		String in = null;
		
		try (BufferedReader fileReader = new BufferedReader(new FileReader(ukupnaSredstva))) {
			
			in = fileReader.readLine();
			klijentOutput.println("Do sada je sakupljeno: " + in + " din");
						
		} catch (FileNotFoundException e) {
			
			// Nijednom jos nije izvrsena uplata (fajl nije kreiran)
			klijentOutput.println("Do sada je sakupljeno: 0 din");
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void azurirajUkupno(String iznos) {
		
		String in;
		
		try (PrintWriter fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(ukupnaSredstva, true)));
				BufferedReader fileReader = new BufferedReader(new FileReader(ukupnaSredstva))) {
			
			in = fileReader.readLine();
			
			// Prvi put otvaramo fajl tj. tek je kreiran u ovoj metodi - pisemo samo iznos
			if (in == null || in.isEmpty()) {
				fileWriter.println(iznos);	
				return;
			}
			
			// Otvaramo novi FileWriter bez append moda koji ce obrisati prethodni sadrzaj unutar fajla
			(new FileWriter(ukupnaSredstva)).close();
			
			fileWriter.println(Integer.parseInt(in) + Integer.parseInt(iznos));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
				
	}
	
}
