package experiments.field.complexity.benchmark;

import test.core.selfrunning.AllocatedObject;

public class Fields18LongTest extends Base{
	public static void main(String...args){
		Fields18LongTest o = new Fields18LongTest();
		o.test();
	}
	public void test() {
		TreeNode x = new TreeNode();
		TreeNode p = null;
		while (staticallyUnknown()) {
			if (staticallyUnknown()) {
				x.a = p;
			}
			if (staticallyUnknown()) {
				x.b = p;
			}
			if (staticallyUnknown()) {
				x.c = p;
			}
			if (staticallyUnknown()) {
				x.d = p;
			}
			if (staticallyUnknown()) {
				x.e = p;
			}
			if (staticallyUnknown()) {
				x.f = p;
			}
			if (staticallyUnknown()) {
				x.g = p;
			}
			if (staticallyUnknown()) {
				x.h = p;
			}
			if (staticallyUnknown()) {
				x.i = p;
			}
			if (staticallyUnknown()) {
				x.j = p;
			}
			if (staticallyUnknown()) {
				x.k = p;
			}
			if (staticallyUnknown()) {
				x.l = p;
			}
			if (staticallyUnknown()) {
				x.m = p;
			}
			if (staticallyUnknown()) {
				x.n = p;
			}
			if (staticallyUnknown()) {
				x.o = p;
			}
			if (staticallyUnknown()) {
				x.p = p;
			}
			if (staticallyUnknown()) {
				x.q = p;
			}
			if (staticallyUnknown()) {
				x.r = p;
			}
			p = x;
		}
		TreeNode t = null;
		if (staticallyUnknown()) {
			t = x.a;
		}
		if (staticallyUnknown()) {
			t = x.b;
		}
		if (staticallyUnknown()) {
			t = x.c;
		}
		if (staticallyUnknown()) {
			t = x.d;
		}
		if (staticallyUnknown()) {
			t = x.e;
		}
		if (staticallyUnknown()) {
			t = x.f;
		}

		if (staticallyUnknown()) {
			t = x.g;
		}
		if (staticallyUnknown()) {
			t = x.h;
		}
		if (staticallyUnknown()) {
			t = x.i;
		}
		if (staticallyUnknown()) {
			t = x.j;
		}
		if (staticallyUnknown()) {
			t = x.k;
		}
		if (staticallyUnknown()) {
			t = x.l;
		}

		if (staticallyUnknown()) {
			t = x.m;
		}
		if (staticallyUnknown()) {
			t = x.n;
		}
		if (staticallyUnknown()) {
			t = x.o;
		}
		if (staticallyUnknown()) {
			t = x.p;
		}
		if (staticallyUnknown()) {
			t = x.q;
		}
		if (staticallyUnknown()) {
			t = x.r;
		}
		TreeNode h = t;
		queryFor(h);
	}

	private class TreeNode implements AllocatedObject {
		TreeNode a = new TreeNode();
		TreeNode b = new TreeNode();
		TreeNode c = new TreeNode();
		TreeNode d = new TreeNode();
		TreeNode e = new TreeNode();
		TreeNode f = new TreeNode();
		TreeNode g = new TreeNode();
		TreeNode h = new TreeNode();
		TreeNode i = new TreeNode();
		TreeNode j = new TreeNode();
		TreeNode k = new TreeNode();
		TreeNode l = new TreeNode();
		TreeNode m = new TreeNode();
		TreeNode n = new TreeNode();
		TreeNode o = new TreeNode();
		TreeNode p = new TreeNode();
		TreeNode q = new TreeNode();
		TreeNode r = new TreeNode();
	}
}
