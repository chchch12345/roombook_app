package com.example.roombookingsignage;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

public class loading_screen extends AppCompatActivity {
    private RelativeLayout loadingScreen;
    private LoadDataTask loadDataTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        loadingScreen = findViewById(R.id.loading_screen);
    }
    private void showLoadingScreen() {
        loadingScreen.setVisibility(View.VISIBLE);
    }

    private void hideLoadingScreen() {
        loadingScreen.setVisibility(View.GONE);
    }

    private class LoadDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            // Load data or resources here
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideLoadingScreen();
            // Show the main content
            // setContentView(R.layout.main_content);
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        showLoadingScreen();
        loadDataTask = new LoadDataTask();
        loadDataTask.execute();
    }

    private void cancelLoading() {
        if (loadDataTask != null) {
            loadDataTask.cancel(true);
            hideLoadingScreen();
        }
    }

}