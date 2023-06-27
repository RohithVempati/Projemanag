package com.pctipsguy.projemanag

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pctipsguy.projemanag.databinding.ItemCardSelectedMemberBinding

open class CardMemberListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<SelectedMembers>,
    private val assignMembers:Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(ItemCardSelectedMemberBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {

            if (position == list.size - 1 && assignMembers) {
                holder.iv_add_member.visibility = View.VISIBLE
                holder.iv_selected_member_image.visibility = View.GONE
            } else {
                holder.iv_add_member.visibility = View.GONE
                holder.iv_selected_member_image.visibility = View.VISIBLE

                Glide
                    .with(context)
                    .load(model.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(holder.iv_selected_member_image)
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick()
    }

    class MyViewHolder(itemCardSelectedMemberBinding: ItemCardSelectedMemberBinding) :
        RecyclerView.ViewHolder(itemCardSelectedMemberBinding.root){
        val iv_selected_member_image = itemCardSelectedMemberBinding.ivSelectedMemberImage
        val iv_add_member = itemCardSelectedMemberBinding.ivAddMember
        }
}