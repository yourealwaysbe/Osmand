
package net.osmand.plus.widgets;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import net.osmand.Location;
import net.osmand.ValueHolder;
import net.osmand.GPXUtilities.GPXFile;
import net.osmand.GPXUtilities.GPXTrackAnalysis;
import net.osmand.StateChangedListener;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmAndLocationProvider;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.helpers.GpxUiHelper;
import net.osmand.plus.helpers.GpxUiHelper.GPXDataSetAxisType;
import net.osmand.plus.helpers.GpxUiHelper.OrderedLineDataSet;
import net.osmand.plus.routing.IRouteInformationListener;
import net.osmand.plus.routing.RouteCalculationResult;
import net.osmand.plus.routing.RoutingHelper;

public class ElevationNavigationFragment extends Fragment implements IRouteInformationListener, OsmAndLocationProvider.OsmAndLocationListener, StateChangedListener<Boolean> {
	public static final String TAG = "ElevationNavigationFragment";

	@Nullable
	private View view;
	@Nullable
	private MapActivity mapActivity;

	public void setMapActivity(MapActivity mapActivity) {
		if (this.mapActivity != null) {
			mapActivity.getRoutingHelper().removeListener(this);
			mapActivity.getMyApplication()
			           .getSettings()
			           .ELEVATION_NAVIGATION.removeListener(this);
		}
		this.mapActivity = mapActivity;
		if (this.mapActivity != null) {
			mapActivity.getRoutingHelper().addListener(this);
			mapActivity.getMyApplication()
			           .getSettings()
			           .ELEVATION_NAVIGATION.addListener(this);
			setVisibilityAndRefresh();
		}
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final MapActivity mapActivity = getMapActivity();

		if (mapActivity == null) {
			return null;
		}

		boolean nightMode = mapActivity.getMyApplication().getDaynightHelper().isNightModeForMapControls();
		ContextThemeWrapper context =
				new ContextThemeWrapper(mapActivity, !nightMode ? R.style.OsmandLightTheme : R.style.OsmandDarkTheme);

		this.view = LayoutInflater.from(context).inflate(R.layout.elevation_navigation, container, false);

		refreshData();

		return this.view;
	}

	public void newRouteIsCalculated(boolean newRoute, ValueHolder<Boolean> showToast) {
		setVisibilityAndRefresh();
		routingStarted();
	}

	public void routeWasCancelled() {
		setVisibilityAndRefresh();
		routingFinished();
	}

	public void routeWasFinished() {
		setVisibilityAndRefresh();
		routingFinished();
	}

	public void updateLocation(Location location) {
		if (this.mapActivity == null || this.view == null || !isShowState() || !isAdded()) {
			return;
		}

		final RoutingHelper rh = mapActivity.getRoutingHelper();
		int leftDistance = rh.getLeftDistance();

		final LineChart chart = (LineChart) this.view.findViewById(R.id.chart);

		if (chart.getLineData() != null) {
			float maxXValue = chart.getLineData().getXMax();

			chart.highlightValue(maxXValue - leftDistance, 0);
			chart.invalidate();
		}
	}

	/**
	 * This will monitor the "show elevation" setting
	 */
	public void stateChanged(Boolean onoff) {
		setVisibilityAndRefresh();
	}

	private void routingStarted() {
		if (this.mapActivity != null) {
			OsmandApplication app = this.mapActivity.getMyApplication();
			app.getLocationProvider().addLocationListener(this);
		}
	}

	private void routingFinished() {
		if (this.mapActivity != null) {
			OsmandApplication app = this.mapActivity.getMyApplication();
			app.getLocationProvider().removeLocationListener(this);
		}
	}

	/**
	 * Makes visible if in route following mode with show elevation selected
	 *
	 * @return true if is now visible
	 */
	private void setVisibilityAndRefresh() {
		try {
			// need isAdded to be up to date
			mapActivity.getSupportFragmentManager()
					   .executePendingTransactions();

			if (!isShowState()) {
				if (isAdded()) {
					dismiss();
				}
			} else {
				if (!isAdded()) {
					final MapActivity mapActivity = getMapActivity();
					mapActivity.getSupportFragmentManager()
					           .beginTransaction()
					           .add(R.id.elevation_navigation, this, TAG)
					           .commitAllowingStateLoss();
				} else {
					refreshData();
				}
			}
		} catch (RuntimeException e) {
			// ignore
		}
	}

	public void dismiss() {
		final MapActivity mapActivity = getMapActivity();
		if (mapActivity != null) {
			try {
				mapActivity.getSupportFragmentManager()
				           .beginTransaction()
				           .remove(this)
				           .commitAllowingStateLoss();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	private void refreshData() {
		if (this.view == null || !isShowState() || !isAdded()) {
			return;
		}

		final RoutingHelper rh = mapActivity.getRoutingHelper();
		final RouteCalculationResult route = rh.getRoute();

		if (route.isCalculated()) {
			final MapActivity mapActivity = getMapActivity();
			final OsmandApplication app = mapActivity.getMyApplication();

			final LineChart chart = (LineChart) this.view.findViewById(R.id.chart);
			boolean isLight = app.getSettings().isLightContent();

			GpxUiHelper.setupGPXChart(chart, 3, 32f, 4f, isLight, false);

			final GPXFile gpx = GpxUiHelper.makeGpxFromRoute(route, app);
			final GPXTrackAnalysis analysis = gpx.getAnalysis(0);

			OrderedLineDataSet dataSet = GpxUiHelper.createGPXElevationDataSet(app, chart, analysis,
									   GPXDataSetAxisType.DISTANCE, false, true);
			if (dataSet != null) {
				List<ILineDataSet> dataSets = new ArrayList<>();
				dataSets.add(dataSet);
				chart.setData(new LineData(dataSets));
				chart.notifyDataSetChanged();
				chart.invalidate();
			}
		}
	}

	@NonNull
	private MapActivity getMapActivity() {
		return mapActivity;
	}

	/**
	 * Calculates whether the elevation navigation fragment should be
	 * shown
	 */
	private boolean isShowState() {
		final MapActivity mapActivity = getMapActivity();
		if (mapActivity == null) {
			return false;
		}

		final OsmandSettings settings = this.mapActivity
		                                    .getMyApplication()
		                                    .getSettings();
		if (!settings.ELEVATION_NAVIGATION.get()) {
			return false;
		}

		final RoutingHelper rh = mapActivity.getRoutingHelper();
		if (!rh.isFollowingMode() && !rh.isRoutePlanningMode()) {
			return false;
		}

		boolean hasChartData = false;
		if (this.view != null) {
			final LineChart chart = (LineChart) this.view.findViewById(R.id.chart);
			hasChartData = (chart.getLineData() == null);
		}
		final RouteCalculationResult route = rh.getRoute();
		// can still be showing old data if recalculation is happening
		if (hasChartData && !route.isCalculated()) {
			return false;
		}

		return true;
	}
}
