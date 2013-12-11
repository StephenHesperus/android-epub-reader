package com.blogspot.stewannahavefun.epubreader;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FilePickerDialog extends DialogFragment {

	public interface OnFilePickListener {
		void onFilePick(Context context, final File file);
	}

	private static final CharSequence TITLE_OPEN_EPUB_FILE = "Open An Epub File";
	private static final CharSequence BUTTON_POSITIVE = "Open";
	private static final CharSequence BUTTON_NAGATIVE = "Cancel";
	protected static final String EPUB_EXTENSION = ".epub";
	private ArrayList<File> fileList;
	private ArrayAdapter<File> mAdapter;
	protected ListView mListView;
	private File mBase;

	public FilePickerDialog() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		fileList = new ArrayList<File>();

		mBase = Environment.getExternalStorageDirectory();
		popFileList(mBase);

		mAdapter = new ArrayAdapter<File>(getActivity(),
				android.R.layout.simple_list_item_activated_1,
				android.R.id.text1, fileList);

		mListView = new ListView(getActivity());

		mListView.setAdapter(mAdapter);
		mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				File file = mAdapter.getItem(position);

				if (position == 0) {
					if (file.equals(mBase))
						return;

					file = mAdapter.getItem(position).getParentFile();
				}

				popFileList(file);
				mAdapter.notifyDataSetChanged();

			}
		});

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
				.setView(mListView);

		return builder.create();
	}

	private void popFileList(File file) {
		if (!file.isDirectory())
			return;

		File[] files = file.listFiles(new FileFilter() {

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

		fileList.clear();
		fileList.add(file);
		for (int i = 0; i < files.length; i++) {
			fileList.add(i + 1, files[i]);
		}
	}
}
