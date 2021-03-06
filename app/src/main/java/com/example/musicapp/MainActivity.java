package com.example.musicapp;

import static com.example.musicapp.Base.albums;
import static com.example.musicapp.Base.artists;
import static com.example.musicapp.Base.favoritePlaylist;
import static com.example.musicapp.Base.nowPlaying;
import static com.example.musicapp.Base.nowPosition;
import static com.example.musicapp.Base.sortSong;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicapp.fragments.AlbumFragment;
import com.example.musicapp.fragments.ArtistFragment;
import com.example.musicapp.fragments.FavouriteFragment;
import com.example.musicapp.fragments.SettingsFragment;
import com.example.musicapp.fragments.allSongFragment;
import com.example.musicapp.fragments.isPlayingFragment;
import com.example.musicapp.models.Account;
import com.example.musicapp.models.Song;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SettingsFragment.OnDataPass {

    private static final int REQUEST_CODE = 1;

    private static final int FRAGMENT_IS_PLAYING = 0;
    private static final int FRAGMENT_ALL_SONG = 1;
    private static final int FRAGMENT_FAVOURITE = 2;
    private static final int FRAGMENT_ALBUM = 3;
    private static final int FRAGMENT_ARTIST = 4;
    private static final int FRAGMENT_SETTINGS = 5;

    public static int currentFragment = 0;
    private DrawerLayout drawerLayout;
    public static ArrayList<Song> allOfSong = new ArrayList<>();
    public static NavigationView navigationView;
    public static ApplicationClass appMusic;
    public static Account account;
    public static View header;

    public static final String LIGHT_DARK = "lightDark";
    public static final String key = "dark";

    public static TextView usernameStatic;
    public static ImageView avatarStatic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getDarkOrLightMode();

        setContentView(R.layout.activity_main);
        permission();
        sortSong(allOfSong);

        deleteDataDuplicateAlbum(isDuplicateAlbum());
        deleteDataDuplicateArtist(isDuplicateArtist());

        appMusic = (ApplicationClass) getApplication();
        appMusic.dbManager.open();
        favoritePlaylist = appMusic.dbManager.getAll();
        sortSong(favoritePlaylist);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        header = navigationView.getHeaderView(0);
        usernameStatic = header.findViewById(R.id.usernameOfAccount);
        avatarStatic = header.findViewById(R.id.avatarOfAccount);
        loadAccount();

        changeFragment(new allSongFragment());
        navigationView.getMenu().findItem(R.id.menu_allSong).setChecked(true);
        currentFragment = FRAGMENT_ALL_SONG;
    }

    public static void loadAccount() {
        account = appMusic.dbManager.getAccount();
        usernameStatic.setText(account.getUsername());
        if (account.getAvatar() == null) {
            avatarStatic.setImageResource(R.drawable.avatar);
        } else {
            avatarStatic.setImageBitmap(BitmapFactory.decodeFile(account.getAvatar()));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        appMusic.dbManager.close();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_playing) {
            if (currentFragment != FRAGMENT_IS_PLAYING) {
                goToIsPlayingFragment(nowPlaying.get(nowPosition));
            }
        } else if (id == R.id.menu_allSong) {
            if (currentFragment != FRAGMENT_ALL_SONG) {
                changeFragment(new allSongFragment());
                currentFragment = FRAGMENT_ALL_SONG;
            }
        } else if (id == R.id.menu_favorite) {
            if (currentFragment != FRAGMENT_FAVOURITE) {
                changeFragment(new FavouriteFragment());
                currentFragment = FRAGMENT_FAVOURITE;
            }
        } else if (id == R.id.menu_album) {
            if (currentFragment != FRAGMENT_ALBUM) {
                changeFragment(new AlbumFragment());
                currentFragment = FRAGMENT_ALBUM;
            }
        } else if (id == R.id.menu_artist) {
            if (currentFragment != FRAGMENT_ARTIST) {
                changeFragment(new ArtistFragment());
                currentFragment = FRAGMENT_ARTIST;
            }
        } else if (id == R.id.menu_setting) {
            if (currentFragment != FRAGMENT_SETTINGS) {
                changeFragment(new SettingsFragment());
                currentFragment = FRAGMENT_SETTINGS;
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    private void changeFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(fragment.getClass().getName());
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.commit();
    }

    public void goToIsPlayingFragment(Song song) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        nowPosition = nowPlaying.indexOf(song);

        isPlayingFragment isPlayingFragment = new isPlayingFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable("song_item", song);

        isPlayingFragment.setArguments(bundle);

        currentFragment = FRAGMENT_IS_PLAYING;
        navigationView.getMenu().findItem(R.id.menu_playing).setChecked(true);


        fragmentTransaction.addToBackStack(isPlayingFragment.getClass().getName());
        fragmentTransaction.replace(R.id.content_frame, isPlayingFragment);
        fragmentTransaction.commit();
    }

    /**
     * Click Back on device => close nav if it is opening
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * request permissions to write external storage
     */
    private void permission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        } else {
            allOfSong = getAllSong(this);
            nowPlaying = allOfSong;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
                allOfSong = getAllSong(this);
                sortSong(allOfSong);
                nowPlaying = allOfSong;
                goToIsPlayingFragment(nowPlaying.get(nowPosition));
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        }
    }

    public static ArrayList<Song> getAllSong(Context context) {
        ArrayList<Song> output = new ArrayList<>();
        ArrayList<String> duplicateAlbum = new ArrayList<>();
        ArrayList<String> duplicateArtist = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST
        };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String data = cursor.getString(3);
                String artist = cursor.getString(4);

                Song song = new Song(album, title, duration, data, artist);
                output.add(song);
                if (!duplicateAlbum.contains(album)) {
                    albums.add(song);
                    duplicateAlbum.add(album);
                }
                if (!duplicateArtist.contains(artist)) {
                    artists.add(song);
                    duplicateArtist.add(artist);
                }
            }
            cursor.close();
        }
        return output;
    }

    private void getDarkOrLightMode() {
        SharedPreferences sharedPref = getSharedPreferences(LIGHT_DARK, Context.MODE_PRIVATE);
        boolean darkMode = sharedPref.getBoolean(key, false);

        if (darkMode) {
            setTheme(R.style.Theme_ThemeDark);
        } else {
            setTheme(R.style.Theme_MusicApp);
        }
    }

    @Override
    public void onDataPass(boolean data) {
        SharedPreferences.Editor editor = getSharedPreferences(LIGHT_DARK, Context.MODE_PRIVATE).edit();
        editor.putBoolean(key, data);
        editor.apply();
        navigationView.getMenu().findItem(R.id.menu_allSong).setChecked(true);
        recreate();
    }

    private boolean isDuplicateAlbum() {
        for (int i = 0; i < albums.size(); i++) {
            for (int j = i + 1; j < albums.size(); j++) {
                if (albums.get(i).getAlbum().trim().equalsIgnoreCase(albums.get(j).getAlbum().trim())) {
                    return true;
                }
            }
        }

        return false;
    }

    private void deleteDataDuplicateAlbum(boolean isDuplicate) {
        if (isDuplicate) {
            for (int i = 0; i < albums.size(); i++) {
                for (int j = i + 1; j < albums.size(); j++) {
                    if (albums.get(i).getAlbum().trim().equalsIgnoreCase(albums.get(j).getAlbum().trim())) {
                        albums.remove(i);
                    }
                }
            }
        }
    }

    private boolean isDuplicateArtist() {
        for (int i = 0; i < artists.size(); i++) {
            for (int j = i + 1; j < artists.size(); j++) {
                if (artists.get(i).getArtist().trim().equalsIgnoreCase(artists.get(j).getArtist().trim())) {
                    return true;
                }
            }
        }

        return false;
    }

    private void deleteDataDuplicateArtist(boolean isDuplicate) {
        if (isDuplicate) {
            for (int i = 0; i < artists.size(); i++) {
                for (int j = i + 1; j < artists.size(); j++) {
                    if (artists.get(i).getArtist().trim().equalsIgnoreCase(artists.get(j).getArtist().trim())) {
                        artists.remove(i);
                    }
                }
            }
        }
    }
}