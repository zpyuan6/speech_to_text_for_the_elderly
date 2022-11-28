package com.example.speech_to_text_for_the_elderly;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPref;

    private Button addButton;
    private Button reduceButton;
    private Button cleanButton;
    private EditText editPresentText;
    private EditText editTextSize;

    private String presentString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editPresentText = findViewById(R.id.editPresentText);
        editTextSize = findViewById(R.id.editTextSize);
        addButton = findViewById(R.id.add);
        reduceButton = findViewById(R.id.redu);
        cleanButton = findViewById(R.id.clean);

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
}