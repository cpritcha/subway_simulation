package Subway;

import java.util.ArrayList;
import java.util.Collection;
import view.modeling.ViewableAtomic;

@SuppressWarnings("serial")
public class CircularList<E> extends ArrayList<E> {

	public CircularList() {
		super();
	}
	
	public CircularList(Collection<? extends E> c) {
		super(c);
	}
	
	public CircularList(int c) {
		super(c);
	}
	
	public E next(int index) {
		int length = this.size();
		if (index < length-1) {
			return this.get(index+1);
		}
		else {
			return this.get(0);
		}
	}
	
}
