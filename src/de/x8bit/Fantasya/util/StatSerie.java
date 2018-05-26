package de.x8bit.Fantasya.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author hb
 */
public class StatSerie extends ArrayList<Integer> {
	private static final long serialVersionUID = 1492254584960176777L;

	
	boolean sorted = false;

	@Override
	public void add(int index, Integer element) {
		super.add(index, element);
		sorted = false;
	}

	@Override
	public boolean addAll(Collection<? extends Integer> c) {
		sorted = false;
		return super.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Integer> c) {
		sorted = false;
		return super.addAll(index, c);
	}

	@Override
	public Integer remove(int index) {
		sorted = false;
		return super.remove(index);
	}

	@Override
	public boolean remove(Object o) {
		sorted = false;
		return super.remove(o);
	}

	@Override
	protected void removeRange(int fromIndex, int toIndex) {
		sorted = false;
		super.removeRange(fromIndex, toIndex);
	}

	@Override
	public Integer set(int index, Integer element) {
		sorted = false;
		return super.set(index, element);
	}



	public int getMinValue() {
		int min = Integer.MAX_VALUE;
		for (int value : this) {
			if (value < min) min = value;
		}
		return min;
    }

    public int getMaxValue() {
		int max = Integer.MIN_VALUE;
		for (int value : this) {
			if (value > max) max = value;
		}
		return max;
    }

    public double getAverage() {
        int summe = 0;
        for (int n : this) {
            summe += n;
        }
        return (double) summe / (double) this.size();
    }

	/**
	 * @param value der fragliche Wert
	 * @return 0..1 - Rangwert des nächsthöheren Wertes
	 */
	public float getQuantilForValue(int value) {
		if (!isSorted()) {
			Collections.sort(this);
			sorted = true;
		}

		for (int i = 0; i < this.size(); i++) {
			if (this.get(i) > value) return (float)i / (float)size();
		}
		return 1f;
	}

	public boolean isSorted() {
		return sorted;
	}

}
