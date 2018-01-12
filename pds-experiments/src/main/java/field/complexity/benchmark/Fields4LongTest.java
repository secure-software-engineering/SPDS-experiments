package field.complexity.benchmark;

import test.core.selfrunning.AllocatedObject;

public class Fields4LongTest extends Base{
	public static void main(String...args){
		Fields4LongTest o = new Fields4LongTest();
		o.test();
	}
	public void test() {
		TreeNode x = new TreeNode();
		TreeNode p = null;
		while(staticallyUnknown()){
			if(staticallyUnknown()){
				x.a = p;
			}
			if(staticallyUnknown()){
				x.b = p;
			}
			if(staticallyUnknown()){
				x.c = p;
			}
			if(staticallyUnknown()){
				x.d = p;
			}
			p = x;
		}
		TreeNode t = null;
		if(staticallyUnknown()){
			t = x.a;
		}
		if(staticallyUnknown()){
			t = x.b;
		}
		if(staticallyUnknown()){
			t = x.c;
		}
		if(staticallyUnknown()){
			t = x.d;
		}
		TreeNode h = t;
		queryFor(h);
	}

	private class TreeNode implements AllocatedObject{
		TreeNode a = new TreeNode();
		TreeNode b = new TreeNode();
		TreeNode c = new TreeNode();
		TreeNode d = new TreeNode();
	}
}
