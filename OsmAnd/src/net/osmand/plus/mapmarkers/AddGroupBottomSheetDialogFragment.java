package net.osmand.plus.mapmarkers;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.View;

import net.osmand.plus.MapMarkersHelper.MapMarkersGroup;
import net.osmand.plus.R;
import net.osmand.plus.base.MenuBottomSheetDialogFragment;
import net.osmand.plus.base.bottomsheetmenu.BaseBottomSheetItem;
import net.osmand.plus.mapmarkers.adapters.GroupsAdapter;

public abstract class AddGroupBottomSheetDialogFragment extends MenuBottomSheetDialogFragment {

	public static final String TAG = "AddGroupBottomSheetDialogFragment";

	protected View mainView;
	protected GroupsAdapter adapter;

	@Override
	public void createMenuItems(Bundle savedInstanceState) {
		final int themeRes = nightMode ? R.style.OsmandDarkTheme : R.style.OsmandLightTheme;
		mainView = View.inflate(new ContextThemeWrapper(getContext(), themeRes),
				R.layout.fragment_marker_add_group_bottom_sheet_dialog, null);

		final RecyclerView recyclerView = mainView.findViewById(R.id.groups_recycler_view);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		adapter = createAdapter();
		adapter.setAdapterListener(new GroupsAdapter.GroupsAdapterListener() {
			@Override
			public void onItemClick(View view) {
				int position = recyclerView.getChildAdapterPosition(view);
				if (position != RecyclerView.NO_POSITION) {
					AddGroupBottomSheetDialogFragment.this.onItemClick(position);
				}
			}
		});
		recyclerView.setAdapter(adapter);

		items.add(new BaseBottomSheetItem.Builder().setCustomView(mainView).create());
	}

	@Override
	public void onDestroyView() {
		Dialog dialog = getDialog();
		if (dialog != null && getRetainInstance()) {
			dialog.setDismissMessage(null);
		}
		super.onDestroyView();
	}

	@Override
	protected boolean useScrollableItemsContainer() {
		return false;
	}

	protected void addAndSyncGroup(MapMarkersGroup group) {
		getMyApplication().getMapMarkersHelper().addOrEnableGroup(group);
		dismiss();
	}

	protected abstract GroupsAdapter createAdapter();

	protected abstract void onItemClick(int position);
}
