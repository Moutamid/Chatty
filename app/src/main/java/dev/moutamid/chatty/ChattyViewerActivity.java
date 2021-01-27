package dev.moutamid.chatty;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.android.AIService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChattyViewerActivity extends AppCompatActivity implements AIListener {

    private RecyclerView recyclerView;
    private RecyclerViewAdapterMessages adapter;
    private LinearLayoutManager linearLayoutManager;
    private Handler handler;

    private Button tabBtn;

    private SharedPreferences sharedPreferences;

    private ImageView fab_img;

    private CircleImageView myMsgStatusImg;

    private EditText editText;

    private RelativeLayout addBtn;
    private ScrollView editTextLayout;

    private DatabaseReference ref;

    private String message;

    private Bitmap imgBoy;

    private ArrayList<String> msgText = new ArrayList<>();
    private ArrayList<String> msgUser = new ArrayList<>();

    private AIService aiService;
    private AIDataService aiDataService;
    private AIRequest aiRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatty_viewer);
        editText = findViewById(R.id.editText);
        addBtn = findViewById(R.id.addBtn);
        editTextLayout = findViewById(R.id.edittextLayout);
        myMsgStatusImg = findViewById(R.id.myMessageStatus);
        fab_img = findViewById(R.id.fab_img);

        imgBoy = BitmapFactory.decodeResource(getResources(), R.drawable.boy);

        tabBtn = findViewById(R.id.tabBtn);

        handler = new Handler();
        sharedPreferences = this.getSharedPreferences("moutamid.spdf.com.chatty", Context.MODE_PRIVATE);
        String msgStatus = sharedPreferences.getString("msgStatus", "Error");

        // Changing the color of EditText bar
        if (Build.VERSION.SDK_INT < 21) {
            editText.setBackgroundColor(getResources().getColor(R.color.lighterGrey));
            editTextLayout.setBackgroundColor(getResources().getColor(R.color.lighterGrey));

        }


        if (msgStatus.equals("true")) {
            myMsgStatusImg.setImageResource(R.drawable.boy);
        }

        String userName = sharedPreferences.getString("userName", "Error");

        ref = FirebaseDatabase.getInstance().getReference().child(userName);
        ref.keepSynced(true);

        final AIConfiguration config = new AIConfiguration("9a656058c9ba4eed9ce60bdb2ad6613b",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(this, config);
        aiService.setListener(this);
        aiDataService = new AIDataService(this, config);
        aiRequest = new AIRequest();

        final Bitmap sendImg = BitmapFactory.decodeResource(getResources(), R.drawable.ic_send_white_24dp);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                message = editText.getText().toString().trim();

                ImageViewAnimatedChange(ChattyViewerActivity.this, fab_img, sendImg);

                if (!message.equals("") && isOnline() && !TextUtils.isEmpty(message)) {

                    msgUser.add("user");
                    msgText.add(message);
                    initRecyclerView();

                    setMyMsgStatusImg();

                    editText.setText("");

                    sharedPreferences.edit().putString("chattyLastMsg", message).apply();

                    addBtn.setEnabled(false);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addBtn.setEnabled(true);
                        }
                    }, 3100);


                } else if (message.equals("")) {

                    editText.setError("Please add a message!");
                    editText.requestFocus();

                    //aiService.startListening();
                } else if (!isOnline()) {
                    Toast.makeText(ChattyViewerActivity.this, "You are not online!", Toast.LENGTH_SHORT).show();
                }


            }
        });

    }

    private void uploadMessageAndGetResponse(String message) {

        if (!TextUtils.isEmpty(message)) {

            aiRequest.setQuery(message);

            tabBtn.setText("Typing...");

            ChatMessage chatMessage = new ChatMessage(message, "user");
            ref.child("chat").push().setValue(chatMessage);

            new AsyncTask<AIRequest, Void, AIResponse>() {

                @Override
                protected AIResponse doInBackground(AIRequest... aiRequests) {
                    final AIRequest request = aiRequests[0];
                    try {

                        final AIResponse response = aiDataService.request(aiRequest);

                        return response;

                    } catch (AIServiceException e) {
                        e.printStackTrace();
                        Log.d("MainActivity", " moutamid.spdf.com.chatty: doInBackground: AIResposne Error!");
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(AIResponse response) {
                    if (response != null) {

                        Result result = response.getResult();
                        String reply = result.getFulfillment().getSpeech();

                        tabBtn.setText("Online");

                        msgUser.add("bot");
                        msgText.add(reply);

                        CircleImageViewAnimatedChange(ChattyViewerActivity.this, myMsgStatusImg, imgBoy);

                        initRecyclerView();

                        ChatMessage chatMessage = new ChatMessage(reply, "bot");
                        ref.child("chat").push().setValue(chatMessage);
                        sharedPreferences.edit().putString("chattyLastMsg", reply).apply();
                    }
                }
            }.execute(aiRequest);
        } else if (message.equals("")) {

            editText.setError("Please add a message!");
            editText.requestFocus();

        }
    }

    private Boolean isNetworkAvailable() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }

    private Boolean isOnline() {

        try {

            Process p1 = Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal == 0);
            return reachable;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void ImageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, R.anim.zoom_out);
        final Animation anim_in = AnimationUtils.loadAnimation(c, R.anim.zoom_in);
        anim_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    public void CircleImageViewAnimatedChange(Context c, final CircleImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, R.anim.zoom_out);
        final Animation anim_in = AnimationUtils.loadAnimation(c, R.anim.zoom_in);
        anim_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewMessages);
        adapter = new RecyclerViewAdapterMessages();
        recyclerView.setAdapter(adapter);

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        scrollRecyclerViewToEnd();

    }

    private void scrollRecyclerViewToEnd() {
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int msgCount = adapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (msgCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);

                }

            }
        });
    }

    @Override
    public void onResult(ai.api.model.AIResponse response) {

        Result result = response.getResult();

        String message = result.getResolvedQuery();
        ChatMessage chatMessage0 = new ChatMessage(message, "user");
        ref.child("chat").push().setValue(chatMessage0);

        String reply = result.getFulfillment().getSpeech();

        msgUser.add("bot");
        msgText.add(reply);
        initRecyclerView();

        ChatMessage chatMessage = new ChatMessage(reply, "bot");
        ref.child("chat").push().setValue(chatMessage);

        Toast.makeText(this, "On Result Executed!", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onError(ai.api.model.AIError error) {
    }

    @Override
    public void onAudioLevel(float level) {
    }

    @Override
    public void onListeningStarted() {
    }

    @Override
    public void onListeningCanceled() {
    }

    @Override
    public void onListeningFinished() {
    }

    @Override
    protected void onStart() {
        super.onStart();

        ref.child("chat").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                msgUser.clear();
                msgText.clear();

                for (DataSnapshot questions : dataSnapshot.getChildren()) {

                    String message = questions.child("msgText").getValue(String.class);
                    String user = questions.child("msgUser").getValue(String.class);

                    msgUser.add(user);
                    msgText.add(message);

                }

                initRecyclerView();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.i("Database Error", databaseError.getMessage());
                Log.i("Database Error", databaseError.getDetails());
                Log.d("MainActivity", "onCancelled: " + databaseError.getMessage());

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ChattyViewerActivity.this, TabbedActivity.class));
    }

    private void setMyMsgStatusImg() {

        myMsgStatusImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_msg_not_sent));

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                myMsgStatusImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_msg_sent));
                setDeliveredImage();
            }
        }, 1000);

    }

    private void setDeliveredImage() {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                myMsgStatusImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_msg_delivered));
                setSeenImage();
            }
        }, 1000);

    }

    private void setSeenImage() {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                myMsgStatusImg.setImageDrawable(getResources().getDrawable(R.drawable.boy));
                uploadMessageAndGetResponse(message);
                sharedPreferences.edit().putString("msgStatus", "true").apply();
            }
        }, 1000);

    }

    private class RecyclerViewAdapterMessages extends RecyclerView.Adapter
            <RecyclerViewAdapterMessages.ViewHolderMessages> {

        @NonNull
        @Override
        public ViewHolderMessages onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msglist, parent, false);

            ViewHolderMessages holderMessages = new ViewHolderMessages(view);
            return holderMessages;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderMessages holder, int position) {

            //holder.receivedImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.boy));

            if (msgUser.get(position).equals("user")) {

                holder.rightText.setText(msgText.get(position));

                holder.rightText.setVisibility(View.VISIBLE);
                holder.leftText.setVisibility(View.GONE);

            } else {

                holder.leftText.setText(msgText.get(position));

                holder.rightText.setVisibility(View.GONE);
                holder.leftText.setVisibility(View.VISIBLE);
            }

        }

        @Override
        public int getItemCount() {
            return msgUser.size();
        }

        public class ViewHolderMessages extends RecyclerView.ViewHolder {

            TextView leftText, rightText;
            LinearLayout rightTextLayout;

            public ViewHolderMessages(@NonNull View v) {
                super(v);

                leftText = (TextView) v.findViewById(R.id.leftText);
                rightText = (TextView) v.findViewById(R.id.rightText);
                rightTextLayout = v.findViewById(R.id.rightTextLayout);
            }
        }

    }

}
