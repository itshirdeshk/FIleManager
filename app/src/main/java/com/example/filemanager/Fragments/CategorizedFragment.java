package com.example.filemanager.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.FileAdapter;
import com.example.filemanager.FileOpener;
import com.example.filemanager.OnFileSelectedListener;
import com.example.filemanager.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class CategorizedFragment extends Fragment implements OnFileSelectedListener {
    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private List<File> fileList;
    String data;
    File storage;
    String[] items = {"Details", "Rename", "Delete", "Share"};
    File path;
    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_categorized, container, false);

        Bundle bundle = this.getArguments();
        assert bundle != null;
        if (bundle.getString("fileType").equals("downloads")) {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        } else {
            path = Environment.getExternalStorageDirectory();
        }

        runtimePermission();
        return view;
    }

    private void runtimePermission() {
        Dexter.withContext(getContext()).withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                displayFiles();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    public ArrayList<File> findFiles(File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        assert files != null;
        for (File singleFiles : files) {
            if (singleFiles.isDirectory() && !singleFiles.isHidden()) {
                arrayList.addAll(findFiles(singleFiles));
            } else {
                switch (requireArguments().getString("fileType")) {
                    case "image":
                        if (singleFiles.getName().toLowerCase().endsWith(".jpeg") || singleFiles.getName().toLowerCase().endsWith(".jpg")){
                            arrayList.add(singleFiles);
                        }
                        break;

                    case "video":
                        if (singleFiles.getName().toLowerCase().endsWith(".mp4")) {
                            arrayList.add(singleFiles);
                        }
                        break;

                    case "music":
                        if (singleFiles.getName().toLowerCase().endsWith(".mp3") || singleFiles.getName().toLowerCase().endsWith(".wav")) {
                            arrayList.add(singleFiles);
                        }
                        break;

                    case "docs":
                        if (singleFiles.getName().toLowerCase().endsWith(".doc") || singleFiles.getName().toLowerCase().endsWith(".pdf")) {
                            arrayList.add(singleFiles);
                        }
                        break;

                    case "apk":
                        if (singleFiles.getName().toLowerCase().endsWith(".apk")) {
                            arrayList.add(singleFiles);
                        }
                        break;

                    case "downloads":
                        if (singleFiles.getName().toLowerCase().endsWith(".jpeg") || singleFiles.getName().toLowerCase().endsWith(".jpg") || singleFiles.getName().toLowerCase().endsWith(".mp3") || singleFiles.getName().toLowerCase().endsWith(".wav") || singleFiles.getName().toLowerCase().endsWith(".mp4") || singleFiles.getName().toLowerCase().endsWith(".pdf") || singleFiles.getName().toLowerCase().endsWith(".doc") || singleFiles.getName().toLowerCase().endsWith(".apk")) {
                            arrayList.add(singleFiles);
                        }
                        break;
                }
            }
        }
        return arrayList;
    }

    private void displayFiles() {
        recyclerView = view.findViewById(R.id.recycler_internal);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        fileList = new ArrayList<>();
        fileList.addAll(findFiles(path));

        fileAdapter = new FileAdapter(getContext(), fileList, this);
        recyclerView.setAdapter(fileAdapter);
    }

    @Override
    public void onFileClicked(File file) {
        if (file.isDirectory()) {
            Bundle bundle = new Bundle();
            bundle.putString("path", file.getAbsolutePath());
            CategorizedFragment internalFragment = new CategorizedFragment();
            internalFragment.setArguments(bundle);
            assert getFragmentManager() != null;
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, internalFragment).addToBackStack(null).commit();
        } else {
            try {
                FileOpener.openFile(getContext(), file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onFileLongClicked(File file, int position) {
        Dialog optionDialog = new Dialog(getContext());
        optionDialog.setContentView(R.layout.options_dialog);
        optionDialog.setTitle("Select Options...");
        ListView options = (ListView) optionDialog.findViewById(R.id.list);
        CustomAdapter customAdapter = new CustomAdapter();
        options.setAdapter(customAdapter);
        optionDialog.show();

        options.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                switch (selectedItem) {
                    case "Details":
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Details :");
                        final TextView details = new TextView(getContext());
                        builder.setView(details);
                        Date lastModified = new Date(file.lastModified());
                        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        String formattedDate = format.format(lastModified);

                        details.setText("File Name :" + file.getName() + "\n" + "Size : " + Formatter.formatShortFileSize(getContext(), file.length()) + "\n" + "Path : " + file.getAbsolutePath() + "\n" + "Last Modified : " + formattedDate);

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                optionDialog.cancel();
                            }
                        });
                        AlertDialog alertDialog_details = builder.create();
                        alertDialog_details.show();
                        break;

                    case "Rename":
                        AlertDialog.Builder renameDialog = new AlertDialog.Builder(getContext());
                        renameDialog.setTitle("Rename File : ");
                        final EditText name = new EditText(getContext());
                        renameDialog.setView(name);

                        renameDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String new_name = name.getEditableText().toString();
                                String extension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
                                File current = new File(file.getAbsolutePath());
                                File destination = new File(file.getAbsolutePath().replace(file.getName(), new_name) + extension);

                                if (current.renameTo(destination)) {
                                    fileList.set(position, destination);
                                    fileAdapter.notifyItemChanged(position);
                                    Toast.makeText(getContext(), "Renamed Successfully...", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Couldn't Rename...", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        renameDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                optionDialog.cancel();
                            }
                        });

                        AlertDialog alertDialog_rename = renameDialog.create();
                        alertDialog_rename.show();

                        break;
                    case "Delete":
                        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getContext());
                        deleteDialog.setTitle("Delete : " + file.getName() + "?");

                        deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                file.delete();
                                fileList.remove(position);
                                fileAdapter.notifyDataSetChanged();
                                Toast.makeText(getContext(), "Deleted Successfully...", Toast.LENGTH_SHORT).show();
                            }
                        });

                        deleteDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                optionDialog.cancel();
                            }
                        });

                        AlertDialog alertDialog_delete = deleteDialog.create();
                        alertDialog_delete.show();
                        break;

                    case "Share":
                        String fileName = file.getName();
                        Intent share = new Intent();
                        share.setAction(Intent.ACTION_SEND);
                        share.setType("image/jpg");
                        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                        startActivity(Intent.createChooser(share, "Share : " + fileName));
                        break;
                }
            }
        });
    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return items[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View myView = getLayoutInflater().inflate(R.layout.options_layout, null);
            TextView txtOptions = myView.findViewById(R.id.txtOption);
            ImageView imgOptions = myView.findViewById(R.id.imgOption);
            txtOptions.setText(items[position]);

            if (items[position].equals("Details")) {
                imgOptions.setImageResource(R.drawable.ic_details);
            } else if (items[position].equals("Rename")) {
                imgOptions.setImageResource(R.drawable.ic_rename);
            } else if (items[position].equals("Delete")) {
                imgOptions.setImageResource(R.drawable.ic_delete);
            } else if (items[position].equals("Share")) {
                imgOptions.setImageResource(R.drawable.ic_share);
            }
            return myView;
        }
    }
}
