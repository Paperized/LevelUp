package it.ilogreco.levelup;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import it.ilogreco.levelup.databinding.ActivityMainBinding;
import it.ilogreco.levelup.receiver.MidnightTimerReceiver;
import it.ilogreco.levelup.service.BackgroundService;
import it.ilogreco.levelup.utils.LevelUpUtils;

/**
 * Only one activity that configure the nav controller with the available fragments, and the app bar, initialize a midnight timer, ask for permissions StepCounter and GPS, starts the background service.
 */
public class MainActivity extends AppCompatActivity implements MenuProvider, ServiceConnection, SharedPreferences.OnSharedPreferenceChangeListener {
    private BackgroundService service;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    private SharedPreferences sharedPreferences;
    private TextView headerNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addMenuProvider(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_profile, R.id.nav_home, R.id.nav_edit_category_list)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    6);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            startStepCounterService();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    5);
        }

        // set the name in the nav header
        sharedPreferences = LevelUpUtils.initializeDefaultSharedPreferences(getBaseContext());
        headerNameText = binding.navView.getHeaderView(0).findViewById(R.id.navHeaderName);
        headerNameText.setText(sharedPreferences.getString("KEY_NAME", "Bob"));

        MidnightTimerReceiver.initializeAlarm(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(sharedPreferences != null) {
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(sharedPreferences != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s.equals("KEY_NAME")) {
            headerNameText.setText(sharedPreferences.getString(s, "Bob"));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 5 && Objects.equals(permissions[0], Manifest.permission.ACTIVITY_RECOGNITION)
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            startStepCounterService();
    }

    private void startStepCounterService() {
        Intent serviceIntent = new Intent(getBaseContext(), BackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        if(!bindService(serviceIntent, this, BIND_AUTO_CREATE)) {
            Snackbar.make(binding.getRoot(), "Error during the bind of the server!", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.main, menu);
    }

    @Override
    public void onPrepareMenu(@NonNull Menu menu) {
        menu.findItem(R.id.action_settings).setVisible(true);
        MenuProvider.super.onPrepareMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.action_settings) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.open_settings_frag);
            return true;
        }

        return false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        service = ((BackgroundService.BackgroundBinder)iBinder).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        service = null;
    }

    public BackgroundService getService() {
        return service;
    }
}