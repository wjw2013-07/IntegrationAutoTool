package com.sdk.chat.chatrow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.sdk.chat.holder.BaseHolder;
import com.moor.imkf.model.entity.FromToMessage;

/**
 * Created by longwei on 2016/3/9.
 */
public interface IChatRow {


    View buildChatView(LayoutInflater inflater, View convertView);


    void buildChattingBaseData(Context context, BaseHolder baseHolder, FromToMessage detail, int position);


    int getChatViewType();
}
