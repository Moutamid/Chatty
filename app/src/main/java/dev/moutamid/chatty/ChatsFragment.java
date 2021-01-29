package dev.moutamid.chatty;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChatsFragment extends Fragment {

    private ArrayList<String> mChatImage = new ArrayList<>();
    private ArrayList<String> mChatName = new ArrayList<>();
    private ArrayList<String> mChatLastMsg = new ArrayList<>();
    private View view;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.chats_fragment, container, false);
        sharedPreferences = getActivity().getSharedPreferences("dev.moutamid.chatty", Context.MODE_PRIVATE);

        initImageBitmaps();

        return view;
    }

    private void initImageBitmaps() {

        String chattyLastMsg = sharedPreferences.getString("chattyLastMsg", "Hy...");

        mChatImage.add(String.valueOf(R.drawable.chatty_girl));
        mChatName.add("Chatty");
        mChatLastMsg.add(chattyLastMsg);

        /*mChatImage.add(String.valueOf(R.color.black));
        mChatName.add("Black People");
        mChatLastMsg.add("How are you?");

        mChatImage.add(String.valueOf(R.color.colorPrimary));
        mChatName.add("Green People");
        mChatLastMsg.add("Where are you?");

        mChatImage.add(String.valueOf(R.color.googleColor));
        mChatName.add("Red People");
        mChatLastMsg.add("Who are you?");*/

        initRecyclerView();

    }

    private void initRecyclerView() {

        RecyclerView recyclerView = view.findViewById(R.id.chats_recycler_view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(
                getActivity(),
                mChatImage,
                mChatName,
                mChatLastMsg
        );

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);

    }

}
