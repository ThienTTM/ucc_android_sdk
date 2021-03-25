package com.vnptit.videocallsample.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.vnptit.videocallsample.R
import com.vnptit.videocallsample.databinding.ItemContactBinding
import com.vnptit.videocallsample.model.ContactCall
import kotlinx.android.synthetic.main.item_contact.view.*

/**
 * Created by Ông Hoàng Nhật Phương on 1/27/2021.
 * VNPT COM
 * phuonghn@vnpt.vn
 */
class HomeAdapter(private val context: Context, private var contacts: List<ContactCall>, private val mListener: IOnClickListener) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding : ItemContactBinding = DataBindingUtil.inflate( LayoutInflater.from(parent.context),R.layout.item_contact,parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        contact.let { holder.bind(it) }
        holder.contactBinding.llChiTiet.setOnClickListener {
            mListener.onClickContact(contact, position)
        }
        holder.contactBinding.ivCall.setOnClickListener {
            mListener.onClickCall(contact, position)
        }
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

     fun replaceData(contacts: List<ContactCall>){
        this.contacts = contacts;
         notifyDataSetChanged()
    }

    fun clearData(){
        contacts = emptyList()
    }

    class ViewHolder(val contactBinding: ItemContactBinding) : RecyclerView.ViewHolder(contactBinding.root) {

        fun bind(contact: ContactCall) {
                contactBinding.contact = contact
           contactBinding.executePendingBindings()
        }
    }

    interface IOnClickListener {
        fun onClickContact(entity: ContactCall, position: Int)
        fun onClickCall(entity: ContactCall, position: Int)
    }
}