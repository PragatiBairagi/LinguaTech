package com.pragati.linguateach;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner fromSpinner, toSpinner;
    private AutoCompleteTextView sourceEdt;
    private MaterialButton translateBtn;
    private TextView translatedTV;
    private ImageView micIV;

    String[] fromLanguages = {"From", "English", "Afrikaans", "Arabic", "Belarusian", "Bengali", "Catalan",
            "Czech", "Welsh", "Hindi", "Urdu", "Chinese", "French", "German",
            "Greek", "Italian", "Japanese", "Korean", "Portuguese", "Russian", "Spanish", "Tamil", "Telugu",
            "Kannada", "Marathi", "Gujarati", "Dutch", "Swedish", "Thai", "Vietnamese",
            "Finnish", "Hebrew", "Indonesian", "Malay", "Norwegian", "Polish", "Romanian", "Turkish", "Ukrainian"};

    String[] toLanguages = {"To", "English", "Afrikaans", "Arabic", "Belarusian", "Bengali", "Catalan",
            "Czech", "Welsh", "Hindi", "Urdu", "Chinese", "French", "German",
            "Greek", "Italian", "Japanese", "Korean", "Portuguese", "Russian", "Spanish", "Tamil", "Telugu",
            "Kannada", "Marathi", "Gujarati", "Dutch", "Swedish", "Thai", "Vietnamese",
            "Finnish", "Hebrew", "Indonesian", "Malay", "Norwegian", "Polish", "Romanian", "Turkish", "Ukrainian"};

    private static final int REQUEST_CODE_SPEECH_INPUT = 1;
    private String fromLanguageCode = "";
    private String toLanguageCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromSpinner = findViewById(R.id.idFromSpinner);
        toSpinner = findViewById(R.id.idToSpinner);
        sourceEdt = findViewById(R.id.idEdtSource);
        translateBtn = findViewById(R.id.idBtnTranslate);
        translatedTV = findViewById(R.id.idTvTranslatedTV);
        micIV = findViewById(R.id.idMicIcon);

        ArrayAdapter<String> fromAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, fromLanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        ArrayAdapter<String> toAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, toLanguages);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLanguageCode = getLanguageCode(fromLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                fromLanguageCode = "";
            }
        });

        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLanguageCode = getLanguageCode(toLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                toLanguageCode = "";
            }
        });

        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                translatedTV.setText("");
                if (sourceEdt.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter your text to translate", Toast.LENGTH_SHORT).show();
                } else if (fromLanguageCode.isEmpty() || fromLanguageCode.equals("From")) {
                    Toast.makeText(MainActivity.this, "Please select source language", Toast.LENGTH_SHORT).show();
                } else if (toLanguageCode.isEmpty() || toLanguageCode.equals("To")) {
                    Toast.makeText(MainActivity.this, "Please select language to translate", Toast.LENGTH_SHORT).show();
                } else {
                    translateText(fromLanguageCode, toLanguageCode, sourceEdt.getText().toString());
                }
            }
        });

        micIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, fromLanguageCode.isEmpty() ? Locale.getDefault().getLanguage() : fromLanguageCode);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to translate");
                try {
                    startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null && !result.isEmpty()) {
                    sourceEdt.setText(result.get(0));
                }
            }
        }
    }

    private void translateText(String fromLanguageCode, String toLanguageCode, String src) {
        translatedTV.setText("Downloading Model...");
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();

        final Translator translator = com.google.mlkit.nl.translate.Translation.getClient(options);

        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translatedTV.setText("Translating...");
                translator.translate(src).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translatedTV.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("TranslationError", "Failed to translate: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "Failed to translate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("ModelDownloadError", "Failed to download the language model: " + e.getMessage());
                Toast.makeText(MainActivity.this, "Failed to download the language model: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getLanguageCode(String language) {
        switch (language) {
            case "English":
                return TranslateLanguage.ENGLISH;
            case "Afrikaans":
                return TranslateLanguage.AFRIKAANS;
            case "Arabic":
                return TranslateLanguage.ARABIC;
            case "Belarusian":
                return TranslateLanguage.BELARUSIAN;
            case "Bengali":
                return TranslateLanguage.BENGALI;
            case "Catalan":
                return TranslateLanguage.CATALAN;
            case "Czech":
                return TranslateLanguage.CZECH;
            case "Welsh":
                return TranslateLanguage.WELSH;
            case "Hindi":
                return TranslateLanguage.HINDI;
            case "Urdu":
                return TranslateLanguage.URDU;
            case "Chinese":
                return TranslateLanguage.CHINESE;
            case "French":
                return TranslateLanguage.FRENCH;
            case "German":
                return TranslateLanguage.GERMAN;
            case "Greek":
                return TranslateLanguage.GREEK;
            case "Italian":
                return TranslateLanguage.ITALIAN;
            case "Japanese":
                return TranslateLanguage.JAPANESE;
            case "Korean":
                return TranslateLanguage.KOREAN;
            case "Portuguese":
                return TranslateLanguage.PORTUGUESE;
            case "Russian":
                return TranslateLanguage.RUSSIAN;
            case "Spanish":
                return TranslateLanguage.SPANISH;
            case "Tamil":
                return TranslateLanguage.TAMIL;
            case "Telugu":
                return TranslateLanguage.TELUGU;
            case "Kannada":
                return TranslateLanguage.KANNADA;
            case "Marathi":
                return TranslateLanguage.MARATHI;
            case "Gujarati":
                return TranslateLanguage.GUJARATI;
            case "Dutch":
                return TranslateLanguage.DUTCH;
            case "Swedish":
                return TranslateLanguage.SWEDISH;
            case "Thai":
                return TranslateLanguage.THAI;
            case "Vietnamese":
                return TranslateLanguage.VIETNAMESE;
            case "Finnish":
                return TranslateLanguage.FINNISH;
            case "Hebrew":
                return TranslateLanguage.HEBREW;
            case "Indonesian":
                return TranslateLanguage.INDONESIAN;
            case "Malay":
                return TranslateLanguage.MALAY;
            case "Norwegian":
                return TranslateLanguage.NORWEGIAN;
            case "Polish":
                return TranslateLanguage.POLISH;
            case "Romanian":
                return TranslateLanguage.ROMANIAN;
            case "Turkish":
                return TranslateLanguage.TURKISH;
            case "Ukrainian":
                return TranslateLanguage.UKRAINIAN;
            default:
                return "";
        }
    }
}
