package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class ClientHandler extends Thread {
	
	Socket soketZaKomunikaciju = null;
	
	PrintStream klijentOutput = null;
	BufferedReader klijentInput = null;
	
	final File ukupnaSredstva = new File("ukupna_sredstva.txt");
	
	boolean prijavljen = false;
	
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
						registracija();
					break;
					case 3:
						klijentOutput.println("Odabrali ste prijavljivanje");
						prijava();
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
				if (ime == null || ime.contains(" ") || ime.isBlank()) {
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
				if (prezime == null || ime.contains(" ") || prezime.isBlank()) {
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
				if (adresa == null || adresa.isBlank()) {
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
				if (brKartice == null || !brKartice.matches("\\d{4}-\\d{4}-\\d{4}-\\d{4}")) {
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
		
		if (!prijavljen) {
			while (!cvvIsValid) {
				try {
					
					klijentOutput.println("Unesite CVV broj(trocifren broj): ");
					cvv = klijentInput.readLine();
					if (cvv == null || !cvv.matches("\\d{3}")) {
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
		}
		
		while (!iznosIsValid) {
			try {
				
				klijentOutput.println("Unesite iznos: ");
				iznos = klijentInput.readLine();
				if (iznos == null || Integer.parseInt(iznos) < 200) {
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
	
	private void registracija() {
		
		String username = null,
			pass = null,
			ime = null,
			prezime = null,
			jmbg = null,
			brKartice = null,
			email = null;
		
		boolean usernameIsValid = false, 
				passIsValid = false, 
				imeIsValid = false,
				prezimeIsValid = false,
				jmbgIsValid = false,
				brKarticeIsValid = false,
				emailIsValid = false;
		
		while (!usernameIsValid) {
			try {
				
				klijentOutput.println("Unesite username: ");
				username = klijentInput.readLine();
				
				if (username == null || username.contains(" ") || username.isBlank()) {
					klijentOutput.println("Username ne sme biti prazan i ne sme sadrzati razmake!");
					continue;
				}
				if (usernamePostoji(username)) {
					klijentOutput.println("Username koji ste uneli je zauzet!");
					continue;
				}
				
				usernameIsValid = true;
				
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		
		while (!passIsValid) {
			try {
				
				klijentOutput.println("Unesite password: ");
				pass = klijentInput.readLine();
				if (pass == null || pass.contains(" ") || pass.isBlank()) {
					klijentOutput.println("Password ne sme biti prazan i ne sme sadrzati razmake!");
					continue;
				}
				passIsValid = true;
				
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		
		while (!imeIsValid) {
			try {
				
				klijentOutput.println("Unesite ime: ");
				ime = klijentInput.readLine();
				if (ime == null || ime.contains(" ") || ime.isBlank()) {
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
				if (prezime == null || ime.contains(" ") || prezime.isBlank()) {
					klijentOutput.println("Polje prezime ne sme biti prazno i ne sme sadrzati razmake!");
					continue;
				}
				prezimeIsValid = true;	
				
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		
		while (!jmbgIsValid) {
			try {
				
				klijentOutput.println("Unesite jmbg: ");
				jmbg = klijentInput.readLine();
				if (jmbg == null || jmbg.contains(" ") || jmbg.isBlank()) {
					klijentOutput.println("Jmbg ne sme biti prazan i ne sme sadrzati razmake!");
					continue;
				}
				jmbgIsValid = true;
				
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		
		while (!brKarticeIsValid) {
			try {
				
				klijentOutput.println("Unesite broj kartice u formatu XXXX-XXXX-XXXX-XXXX: ");
				brKartice = klijentInput.readLine();
				if (brKartice == null || !brKartice.matches("\\d{4}-\\d{4}-\\d{4}-\\d{4}")) {
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
		
		while (!emailIsValid) {
			try {
				
				klijentOutput.println("Unesite email: ");
				email = klijentInput.readLine();
				if (email == null || email.contains(" ") || email.isBlank()) {
					klijentOutput.println("Email ne sme biti prazan i ne sme sadrzati razmake!");
					continue;
				}
				emailIsValid = true;
				
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		
		klijentOutput.println("Uspesno ste se registrovali!");
		
		ArrayList<String> podaci = new ArrayList<>(7);
		
		podaci.add(username);
		podaci.add(pass);
		podaci.add(ime);
		podaci.add(prezime);
		podaci.add(jmbg);
		podaci.add(brKartice);
		podaci.add(email);
				
		azurirajBazuKorisnika(podaci);
		
	}
 		
	private void azurirajBazuKorisnika(ArrayList<String> podaci) {
		
		try (ObjectOutputStream out = new ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream("registrovaniKorisnici", true)))) {
			
			out.writeObject(podaci);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void prijava() {
		
		String username = null,
				pass = null;
		
		boolean usernameIsValid = false,
				passIsValid = false;
		
		while (!usernameIsValid) {
			try {
				
				klijentOutput.println("Unesite username: ");
				username = klijentInput.readLine();
				if (username == null || username.contains(" ") || username.isBlank()) {
					klijentOutput.println("Username ne sme biti prazan i ne sme sadrzati razmake!");
					continue;
				}
				if (!usernamePostoji(username)) {
					klijentOutput.println("Korisnik nije pronadjen, pokusajte ponovo!");
					continue;
				}
				usernameIsValid = true;
				
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		
		while (!passIsValid) {
			try {
				
				klijentOutput.println("Unesite password: ");
				pass = klijentInput.readLine();
				if (pass == null || pass.contains(" ") || pass.isBlank()) {
					klijentOutput.println("Password ne sme biti prazan i ne sme sadrzati razmake!");
					continue;
				}
				if (!passOdgovaraUsername(username, pass)) {
					klijentOutput.println("Neispravna lozinka, pokusajte ponovo!");
					continue;
				}
				passIsValid = true;
				
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		
		prijavljen = true;
		klijentOutput.println("Uspesno ste se prijavili!");
		
	}

	@SuppressWarnings("unchecked")
	private boolean passOdgovaraUsername(String username, String pass) {
		try (ObjectInputStream in = new ObjectInputStream(
				new BufferedInputStream(
						new FileInputStream("registrovaniKorisnici")))) {
			
			ArrayList<String> pom = null;
			
			while (true) {
				try {
					
					pom = (ArrayList<String>)in.readObject();
					if (pom.get(0).equals(username) && pom.get(1).equals(pass)) {
						return true;
					} 
					return false;
					
				} catch (EOFException e) {
					
				}catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private boolean usernamePostoji(String username) {		
		try (ObjectInputStream in = new ObjectInputStream(
				new BufferedInputStream(
						new FileInputStream("registrovaniKorisnici")))) {
			
			ArrayList<String> pom = null;
			
			while (true) {
				try {
					
					pom = (ArrayList<String>)in.readObject();
					if (pom.get(0).equals(username)) {
						return true;
					}
					
				} catch (EOFException e) {
					return false;
				}catch (ClassNotFoundException e) {
					e.printStackTrace();
					return false;
				}
			}
			
		} catch (FileNotFoundException e) {
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		return false;		
	}
	
}
