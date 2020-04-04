package com.example.noteprinter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.noteprinter.entity.Note_Info;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteListAdapter extends RecyclerView.Adapter<NoteListAdapter.ViewHold> {
    private Context mContext;

    private List<Note_Info> mNoteInfoList;

    SimpleDateFormat df1 = new SimpleDateFormat("yyMMddHHmmss", Locale.CHINA);

    DateFormat df2 = DateFormat.getDateTimeInstance(DateFormat.YEAR_FIELD,DateFormat.DATE_FIELD,Locale.CHINA);

    private final int HEADER_LAYOUT = 0;
    private final int COMMON_LAYOUT = 1;

    public NoteListAdapter(List<Note_Info> noteInfoList){
        this.mNoteInfoList = noteInfoList;
    }

    class ViewHold extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tv_content;
        TextView tv_createTime;
        Button btn_create;
        public ViewHold (View view) {
            super(view);
        }
    }

    class CommonViewHold extends ViewHold {
        public CommonViewHold (View view) {
            super(view);
            cardView = (CardView)view;
            tv_content = view.findViewById(R.id.tv_content);
            tv_createTime = view.findViewById(R.id.tv_createTime);
        }
    }

    class HeaderViewHold extends ViewHold {
        public HeaderViewHold (View view) {
            super(view);
            cardView = (CardView)view;
            btn_create = view.findViewById(R.id.btn_create);
        }
    }

    @NonNull
    @Override
    public ViewHold onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (mContext == null) {
            mContext = viewGroup.getContext();
        }
        View view;
        if (viewType == HEADER_LAYOUT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.note_list_item2,viewGroup,false);
            return new HeaderViewHold(view);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.note_list_item, viewGroup, false);
            return new CommonViewHold(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHold viewHold, int position) {
        int viewType = getItemViewType(position);
        if (viewType == HEADER_LAYOUT) {
            viewHold.btn_create.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, EditActivity.class);
                    ((Activity)mContext).startActivityForResult(intent, MainActivity.REQUEST_CODE);
                }
            });
        } else {
            final Note_Info note_info = mNoteInfoList.get(position-1);
            viewHold.tv_createTime.setText(transformTime(note_info.getCreateTime()));
            viewHold.tv_content.setText(note_info.getTextContent());
            viewHold.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, EditActivity.class);
                    intent.putExtra("fileName",note_info.getCreateTime());
                    ((Activity)mContext).startActivityForResult(intent, MainActivity.REQUEST_CODE);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mNoteInfoList.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEADER_LAYOUT;
        } else {
            return COMMON_LAYOUT;
        }
    }

    public String transformTime(String fileName) {
        Date createTime;
        try{
            createTime = df1.parse(fileName);
            return df2.format(createTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
