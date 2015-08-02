package com.leocardz.silence.please.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.devspark.appmsg.AppMsg;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.leocardz.silence.please.R;
import com.leocardz.silence.please.custom.SPButton;
import com.leocardz.silence.please.custom.SPTextView;
import com.leocardz.silence.please.db.AppDb;
import com.leocardz.silence.please.db.SoundLevel;
import com.leocardz.silence.please.db.SoundLevelTable;
import com.leocardz.silence.please.utils.Constants;
import com.leocardz.silence.please.utils.Manager;

import java.util.Calendar;

public class PSilenceFragment extends SherlockFragment {

	private static Manager manager = Manager.getInstance();
	private RelativeLayout normalRel,groupRel,noiseRel;
	private TextView noiseDur,groupDur,quietDur;
	private SoundLevel soundLevel;

	public PSilenceFragment() {
		pSilenceFragment = this;
	}
	private static PSilenceFragment pSilenceFragment;
	private SPTextView currentDecibelsTextView;
	public static SPButton listenButton;
	private ImageButton graphTable;
	private boolean graphButtonImage;
	private LinearLayout listenWrap;
	private Animation fadeInListen, fadeOutListen, fadeInNoise, fadeOutNoise;
	private double decibelsRate;
	private static MediaRecorder mRecorder;
	private double mEMA = 0.0;
	private MenuItem menuSettings;

	private Runnable updater;
	private final Handler mHandler = new Handler();
	private final int HANDLER_TICK = 100;

	private static SharedPreferences settings;
	private View rootView;
	public boolean isListen = false;
	private GraphView graphView;
	private LineGraphSeries exampleSeries;
	private double graphLastXValue = 5D;
	private LinearLayout graphLayout, tableLayout;
//	private long quietMillis,groupMillis,noiseMillis;
	private final int[] DECIBEL_RANGES = { 100, 80, 60};

	private int returnListViewSelectedPosition() {
		int pos = 0;
		if (decibelsRate >= DECIBEL_RANGES[pos])
			return pos;
		pos++;
		for (int i = 1; i < DECIBEL_RANGES.length - 1; i++) {
			if (decibelsRate < DECIBEL_RANGES[i]
					&& decibelsRate >= DECIBEL_RANGES[i + 1])
				return pos;
			pos++;
		}
		return pos;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		long startTime=getCurrentDateMillis();
		SoundLevelTable soundLevelTable = AppDb.getInstance().getSoundLevelTable();
		soundLevel = soundLevelTable.getSoundLevel(startTime);
		if(soundLevel==null){
			soundLevel=soundLevelTable.insetSound(0,0,0,startTime);
		}
	}

	private long getCurrentDateMillis(){
		Calendar calendar=Calendar.getInstance();
		calendar.set(Calendar.HOUR,0);
		calendar.set(Calendar.MINUTE,0);
		calendar.set(calendar.SECOND,0);
		return calendar.getTimeInMillis();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.silence_layout, container,false);
		pSilenceFragment.rootView = rootView;
		settings = getActivity().getApplicationContext().getSharedPreferences(
				Constants.PREFS_NAME, 0);
		listenWrap = (LinearLayout) pSilenceFragment.rootView
				.findViewById(R.id.listen_wrap);
		currentDecibelsTextView = (SPTextView) rootView
				.findViewById(R.id.current_decibels);
		currentDecibelsTextView.setText(String.valueOf(Constants.DB_INITIAL));
		graphLayout = (LinearLayout) pSilenceFragment.rootView
				.findViewById(R.id.graph);
		tableLayout = (LinearLayout) pSilenceFragment.rootView
				.findViewById(R.id.table);
		graphTable = (ImageButton) pSilenceFragment.rootView
				.findViewById(R.id.graph_table);
		graphButtonImage = settings.getBoolean("graph", false);
		setGraphButtonImage(graphButtonImage);
		graphTable.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				graphButtonImage = !graphButtonImage;
				setGraphButtonImage(graphButtonImage);
			}
		});
		listenButton = (SPButton) pSilenceFragment.rootView
				.findViewById(R.id.listen);
		listenButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				buttonClicked();
			}

		});
		initAnimations(rootView);
		initTable(rootView);
		return rootView;
	}

	private void initTable(View rootView) {
		normalRel = (RelativeLayout) rootView.findViewById(R.id.normal_level_rel);
		groupRel = (RelativeLayout) rootView.findViewById(R.id.group_level_rel);
		noiseRel = (RelativeLayout) rootView.findViewById(R.id.noise_level_rel);

		noiseDur=(TextView)noiseRel.findViewById(R.id.noise_decibels);
		groupDur=(TextView)groupRel.findViewById(R.id.group_decibels);
		quietDur=(TextView)normalRel.findViewById(R.id.normal_decibels);
	}

	public void setGraphButtonImage(boolean graphButtonImage) {
		if (graphButtonImage) {
			graphTable.setImageResource(R.drawable.table);
			graphLayout.setVisibility(View.VISIBLE);
			tableLayout.setVisibility(View.GONE);
		} else {
			graphTable.setImageResource(R.drawable.graph);
			tableLayout.setVisibility(View.VISIBLE);
			graphLayout.setVisibility(View.GONE);
		}
		settings.edit().putBoolean("graph", graphButtonImage).commit();
	}

	private void buttonClicked() {
		isListen = !isListen;
		if (isListen) {
			startListen();
			listenButton.setText(R.string.stop_listen);
		} else {
			decibelsRate = 0.0;
			updateTable();
			stopListen();
			listenButton.setText(R.string.start_listen);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		exampleSeries = new LineGraphSeries<DataPoint>(new DataPoint[] {
				new DataPoint(.0d, .0d)});
		graphView = new GraphView(getActivity());
		graphView.addSeries(exampleSeries);
//		graphView.setLimit(limitDecibels);
//		graphView.setViewPort(0, 150);
//		graphView.setManualYAxisBounds(150, 0);
		graphView.getViewport().setYAxisBoundsManual(true);
		graphView.getViewport().setMinX(0);
		graphView.getViewport().setMaxY(150);
		graphView.getGridLabelRenderer().setHorizontalLabelsColor(
				getResources().getColor(android.R.color.transparent));
		graphView.getGridLabelRenderer().setVerticalLabelsColor(Color.BLACK);
		graphLayout.addView(graphView);
		isListen = false;
		buttonClicked();
		super.onActivityCreated(savedInstanceState);
	}

	private void startListen() {
		startRecorder();
	}

	public void stopListen() {
		currentDecibelsTextView.setText(String.valueOf(Constants.DB_INITIAL));
		stopRecorder();
	}

	public void startRecorder() {
		if (mRecorder == null) {
			mRecorder = new MediaRecorder();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mRecorder.setOutputFile("/dev/null");

			try {
				mRecorder.prepare();
			} catch (java.io.IOException ioe) {
				AppMsg.makeText(getActivity(), R.string.mic_error,
						AppMsg.STYLE_ALERT).show();

			} catch (SecurityException e) {
				AppMsg.makeText(getActivity(), R.string.mic_error,
						AppMsg.STYLE_ALERT).show();
			}
			try {
				mRecorder.start();
			} catch (SecurityException e) {
				AppMsg.makeText(getActivity(), R.string.mic_error,
						AppMsg.STYLE_ALERT).show();
			}

		}
	}


	private String getDuration(long milliseconds){
		int seconds = (int) (milliseconds / 1000) % 60 ;
		int minutes = (int) ((milliseconds / (1000*60)) % 60);
		int hours   = (int) ((milliseconds / (1000*60*60)) % 24);
		return String.format("%d:%d:%d",hours,minutes,seconds);
	}

	public void stopRecorder() {
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}
	}

	private void initAnimations(View rootView) {
		fadeInListen = new AlphaAnimation(0f, 1f);
		fadeOutListen = new AlphaAnimation(1f, 0f);
		fadeOutListen.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				listenWrap.setVisibility(View.GONE);
				menuSettings.setVisible(false);
			}
		});

		fadeInNoise = new AlphaAnimation(0f, 1f);
		fadeOutNoise = new AlphaAnimation(1f, 0f);
		fadeOutNoise.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				menuSettings.setVisible(true);
				listenWrap.setVisibility(View.VISIBLE);
				listenWrap.startAnimation(fadeInListen);
				buttonClicked();
			}
		});
		fadeInListen.setDuration(Constants.ANIMATION_DURATION);
		fadeOutListen.setDuration(Constants.ANIMATION_DURATION);
		fadeInNoise.setDuration(Constants.ANIMATION_DURATION);
		fadeOutNoise.setDuration(Constants.ANIMATION_DURATION);
	}

	private void updateDecibels() {
		decibelsRate = soundDb();
		if (decibelsRate > 0.0) {
			currentDecibelsTextView.setText(String.valueOf((int) decibelsRate));
		} else {
			currentDecibelsTextView.setText(String.valueOf(Constants.DB_INITIAL));
		}
	}

	@SuppressWarnings("deprecation")
	private void updateTable() {
		int pos = returnListViewSelectedPosition();
		noiseRel.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.p_background_transparent));
		groupRel.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.p_background_transparent));
		normalRel.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.p_background_transparent));

		if (decibelsRate == 0.0)
			pos = -1;

		if (pos == 0){
			noiseRel.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.p_warning));
			long groupTime = soundLevel.getNoiseTime();
			soundLevel.setNoiseTime(groupTime + HANDLER_TICK);
			noiseDur.setText(getDuration(soundLevel.getNoiseTime()));
		}
		else if (pos == 1) {
			groupRel.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.p_background_fourth));
			long groupTime = soundLevel.getGroupTime();
			soundLevel.setGroupTime(groupTime+HANDLER_TICK);
			groupDur.setText(getDuration(soundLevel.getGroupTime()));
		}
		else if (pos == 2) {
			normalRel.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.p_background_eighth));
			long groupTime = soundLevel.getQuietTime();
			soundLevel.setQuietTime(groupTime + HANDLER_TICK);
			quietDur.setText(getDuration(soundLevel.getQuietTime()));
		}
	}

	private void updateGraph() {
		graphLastXValue += 1d;
		exampleSeries.appendData(new DataPoint(graphLastXValue,
				decibelsRate),true,5);
	}

	public double soundDb() {
		return 20 * Math.log10(getAmplitudeEMA() / Constants.AMP);
	}

	public double getAmplitude() {
		if (mRecorder != null)
			return (mRecorder.getMaxAmplitude());
		else
			return 0;

	}

	public double getAmplitudeEMA() {
		double amp = getAmplitude();
		mEMA = Constants.EMA_FILTER * amp + (1.0 - Constants.EMA_FILTER) * mEMA;
		return mEMA;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menuSettings = menu.findItem(R.id.menu_settings);
		menu.findItem(R.id.repeat_sound).setChecked(
				getActivity().getApplicationContext()
						.getSharedPreferences(Constants.PREFS_NAME, 0)
						.getBoolean("repeat", true));
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.repeat_sound:
			settings.edit().putBoolean("repeat", !item.isChecked()).commit();
			item.setChecked(!item.isChecked());
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPause() {
		manager.setInFront(false);
		stopListen();
		mHandler.removeCallbacks(updater);
		SoundLevelTable soundLevelTable = AppDb.getInstance().getSoundLevelTable();
		soundLevelTable.updateSound(soundLevel);
		super.onPause();
	}

	@Override
	public void onStop() {
		manager.setInFront(false);
		super.onStop();
	}

	@Override
	public void onResume() {
		manager.setInFront(true);
		updater = new Runnable() {
			@Override
			public void run() {
				if (isListen) {
					updateDecibels();
					updateGraph();
					updateTable();
				}
				mHandler.postDelayed(this, HANDLER_TICK);
			}
		};
		mHandler.postDelayed(updater, HANDLER_TICK);

		if (isListen) {
			startListen();
		}
		super.onResume();
	}

}