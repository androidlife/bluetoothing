package com.wordpress.laaptu.bluetooth.test;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wordpress.laaptu.bluetooth.R;

import timber.log.Timber;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        addFirstFragment();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addSecondFragment();
            }
        },5000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getSupportFragmentManager().popBackStack();
            }
        },10000);
    }

    private void addFirstFragment(){
        getSupportFragmentManager().beginTransaction().replace(R.id.container,new FirstFragment()).commit();

    }
    private void addSecondFragment(){
        getSupportFragmentManager().beginTransaction().replace(R.id.container,new SecondFragment())
                .addToBackStack(null).commit();

    }

    public static class FirstFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_first,container,false);
        }

        @Override
        public void onPause() {
            super.onPause();
            Timber.d("FirstFragment Paused");
        }

        @Override
        public void onResume() {
            super.onResume();
            Timber.d("FirstFragment Resumed");
        }
    }


    public static class SecondFragment extends Fragment{
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_second,container,false);
        }

        @Override
        public void onPause() {
            super.onPause();
            Timber.d("SecondFragment Paused");
        }

        @Override
        public void onResume() {
            super.onResume();
            Timber.d("SecondFragment Resumed");
        }
    }
}

