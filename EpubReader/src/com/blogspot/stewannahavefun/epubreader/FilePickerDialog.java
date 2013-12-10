package com.blogspot.stewannahavefun.epubreader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class FilePickerDialog extends DialogFragment {

	private static final CharSequence TITLE_OPEN_EPUB_FILE = "Open An Epub File";
	private static final CharSequence MESSAGE_PLACEHOLDER = "File Picker Not Implemented Yet";
	private static final CharSequence BUTTON_POSITIVE = "Open";
	private static final CharSequence BUTTON_NAGATIVE = "Cancel";

	public FilePickerDialog() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(TITLE_OPEN_EPUB_FILE)
				.setMessage(MESSAGE_PLACEHOLDER)
				.setPositiveButton(BUTTON_POSITIVE, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}
				})
				.setNegativeButton(BUTTON_NAGATIVE, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.setCancelable(true);

		return builder.create();
	}

}
