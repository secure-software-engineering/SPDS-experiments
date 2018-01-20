package alias;

import java.util.Iterator;
import java.util.LinkedList;

import edu.osu.cse.pa.spg.AbstractAllocNode;

public class SummaryRecord {
	AbstractAllocNode beg;
	AbstractAllocNode end;
	LinkedList<CtxPair> ctxSumm;
	LinkedList<NumberedFldPair> fldSumm;
	
	LinkedList<NumberedObject> seqSumm;
	
	public SummaryRecord(AbstractAllocNode beg, AbstractAllocNode end,
			LinkedList<CtxPair> ctxSumm, LinkedList<NumberedFldPair> fldSumm) {
		this.beg = beg;
		this.end = end;
		this.ctxSumm = ctxSumm;
		this.fldSumm = fldSumm;
		this.seqSumm = new LinkedList<NumberedObject>();
		computeSeqSumm();
	}
	
	private void computeSeqSumm() {
		boolean shouldContinue = false;
		if (ctxSumm.isEmpty()) {
			if (!fldSumm.isEmpty()) {				
				seqSumm.addAll(fldSumm);
			}
		} else {
			if (fldSumm.isEmpty()) {
				seqSumm.addAll(ctxSumm);
			} else {
				shouldContinue = true;
			}
		}
		if (!shouldContinue) {
			return;
		}
		Iterator<CtxPair> it1 = ctxSumm.iterator();
		Iterator<NumberedFldPair> it2 = fldSumm.iterator();		
		CtxPair cp = it1.next();
		FldPair fp = it2.next();

		while (true) {
			if (cp.getId() < fp.getId()) {
				seqSumm.add(cp);
				if (it1.hasNext()) {
					cp = it1.next();
				} else {
					while (it2.hasNext()) {
						seqSumm.add(it2.next());
					}
					return;
				}
			} else {
				seqSumm.add(fp);
				if (it2.hasNext()) {
					fp = it2.next();
				} else {
					while (it1.hasNext()) {
						seqSumm.add(it1.next());
					}
					return;
				}
			}
		}
	}
	
	public LinkedList<NumberedObject> getSeqSumm() {
		return seqSumm;
	}
	
	public AbstractAllocNode begin() {
		return beg;
	}
	
	public AbstractAllocNode end() {
		return end;
	}
	
	public LinkedList<CtxPair> getCtxSumm() {
		return ctxSumm;
	}
	
	public LinkedList<NumberedFldPair> getFldSumm() {
		return fldSumm;
	}
	
//	public void dump() {
//		System.out.println(" [Summary] begin: " + beg);
//		System.out.println(" [Summary] end: " + end);
//		dumpCtxSumm();
//		dumpFldSumm();
//	}
//	
//	public void dumpCtxSumm() {
//		System.out.println(" [Summary] Ctx Summ:");
//		for (CtxPair cp : ctxSumm) {
//			System.out.println("  [Summary] " + cp.getEdge());
//		}
//		System.out.println();
//	}
//	
//	public void dumpFldSumm() {
//		System.out.println(" [Summary] Fld Summ:");
//		for (FldPair fp : fldSumm) {
//			System.out.println("  [Summary] " + fp.getEdge());
//		}
//		System.out.println();
//	}
}
