package com.example.modernartui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.chiralcode.colorpicker.ColorPickerDialog;
import com.chiralcode.colorpicker.ColorPickerDialog.OnColorSelectedListener;

public class MainActivity extends ActionBarActivity {
	private static final String TAG = "ModernArt";

	private static final double SEEKBAR_LENGTH = 100;

	// Path to temporary image to store the painting when sharing.
	private static final String IMAGE_NAME = "painting.jpg";
	private static final String mPath =
			Environment.getExternalStorageDirectory() + File.separator + IMAGE_NAME;

	private RelativeLayout mMainLayout;
	private LinearLayout mOuterLayout;

	private SeekBar mSeekBar;

	private static final int HORIZONTAL = 0;
	// private static final int VERTICAL = 1;
	private static final int[] MIN_VIEWS = {2, 2};
	private static final int[] MAX_VIEWS = {3, 4};

	private int seek = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mMainLayout = (RelativeLayout) findViewById(R.id.main_layout);
		mSeekBar = (SeekBar) findViewById(R.id.seek_bar);

		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				seek = progress;
				Log.d(TAG, "Seekbar moved to: " + progress);
				updateArt();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				Log.d(TAG, "Seek started at: " + MainActivity.this.seek);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Log.d(TAG, "Seek stopped at: " + MainActivity.this.seek);
			}
		});

		drawArt();
	}

	private void drawArt() {
		mMainLayout.removeAllViews();

		int marginsDp = 1; // margin in dips
		float d = getResources().getDisplayMetrics().density;
		int marginPx = (int)(marginsDp * d); // margin in pixels

		int outerOrientation = randInt(0, 1);  // Horizontal or Vertical.
		int innerOrientation = 1 - outerOrientation;  // The opposite.

		mOuterLayout = new LinearLayout(this);
		mOuterLayout.setOrientation(outerOrientation == HORIZONTAL ? LinearLayout.HORIZONTAL
			                                                      : LinearLayout.VERTICAL);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		mOuterLayout.setLayoutParams(params);
		mOuterLayout.setPadding(marginPx, marginPx, marginPx, marginPx);
		mOuterLayout.setBackgroundColor(Color.BLACK);
		mMainLayout.addView(mOuterLayout);
		int numInnerLayouts = randInt(MIN_VIEWS[outerOrientation], MAX_VIEWS[outerOrientation]);
		int numRectanglesSeed = randInt(MIN_VIEWS[innerOrientation], MAX_VIEWS[innerOrientation]);

		for (int i = 0; i < numInnerLayouts; i++) {
			LinearLayout innerLayout = new LinearLayout(this);
			innerLayout.setOrientation(innerOrientation == HORIZONTAL ? LinearLayout.HORIZONTAL
				                                                      : LinearLayout.VERTICAL);
			innerLayout.setLayoutParams(new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));
			int numRectangles = randIntExcept(
					Math.max(MIN_VIEWS[innerOrientation], numRectanglesSeed - 1),
					Math.min(MAX_VIEWS[innerOrientation], numRectanglesSeed + 1),
					numRectanglesSeed);
			numRectanglesSeed = numRectangles;
			for (int j = 0; j < numRectangles; j++) {
				final View rectangle = new View(this);
				colorRectangle(rectangle, i, j);
				LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
				param.setMargins(marginPx, marginPx, marginPx, marginPx);
				rectangle.setLayoutParams(param);
				innerLayout.addView(rectangle);
				Log.d(TAG, "i = " + i + ", j = " + j);
			}
			mOuterLayout.addView(innerLayout);
		}

		for (int i = 0; i < mOuterLayout.getChildCount(); i++) {
			LinearLayout innerLayout = (LinearLayout) mOuterLayout.getChildAt(i);
			for (int j = 0; j < innerLayout.getChildCount(); j++) {
				final View rectangle = innerLayout.getChildAt(j);
				final boolean isGray = (i == 0 && j == 0);  // First rectangle is gray.
				rectangle.setOnClickListener(new View.OnClickListener() {
			        @Override
					public void onClick(View v) {
			        	colorRectangle(v, isGray);
			        }
			    });
				rectangle.setOnLongClickListener(new OnLongClickListener() {
				    @Override
					public boolean onLongClick(View arg0) {
				        // https://github.com/chiralcode/Android-Color-Picker/
				        ColorPickerDialog colorPickerDialog = new ColorPickerDialog(
				        		MainActivity.this,
				        		(int) rectangle.getTag(R.id.MIXED_COLOR),
				        		new OnColorSelectedListener() {
				            @Override
				            public void onColorSelected(int color) {
				            	rectangle.setTag(R.id.COLOR1, color);
				            	rectangle.setTag(R.id.COLOR2, color);
				            	rectangle.setTag(R.id.MIXED_COLOR, color);
				            	rectangle.setBackgroundColor(color);
				            }
				        });
				        colorPickerDialog.show();

				        return true;
				    }
				});
			}
		}
	}

	private void colorRectangle(View rectangle, boolean isGray) {
		int color1 = isGray ? randomGrayscale() : randomColor();
		int color2 = isGray ? color1 : randomColor();
		int mixed_color = mixColors(color1, color2);
		rectangle.setTag(R.id.COLOR1, color1);
		rectangle.setTag(R.id.COLOR2, color2);
		rectangle.setTag(R.id.MIXED_COLOR, mixed_color);
		rectangle.setBackgroundColor(mixed_color);
	}

	private void colorRectangle(View rectangle, int i, int j) {
		final boolean isGray = (i == 0 && j == 0);  // First rectangle is gray.
		colorRectangle(rectangle, isGray);
	}

	private void updateArt() {
		for (int i = 0; i < mOuterLayout.getChildCount(); i++) {
			LinearLayout innerLayout = (LinearLayout) mOuterLayout.getChildAt(i);
			for (int j = 0; j < innerLayout.getChildCount(); j++) {
				View rectangle = innerLayout.getChildAt(j);
				int color1 = (int) rectangle.getTag(R.id.COLOR1);
				int color2 = (int) rectangle.getTag(R.id.COLOR2);
				int mixed_color = mixColors(color1, color2);
				rectangle.setTag(R.id.MIXED_COLOR, mixed_color);
				rectangle.setBackgroundColor(mixed_color);
			}
		}
	}


	private int mixColors(int color1, int color2) {
		int r = mixSingleColor(Color.red(color1), Color.red(color2));
		int g = mixSingleColor(Color.green(color1), Color.green(color2));
		int b = mixSingleColor(Color.blue(color1), Color.blue(color2));
		return Color.rgb(r, g, b);
	}

	private int mixSingleColor(int rgb1, int rgb2) {
		return (int) ((SEEKBAR_LENGTH - seek) / SEEKBAR_LENGTH * rgb1 + seek / SEEKBAR_LENGTH * rgb2);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.more_info:
				new AlertDialog.Builder(this)
				    .setMessage(R.string.information)
				    .setPositiveButton(R.string.visit_moma, new DialogInterface.OnClickListener() {
				        @Override
						public void onClick(DialogInterface dialog, int which) {
				        	String url = "http://www.moma.org/collection/browse_results.php?criteria=O%3AAD%3AE%3A4057&page_number=1&template_id=6&sort_order=1&displayall=1#skipToContent";
				        	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				        	startActivity(browserIntent);
				        }
				     })
				    .setNegativeButton(R.string.not_now, null)
				    .show();
				return true;
			case R.id.cool_features:
				new AlertDialog.Builder(this)
				    .setMessage(R.string.cool_features_list)
				    .setPositiveButton(R.string.wow, null)
				    .show();
				return true;
			case R.id.new_painting:
				drawArt();
				return true;
			case R.id.share_painting:
				if (storePainting(mMainLayout)) {
					Intent share = new Intent(Intent.ACTION_SEND);
					share.setType("image/*");
					share.putExtra(Intent.EXTRA_TITLE, R.string.share_text);
					share.putExtra(Intent.EXTRA_TEXT, R.string.share_text);
					share.putExtra(Intent.EXTRA_SUBJECT, R.string.share_text);
					share.putExtra(Intent.EXTRA_STREAM, Uri.parse(mPath));
					startActivity(Intent.createChooser(share, "Share painting!"));
				}
				return true;
		}
		return false;
	}

	/**
	 * http://stackoverflow.com/questions/363681/generating-random-integers-in-a-range-with-java
	 * Returns a pseudo-random number between min and max, inclusive.
	 * The difference between min and max can be at most
	 * <code>Integer.MAX_VALUE - 1</code>.
	 *
	 * @param min Minimum value
	 * @param max Maximum value.  Must be greater than min.
	 * @return Integer between min and max, inclusive.
	 * @see java.util.Random#nextInt(int)
	 */
	public static int randInt(int min, int max) {

	    // NOTE: Usually this should be a field rather than a method
	    // variable so that it is not re-seeded every call.
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}

	public static int randIntExcept(int min, int max, int except) {
		int randomNum;
		while ((randomNum = randInt(min, max)) == except) {
			continue;
		}
		return randomNum;
	}

	public static int randomColor() {
	    Random rand = new Random();

		// generate the random integers for r, g and b value
		int r = rand.nextInt(255);
		int g = rand.nextInt(255);
		int b = rand.nextInt(255);

		return Color.rgb(r, g, b);
	}

	public static int randomGrayscale() {
	    Random rand = new Random();

		// generate the random gray intensity.
		int i = rand.nextInt(255);

		return Color.rgb(i, i, i);
	}

	// http://stackoverflow.com/questions/2661536/how-to-programatically-take-a-screenshot-on-android
	private boolean storePainting(View v) {
		// create bitmap screen capture
		Bitmap bitmap;
		v.setDrawingCacheEnabled(true);
		bitmap = Bitmap.createBitmap(v.getDrawingCache());
		v.setDrawingCacheEnabled(false);

		OutputStream fout = null;
		File imageFile = new File(mPath);

		try {
		    fout = new FileOutputStream(imageFile);
		    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
		    fout.flush();
		    fout.close();
		    return true;
		} catch (FileNotFoundException e) {
		    Log.e(TAG, e.getMessage());
		} catch (IOException e) {
		    Log.e(TAG, e.getMessage());
		}
		return false;
	}
}
