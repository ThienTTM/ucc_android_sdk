package com.vnptit.videocallsample.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vnptit.video_call_sdk.model.calling.PersionReceiver;
import com.vnptit.videocallsample.R;
import com.vnptit.videocallsample.model.Contact;

import java.util.List;


public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private Context context;
    private List<Contact> listContact;
    private IOnClickListener mListener;

    public ContactAdapter(Context context, List<Contact> listContact, IOnClickListener mListener) {
        this.context = context;
        this.listContact = listContact;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContactViewHolder(View.inflate(context, R.layout.item_contact,null));
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact persionReceiver = listContact.get(position);
        holder.bind(persionReceiver,position);
    }

    @Override
    public int getItemCount() {
        return listContact.size();
    }

    public void replaceData(List<Contact> contacts) {
        listContact = contacts;
        notifyDataSetChanged();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvPhone;
        private ImageView ivCall;
        private LinearLayout llChiTiet;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            llChiTiet = itemView.findViewById(R.id.llChiTiet);
            ivCall = itemView.findViewById(R.id.ivCall);
        }

        public void bind(Contact contact, int position) {
            tvName.setText(contact.getName());
            tvPhone.setText(contact.getPhone());

            llChiTiet.setOnClickListener(v -> {
                if(mListener != null) {
                    mListener.onClickContact(contact, position);
                }
            });

            ivCall.setOnClickListener(v -> {
                if(mListener != null) {
                    mListener.onClickCall(contact, position);
                }
            });
        }
    }

    public interface IOnClickListener {
        void onClickContact(Contact entity, int position);
        void onClickCall(Contact entity, int position);
    }
}
