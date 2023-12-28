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
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class ClientHandler extends Thread {
	
	Socket soketZaKomunikaciju = null;
	
	PrintStream klijentOutput = null;
	BufferedReader klijentInput = null;
	
	final File ukupnaSredstva = new File("ukupna_sredstva.txt");
	
	boolean prijavljen = false;
	String korisnickoIme = null;
	
	public ClientHandler(Socket soketZaKomunikaciju) {
		this.soketZaKomunikaciju = soketZaKomunikaciju;

	}
	
	@Override
	public void run() {
		
		try {
			
			klijentOutput = new PrintStream(soketZaKomunikaciju.getOutputStream());
			klijentInput = new BufferedReader(new InputStreamReader(soketZaKomunikaciju.getInputStream()));
			
			klijentOutput.println("Veza uspesno uspostavljena");
			
			String opcija;
			
			while (true) {
				
				meni();

				opcija = klijentInput.readLine();
				
				switch (opcija) {
					case "1": 
						klijentOutput.println("Odabrali ste uplatu humanitarne pomoci");
						uplata(klijentOutput, klijentInput);
						break;				
					case "2":
						klijentOutput.println("Odabrali ste registraciju");
						registracija();
					break;
					case "3":
						klijentOutput.println("Odabrali ste prijavljivanje");
						prijava();
						break;
					case "4":
						klijentOutput.println("Odabrali ste pregled ukupno sakpljenih sredstava");
						pregledSredstava();
						break;
					case "5":
					if (prijavljen) {
						klijentOutput.println("Odabrali ste pregled transakcija");
						pregledTransakcija();
					} else {
						klijentOutput.println("Morate biti prijavljeni da biste imali pristup ovoj opciji!");
					}
					break;
					case "6":							
						klijentOutput.println("*** izlaz ***");
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
		
		klijentOutput.println("Izaberite opciju(1-6): ");
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
		
		GregorianCalendar datumVreme = null;
			
		try {
			while (!imeIsValid) {
				klijentOutput.println("Unesite ime: ");
				ime = klijentInput.readLine();
				if (ime == null || ime.contains(" ") || ime.isBlank()) {
					klijentOutput.println("Polje ime ne sme biti prazno i ne sme sadrzati razmake!");
					continue;
				}
				imeIsValid = true;
			}
		} catch (IOException e) {
			klijentOutput.println("Greska prilikom unosa!");
			return;
		}			
		
		try {
			while (!prezimeIsValid) {
				klijentOutput.println("Unesite prezime: ");
				prezime = klijentInput.readLine();
				if (prezime == null || ime.contains(" ") || prezime.isBlank()) {
					klijentOutput.println("Polje prezime ne sme biti prazno i ne sme sadrzati razmake!");
					continue;
				}
				prezimeIsValid = true;	
			}
		} catch (IOException e) {
			klijentOutput.println("Greska prilikom unosa!");
			return;
		}			
		
		try {
			while (!adresaIsValid) {
				klijentOutput.println("Unesite adresu: ");
				adresa = klijentInput.readLine();
				if (adresa == null || adresa.isBlank()) {
					klijentOutput.println("Polje adresa ne sme biti prazno!");
					continue;
				}
				adresaIsValid = true;	
			}
		} catch (IOException e) {
			klijentOutput.println("Greska prilikom unosa!");
			return;
		}			
		
		if (!prijavljen) {
			
			try {
				while (!brKarticeIsValid) {
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
				}
			} catch (IOException e) {
				klijentOutput.println("Greska prilikom unosa!");
				return;
			}
			
		} else {
			
			brKartice = vratiBrKarticeNaOsnovuUsername(korisnickoIme);
			
			if (brKartice == null) {
				klijentOutput.println("Doslo je do greske prilikom ucitavanja vase kartice.");
				return;
			}
			
		}
			
		try {
			while (!cvvIsValid) {
				klijentOutput.println("Unesite CVV broj(trocifren broj): ");
				cvv = klijentInput.readLine();
				if (cvv == null || !cvv.matches("\\d{3}")) {
					klijentOutput.println("Niste ispravno uneli CVV!");
					continue;
				}
				if (!cvvOdgovaraBrKartice(cvv, brKartice)) {
					klijentOutput.println("CVV se ne poklapa sa brojem kartice!");
					continue;
				}
				cvvIsValid = true;
			}
		} catch (IOException e) {
			klijentOutput.println("Greska prilikom unosa!");
			return;
		}
			
		try {
			while (!iznosIsValid) {
				klijentOutput.println("Unesite iznos: ");
				iznos = klijentInput.readLine();
				if (iznos == null || iznos.isBlank() || Integer.parseInt(iznos) < 200) {
					klijentOutput.println("Minimalan iznos je 200 dinara!");
					continue;
				}
				iznosIsValid = true;
			}
		} catch (NumberFormatException e) {
			klijentOutput.println("Iznos koji ste uneli nije validan!");
			return;
		} catch (IOException e) {
			klijentOutput.println("Greska prilikom unosa!");
			return;
		}			
		
		azurirajUkupno(iznos);
		
		datumVreme = new GregorianCalendar();
		
		String datum = String.valueOf(datumVreme.get(GregorianCalendar.DAY_OF_MONTH)) + "." +
				String.valueOf(datumVreme.get(GregorianCalendar.MONTH)) + "." +
				String.valueOf(datumVreme.get(GregorianCalendar.YEAR)) + ".";
		
		String vreme = String.valueOf(datumVreme.get(GregorianCalendar.HOUR_OF_DAY)) + ":" +
				(datumVreme.get(GregorianCalendar.MINUTE) < 10 ? 
						"0" + String.valueOf(datumVreme.get(GregorianCalendar.MINUTE)) :
							String.valueOf(datumVreme.get(GregorianCalendar.MINUTE)));
		
		ArrayList<String> pom = new ArrayList<>(5);
		pom.add(ime);
		pom.add(prezime);
		pom.add(datum);
		pom.add(vreme);
		pom.add(iznos);
		
		kesirajUplatu(pom);
		
		try (PrintWriter out = 
				new PrintWriter(
						new BufferedWriter(
								new FileWriter(ime + prezime + (new GregorianCalendar()).getTimeInMillis() + ".txt")))) {
						
			out.print("Ime: " + ime + "\nPrezime: " + prezime + "\nAdresa: " + adresa + 
					"\nDatum i vreme uplate: " + String.valueOf(datumVreme.getTime()) + "\nIznos: " + iznos);

		} catch (IOException e) {
			e.printStackTrace();		
		}
		
		klijentOutput.println("Uspesno ste izvrsili uplatu!");
	
	}
	
	private String vratiBrKarticeNaOsnovuUsername(String username) {
		try (BufferedReader in = new BufferedReader(new FileReader("registrovaniKorisnici.txt"))) {
			
			String pom;
			String[] pomNiz;
			
			while ((pom = in.readLine()) != null) {
				
				pomNiz = pom.split(";");
				
				if (pomNiz[0].equals(username)) {
						return pomNiz[5];
				}
		
			}
			
		} catch (FileNotFoundException e) {
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		return null;	
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
		
		try {
			while (!usernameIsValid) {
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
			}
		} catch (IOException e) {
			klijentOutput.println("Greska prilikom unosa!");
			return;
		}			
		
		try {
			while (!passIsValid) {
				klijentOutput.println("Unesite password: ");
				pass = klijentInput.readLine();
				if (pass == null || pass.contains(" ") || pass.isBlank()) {
					klijentOutput.println("Password ne sme biti prazan i ne sme sadrzati razmake!");
					continue;
				}
				passIsValid = true;
			}
		} catch (IOException e) {
			klijentOutput.println("Greska prilikom unosa!");
			return;
		}			
		
		try {
			while (!imeIsValid) {
				klijentOutput.println("Unesite ime: ");
				ime = klijentInput.readLine();
				if (ime == null || ime.contains(" ") || ime.isBlank()) {
					klijentOutput.println("Polje ime ne sme biti prazno i ne sme sadrzati razmake!");
					continue;
				}
				imeIsValid = true;
			}
		} catch (IOException e) {
			klijentOutput.println("Greska prilikom unosa!");
			return;
		}			
		
		try {
			while (!prezimeIsValid) {
				klijentOutput.println("Unesite prezime: ");
				prezime = klijentInput.readLine();
				if (prezime == null || ime.contains(" ") || prezime.isBlank()) {
					klijentOutput.println("Polje prezime ne sme biti prazno i ne sme sadrzati razmake!");
					continue;
				}
				prezimeIsValid = true;	
			}
		} catch (IOException e) {
			klijentOutput.println("Greska prilikom unosa!");
			return;
		}			
		
		try {
			while (!jmbgIsValid) {
				klijentOutput.println("Unesite jmbg: ");
				jmbg = klijentInput.readLine();
				if (jmbg == null || jmbg.contains(" ") || jmbg.isBlank()) {
					klijentOutput.println("Jmbg ne sme biti prazan i ne sme sadrzati razmake!");
					continue;
				}
				jmbgIsValid = true;
			}
		} catch (IOException e) {
			klijentOutput.println("Greska prilikom unosa!");
			return;
		}			
		
		try {
			while (!brKarticeIsValid) {
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
			}
		} catch (IOException e) {
			klijentOutput.println("Greska prilikom unosa!");
			return;
		}			
		
		try {
			while (!emailIsValid) {
				klijentOutput.println("Unesite email: ");
				email = klijentInput.readLine();
				if (email == null || email.contains(" ") || email.isBlank()) {
					klijentOutput.println("Email ne sme biti prazan i ne sme sadrzati razmake!");
					continue;
				}
				emailIsValid = true;
			}
		} catch (IOException e) {
			klijentOutput.println("Greska prilikom unosa!");
			return;
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
		
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("registrovaniKorisnici.txt", true)))) {
			
			out.println(podaci.get(0) + ";" + podaci.get(1) + ";" + podaci.get(2) + ";" + podaci.get(3) + ";" +
					podaci.get(4) + ";" + podaci.get(5) + ";" + podaci.get(6));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void prijava() {
		
		String username = null,
				pass = null;
		
		boolean usernameIsValid = false,
				passIsValid = false;
		
		try {
			while (!usernameIsValid) {
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
			}
		} catch (IOException e) {
			klijentOutput.println("Greska prilikom unosa!");
			return;
		}			
		
		try {
			while (!passIsValid) {
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
			}
		} catch (IOException e) {
			klijentOutput.println("Greska prilikom unosa!");
			return;
		}			
		
		prijavljen = true;
		korisnickoIme = username;
		
		klijentOutput.println("Uspesno ste se prijavili!");
		
	}

	private boolean passOdgovaraUsername(String username, String pass) {
		try (BufferedReader in = new BufferedReader(new FileReader("registrovaniKorisnici.txt"))) {
			
			String pom;
			String[] pomNiz;
			
			while ((pom = in.readLine()) != null) {
				
				pomNiz = pom.split(";");
				
				if (pomNiz[0].equals(username)) {
						if (pomNiz[1].equals(pass)) {
							return true;
						}
						return false;
				}
		
			}
			
		} catch (FileNotFoundException e) {
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;	
	}

	private boolean usernamePostoji(String username) {		
		try (BufferedReader in = new BufferedReader(new FileReader("registrovaniKorisnici.txt"))) {
		
			String pom;
			String[] pomNiz;
			
			while ((pom = in.readLine()) != null) {
				
				pomNiz = pom.split(";");
				
				if (pomNiz[0].equals(username)) {
						return true;
				}
		
			}
			
		} catch (FileNotFoundException e) {
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		return false;		
	}
	
	private void pregledTransakcija() {
		
		List<ArrayList<String>> uplate = new LinkedList<>();
		ArrayList<String> pomLista = null;
		String pom = null;
		String[] pomNiz = null;
		
		try (BufferedReader in = new BufferedReader(new FileReader("kesiraneUplate.txt"))) {		
			
			while ((pom = in.readLine()) != null) {
				
				pomNiz = pom.split(";");
				pomLista = new ArrayList<>(5);
				
				pomLista.add(pomNiz[0]);
				pomLista.add(pomNiz[1]);
				pomLista.add(pomNiz[2]);
				pomLista.add(pomNiz[3]);
				pomLista.add(pomNiz[4]);
				
				uplate.add(pomLista);
				
			}
			
		} catch (FileNotFoundException e) {

			klijentOutput.println("Jos uvek nema nijedne uplate.");
			return;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ListIterator<ArrayList<String>> iterator = uplate.listIterator(uplate.size());
		int brojac = 1;
		
		klijentOutput.println("Najskorije uplate:\n");
		
		while (brojac <= 10 && iterator.hasPrevious()) {
			
			pomLista = iterator.previous();	
			klijentOutput.println(brojac + ".");
			klijentOutput.println("Ime: " + pomLista.get(0));
			klijentOutput.println("Prezime: " + pomLista.get(1));
			klijentOutput.println("Datum: " + pomLista.get(2));
			klijentOutput.println("Vreme: " + pomLista.get(3));
			klijentOutput.println("Iznos: " + pomLista.get(4) + " din");
			klijentOutput.println();
			brojac++;
			
		}
		
	}
	
	private void kesirajUplatu(ArrayList<String> pom) {
		
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("kesiraneUplate.txt", true)))) {
			
			out.println(pom.get(0) + ";" + pom.get(1) + ";" + pom.get(2) + ";" 
					+ pom.get(3) + ";" + pom.get(4));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
