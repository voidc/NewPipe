package org.schabi.newpipe.local.holder;

import android.view.ViewGroup;

import org.schabi.newpipe.R;
import org.schabi.newpipe.local.LocalItemBuilder;

public class LocalStatisticStreamGridItemHolder extends LocalStatisticStreamItemHolder {

	public LocalStatisticStreamGridItemHolder(LocalItemBuilder infoItemBuilder, ViewGroup parent) {
		super(infoItemBuilder, R.layout.list_stream_grid_item, parent);
	}
}
