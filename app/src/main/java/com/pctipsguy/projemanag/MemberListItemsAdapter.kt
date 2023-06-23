package com.pctipsguy.projemanag


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pctipsguy.projemanag.databinding.ItemMemberBinding

open class MemberListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<User>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, user: User, action: String)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(ItemMemberBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {

            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.iv_member_image)

            holder.tv_member_name.text = model.name
            holder.tv_member_email.text = model.email

            if(model.selected) {
                holder.iv_selected_member.visibility = View.VISIBLE
            }
            else{
                holder.iv_selected_member.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(itemMemberBinding: ItemMemberBinding) :
        RecyclerView.ViewHolder(itemMemberBinding.root){
            val tv_member_name = itemMemberBinding.tvMemberName
            val tv_member_email = itemMemberBinding.tvMemberEmail
            val iv_member_image = itemMemberBinding.ivMemberImage
            val iv_selected_member = itemMemberBinding.ivSelectedMember
    }
}