package client.slicing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class PrecisionReporter {
	private static HashMap<Triple, String> read(String fn) throws Exception {
		HashMap<Triple, String> map = new HashMap<Triple, String>();
		BufferedReader br = new BufferedReader(new FileReader(fn));
		String line = "";
		while ((line = br.readLine()) != null) {
			line = line.substring(line.indexOf(' ') + 1);
			String[] args = line.split("!");
//			System.out.println(args.length);
			String mtd = args[0];
			String v1 = args[1];
			String v2 = args[2];
			String res = args[3];
			map.put(new Triple(mtd, v1, v2), res);
		}
		br.close();
		
		return map;
	}
	
	public static void main(String[] args) throws Exception {
		HashMap<Triple, String> spaMap = read(args[0]);
		HashMap<Triple, String> manuMap = read(args[1]);
		
		double diff = 0;
		double total = 0;
		for (Map.Entry<Triple, String> ent : manuMap.entrySet()) {
			Triple t = ent.getKey();
			String r1 = ent.getValue();
			String r2 = spaMap.get(t);
			if (r2 != null) {
				total++;
				if (!r1.equals(r2)) {
					diff++;
				}
			}
		}
		String benchName = System.getProperty("BenchName");
		System.out.println(benchName + " precision: " + (1 - diff / total) * 100 + "%");
	}
}

class Triple {
	String s1;
	String s2;
	String s3;
	
	public Triple(String s1, String s2, String s3) {
		this.s1 = s1;
		this.s2 = s2;
		this.s3 = s3;
	}
	
	public int hashCode() {
		return s1.hashCode() + s2.hashCode() + s3.hashCode();
	}
	
	public boolean equals(Object o) {
		if (o instanceof Triple) {
			Triple t = (Triple) o;
			
			return
				this.s1.equals(t.s1) &&
				this.s2.equals(t.s2) &&
				this.s3.equals(t.s3);
		}
		
		return false;
	}
}