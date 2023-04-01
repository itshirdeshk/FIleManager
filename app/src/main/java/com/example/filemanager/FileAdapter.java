package com.example.filemanager;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileViewHolder> {
    private Context context;
    private List<File> file;
    private OnFileSelectedListener listener;

    public FileAdapter(Context context, List<File> file, OnFileSelectedListener listener) {
        this.context = context;
        this.file = file;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileViewHolder(LayoutInflater.from(context).inflate(R.layout.file_container, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
    holder.tvName.setText(file.get(holder.getAdapterPosition()).getName());
        holder.tvName.setSelected(true);
        int items = 0;
        if(file.get(holder.getAdapterPosition()).isDirectory()) {
            File[] files = file.get(holder.getAdapterPosition()).listFiles();
            assert files != null;
            for(File singleFile : files) {
                if (!singleFile.isHidden()) {
                    items += 1;
                }
            }
            holder.tvSize.setText(String.valueOf(items) + " Files");
        } else {
            holder.tvSize.setText(Formatter.formatShortFileSize(context, file.get(holder.getAdapterPosition()).length()));
        }

        if (file.get(holder.getAdapterPosition()).getName().toLowerCase().endsWith(".jpeg")){
            holder.imgFile.setImageResource(R.drawable.ic_image);
        }

        else if (file.get(holder.getAdapterPosition()).getName().toLowerCase().endsWith(".jpg")){
            holder.imgFile.setImageResource(R.drawable.ic_image);
        }

        else if (file.get(holder.getAdapterPosition()).getName().toLowerCase().endsWith(".pdf")){
            holder.imgFile.setImageResource(R.drawable.ic_pdf);
        }

        else if (file.get(holder.getAdapterPosition()).getName().toLowerCase().endsWith(".doc")){
            holder.imgFile.setImageResource(R.drawable.ic_docs);
        }

        else if (file.get(holder.getAdapterPosition()).getName().toLowerCase().endsWith(".mp3")){
            holder.imgFile.setImageResource(R.drawable.ic_music);
        }

        else if (file.get(holder.getAdapterPosition()).getName().toLowerCase().endsWith(".wav")){
            holder.imgFile.setImageResource(R.drawable.ic_music);
        }

        else if (file.get(holder.getAdapterPosition()).getName().toLowerCase().endsWith(".apk")){
            holder.imgFile.setImageResource(R.drawable.ic_android);
        }

        else if (file.get(holder.getAdapterPosition()).getName().toLowerCase().endsWith(".mp4")){
            holder.imgFile.setImageResource(R.drawable.ic_play);
        }

        else {
            holder.imgFile.setImageResource(R.drawable.baseline_folder_24);
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onFileClicked(file.get(holder.getAdapterPosition()));
            }
        });

        holder.container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onFileLongClicked(file.get(holder.getAdapterPosition()), position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return file.size();
    }
}
