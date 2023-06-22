package com.pctipsguy.projemanag


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pctipsguy.projemanag.databinding.ItemMemberBinding

open class MemberListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<User>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
    }
}