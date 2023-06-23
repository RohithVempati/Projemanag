package com.pctipsguy.projemanag

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pctipsguy.projemanag.databinding.ItemCardBinding

open class CardListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Card>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(ItemCardBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if (holder is MyViewHolder) {
            holder.tvCardName.text = model.name
            if(model.labelColor.isNotEmpty()){
                holder.cardColor.visibility = View.VISIBLE
                holder.cardColor.setBackgroundColor(Color.parseColor(model.labelColor))
            }
            holder.itemView.setOnClickListener{
                if (onClickListener != null) {
                    onClickListener!!.onClick(position)
                }
            }
            if ((context as TaskListActivity).mAssignedMemberDetailList.size > 0) {
                val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()
                for (i in context.mAssignedMemberDetailList.indices) {
                    for (j in model.assignedTo) {
                        if (context.mAssignedMemberDetailList[i].id == j) {
                            val selectedMember = SelectedMembers(
                                context.mAssignedMemberDetailList[i].id,
                                context.mAssignedMemberDetailList[i].image
                            )

                            selectedMembersList.add(selectedMember)
                        }
                    }
                }

                if (selectedMembersList.size > 0) {


                    if (selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy) {
                        holder.rv_card_selected_members_list.visibility = View.GONE
                    } else {
                        holder.rv_card_selected_members_list.visibility = View.VISIBLE

                        holder.rv_card_selected_members_list.layoutManager =
                            GridLayoutManager(context, 4)
                        val adapter = CardMemberListItemsAdapter(context, selectedMembersList,false)
                        holder.rv_card_selected_members_list.adapter = adapter
                        adapter.setOnClickListener(object :
                            CardMemberListItemsAdapter.OnClickListener {
                            override fun onClick() {
                                if (onClickListener != null) {
                                    onClickListener!!.onClick(holder.adapterPosition)
                                }
                            }
                        })
                    }
                } else {
                    holder.rv_card_selected_members_list.visibility = View.GONE
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
        fun onClick(cardPosition:Int)
    }

    class MyViewHolder(itemCardBinding: ItemCardBinding) :
        RecyclerView.ViewHolder(itemCardBinding.root){
            val tvCardName = itemCardBinding.tvCardName
        val cardColor = itemCardBinding.viewLabelColor
        val rv_card_selected_members_list = itemCardBinding.rvCardSelectedMembersList
        }
}