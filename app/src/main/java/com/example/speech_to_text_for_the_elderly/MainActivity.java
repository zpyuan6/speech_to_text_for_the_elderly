package com.example.speech_to_text_for_the_elderly;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import net.gotev.speech.GoogleVoiceTypingDisabledException;
import net.gotev.speech.Speech;
import net.gotev.speech.SpeechDelegate;
import net.gotev.speech.SpeechRecognitionNotAvailable;
import net.gotev.speech.SpeechUtil;
import net.gotev.speech.SupportedLanguagesListener;
import net.gotev.speech.UnsupportedReason;
import net.gotev.speech.ui.SpeechProgressView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity{

    SharedPreferences sharedPref;
//    private Spinner languageSpinner;
    private Button addButton;
    private Button reduceButton;
    private Button cleanButton;
    private ImageButton sayButton;
    private EditText editPresentText;
    private EditText editTextSize;
    private SpeechProgressView speechProgressView;

    private SpeechDelegate speechDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Speech.init(this,getPackageName()).setLocale(Locale.SIMPLIFIED_CHINESE).setTransitionMinimumDelay(500);

        initView();

        refreshSize();
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        loadSetting();

        cleanButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editPresentText.setText("");
                    }
                }
        );

        addButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("text size reduce", editPresentText.getTextSize()+"");
                        editPresentText.setTextSize(TypedValue.COMPLEX_UNIT_PX,editPresentText.getTextSize()+4.0f);
                        refreshSize();
                        modifySetting();
                    }
                }
        );


        reduceButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("text size reduce", editPresentText.getTextSize()+"");
                        editPresentText.setTextSize(TypedValue.COMPLEX_UNIT_PX,editPresentText.getTextSize()-4.0f);
                        refreshSize();
                        modifySetting();
                    }
                }
        );

        editTextSize.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                        Log.i("editor action", textView.getText().toString());

                        if (Double.valueOf(textView.getText().toString())>50 && Double.valueOf(textView.getText().toString())<500){
                            editPresentText.setTextSize(TypedValue.COMPLEX_UNIT_PX,Float.valueOf(textView.getText().toString()));
                            modifySetting();
                        }

                        return false;
                    }
                }
        );

        Context that = this;

        speechDelegate = new SpeechDelegate() {
            @Override
            public void onStartOfSpeech() {

            }

            @Override
            public void onSpeechRmsChanged(float value) {
                //Log.d(getClass().getSimpleName(), "Speech recognition rms is now " + value +  "dB");
            }

            @Override
            public void onSpeechPartialResults(List<String> results) {
                editPresentText.setText("");
                for (String partial : results) {
                    editPresentText.append(partial + " ");

                    Log.i("results", partial);
                }
            }

            @Override
            public void onSpeechResult(String result) {
                sayButton.setVisibility(View.VISIBLE);
                speechProgressView.setVisibility(View.GONE);

                editPresentText.setText(result);

                Log.i("result", result);
            }
        };

        sayButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("The language setting: ", Speech.getInstance().getSpeechToTextLanguage().getLanguage());

                        if (Speech.getInstance().isListening()) {
                            Speech.getInstance().stopListening();
                            Log.d("The isListening: ", Speech.getInstance().isListening()+"");
                        } else {
                            if (ContextCompat.checkSelfPermission(that, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                sayButton.setVisibility(View.GONE);
                                speechProgressView.setVisibility(View.VISIBLE);

                                try{
                                    Speech.getInstance().stopTextToSpeech();
                                    Log.d("Click Progress","Click Progress!!!!!!!"+Speech.getInstance());

                                    Speech.getInstance().startListening(speechProgressView, speechDelegate);
                                } catch (SpeechRecognitionNotAvailable exc) {
                                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    SpeechUtil.redirectUserToGoogleAppOnPlayStore(MainActivity.this);
                                                    break;

                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    break;
                                            }
                                        }
                                    };

                                    AlertDialog.Builder builder = new AlertDialog.Builder(that);
                                    builder.setMessage("Speech recognition is not available on this device. Do you want to install Google app to have speech recognition?")
                                            .setCancelable(false)
                                            .setPositiveButton("Yes", dialogClickListener)
                                            .setNegativeButton("No", dialogClickListener)
                                            .show();

                                } catch (GoogleVoiceTypingDisabledException exc) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(that);
                                    builder.setMessage("Please enable Google Voice Typing to use speech recognition!")
                                            .setCancelable(false)
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    // do nothing
                                                }
                                            })
                                            .show();
                                }

                            } else {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                            }
                        }
                    }
                }
        );

        speechProgressView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (Speech.getInstance().isListening()) {
                            Speech.getInstance().stopListening();
                        }

                        Log.d("Click Progress","Click Progress!!!!!!!"+Speech.getInstance()+":"+Speech.getInstance().isListening());
                        speechProgressView.setVisibility(View.GONE);
                        sayButton.setVisibility(View.VISIBLE);
                    }
                }
        );
    }

    private void initView(){
        editPresentText = findViewById(R.id.editPresentText);
        editTextSize = findViewById(R.id.editTextSize);
        addButton = findViewById(R.id.add);
        reduceButton = findViewById(R.id.redu);
        cleanButton = findViewById(R.id.clean);
        speechProgressView = findViewById(R.id.progress);
        sayButton = findViewById(R.id.sayButton);

//        languageSpinner = findViewById(R.id.languageSpinner);
//
//        Speech.getInstance().getSupportedSpeechToTextLanguages(new SupportedLanguagesListener() {
//            @Override
//            public void onSupportedLanguages(List<String> supportedLanguages) {
//                CharSequence[] items = new CharSequence[supportedLanguages.size()];
//                supportedLanguages.toArray(items);
//
//                for (String l:supportedLanguages){
//                    Log.d("l",l);
//                }
//
//                ArrayAdapter<String> starAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.language_item_selected,supportedLanguages);
//
//                starAdapter.setDropDownViewResource(R.layout.language_item);
//
//                languageSpinner.setAdapter(starAdapter);
//
//                languageSpinner.setSelection(sharedPref.getInt("speechLanguage",21));
//            }
//
//            @Override
//            public void onNotSupported(UnsupportedReason reason) {
//                Log.e("Not Supported", reason.toString());
//            }
//        });
//
//        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });
    }

    private void refreshSize(){
        editTextSize.setText(editPresentText.getTextSize()+"");
    }

    private void loadSetting(){
        Float presentTextSize = sharedPref.getFloat("PresentTextSize",83f);
        editTextSize.setText(presentTextSize+"");
        editPresentText.setTextSize(TypedValue.COMPLEX_UNIT_PX,presentTextSize);
    }

    private void modifySetting(){
        Float presentTextSize = editPresentText.getTextSize();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat("PresentTextSize", presentTextSize);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        Speech.getInstance().shutdown();
        super.onDestroy();
    }

}