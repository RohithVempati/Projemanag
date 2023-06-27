package com.pctipsguy.projemanag

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pctipsguy.projemanag.databinding.ItemBoardBinding


open class BoardItemsAdapter(private val context: Context, private var list: ArrayList<Board>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(ItemBoardBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            Glide
                .with(context)
                .load(model.boardImage)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(holder.boardImage)

            holder.boardName.text = model.boardName
            val creator = "Created By : ${model.createdBy}"
            holder.createdBy.text = creator

            holder.itemView.setOnClickListener {

                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model,false)
                }
            }
            holder.boardImage.setOnClickListener{
                if(onClickListener!=null){
                    onClickListener!!.onClick(position,model,true)
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
        fun onClick(position: Int, model: Board,edit:Boolean)
    }

    fun notifyDeleteItem(user:User, position: Int){
        if(user.id==list[position].assignedTo[0]) {
            FirestoreClass().deleteBoard(list[position].documentId)
            list.removeAt(position)
        }
        else{
            (context as MainActivity).showErrorSnackBar("ONly creator can destroy")
        }
        (context as MainActivity).updateNavUserDetails(user,true)
        notifyItemChanged(position)
    }

    fun notifyEditItem(user: User,position: Int,board: String){
        list[position].boardName = board
        FirestoreClass().editBoard(list[position].documentId, list[position])
        (context as MainActivity).updateNavUserDetails(user,true)
        notifyItemChanged(position)
    }

    fun notifyEditPic(user:User,position: Int,imageUrl: String){
        list[position].boardImage = imageUrl
        FirestoreClass().editBoard(list[position].documentId,list[position])
        notifyItemChanged(position)
        (context as MainActivity).updateNavUserDetails(user,true)
        context.hideProgressDialog()

    }

    private class MyViewHolder(itemBoardBinding: ItemBoardBinding) :
        RecyclerView.ViewHolder(itemBoardBinding.root){
            val boardName = itemBoardBinding.tvName
            val boardImage = itemBoardBinding.ivBoardImage
            val createdBy = itemBoardBinding.tvCreatedBy
        }
}