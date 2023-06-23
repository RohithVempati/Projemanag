package com.pctipsguy.projemanag

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pctipsguy.projemanag.databinding.ItemLabelColorBinding

class LabelColorListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<String>,
    private val mSelectedColor: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(ItemLabelColorBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]

        if (holder is MyViewHolder) {

            holder.view_main.setBackgroundColor(Color.parseColor(item))

            if (item == mSelectedColor) {
                holder.iv_selected_color.visibility = View.VISIBLE
            } else {
                holder.iv_selected_color.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {

                if (onItemClickListener != null) {
                    onItemClickListener!!.onClick(position, item)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(itemLabelColorBinding: ItemLabelColorBinding) :
        RecyclerView.ViewHolder(itemLabelColorBinding.root){
            val iv_selected_color = itemLabelColorBinding.ivSelectedColor
            val view_main = itemLabelColorBinding.viewMain
        }

    interface OnItemClickListener {
        fun onClick(position: Int, color: String)
    }
}