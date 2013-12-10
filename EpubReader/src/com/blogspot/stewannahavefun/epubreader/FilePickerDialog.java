package com.blogspot.stewannahavefun.epubreader;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;

public class FilePickerDialog extends DialogFragment {

	private static final CharSequence TITLE_OPEN_EPUB_FILE = "Open An Epub File";
	private static final CharSequence BUTTON_POSITIVE = "Open";
	private static final CharSequence BUTTON_NAGATIVE = "Cancel";
	protected static final String EPUB_EXTENSION = ".epub";

	public FilePickerDialog() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		File[] files = new File[] {};
		File base = Environment.getExternalStorageDirectory();

		files = base.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) {
					return true;
				} else if (pathname.getName().endsWith(EPUB_EXTENSION)) {
					return true;
				}

				return false;
			}
		});

		ArrayList<File> fileList = new ArrayList<File>();

		fileList.clear();
		for (File file : files) {
			fileList.add(file);
		}
		fileList.add(0, base);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(TITLE_OPEN_EPUB_FILE)
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
				.setCancelable(true)
				.setAdapter(
						new ArrayAdapter<File>(getActivity(),
								android.R.layout.simple_list_item_1,
								android.R.id.text1, fileList),
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub

							}
						}
				);

		return builder.create();
	}

}
