package ru.mendeo.chronos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.ListPreference;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceManager;
import android.preference.Preference;
import android.provider.Settings;

@SuppressWarnings("deprecation")
public class ChronosPreferences extends PreferenceActivity
{
	private static final int NO_AVAILABLE_ALL_LOCATION_PROVIDERS_DIALOG = 1;
	private static final int NO_AVAILABLE_GPS_LOCATION_PROVIDER_DIALOG = 2;
	private static final int NO_AVAILABLE_NETWORK_LOCATION_PROVIDER_DIALOG = 3;
	private ExternalDbAdapter mExternalDbAdapter;
	private ListPreference mLpRegion, mLpCity, mLpLocFreq;
	private CheckBoxPreference mCpAutoLoc;
	private String mRegionKey, mCityKey, mLocFreqKey, mAutoLocKey, mRegionDefaultTitle, mCityDefaultTitle;
	private LocationManager mLocationManager;
	private SharedPreferences mSharedPreferences;
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.main_preferences);                
        mExternalDbAdapter = new ExternalDbAdapter(getApplicationContext());
        mExternalDbAdapter.open();
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mRegionKey = getString(R.string.preferences_list_region_key);
        mCityKey = getString(R.string.preferences_list_city_key);        
        mLpRegion = (ListPreference)findPreference(mRegionKey);
        mLpCity = (ListPreference)findPreference(mCityKey);
        mLocFreqKey=getString(R.string.preferences_auto_location_freq_key);
        mAutoLocKey=getString(R.string.preferences_auto_location_key);
        mLpLocFreq=(ListPreference)findPreference(mLocFreqKey);
        mCpAutoLoc=(CheckBoxPreference)findPreference(mAutoLocKey);
        mRegionDefaultTitle = getString(R.string.preferences_list_region_title);
        mCityDefaultTitle = getString(R.string.preferences_list_city_title);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (mCpAutoLoc.isChecked())
        {
        	mLpCity.setEnabled(false);
        	mLpRegion.setEnabled(false);
        	//checkLocationProviders();
        }
        else
        {
        	mLpCity.setEnabled(true);
        	mLpRegion.setEnabled(true);
        }
        registerListPreferenceChangeListeners();
        boolean iSfirstStart = getIntent().getBooleanExtra(getString(R.string.preferences_is_first_start_key), false); //Определяем первый это запуск настроек.
        String[] regions = mExternalDbAdapter.getRegions();
        String region = mSharedPreferences.getString(mRegionKey, "");
        if (iSfirstStart) region = regions[43]; //Московская область
        String[] citys = mExternalDbAdapter.getCitysByRegion(region);
        mLpRegion.setEntries(regions);
        mLpRegion.setEntryValues(regions);
        mLpCity.setEntries(citys);
        mLpCity.setEntryValues(citys);
        String city;
        if (iSfirstStart)
        {
        	city = citys[42]; //г. Москва
        	mLpCity.setValue(city);
        	mLpRegion.setValue(region);
        	mLpCity.setEnabled(false);
        	mLpRegion.setEnabled(false);
            mCpAutoLoc.setChecked(true);
            mLpLocFreq.setValueIndex(0);
            checkLocationProviders();
        }
        else
        {
        	city =  mSharedPreferences.getString(mCityKey, "");
        }
       	mLpRegion.setTitle(mRegionDefaultTitle + "\n" + region);
        mLpCity.setTitle(mCityDefaultTitle + "\n" + city);
        //Если выбрано "Обновить только один раз сейчас", и выбрано это было раньше, то в summary просто пишем "не обновлять".
        if (mLpLocFreq.getValue().equals(getResources().getStringArray(R.array.auto_location_freq_pref)[3]) && !mSharedPreferences.getBoolean(getString(R.string.preferences_is_location_update_key), false))
        {
        	mLpLocFreq.setSummary(getString(R.string.no_location_upadates_text));
        }
        else
        {
        	mLpLocFreq.setSummary(mLpLocFreq.getValue());
        }
    }
	private void checkLocationProviders()
	{
		if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
		{
			showDialog(NO_AVAILABLE_ALL_LOCATION_PROVIDERS_DIALOG);
		}
		else if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{
			showDialog(NO_AVAILABLE_GPS_LOCATION_PROVIDER_DIALOG);
		}
		else if (!mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
		{
			showDialog(NO_AVAILABLE_NETWORK_LOCATION_PROVIDER_DIALOG);
		}
	}
	@Override
	protected Dialog onCreateDialog(int id)
	{
		return showNoAvailableLocationProvidersDialog(id);
	}
	private AlertDialog showNoAvailableLocationProvidersDialog(int id)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle(getString(R.string.no_available_location_providers_dialog_title));
		switch (id)
		{
			case NO_AVAILABLE_ALL_LOCATION_PROVIDERS_DIALOG:
				builder.setMessage(getString(R.string.no_available_all_location_providers_dialog_message));
				break;
			case NO_AVAILABLE_GPS_LOCATION_PROVIDER_DIALOG:
				builder.setMessage(getString(R.string.no_available_GPS_location_provider_dialog_message));
				break;
			case NO_AVAILABLE_NETWORK_LOCATION_PROVIDER_DIALOG:
				builder.setMessage(getString(R.string.no_available_network_location_provider_dialog_message));
				break;
			default:
				return null;				
		}		
		builder.setPositiveButton(getString(R.string.confirm_dialog_positive_button_text), new DialogInterface.OnClickListener()
		{				
			public void onClick(DialogInterface dialog, int which)
			{
				// TODO Auto-generated method stub
				// Открываем меню андроида, где включается GPS
				Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(i);
			}
		});
		builder.setNegativeButton(getString(R.string.confirm_dialog_negative_button_text), new DialogInterface.OnClickListener()
		{				
			public void onClick(DialogInterface dialog, int which)
			{
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
		return builder.create();
	}
	@Override
    protected void onDestroy()
    {
    	super.onDestroy();
    	mExternalDbAdapter.close();
    }
	private void registerListPreferenceChangeListeners()
	{
		Preference.OnPreferenceChangeListener onPreferenceChangeListener = new Preference.OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				// TODO Auto-generated method stub
				if (preference.getKey().equals(mRegionKey))
				{
					String region = (String)newValue;
					preference.setTitle(mRegionDefaultTitle + "\n" + region);
					String[] citys = mExternalDbAdapter.getCitysByRegion(region);
					mLpCity.setValue(citys[0]);
			        mLpCity.setEntries(citys);
			        mLpCity.setEntryValues(citys);
			        mLpCity.setTitle(mCityDefaultTitle + "\n" + citys[0]);
				}
				else if (preference.getKey().equals(mCityKey))
				{
					preference.setTitle(mCityDefaultTitle + "\n" + (String)newValue);
				}
				else if (preference.getKey().equals(mAutoLocKey))
				{
			        if ((Boolean)newValue)
			        {
			        	mLpRegion.setEnabled(false);
			        	mLpCity.setEnabled(false);
			        	checkLocationProviders();
			        	//Если в частоте обновления написано "Не обновлять", то надо исправить на "Обновить один раз и больше не обновлять" и при выходе обновить координаты.
						if (mLpLocFreq.getValue().equals(getResources().getStringArray(R.array.auto_location_freq_pref)[3])) 
						{
							if (!mSharedPreferences.getBoolean(getString(R.string.preferences_is_location_update_key), false))
							{
								Editor e = mSharedPreferences.edit();
								e.putBoolean(getString(R.string.preferences_is_location_update_key), true);
								e.commit();
							}
							mLpLocFreq.setSummary(getResources().getStringArray(R.array.auto_location_freq_pref)[3]);
						}								
			        }
			        else
			        {
			        	mLpRegion.setEnabled(true);
			        	mLpCity.setEnabled(true);
			        }			        
				}
				else if (preference.getKey().equals(mLocFreqKey))
				{
					//Если выбрано обновление координат "Обновить один раз и больше не обновлять"
					if (mLpLocFreq.getValue().equals(getResources().getStringArray(R.array.auto_location_freq_pref)[3]) && !mSharedPreferences.getBoolean(getString(R.string.preferences_is_location_update_key), false))
					{
						Editor e = mSharedPreferences.edit();
						e.putBoolean(getString(R.string.preferences_is_location_update_key), true);
						e.commit();
					}
					mLpLocFreq.setSummary((String)newValue);
				}
				else
				{
					return false;
				}
				return true;
			}
		};
		mLpRegion.setOnPreferenceChangeListener(onPreferenceChangeListener);
		mLpCity.setOnPreferenceChangeListener(onPreferenceChangeListener);
		mCpAutoLoc.setOnPreferenceChangeListener(onPreferenceChangeListener);
		mLpLocFreq.setOnPreferenceChangeListener(onPreferenceChangeListener);
	}
}