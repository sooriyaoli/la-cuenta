package org.blanco.lacuenta;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.blanco.lacuenta.db.entities.Split;
import org.blanco.lacuenta.listeners.CalculateClickListener;
import org.blanco.lacuenta.misc.NumPad;
import org.blanco.lacuenta.receivers.DialogResultReceiver;
import org.blanco.lacuenta.receivers.ResultReceiver;
import org.blanco.lacuenta.receivers.SpeechResultReceiver;
import org.blanco.lacuenta.receivers.TextViewResultReceiver;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.DigitsKeyListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/***
 * @author Alexandro Blanco <ti3r.bubblenet@gmail.com>
 * Initial Activity of the Application.
 */
public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        initComponents();
    }
    
    /***
     * Initializes the components for the current activity. Visual and 
     * non visual members of this context.
     */
	public void initComponents(){
		
		edtTotal = (EditText) findViewById(R.id.main_activity_edt_bill_total);
    	//Set the key listener when the orientation is landscape and the input
		//is done through the softkeyboard
		if (getWindowManager().getDefaultDisplay().getOrientation() 
    			== Configuration.ORIENTATION_LANDSCAPE)		{
			edtTotal.setKeyListener(new DigitsKeyListener(false, true));
    	}
    	txtResult = (TextView) findViewById(R.id.main_activity_txt_result);
    	spnTip = (Spinner) findViewById(R.id.main_activity_spn_tip);
    	spnPeople = (Spinner) findViewById(R.id.main_activity_spn_people);
    	btnCalculate = (Button) findViewById(R.id.main_activity_btn_calculate);
    	clickListener = new CalculateClickListener(edtTotal, spnTip, spnPeople, getResultReceiver());
    	btnCalculate.setOnClickListener(clickListener);
    	numPad = (NumPad) findViewById(R.id.main_activity_num_pad);
    	
    	if (numPad != null) //Landscape layout will not have numPad
    		numPad.setText(edtTotal);
    }
	
	/***
     * This method will return the result Receiver that will be used when displaying 
     * the calculus results. It will return an instance of an object that implements the
     * ResultReceiver interface depending on established application settings.
     * @return an Object that implements ResultReceiver Interface.
     */
    private List<ResultReceiver> getResultReceiver(){
    	boolean showResOnDialog = 
    	PreferenceManager.getDefaultSharedPreferences(this)
    		.getBoolean(SettingsActivity.SHOW_RES_DIALOG_SETTING_NAME, false);
    	List<ResultReceiver> result = new ArrayList<ResultReceiver>(2);
    	if (showResOnDialog){
    		//deactivate the Result Label
    		this.txtResult.setVisibility(View.GONE);
    		result.add(new DialogResultReceiver(this));
    	}
    	else{
    		this.txtResult.setVisibility(View.VISIBLE);
    		result.add(new TextViewResultReceiver(this,txtResult));
    	}
    	boolean textToSpeech = PreferenceManager.getDefaultSharedPreferences(this)
    		.getBoolean(SettingsActivity.SAY_RES_OUT_LOUD, false);
    	if (textToSpeech)
    		result.add(new SpeechResultReceiver(this, Locale.getDefault()));
    	
    	return result;
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
    	new MenuInflater(this).inflate(R.menu.main_activity_main_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.main_activity_main_menu_exit_item:
				setResult(0);
				finish();
			break;
		case R.id.main_activity_main_menu_settings_item:
				startConfiguration();
			break;
		case R.id.main_activity_main_menu_save_expense_item:
				saveExpense();
				break;
		case R.id.main_activity_main_menu_expenses_item:
				startSplits();
		default:
			return super.onMenuItemSelected(featureId, item);
		}
		return true;
	}

	@Override
	protected void onPause() {
		boolean savePrefs = 
			PreferenceManager.getDefaultSharedPreferences(this)
			.getBoolean(SettingsActivity.SAVE_PREFS_SETTING_NAME, false);
		if (savePrefs)
			saveControlPreferences();
		super.onPause();
	}
	
	@Override
	protected void onStart() {
		boolean savePrefs = 
			PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean(SettingsActivity.SAVE_PREFS_SETTING_NAME, false);
		if (savePrefs)
			loadControlPreferences();
		super.onStart();
	}

	/***
	 * Starts the configuration Activity
	 */
	private void startConfiguration() {
		Intent settingsIntent = new Intent(this, SettingsActivity.class);
		startActivityForResult(settingsIntent, 0);
	}

	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0 && resultCode == SettingsActivity.SETTINGS_CHANGED)
			initComponents();
		super.onActivityResult(requestCode, resultCode, data);
	}

	/***
	 * Starts the Spits activity
	 */
	private void startSplits(){
		Intent splitsIntent = new Intent(this,SplitsActivity.class);
		startActivity(splitsIntent);
	}
	
	/***
	 * Save the control values in a preferences file in order to be
	 * restored on the next application execution.
	 */
	private void saveControlPreferences(){
		getSharedPreferences("control_preferences", MODE_PRIVATE).edit().putString("edtTotal", edtTotal.getText().toString()).commit();
		getSharedPreferences("control_preferences", MODE_PRIVATE).edit().putInt("spnTip", spnTip.getSelectedItemPosition()).commit();
		getSharedPreferences("control_preferences", MODE_PRIVATE).edit().putInt("spnPeople", spnPeople.getSelectedItemPosition()).commit();
	}
	/***
	 * Load the control values from a preferences file in order to 
	 * present the user the same interface that when it left
	 */
	private void loadControlPreferences(){
		edtTotal.setText(getSharedPreferences("control_preferences", MODE_PRIVATE).getString("edtTotal", "0"));
		spnTip.setSelection(getSharedPreferences("control_preferences", MODE_PRIVATE).getInt("spnTip", 0));
		spnPeople.setSelection(getSharedPreferences("control_preferences", MODE_PRIVATE).getInt("spnPeople", 0));
	}
	
	/***
	 * Saves the expense that has been calculated into the application's database
	 */
	private void saveExpense(){
		if (clickListener.getResult() != null){
			Uri uri = Split.insert(clickListener.getResult(), this);
			StringBuilder msg = new StringBuilder(getString(R.string.record));
			msg.append(" ").append(ContentUris.parseId(uri)).append(" ").
			append(getString(R.string.created));
			Toast.makeText(this, msg.toString(),500).show();
		}else
			Toast.makeText(this, getString(R.string.no_calculus_done_yet_msg), 500).show();
	}
	
	
	@Override
	protected void onDestroy() {
		//finalize the result receivers, it will free the text to speech service in case it is activated
		if (clickListener != null)
			clickListener.Destroy();
		super.onDestroy();
	}


	EditText edtTotal = null;
    Button btnCalculate = null;
    Spinner spnTip = null;
    Spinner spnPeople = null;
    TextView txtResult = null;
    NumPad numPad= null;
    CalculateClickListener clickListener = null;
	
}