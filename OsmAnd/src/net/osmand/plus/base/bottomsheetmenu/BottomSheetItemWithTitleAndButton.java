package net.osmand.plus.base.bottomsheetmenu;

import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;

public class BottomSheetItemWithTitleAndButton extends SimpleBottomSheetItem {

	private View.OnClickListener onButtonClickListener;
	private Drawable leftCompoundDrawable;
	private Drawable rightCompoundDrawable;
	private String buttonTitle;
	@ColorRes
	private int buttonTextColor = INVALID_ID;

	private TextView textButtonTV;

	public BottomSheetItemWithTitleAndButton(View customView,
	                                         @LayoutRes int layoutId,
	                                         Object tag,
	                                         boolean disabled,
	                                         View.OnClickListener onClickListener,
	                                         int position,
	                                         Drawable icon,
	                                         String title,
	                                         @ColorRes int titleColorId,
	                                         String buttonTitle,
	                                         View.OnClickListener onButtonClickListener,
	                                         Drawable leftCompoundDrawable,
	                                         Drawable rightCompoundDrawable,
	                                         @ColorRes int buttonTextColor) {
		super(customView, layoutId, tag, disabled, onClickListener, position, icon, title, titleColorId);
		this.buttonTitle = buttonTitle;
		this.onButtonClickListener = onButtonClickListener;
		this.leftCompoundDrawable = leftCompoundDrawable;
		this.rightCompoundDrawable = rightCompoundDrawable;
		this.buttonTextColor = buttonTextColor;
	}

	public void setButtonIcons(@Nullable Drawable leftCompoundDrawable, @Nullable Drawable rightCompoundDrawable) {
		this.leftCompoundDrawable = leftCompoundDrawable;
		this.rightCompoundDrawable = rightCompoundDrawable;
		textButtonTV.setCompoundDrawablesWithIntrinsicBounds(leftCompoundDrawable, null, rightCompoundDrawable, null);
	}

	public void setButtonText(String buttonTitle) {
		this.buttonTitle = buttonTitle;
		textButtonTV.setText(buttonTitle);
	}

	@Override
	public void inflate(OsmandApplication app, ViewGroup container, boolean nightMode) {
		super.inflate(app, container, nightMode);
		textButtonTV = (TextView) view.findViewById(R.id.text_button);
		textButtonTV.setOnClickListener(onButtonClickListener);
		textButtonTV.setCompoundDrawablesWithIntrinsicBounds(leftCompoundDrawable, null, rightCompoundDrawable, null);
		textButtonTV.setText(buttonTitle);
		if (buttonTextColor != INVALID_ID) {
			textButtonTV.setTextColor(ContextCompat.getColor(app, buttonTextColor));
		}
	}

	public static class Builder extends SimpleBottomSheetItem.Builder {

		protected String buttonTitle;
		protected View.OnClickListener onButtonClickListener;
		protected Drawable leftCompoundDrawable;
		protected Drawable rightCompoundDrawable;
		@ColorRes
		protected int buttonTextColor = INVALID_ID;

		public BottomSheetItemWithTitleAndButton.Builder setButtonIcons(Drawable leftCompoundDrawable, Drawable rightCompoundDrawable) {
			this.leftCompoundDrawable = leftCompoundDrawable;
			this.rightCompoundDrawable = rightCompoundDrawable;
			return this;
		}

		public BottomSheetItemWithTitleAndButton.Builder setonButtonClickListener(View.OnClickListener onButtonClickListener) {
			this.onButtonClickListener = onButtonClickListener;
			return this;
		}

		public BottomSheetItemWithTitleAndButton.Builder setButtonTitle(String buttonTitle) {
			this.buttonTitle = buttonTitle;
			return this;
		}

		public BottomSheetItemWithTitleAndButton.Builder setButtonTextColor(@ColorRes int buttonTextColor) {
			this.buttonTextColor = buttonTextColor;
			return this;
		}

		public BottomSheetItemWithTitleAndButton create() {
			return new BottomSheetItemWithTitleAndButton(customView,
					layoutId,
					tag,
					disabled,
					onClickListener,
					position,
					icon,
					title,
					titleColorId,
					buttonTitle,
					onButtonClickListener,
					leftCompoundDrawable,
					rightCompoundDrawable,
					buttonTextColor);
		}
	}
}
