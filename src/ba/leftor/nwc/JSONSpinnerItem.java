package ba.leftor.nwc;

import java.util.ArrayList;

public class JSONSpinnerItem {
	public int id;
	public String text;
	
	@Override
	public String toString() {
		return text;
	}

	@Override
	public boolean equals(Object o) {
		JSONSpinnerItem tmp=(JSONSpinnerItem)o;
		return tmp.id==this.id;
	}
}
