package com.pctipsguy.projemanag

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pctipsguy.projemanag.databinding.ItemTaskBinding
import java.util.Collections

open class TaskListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Task>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mDraggedPositionFrom = -1
    private var mDraggedPositionTo = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view = ItemTaskBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins((15.toDp()).toPx(), 0, (40.toDp()).toPx(), 0)
        view.root.layoutParams = layoutParams

        return MyViewHolder(view)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {

            if (position == list.size-1) {
                Log.e("positionIf", "onBindViewHolder: $position" )
                holder.addTaskList.visibility = View.VISIBLE
                holder.taskItem.visibility = View.GONE
            } else {
                Log.e("position", "onBindViewHolder: $position" )
                holder.addTaskList.visibility = View.GONE
                holder.taskItem.visibility = View.VISIBLE
            }
            holder.tvTaskListTitle.text = model.title

            holder.tvAddTaskList.setOnClickListener {

                holder.tvAddTaskList.visibility = View.GONE
                holder.cvAddTaskListName.visibility = View.VISIBLE
            }

            holder.ibCloseListName.setOnClickListener {
                holder.tvAddTaskList.visibility = View.VISIBLE
                holder.cvAddTaskListName.visibility = View.GONE
            }

            holder.doneListItemName.setOnClickListener {
                val listName = holder.taskListName.text.toString()

                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.createTaskList(listName)
                    }
                } else {
                    Toast.makeText(context, "Please Enter List Name.", Toast.LENGTH_SHORT).show()
                }
            }

            holder.ibEditListItem.setOnClickListener {

                holder.etEditListItem.setText(model.title) // Set the existing title
                holder.llTitleView.visibility = View.GONE
                holder.cvEditTaskItemList.visibility = View.VISIBLE
            }

            holder.ibCloseEV.setOnClickListener {
                holder.llTitleView.visibility = View.VISIBLE
                holder.cvEditTaskItemList.visibility = View.GONE
            }

            holder.ibDeleteList.setOnClickListener {
                alertDialogForDeleteList(position, model.title)
            }

            holder.ibDoneEditList.setOnClickListener {
                val listName = holder.etEditListItem.text.toString()

                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.updateTaskList(position, listName, model)
                    }
                } else {
                    Toast.makeText(context, "Please Enter List Name.", Toast.LENGTH_SHORT).show()
                }
            }

            holder.tvAddCard.setOnClickListener {
                holder.tvAddCard.visibility = View.GONE
                holder.cvAddCard.visibility = View.VISIBLE
            }

            holder.ibCloseCN.setOnClickListener {
                holder.tvAddCard.visibility = View.VISIBLE
                holder.cvAddCard.visibility = View.GONE
            }

            holder.ibDoneCard.setOnClickListener {

                val cardName = holder.etCardName.text.toString()

                if (cardName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.addCardToTaskList(position, cardName)
                    }
                }else{
                    Toast.makeText(context, "Please Enter Card Detail.", Toast.LENGTH_SHORT).show()
                }
            }

            holder.rvCardList.layoutManager = LinearLayoutManager(context)
            holder.rvCardList.setHasFixedSize(true)
            val adapter = CardListItemsAdapter(context, model.cards)
            holder.rvCardList.adapter = adapter
            adapter.setOnClickListener(object :
                CardListItemsAdapter.OnClickListener {
                override fun onClick(cardPosition: Int) {
                    if (context is TaskListActivity) {
                        context.cardDetails(holder.adapterPosition, cardPosition)
                    }
                }
            })
            val dividerItemDecoration = DividerItemDecoration(context,DividerItemDecoration.VERTICAL)
            holder.rvCardList.addItemDecoration(dividerItemDecoration)

            val helper = ItemTouchHelper(object: ItemTouchHelper.SimpleCallback
                (ItemTouchHelper.UP or ItemTouchHelper.DOWN,0){
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val draggedPosition = viewHolder.adapterPosition
                    val targetPosition = target.adapterPosition
                    if(mDraggedPositionFrom ==-1){
                        mDraggedPositionFrom = draggedPosition
                    }
                    mDraggedPositionTo = targetPosition
                    Collections.swap(list[holder.adapterPosition].cards,draggedPosition,targetPosition)
                    adapter.notifyItemMoved(draggedPosition,targetPosition)
                    return false
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    if(mDraggedPositionFrom != -1 && mDraggedPositionFrom!=mDraggedPositionTo)
                        (context as TaskListActivity).updateCardsInTasKList(holder.adapterPosition,list[holder.adapterPosition].cards)
                    mDraggedPositionTo = -1
                    mDraggedPositionFrom = -1
                    super.clearView(recyclerView, viewHolder)

                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                }

            }
            )
            helper.attachToRecyclerView(holder.rvCardList)

        }
        else{
            Log.e("positionTest", "onBindViewHolder: $position" )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun Int.toDp(): Int =
        (this / Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()

    private class MyViewHolder(itemTaskBinding: ItemTaskBinding) :
        RecyclerView.ViewHolder(itemTaskBinding.root){
        val addTaskList = itemTaskBinding.tvAddTaskList
        val taskItem = itemTaskBinding.llTaskItem
        val doneListItemName = itemTaskBinding.ibDoneListName
        val taskListName = itemTaskBinding.etTaskListName
        val tvAddTaskList = itemTaskBinding.tvAddTaskList
        val cvAddTaskListName = itemTaskBinding.cvAddTaskListName
        val ibCloseListName = itemTaskBinding.ibCloseListName
        val tvTaskListTitle = itemTaskBinding.tvTaskListTitle
        val ibEditListItem = itemTaskBinding.ibEditListName
        val llTitleView = itemTaskBinding.llTitleView
        val cvEditTaskItemList = itemTaskBinding.cvEditTaskListName
        val etEditListItem = itemTaskBinding.etEditTaskListName
        val ibCloseEV = itemTaskBinding.ibCloseEditableView
        val ibCloseCN = itemTaskBinding.ibCloseCardName
        val rvCardList = itemTaskBinding.rvCardList
        val cvAddCard = itemTaskBinding.cvAddCard
        val ibDeleteList = itemTaskBinding.ibDeleteList
        val etCardName = itemTaskBinding.etCardName
        val ibDoneCard = itemTaskBinding.ibDoneCardName
        val ibDoneEditList = itemTaskBinding.ibDoneEditListName
        val tvAddCard = itemTaskBinding.tvAddCard


    }

    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $title.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss()

            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
        }

        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

}
