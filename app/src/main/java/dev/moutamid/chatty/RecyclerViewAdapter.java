package dev.moutamid.chatty;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

// TODO: DOWNLOAD THIS LIBRARY import com.github.library.bubbleview.BubbleTextView;

// TODO: DOWNLOAD THIS LIBRARY import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolderChats> {

    private ArrayList<String> mChatImage = new ArrayList<>();
    private ArrayList<String> mChatName = new ArrayList<>();
    private ArrayList<String> mChatLastMsg = new ArrayList<>();
    private Context mContext;
    private View view;
    private DatabaseReference ref;
    private SharedPreferences sharedPreferences;


    public RecyclerViewAdapter(Context mContext,
                               ArrayList<String> mChatImage,
                               ArrayList<String> mChatName,
                               ArrayList<String> mChatLastMsg
    ) {
        this.mContext = mContext;
        this.mChatImage = mChatImage;
        this.mChatName = mChatName;
        this.mChatLastMsg = mChatLastMsg;
    }

    @NonNull
    @Override
    public ViewHolderChats onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chats, parent, false);

        ViewHolderChats holderChats = new ViewHolderChats(view);
        return holderChats;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolderChats holder, final int position) {

		sharedPreferences = mContext.getSharedPreferences("dev.moutamid.chatty", Context.MODE_PRIVATE);
	
        int image = Integer.parseInt(mChatImage.get(position));

        holder.chatImage.setImageDrawable(mContext.getResources().getDrawable(image));

        holder.chatName.setText(mChatName.get(position));
        holder.chatLastMsg.setText(mChatLastMsg.get(position));

        holder.chatsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mChatName.get(position).equals("Chatty")) {
                    ((TabbedActivity) mContext).finish();
                    mContext.startActivity(new Intent(mContext, ChattyViewerActivity.class));
                } else {
                    Toast.makeText(mContext, mChatName.get(position) + " Coming Soon..", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.chatsLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                String userName = sharedPreferences.getString("userName", "Error");

                ref = FirebaseDatabase.getInstance().getReference().child(userName);
                dialogBoxConfirm(ref);

                return true;
            }
        });
    }

    private void dialogBoxConfirm(final DatabaseReference ref) {
        new AlertDialog.Builder(mContext)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Delete!")
                .setMessage("Are you sure you want to delete your chat?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        ref.removeValue();
                        Toast.makeText(mContext, "Chat deleted!", Toast.LENGTH_SHORT).show();

                    }
                })
                .setNegativeButton("No", null)
                .show()
                .getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(mContext.getResources().getColor(R.color.colorPrimary));

    }

    @Override
    public int getItemCount() {
        return mChatName.size();
    }

    public class ViewHolderChats extends RecyclerView.ViewHolder {

        CircleImageView chatImage;
        TextView chatName;
        TextView chatLastMsg;
        RelativeLayout chatsLayout;

        public ViewHolderChats(@NonNull View v) {
            super(v);

            chatImage = v.findViewById(R.id.chat_image_recycler_view);
            chatName = v.findViewById(R.id.chat_name_recycler_view);
            chatLastMsg = v.findViewById(R.id.chat_last_msg_recycler_view);
            chatsLayout = v.findViewById(R.id.parent_layout_chats);

        }
    }

}
