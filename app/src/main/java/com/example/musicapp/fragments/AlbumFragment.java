package com.example.musicapp.fragments;

import static com.example.musicapp.Base.albums;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.musicapp.adapters.AlbumAdapter;
import com.example.musicapp.R;
import com.example.musicapp.models.Song;

public class AlbumFragment extends Fragment {
    RecyclerView recyclerView;
    AlbumAdapter albumAdapter;
    public AlbumFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        recyclerView = view.findViewById(R.id.listAlbum);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 2));

        deleteDataDuplicate(isDuplicate());

        albumAdapter = new AlbumAdapter(this.getContext(), albums, new AlbumAdapter.IClickAlbumItem() {
            @Override
            public void onClickAlbumItem(Song album) {
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                AlbumDetail albumDetail = new AlbumDetail();
                Bundle bundle = new Bundle();
                bundle.putSerializable("album_item", album);
                albumDetail.setArguments(bundle);

                fragmentTransaction.addToBackStack(albumDetail.getClass().getName());
                fragmentTransaction.replace(R.id.content_frame, albumDetail);
                fragmentTransaction.commit();
            }
        });
        recyclerView.setAdapter(albumAdapter);

        return view;
    }

    private boolean isDuplicate() {
        for (int i = 0; i < albums.size(); i++) {
            for (int j = i + 1; j < albums.size(); j++) {
                if (albums.get(i).getAlbum().trim().equalsIgnoreCase(albums.get(j).getAlbum().trim())) {
                    return true;
                }
            }
        }

        return false;
    }

    private void deleteDataDuplicate(boolean isDuplicate) {
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

}