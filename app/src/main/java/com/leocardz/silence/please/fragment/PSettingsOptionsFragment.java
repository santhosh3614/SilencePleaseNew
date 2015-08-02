package com.leocardz.silence.please.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.leocardz.silence.please.R;
import com.leocardz.silence.please.adapter.PSettingsArrayAdapter;
import com.leocardz.silence.please.adapter.item.PSettingsAdapterItem;
import com.leocardz.silence.please.utils.Constants;
import com.leocardz.silence.please.utils.Manager;

public class PSettingsOptionsFragment extends SherlockFragment {

    private Manager manager = Manager.getInstance();

    private PSettingsAdapterItem[] settingsItems;
    public int settingsCounter = 0;
    public Parcelable settingsListState;

    private SharedPreferences settings;

    private String versionX = "";

    private ListView listView;

    private static PSettingsOptionsFragment pSettingsOptionsFragment;

    public PSettingsOptionsFragment() {
        pSettingsOptionsFragment = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.p_settings_options,
                container, false);

        settings = manager.getContext().getSharedPreferences(
                Constants.PREFS_NAME, 0);

        buildSettingsList();
        listView = (ListView) rootView.findViewById(R.id.settings_list_view);
        listView.setAdapter(manager.getSettingsListAdapter());
        listView.setOnItemClickListener(settingsListener);

        try {
            versionX = manager.getContext().getPackageManager()
                    .getPackageInfo(manager.getContext().getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
        }

        rootView.bringToFront();
        return rootView;
    }

    private OnItemClickListener settingsListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int position,
                                long id) {

            if (position == 1)
                feedbackUs();
            else if (position == 2)
                shareUs();
            else if (position == 3)
                rateUs();
            else if (position == 4)
                manager.getSettingsFragmentViewPager().setCurrentItem(1);

        }
    };

    public void updateSummary(View view, int position, int newSummary) {
        updateSummary(view, position, getString(newSummary));
    }

    public void updateSummary(View view, int position, String newSummary) {
        Animation fadeOut = AnimationUtils.loadAnimation(manager.getContext(),
                R.anim.fade_out);
        Animation rightLeft = AnimationUtils.loadAnimation(
                manager.getContext(), R.anim.right_left_summary);

        TextView summary = (TextView) view.findViewById(R.id.summary);
        summary.startAnimation(fadeOut);
        summary.setText(newSummary);
        summary.startAnimation(rightLeft);

        PSettingsAdapterItem item = manager.getSettingsListAdapter().getItem(
                position);
        item.setSummary(newSummary);
    }

    private void feedbackUs() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("pain/text");
        String[] recipients = new String[]{getString(R.string.feedback_email)};
        intent.putExtra(Intent.EXTRA_EMAIL, recipients);
        intent.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.feedback_subject));
        intent.putExtra(Intent.EXTRA_TEXT,
                "\n\n\nApp Version: " + versionX + "\nModel: "
                        + android.os.Build.MANUFACTURER + " "
                        + android.os.Build.DEVICE + "\nSO: "
                        + android.os.Build.VERSION.RELEASE + "\n");
        startActivity(Intent.createChooser(intent,
                getString(R.string.feedback_via)));
    }

    private void shareUs() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.spread_subject));
        intent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.spread_text));
        startActivity(Intent.createChooser(intent,
                getString(R.string.share_via)));
    }

    private void rateUs() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri
                .parse("market://details?id=" + getActivity().getPackageName()));
        startActivity(intent);
    }

    private void buildSettingsList() {
        if (manager.getSettingsListAdapter() == null) {
            settingsItems = new PSettingsAdapterItem[5];
            int position = 0;
            addNewItem(R.string.about, R.string.empty,
                    Constants.SETTINGS_CATEGORY, position++);
            addNewItem(R.string.feedback_us, R.string.feedback_us_subtitle,
                    Constants.SETTINGS_SUMMARY, position++);
            addNewItem(R.string.share, R.string.share_subtitle,
                    Constants.SETTINGS_SUMMARY, position++);
            addNewItem(R.string.rate_us, R.string.empty,
                    Constants.SETTINGS_SIMPLE, position++);
            addNewItem(R.string.concept, R.string.empty,
                    Constants.SETTINGS_SIMPLE, position++);

            manager.setSettingsListAdapter(new PSettingsArrayAdapter(manager
                    .getContext(), R.layout.p_settings_simple, settingsItems,
                    pSettingsOptionsFragment));
        }
    }

    private void addNewItem(int title, int summary, int type, int position) {
        addNewItem(title, getString(summary), type, position);
    }

    private void addNewItem(int title, String summary, int type, int position) {
        settingsItems[position] = new PSettingsAdapterItem(title, summary, type);
    }

}
