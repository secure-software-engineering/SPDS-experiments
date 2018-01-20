package client.slicing;

import java.io.BufferedReader;
import java.io.FileReader;

public class TimeStatReporter {	
	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(args[0]));
		String line = "";
		int mayAlias = 0;
		int total = 0;
		int cnt = 0;
		String prefix = System.getProperty("Prefix");
		while ((line = br.readLine()) != null) {			
			line = line.substring(line.indexOf(' ') + 1);
			
			String[] nums = line.split("#");
			mayAlias += Integer.parseInt(nums[0]);
			total += Integer.parseInt(nums[1]);
			cnt++;
		}
		
		br.close();
		
		System.out.println(prefix + ": " + getRatio(mayAlias, cnt) + ", " + getRatio(total, cnt));
	}

	private static double getRatio(int m, int n) {
		return ((double)m) / ((double)n);
	}
}
