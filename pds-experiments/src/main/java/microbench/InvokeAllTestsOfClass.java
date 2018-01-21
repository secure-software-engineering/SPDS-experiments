package microbench;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class InvokeAllTestsOfClass {
	public static void main(String... args) throws ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, InstantiationException {
		String testClassName = args[0];
		System.out.println(Arrays.toString(args));
		System.setProperty("analysis", args[1]);
		System.setProperty("method", args[2]);
		System.setProperty("simulate-natives", "true");
		System.setProperty("isTestsuite",Boolean.toString(true));
		Class<?> forName = Class.forName(testClassName);
		
		System.setProperty("outputCsvFile",args[3]);
		System.setProperty("aliasing", Boolean.toString(args[4].equals("true")));
		System.setProperty("strongUpdates", Boolean.toString(args[5].equals("true")));
		for (Method m : forName.getDeclaredMethods()) {
			if (m.getName().equals(System.getProperty("method"))) {
				m.invoke(forName.newInstance(), null);
			}
		}
		
	}

}
