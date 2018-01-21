package experiments.main;

import experiments.dacapo.demand.driven.DacapoDemandDrivenExperiments;

public class DemandDrivenPointsTo {

	public static void main(String[] args) {
		DacapoDemandDrivenExperiments expr = new DacapoDemandDrivenExperiments(args[0], args[1]);
		expr.run();
	}

}
