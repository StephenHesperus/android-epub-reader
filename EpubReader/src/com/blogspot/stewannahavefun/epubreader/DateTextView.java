package com.blogspot.stewannahavefun.epubreader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class DateTextView extends TextView {

	public DateTextView(Context context) {
		super(context);
	}

	public DateTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DateTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setDate(long ms, String suffix) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm",
				Locale.US);
		Date date = new Date(ms);
		String dateString = sdf.format(date);
		String text = dateString + "  " + suffix;

		setText(text);
	}

}
