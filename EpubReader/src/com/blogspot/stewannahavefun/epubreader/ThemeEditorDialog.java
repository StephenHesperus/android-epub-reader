package com.blogspot.stewannahavefun.epubreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

public class ThemeEditorDialog extends DialogFragment {

	public interface ThemeEditorListener {
		public void onThemeChange(String css);
	}

	private static final CharSequence THEME_EDITOR_DIALOG_TITLE = "Edit Theme CSS";

	private static final CharSequence SAVE = "Save";

	private static final CharSequence CANCEL = "Cancel";

	private ThemeEditorListener mListener;

	protected String mThemeCSS;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mListener = (ThemeEditorListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement ThemeEditorListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		final EditText editor = (EditText) inflater.inflate(
				R.layout.dialog_theme_editor, null);

		builder.setTitle(THEME_EDITOR_DIALOG_TITLE)
				.setView(editor)
				.setPositiveButton(SAVE, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mThemeCSS = editor.getText().toString();
						mListener.onThemeChange(mThemeCSS);
					}
				})
				.setNegativeButton(CANCEL,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
				})
				.setCancelable(true);

		return builder.create();
	}

}
