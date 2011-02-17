package DnaDesignGUI.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

import DnaDesign.DDSeqDesigner;
import DnaDesign.DesignerOptions;
import DnaDesign.DomainDesigner;
import DnaDesign.DomainDesigner_SharedUtils;
import DnaDesign.DomainSequence;
import DnaDesign.DomainStructureData;
import DnaDesign.Config.CircDesigNAConfig;

public class DesignMultipleTimes {
	/**
	 * Input is a path to a file of the form
	 * NumTimesToRun
	 * TargetDirectory
	 * Prefix
	 * (Domain definitions)
	 * END
	 * (Molecule definitions)
	 * END
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException{
		Scanner in = new Scanner(System.in);
		int numTimesToRun = new Integer(readToken(in));
		int popSize = new Integer(readToken(in));
		int selfSimilarOpt = new Integer(readToken(in));
		double numSecondsEach = new Double(readToken(in));
		int maximumComplexSize = new Integer(readToken(in));
		double tournamentInterval = new Double(readToken(in));
		String targetDir = in.nextLine();
		String prefix = in.nextLine();
		String Domains = readToEnd(in);
		String Molecules = readToEnd(in);
		in.close();
		
		CircDesigNAConfig config = new CircDesigNAConfig();
		DomainStructureData dsd = new DomainStructureData(config);
		DomainStructureData.readDomainDefs(Domains, dsd);
		if (args.length==0){
			System.out.println("Entering design mode");
			for(int i = 1; i <= numTimesToRun; i++){

				File out2 = new File(targetDir+File.separator+prefix+i+".des");
				System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(out2))));
				DDSeqDesigner<DesignerOptions> defaultDesigner = DomainDesigner.getDefaultDesigner(Molecules, Domains, config);
				DesignerOptions options = defaultDesigner.getOptions();
				options.population_size.setState(popSize);
				options.selfSimilarityPenalty.setState(selfSimilarOpt);
				options.resourcePerMember.setState(tournamentInterval);
				designUsingCircDesigNA(defaultDesigner,numSecondsEach);
				System.err.println("Designed "+i);
				System.err.flush();
				System.out.close();
				
				out2 = new File(targetDir+File.separator+prefix+"rnd"+i+".des");
				System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(out2))));
				outputRandomDesigns(dsd);
				System.out.close();
				
				out2 = new File(targetDir+File.separator+prefix+"ra"+i+".des");
				System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(out2))));
				outputRationalDesign(dsd);
				System.out.close();
			}
		} else {
			System.out.println("Entering Evaluate mode");
			if (args[0].equals("-evaluate")){
				boolean goNupack = true;
				if (args.length >= 2){
					if (args[1].equals("-nopack")){
						goNupack = false;
					}
				}
				for(int i = 1; i <= numTimesToRun; i++){
					for(String rprefix : new String[]{prefix,prefix+"rnd",prefix+"ra"}){
						File out2 = new File(targetDir+File.separator+rprefix+i+".des");
						if (!out2.exists()){
							System.err.println("Could not find file "+rprefix+i+".des");
							continue;
						}
						File out3 = new File(targetDir+File.separator+rprefix+i+".eval");
						System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(out3))));
						RunEvaluation(out2,Molecules,dsd,maximumComplexSize, goNupack);
						System.err.println("Evaluated "+i);
						System.out.close();
					}
				}
			}
		}
	}

	private static void outputRandomDesigns(DomainStructureData dsd) {
		for(int itr = 1; itr <= 100; itr++){
			System.out.println("Iteration "+itr+" Score 0");
			for(int i = 0; i < dsd.domainLengths.length; i++){
				int len = dsd.domainLengths[i];
				System.out.println(dsd.getDomainName(i)+">");
				for(int j = 0; j < len; j++){
					System.out.print(dsd.Std.monomer.displayBase(1+(int)(Math.random()*4)));
				}
				System.out.println();
			}
			System.out.println();
		}
	}
	
	private static final String[] shortReedWords2005 = new String[]{
	     "CACATCACCAATATA", "TCCTCCAATTAATTA", "TCAATCTTTTTCAAT", "TATCTTTCCAATCTA", "TCATTCTTCTCTTAT", "ACTACAATCTCAATA", "TCCTAACCAAAAATT", "TTTACCTTTTCAAAT", "CAACTACATTTTCTA", "TCATCTTATCTCTCT", "TCATTAAATCCATCT", "CATAATACCTTCCTA", "ACTACTTACATTTCA", "TCTTCAATCTACTTA", "TTTACCTCTCTAATC", "ATCTCTTTTCTTTTC", "CTCTTTCACAAAAAT", "AACAAAACAACAAAT", "ACAAAATCTCTTACA", "ACCATTTTTTCACTA", "CTTAATTCTCTCACT", "CTCTCCTCAAATATT", "ATCATCCACATATTT", "TTCCACACTAAAATT", "ACCATCATTTATCTT", "TCTCCTTATTCATTT", "TACCATACCAATTTT", "AAACCACCATAATTA", "TCCTAATCCTCTTAA", "TCCTACTCATAACTA", "CTCAACTTTCAAATT", "AACTTTACTATCCAT", "ATTCACCAAACTTTA", "TATACCATCTTTCAA", "TCCACTAATAAAACT", "CAATAATCCAACATT", "CTCTAATTTCTTCCT", "CACCTACATCAAATA", "AACAAACCATAACTA", "TCTACCTATTCACTA",  
	};

	private static void outputRationalDesign(DomainStructureData dsd) {
		System.out.println("Iteration 1 Score 0");
		for(int i = 0; i < dsd.domainLengths.length; i++){
			int len = dsd.domainLengths[i];
			if (len > 15){
				System.err.println("Aborted rational design");
				return;
			}
			System.out.println(dsd.getDomainName(i)+">");
			//Output a domain of that length
			System.out.println(shortReedWords2005[i].substring(0,len));
		}
		System.out.println();
	}
	
	private static void designUsingCircDesigNA(
			DDSeqDesigner<DesignerOptions> defaultDesigner, double numSecondsEach) {
		long diff = 0;
		defaultDesigner.resume();
		long now = System.nanoTime();
		while(!defaultDesigner.isFinished()){
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if ((diff=System.nanoTime()-now) > numSecondsEach*1e9){
				break;
			}
		}
		defaultDesigner.abort();
		//System.err.print("HE");
		while(!defaultDesigner.isFinished()){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//System.err.println("Y!");
		System.out.println("END. TIME:");
		System.out.println(String.format("%.3f",diff/1e9));
		
	}

	public static void RunEvaluation(File out3, String molecules, DomainStructureData dsd, int maximumComplexSize, boolean goNupack) throws IOException {
		Scanner in = new Scanner(out3);
		System.out.print("ITR\t\tSCORE");
		if (goNupack){
			System.out.print("\t\tNUPACK");
		}
		System.out.println();
		while(in.hasNextLine()){
			String line = in.nextLine();
			System.out.flush();
			if (line.startsWith("Iteration")){
				int i = new Integer(line.split("\\s+")[1]);
				double score = new Double(line.split("\\s+")[3]);
				System.out.printf("%d\t\t%.3f",i,score);
				if (goNupack){
					double nupack = getDefectScore(in,dsd,molecules,maximumComplexSize);
					System.out.printf("\t\t%.3f",nupack);
				}
				System.out.println();
			}	
		}
		in.close();
	}
	

	private static double getDefectScore(Scanner in, DomainStructureData dsd, String molecules, int maximumComplexSize) throws IOException {
		String[] molecule = new String[1];
		StringBuffer seq = new StringBuffer();
		int[][] domains = new int[dsd.domainLengths.length][];
		for(int i = 0; i < domains.length; i++){
			domains[i] = new int[dsd.domainLengths[i]];
		}
		while(in.hasNextLine()){
			String line2 = in.nextLine();
			if (line2.trim().equals("")){
				break;
			}
			if (line2.contains(">")){
				addMolecule(molecule,dsd,seq,domains);
				seq = new StringBuffer();
				molecule[0] = line2.substring(0,line2.length()-1);
			} else {
				//Sequence containing line
				seq.append(line2);
			}
		}
		addMolecule(molecule,dsd,seq,domains);
		
		//Ok, put molecules together
		ArrayList<DomainSequence> alreadyPrintedSequences = new ArrayList();
		ArrayList<TestMolecule> moleculeParse = new ArrayList();
		for(String q : molecules.split("\n")){
			String[] a = q.split("\\s+");
			int ct = 0;
			splitLoop: for(String subStrand : a[1].split("}")){
				DomainSequence ds = new DomainSequence();
				ds.setDomains(subStrand,dsd,null);
				for(DomainSequence g : alreadyPrintedSequences){
					if (g.equals(ds)){
						continue splitLoop;
					}
				}
				alreadyPrintedSequences.add(ds);
				
				TestMolecule mol = new TestMolecule();
				mol.ID = a[0]+ct++;
				mol.molecule = subStrand;
				seq = new StringBuffer();
				int len = ds.length(domains);
				for(int i = 0; i < len; i++){
					seq.append(dsd.Std.monomer.displayBase(ds.base(i, domains, dsd.Std.monomer)));
				}
				mol.seq = seq.toString();
				moleculeParse.add(mol);
			}
		}
		
		//System.out.println(Arrays.deepToString(domains));
		
		//Nupack score
		File nupackDir = new File("nupackTest/");
		String prefix = "nupack"+System.nanoTime();
		System.err.println(prefix);
		nupackDir.mkdirs();
		File nupackList = new File("nupackTest/"+prefix+".in");
		{
			PrintWriter nuPackListOut = new PrintWriter(new FileWriter(nupackList));
			nuPackListOut.println(moleculeParse.size());
			for(TestMolecule k : moleculeParse){
				nuPackListOut.println(k.seq);
			}
			nuPackListOut.println(maximumComplexSize);
			nuPackListOut.close();
			
			PrintWriter conOut = new PrintWriter(new FileWriter(new File("nupackTest/"+prefix+".con")));
			for(int i = 0; i < moleculeParse.size(); i++){
				conOut.println("5e-6");
			}
			conOut.close();

			runProcess(System.getProperty("NUPACKHOME")+"/bin/complexes -material dna -mfe "+prefix,
					new String[]{
				"NUPACKHOME="+System.getProperty("NUPACKHOME")},
			nupackDir);
			
			/*
			runProcess(System.getProperty("NUPACKHOME")+"/bin/complexes -material dna -pairs "+prefix,
					new String[]{
				"NUPACKHOME="+System.getProperty("NUPACKHOME")},
			nupackDir);
			
			runProcess(System.getProperty("NUPACKHOME")+"/bin/concentrations -pairs "+prefix,
					new String[]{
				"NUPACKHOME="+System.getProperty("NUPACKHOME")},
			nupackDir);
			*/
			
		}
		
		//Ok, nupack ran. Analyze!
		return calculateDefectScoreFromNupackOutput(moleculeParse, dsd,"nupackTest/"+prefix);
	}
	private static void runProcess(String string, String[] env,	File nupackDir) throws IOException {
			System.err.println(">"+string);
	
			Process p = Runtime.getRuntime().exec(string,env,nupackDir);
			if(false){
				final Scanner in2 = new Scanner(p.getInputStream());
				new Thread(){
					public void run(){
						try {
							while(in2.hasNextLine()){
								System.err.println(in2.nextLine());
							}
						} catch (Throwable e){

						}
					}
				}.start();
				try {
					p.waitFor();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				in2.close();
			} else {
				try {
					p.waitFor();
					p.getOutputStream().close();
					p.getInputStream().close();
				    p.getErrorStream().close();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				p.destroy();
			}
	}

	private static double calculateDefectScoreFromNupackOutput(
			ArrayList<TestMolecule> moleculeParse, DomainStructureData dsd,
			String filePrefix) throws FileNotFoundException {
		LinkedList<TestComplex> complex = new LinkedList();
		if (true){
			Scanner inkey = new Scanner(new File(filePrefix+".ocx-key"));

			while(inkey.hasNextLine()){
				String line = inkey.nextLine();
				if (line.startsWith("%")){
					continue;
				}
				String[] line2 = line.split("\\s+");
				StringBuffer ds = new StringBuffer();
				//ds.append("[");
				
				for(int i = 2; i< line2.length; i++){
					ds.append(moleculeParse.get(new Integer(line2[i])-1).molecule);
					ds.append("|");
				}
				 
				/*
				for(int i = 0; i < moleculeParse.size(); i++){
					ds.append(moleculeParse.get(i).molecule);
					ds.append("|");
				}
				*/
				//ds.append("}");
				complex.add(new TestComplex(ds.toString(), dsd, line2[0]));
			}
			inkey.close();
		}
		Scanner inPpairs = new Scanner(new File(filePrefix+".ocx-mfe"));
		//Generate pseudo domains from dsd
		int[][] domain = new int[dsd.domainLengths.length][];
		for(int i = 0; i < domain.length; i++){
			domain[i] = new int[dsd.domainLengths[i]];
		}
		double TOTALSCORE = 0;
		StringBuffer dsstr = new StringBuffer();
		for(int i = 0; i < moleculeParse.size(); i++){
			dsstr.append(moleculeParse.get(i).molecule);
			dsstr.append("|");
		}
		//TestComplex toAnalyze = new TestComplex(dsstr.toString(),dsd,"1");
		while(inPpairs.hasNextLine()){
			String line = inPpairs.nextLine();
			if (line.startsWith("%")){
				continue;
			}
			if (line.trim().equals("")){
				continue;
			}
			//int num = new Integer(line);
			double fe = new Double(inPpairs.nextLine());
			TestComplex toAnalyze = complex.removeFirst();
			String struct = inPpairs.nextLine();
			for(; inPpairs.hasNextLine(); ){
				String nextLine = inPpairs.nextLine();
				if (nextLine.startsWith("%")){
					break;
				}
				String[] line2 = nextLine.split("\\s+");
				int i = new Integer(line2[0])-1;
				int j = new Integer(line2[1])-1;
				//double p = new Double(line2[2]);
				DomainSequence ds = toAnalyze.ds;
				int len = ds.length(domain);
				if (j < len){
					int domain1 = ds.domainAt(i, domain);
					int domain2 = ds.domainAt(j, domain);
						
					int offset1 = ds.offsetInto(i, domain, false); //Don't uncomplement - give offset 
					int offset2 = ds.offsetInto(j, domain, false);
					
					if (!DomainDesigner_SharedUtils.isAlignedAndShouldPair(ds, i, ds, j, domain)){
						System.err.print("Shouldn't pair: ");
						TOTALSCORE += 1;
					} else {
						System.err.print("Did pair: ");
					}
					System.err.println(dsd.getDomainName(domain1)+" "+offset1+" "+dsd.getDomainName(domain2)+" "+offset2);
				}
			}
			
		}
		inPpairs.close();
		return TOTALSCORE;
	}

	private static void addMolecule(String[] molecule, DomainStructureData dsd, StringBuffer seq, int[][] domains) {
		if (molecule[0]==null){
			return;
		}
		int domainNum = dsd.lookupDomainName(molecule[0]);
		for(int k = 0; k < seq.length(); k++){
			domains[domainNum][k] = dsd.Std.monomer.decodeBaseChar(seq.charAt(k));
		}
		/*
		TestMolecule newM = new TestMolecule();
		newM.ID = ""+domains.size();
		newM.molecule = molecule[0];
		newM.seq = seq.toString();
		//domains.add(newM);
		 */
		molecule[0] = null;
	}
	private static class TestComplex{
		public TestComplex(String domains, DomainStructureData dsd, String name){
			ds = new DomainSequence();
			ds.setDomains(domains, dsd, null);
			this.name = name;
		}
		public String name;
		private DomainSequence ds;
	}
	private static class TestMolecule{
		public String seq;
		public String molecule;
		public String ID;
	}

	public static String readToken(Scanner in) {
		String line = in.nextLine();
		return line.split("\\s+")[0];
	}

	public static String readToEnd(Scanner in) {
		StringBuffer toRet = new StringBuffer();
		while(in.hasNextLine()){
			String line = in.nextLine();
			if (line.equals("END")){
				break;
			}
			toRet.append(line+"\n");
		}
		return toRet.toString();
	}
}