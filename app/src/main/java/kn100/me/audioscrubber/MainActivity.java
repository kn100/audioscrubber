package kn100.me.audioscrubber;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    public MediaPlayer mediaPlayer = new MediaPlayer();
    public TextView textProgress;
    private Handler mHandler = new Handler();
    SeekBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        progressBar = (SeekBar) findViewById(R.id.seekBar);
        textProgress =  (TextView) findViewById(R.id.textProgress);
    }

    public void togglePlayback() {
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();

        } else {
            mediaPlayer.start();
        }
        updateInterface();
    }

    public void seekRelative(int seek) {
        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+seek);
    }

    public void updateInterface() {
        ImageView btnPlayPause = (ImageView) findViewById(R.id.btn_playpause);
        if (mediaPlayer.isPlaying()) {
            btnPlayPause.setImageResource(R.mipmap.pause_button);
        } else {
            btnPlayPause.setImageResource(R.mipmap.play_button);
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_select_audio) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*");
            startActivityForResult(intent, 1);
            String x = "poptarts";
        } else if (id == R.id.nav_exit) {
            System.exit(0);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Make sure the request was successful
            Uri uri = null;
            if(data != null) {
                uri = data.getData();
                playAudio(uri);
            } else {
                Toast toast = Toast.makeText(this, R.string.fileSelectionError,Toast.LENGTH_SHORT);
                toast.show();
            }

        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        Toast toast = Toast.makeText(this, sharedPref.getString("URI", ""), Toast.LENGTH_SHORT);
        toast.show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        Toast toast = Toast.makeText(this, "onResume woo!", Toast.LENGTH_SHORT);
        toast.show();
    }
    protected void playAudio(Uri filepath) {
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try{
            mediaPlayer.setDataSource(getApplicationContext(),filepath);
            mediaPlayer.prepare();
        } catch(Exception e) {
            Toast toast = Toast.makeText(this, R.string.mediaPlayerError,Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        mediaPlayer.start();
        SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("URI", filepath.getPath());
        editor.commit();
        setButtonListeners();
        updateInterface();
        progressBar.setMax(mediaPlayer.getDuration()/1000);
        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(mediaPlayer != null){

                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    int mTotalSeconds = mediaPlayer.getDuration() / 1000;
                    progressBar.setProgress(mCurrentPosition);
                    int hp = mCurrentPosition / 3600;
                    int mp = (mCurrentPosition % 3600) / 60;
                    int sp = mCurrentPosition % 60;
                    int ht = mTotalSeconds / 3600;
                    int mt = (mTotalSeconds % 3600) / 60;
                    int st = mTotalSeconds % 60;
                    String progress = String.format(Locale.getDefault(), "%02d:%02d:%02d / %02d:%02d:%02d",hp,mp,sp,ht,mt,st);
                    textProgress.setText(progress);
                }
                mHandler.postDelayed(this, 100);
            }
        });
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer != null && fromUser){
                    mediaPlayer.seekTo(progress * 1000);
                }
            }
        });
        TextView playingName = (TextView) findViewById(R.id.txt_audioName);
        playingName.setText(R.string.PlayingAudio);
    }

    protected void setButtonListeners() {
        ImageView btnPlayPause = (ImageView) findViewById(R.id.btn_playpause);
        btnPlayPause.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    togglePlayback();
                    return true;
                }
                return false;
            }
        });
        ImageView btnff = (ImageView) findViewById(R.id.btn_ff);
        btnff.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    seekRelative(30000);
                    return true;
                }
                return false;
            }
        });
        ImageView btnrw = (ImageView) findViewById(R.id.btn_rw);
        btnrw.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    seekRelative(-5000);
                    return true;
                }
                return false;
            }
        });
    }
}
