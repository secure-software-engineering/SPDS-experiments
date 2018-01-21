package dacapo;

import typestate.ap.impl.statemachines.HasNextStateMachine;

public class Util {
	public static boolean strongUpdates() {
		return (System.getProperty("strongUpdates") != null && Boolean.parseBoolean(System.getProperty("strongUpdates")));
	}

	public static boolean aliasing() {
		return (System.getProperty("aliasing") != null && Boolean.parseBoolean(System.getProperty("aliasing")));
	}
	public static Class selectTypestateMachine(String rule) {
		switch (rule) {
		case "IteratorHasNext":
			return HasNextStateMachine.class;
		case "KeyStore":
			return typestate.ap.impl.statemachines.KeyStoreStateMachine.class;
		case "URLConnection":
			return typestate.ap.impl.statemachines.URLConnStateMachine.class;
		case "EmptyVector":
			return typestate.ap.impl.statemachines.VectorStateMachine.class;
		case "InputStreamCloseThenRead":
			return typestate.ap.impl.statemachines.alloc.InputStreamStateMachine.class;
		case "PipedInputStream":
			return typestate.ap.impl.statemachines.PipedInputStreamStateMachine.class;
		case "OutputStreamCloseThenWrite":
			return typestate.ap.impl.statemachines.alloc.OutputStreamStateMachine.class;
		case "PipedOutputStream":
			return typestate.ap.impl.statemachines.PipedOutputStreamStateMachine.class;
		case "PrintStream":
			return typestate.ap.impl.statemachines.alloc.PrintStreamStateMachine.class;
		case "PrintWriter":
			return typestate.ap.impl.statemachines.alloc.PrintWriterStateMachine.class;
		case "Signature":
			return typestate.ap.impl.statemachines.SignatureStateMachine.class;
		}
		throw new RuntimeException("Select an appropriate rule");
	}
}
